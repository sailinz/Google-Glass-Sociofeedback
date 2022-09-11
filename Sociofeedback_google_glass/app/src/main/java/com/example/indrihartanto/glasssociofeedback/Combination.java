package com.example.indrihartanto.glasssociofeedback;

/**
 * Created by Sailin on 14/2/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class Combination extends Activity  {

    private static final String TAG = "feedback";

    /*previous method for detecting volume
    private static final int sampleRate = 11025;
    private AudioRecord audio;
    private int bufferSize;
    private double lastLevel = 0;
    private Thread thread;
    private static final int SAMPLE_DELAY = 75;
    */


    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    static final String STATE_SPEECHRATE = "speech rate selection";
    private static final String Preferences = "setting";

//    private ImageView feedbackImage;
//    private TextView feedbackText;

    private TextView volumeLabel;
    private IconRoundCornerProgressBar volume;
    private TextView textViewV;
    private TextView lblvolume;
    private TextView textVolume;
    private TextView lblresvol;
    private TextView res_textVol;
    private TextView lblpitch;
    private TextView textPitch;
    private TextView lblrespitch;
    private TextView res_textPitch;
    private TextView lblmfcc;
    private TextView res_mfcc;


    private AudioDispatcher dispatcher;
    private GraphView graph;
    private Switch aSwitch;
    private double Vol;
//    private int bufferSize = 1024;
//    private int sampleRate = 8000;
//    private MFCC mfcc = new MFCC(bufferSize, sampleRate);

    private LineGraphSeries<DataPoint> series_aud;
    private static final Random RANDOM = new Random();
    private int lastX = 0;

    private Socket mSocket;
    {
        try{
            mSocket = IO.socket("http://172.22.219.254:3000");   //home:172.20.208.27  LWN:172.22.219.254
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.combination);

        mGestureDetector = createGestureDetector(this);

        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);

        Log.i(TAG, restoredVol.toString());

//        feedbackImage =(ImageView)findViewById(R.id.feedbackimage);
//        feedbackText =(TextView)findViewById(R.id.feedbacktext);

        //Volume
        textViewV=(TextView)findViewById(R.id.textViewV);
        volumeLabel=(TextView)findViewById(R.id.volume);
        volume = (IconRoundCornerProgressBar) findViewById(R.id.progressBarV);
        lblvolume = (TextView) findViewById(R.id.lbl_tvdBlevel);
        textVolume = (TextView) findViewById(R.id.tvdBlevel);
        lblresvol = (TextView) findViewById(R.id.lbl_res_tvdBlevel);
        res_textVol = (TextView) findViewById(R.id.result_tvdBlevel);

        //Pitch
        lblpitch = (TextView) findViewById(R.id.lbl_tvMessage);
        textPitch = (TextView) findViewById(R.id.tvMessage);
        lblrespitch = (TextView) findViewById(R.id.lbl_result_txtview);
        res_textPitch = (TextView) findViewById(R.id.result_txtview);

        aSwitch = (Switch) findViewById(R.id.switchS);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    startFeedback();
                    mSocket.connect();
                } else {
                    stopFeedback();
                }
            }
        });



        //Graph
        graph = (GraphView) findViewById(R.id.graph);
        //data to plot
        series_aud = new LineGraphSeries<DataPoint>();
        series_aud.setColor(Color.parseColor("#ff6666"));
        graph.addSeries(series_aud);
        // customize - viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-1);
        viewport.setMaxY(400);
        viewport.setScrollable(true);

        GridLabelRenderer labelGraph = graph.getGridLabelRenderer();
        labelGraph.setHorizontalAxisTitle("Second");
        labelGraph.setVerticalAxisTitle("Hz");
        labelGraph.setTextSize(8);

        //respond according to settings
        if(restoredVol == true){
            volumeLabel.setVisibility(View.VISIBLE);
            textViewV.setVisibility(View.VISIBLE);
            volume.setVisibility(View.VISIBLE);
            textVolume.setVisibility(View.VISIBLE);
            res_textVol.setVisibility(View.VISIBLE);
            lblvolume.setVisibility(View.VISIBLE);
            lblresvol.setVisibility(View.VISIBLE);
            Log.i(TAG, "Show");
        } else {
            volumeLabel.setVisibility(View.INVISIBLE);
            textViewV.setVisibility(View.INVISIBLE);
            volume.setVisibility(View.INVISIBLE);
            textVolume.setVisibility(View.INVISIBLE);
            res_textVol.setVisibility(View.INVISIBLE);
            lblvolume.setVisibility(View.INVISIBLE);
            lblresvol.setVisibility(View.INVISIBLE);
            Log.i(TAG, "Not show");
        }

        if(restoredPitch == true){
            textPitch.setVisibility(View.VISIBLE);
            res_textPitch.setVisibility(View.VISIBLE);
            lblpitch.setVisibility(View.VISIBLE);
            lblrespitch.setVisibility(View.VISIBLE);
            graph.setVisibility(View.VISIBLE);
        }else{
            textPitch.setVisibility(View.INVISIBLE);
            res_textPitch.setVisibility(View.INVISIBLE);
            lblpitch.setVisibility(View.INVISIBLE);
            lblrespitch.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);
        }

        //client

        /*previous method for detecting volume
        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }
        */

    }


    /**
     * Opening settings menu on tapping on D-pad
     * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    private void startFeedback() {

        //new method use the tarsos lib
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(8000, 1024, 0);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {

                //Calculating SPL value in dB
                final Double dbSPLValue= calculate(e.getFloatBuffer());
                Vol = dbSPLValue + 105.0;  //+70.0

                //Calculating MFCC
//                float[] mfcc_buffer;
//                float[] mfcc_val;
//                float sum=0;
//                mfcc_buffer = e.getFloatBuffer();
//                float bin[] = mfcc.magnitudeSpectrum(mfcc_buffer);
//                float fbank[] = mfcc.melFilter(bin, mfcc.getCenterFrequencies());
//                float f[] = mfcc.nonLinearTransformation(fbank);
//                mfcc_val = mfcc.cepCoefficients(f);
//                float min_mfcc = mfcc_val[0];
//                //for display purpose,  we take the minimum MFCC
//                for (int i = 1; i < mfcc_val.length; i++) {
//                    if (mfcc_val[i] < min_mfcc) {
//                        min_mfcc = mfcc_val[i];
//                    }
//                }
//                final float finalmfcc_val_float = min_mfcc;
                //Getting Pitch Frequency in Hz
                final float pitchInHz = result.getPitch();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        textPitch.setText("" + pitchInHz);
                        if (pitchInHz >= 0) {
                            res_textPitch.setText("SPEECH DETECTED");

                        } else if (pitchInHz == -1) {
                            res_textPitch.setText("SILENCE");

                        }

                        textVolume.setText(String.valueOf(Math.round(dbSPLValue)));
                        res_textVol.setText(String.valueOf(Math.round(Vol)));

                        if(Vol >= 0 && Vol <= 14){
                            volume.setProgressColor(Color.parseColor("#8dcdc1")); //-16737844
                            //Log.i(TAG, "Please Participate: level is" + Double.toString(Vol) +"   " + Integer.toString(volume.getProgressColor()));
                        }else if(Vol > 15 && Vol <= 25){
                            volume.setProgressColor(Color.parseColor("#fff5c3")); //-30720
                            //Log.i(TAG, "Speak Louder: level is" + Double.toString(Vol) +"   " + Integer.toString(volume.getProgressColor()));
                        }else if(Vol > 25 && Vol <= 50){
                            volume.setProgressColor(Color.parseColor("#d3e397")); //-10053376
                            //Log.i(TAG, "Right Volume: level is" + Double.toString(Vol) +"   " +  Integer.toString(volume.getProgressColor()));
                        }else if(Vol > 50){
                            volume.setProgressColor(Color.parseColor("#eb6e44")); //-48060
                            //Log.i(TAG, "Too Loud: level is" + Double.toString(Vol) +"   "+ Integer.toString(volume.getProgressColor()));
                        }
                        //Log.i(TAG,Integer.toString(volume.getProgressColor()) );
                        volume.setProgress((float) (Vol / 60 * 100));
                        textViewV.setText((int) (Vol / 60 * 100) + "%");
                        DecimalFormat df = new DecimalFormat("#.######");
                        Vol = Double.valueOf(df.format(Vol));

                        String message = Double.toString(Vol) + " " + Float.toString(pitchInHz) + " 0"; //+ " " + Float.toString(finalmfcc_val_float);
                        mSocket.emit("new message", message);

                        series_aud.appendData(new DataPoint(lastX++, pitchInHz), true, 20);
                        //res_mfcc.setText(Float.toString(finalmfcc_val_float));


                    }


                });
            }
        };

        //sample rate min:16896
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 8000, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher, "Audio Dispatcher").start();

        /*previous method for detecting volume
        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);


        audio.startRecording();
        thread = new Thread(new Runnable() {
            public void run() {
                while(thread != null && !thread.isInterrupted()){
                    //Let's make the thread sleep for a the approximate sampling time
                    try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                    readAudioBuffer();//After this call we can get the last value assigned to the lastLevel variable



                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            //volume level
                            double l;
                            l = Math.abs((lastLevel-4.5)/11.5*100);
                            if(lastLevel > 3.6 && lastLevel <= 5.4){
//                                feedbackImage.setImageResource(R.drawable.participate);
//                                feedbackText.setText("Please Participate");
                                volume.setProgressColor(-16737844);
                                Log.i(TAG, "Please Participate: level is" + Integer.toString((int)lastLevel) +"   " + Integer.toString(volume.getProgressColor()));

                            }else
                            if(lastLevel > 5.4 && lastLevel <= 8){
//                                feedbackImage.setImageResource(R.drawable.speaklouder);
//                                feedbackText.setText("Speak Louder");
                                volume.setProgressColor(-30720);
                                Log.i(TAG, "Speak Louder: level is" + Integer.toString((int)lastLevel) +"   " + Integer.toString(volume.getProgressColor()));
                            }else
                            if(lastLevel > 8 && lastLevel <= 13){
//                                feedbackImage.setImageResource(R.drawable.right);
//                                feedbackText.setText("Right Volume");
                                volume.setProgressColor(-10053376);
                                Log.i(TAG, "Right Volume: level is" + Integer.toString((int)lastLevel) +"   " +  Integer.toString(volume.getProgressColor()));
                            }
                            if(lastLevel > 13){
//                                feedbackImage.setImageResource(R.drawable.tooloud);
//                                feedbackText.setText("Too Loud");
                                volume.setProgressColor(-48060);
                                Log.i(TAG, "Too Loud: level is" + Integer.toString((int)lastLevel) +"   "+ Integer.toString(volume.getProgressColor()));
                            }
                            //Log.i(TAG,Integer.toString(volume.getProgressColor()) );
                            volume.setProgress((float) l);
                            textViewV.setText((int) l + "%");


                        }
                    });
                }
            }
        });
        thread.start();
        */
    }

    private void stopFeedback(){
        dispatcher.stop();
        dispatcher = null;
        mSocket.disconnect();
    }


    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);
        //Log.i(TAG, restoredVol.toString());


        //respond according to settings
        if(restoredVol == true){
            volumeLabel.setVisibility(View.VISIBLE);
//            textViewV.setVisibility(View.VISIBLE);
            volume.setVisibility(View.VISIBLE);
            textVolume.setVisibility(View.VISIBLE);
            res_textVol.setVisibility(View.VISIBLE);
            lblvolume.setVisibility(View.VISIBLE);
            lblresvol.setVisibility(View.VISIBLE);
            //Log.i(TAG, "Show");
        } else {
            volumeLabel.setVisibility(View.INVISIBLE);
//            textViewV.setVisibility(View.INVISIBLE);
            volume.setVisibility(View.INVISIBLE);
            textVolume.setVisibility(View.INVISIBLE);
            res_textVol.setVisibility(View.INVISIBLE);
            lblvolume.setVisibility(View.INVISIBLE);
            lblresvol.setVisibility(View.INVISIBLE);
            //Log.i(TAG, "Not show");
        }

        if(restoredPitch == true){
            textPitch.setVisibility(View.VISIBLE);
            res_textPitch.setVisibility(View.VISIBLE);
            lblpitch.setVisibility(View.VISIBLE);
            lblrespitch.setVisibility(View.VISIBLE);
            graph.setVisibility(View.VISIBLE);
        }else{
            textPitch.setVisibility(View.INVISIBLE);
            res_textPitch.setVisibility(View.INVISIBLE);
            lblpitch.setVisibility(View.INVISIBLE);
            lblrespitch.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);
        }
    }


    private Double calculate(float[] floatBuffer) {

        double power = 0.0D;
        for (float element : floatBuffer) {
            power += element * element;
        }
        double value = Math.pow(power, 0.5)/ floatBuffer.length;;
        return 20.0 * Math.log10(value);
    }


    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if  (gesture == Gesture.SWIPE_RIGHT) {
                    Intent aboutUs = new Intent(Combination.this, AboutActivity.class);
                    startActivity(aboutUs);
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    Intent settings = new Intent(Combination.this, SettingsActivity.class);
                    startActivity(settings);
                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN){
                    finish();
                    return true;
                }
                return false;
            }
        });

        return gestureDetector;
    }

    /*
 * Send generic motion events to the gesture detector
 */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatcher.stop();
        mSocket.disconnect();

    }

    /*previous method for detecting volume
    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult = 1;

            if (audio != null) {

                // Sense the voice...
                //Reads audio data(current voice amplitude) from the audio hardware for recording into a direct buffer. return int
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                double sumLevel = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
                }
                lastLevel = Math.abs((sumLevel / bufferReadResult));    //take average
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        thread.interrupt();
        thread = null;
        try {
            if (audio != null) {
                audio.stop();
                audio.release();
                audio = null;
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    */

}
