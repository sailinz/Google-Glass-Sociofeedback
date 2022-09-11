package com.example.user.pitchvolfeb;
/**
 * Created by Sailin and Priya on 14/2/16.
 * References: https://github.com/SpecialCyCi/AndroidResideMenu
 * Image source: https://dribbble.com/shots/343597-Switch-button
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class SociometricsActivity extends Activity  {

    private static final String TAG = "feedback";
    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    static final String STATE_SPEECHRATE = "speech rate selection";
    static final String STATE_MFCC = "mfcc selection";
    static final String STATE_IP = "ip address";
    private static final String Preferences = "setting";

    private TextView volumeLabel;
    private TextView volumeLabel2;
    private IconRoundCornerProgressBar volume;
    private IconRoundCornerProgressBar volume2;

    private TextView lblvolume;
    private TextView textVolume;
    private TextView lblresvol;
    private TextView res_textVol;
    private TextView lblpitch;
    private TextView textPitch;
    private TextView lblrespitch;
    private TextView res_textPitch;
    private TextView lblresspeackingPer;
    private TextView res_speakingPer;
    private TextView lblmfcc;
    private TextView res_mfcc;

    private TextView lblvolume2;
    private TextView textVolume2;
    private TextView lblresvol2;
    private TextView res_textVol2;
    private TextView lblpitch2;
    private TextView textPitch2;
    private TextView lblrespitch2;
    private TextView res_textPitch2;
    private TextView lblresspeackingPer2;
    private TextView res_speakingPer2;
    private TextView lblmfcc2;
    private TextView res_mfcc2;

    private GraphView graph;
    private LineGraphSeries<DataPoint> series_aud;
    private LineGraphSeries<DataPoint> series_aud2;
    private static final Random RANDOM = new Random();
    private int lastX = 0;

    private ResideMenu resideMenu;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemAboutUs;
    private ResideMenuItem itemSettings;

    private ImageButton Btnfeedback;
    private ImageView ImgConvState;
    private TextView TextConvState;
    private boolean mStartFeedback = true;
    private AudioDispatcher dispatcher2;

    //low level features
    private double Vol;
    private float pitchInHz;
    private int countSpeakingPer = 0;
    private int countMutualSil = 0;
    private int countIntrup = 0;
    private double Vol2;
    private float pitchInHz2;
    private int countSpeakingPer2 = 0;
    private int count = 0;
    private int updateRate = 80; // the larger the lower update rate - for feedback message;
    private int noOfLLF = 6; //number of low level features : volume (x4: mean, max, min, entropy), pitch (x4), speaking%, MFCC, Interruption count, Mutual silence %
    private float[][] lowLevFeatures = new float[updateRate][noOfLLF];

    //MFCC centerfrequency
    private int bufferSize = 1024;
    private int sampleRate = 8000;
    private MFCC mfcc = new MFCC(bufferSize, sampleRate);

    private Socket mSocket;
    {
//        try {
//            mSocket = IO.socket("http://172.20.208.27:3000");  //LWN:172.22.185.215  HOME: 172.20.208.27
//        } catch (URISyntaxException e) {}
    }

    //read&write permission
    //Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //for classification
    private native int trainClassifierNative(String trainingFile, int kernelType,
                                             int cost, float gamma, int isProb, String modelFile);
    private native int doClassificationNative(float values[][], int indices[][],
                                              int isProb, String modelFile, int labels[], double probs[]);

    static {
        System.loadLibrary("signal");
    }

    private String filePath;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sociometrics);
        verifyStoragePermissions(this);

        //Volume
//        textViewV=(TextView)findViewById(R.id.textViewV);
        volumeLabel=(TextView)findViewById(R.id.volume);
        volume = (IconRoundCornerProgressBar) findViewById(R.id.progressBarV);
        lblvolume = (TextView) findViewById(R.id.lbl_tvdBlevel);
        textVolume = (TextView) findViewById(R.id.tvdBlevel);
        lblresvol = (TextView) findViewById(R.id.lbl_res_tvdBlevel);
        res_textVol = (TextView) findViewById(R.id.result_tvdBlevel);
        lblresspeackingPer = (TextView)findViewById(R.id.lbl_res_speakingPer);
        res_speakingPer = (TextView) findViewById(R.id.result_speakingPer);
        lblmfcc = (TextView) findViewById(R.id.lbl_res_mfcc);
        res_mfcc = (TextView) findViewById(R.id.result_mfcc);


        volumeLabel2=(TextView)findViewById(R.id.volume2);
        volume2 = (IconRoundCornerProgressBar) findViewById(R.id.progressBarV2);
        lblvolume2 = (TextView) findViewById(R.id.lbl_tvdBlevel2);
        textVolume2 = (TextView) findViewById(R.id.tvdBlevel2);
        lblresvol2 = (TextView) findViewById(R.id.lbl_res_tvdBlevel2);
        res_textVol2 = (TextView) findViewById(R.id.result_tvdBlevel2);
        lblmfcc2 = (TextView) findViewById(R.id.lbl_res_mfcc2);
        res_mfcc2 = (TextView) findViewById(R.id.result_mfcc2);


        //Pitch
        lblpitch = (TextView) findViewById(R.id.lbl_tvMessage);
        textPitch = (TextView) findViewById(R.id.tvMessage);
        lblrespitch = (TextView) findViewById(R.id.lbl_result_txtview);
        res_textPitch = (TextView) findViewById(R.id.result_txtview);

        lblpitch2 = (TextView) findViewById(R.id.lbl_tvMessage2);
        textPitch2 = (TextView) findViewById(R.id.tvMessage2);


        //Speaking percentage
        lblresspeackingPer2 = (TextView)findViewById(R.id.lbl_res_speakingPer2);
        res_speakingPer2 = (TextView) findViewById(R.id.result_speakingPer2);

        lblrespitch2 = (TextView) findViewById(R.id.lbl_result_txtview2);
        res_textPitch2 = (TextView) findViewById(R.id.result_txtview2);

        //Graph
        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitleColor(Color.WHITE);

        //data to plot
        series_aud = new LineGraphSeries<DataPoint>();
        series_aud2 = new LineGraphSeries<DataPoint>();
        graph.addSeries(series_aud);
        graph.addSeries(series_aud2);
        series_aud.setColor(Color.parseColor("#ff6666"));
        series_aud2.setColor(Color.parseColor("#80aaff"));
        // customize - viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-1);
        viewport.setMaxY(1200);
        viewport.setScrollable(true);

        GridLabelRenderer labelGraph = graph.getGridLabelRenderer();
        labelGraph.setHorizontalAxisTitle("Time (s)");
        labelGraph.setVerticalAxisTitle("Pitch (Hz)");
        labelGraph.setTextSize(6);
        labelGraph.setVerticalAxisTitleColor(Color.parseColor("#C0C0C0"));
        labelGraph.setHorizontalAxisTitleColor(Color.parseColor("#C0C0C0"));
        labelGraph.setGridColor(Color.parseColor("#C0C0C0"));
        labelGraph.setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        labelGraph.setHorizontalLabelsColor(Color.parseColor("#C0C0C0"));
        labelGraph.setVerticalLabelsColor(Color.parseColor("#C0C0C0"));
        labelGraph.setHorizontalAxisTitleTextSize(6);
        labelGraph.setHorizontalAxisTitleTextSize(6);


        //conversation state and start button
        Btnfeedback = (ImageButton) findViewById(R.id.startfeedback);
        Btnfeedback.setOnClickListener(clicker);
        ImgConvState = (ImageView) findViewById(R.id.imgConvState);
        TextConvState = (TextView) findViewById(R.id.textConvState);

        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.background);
        resideMenu.attachToActivity(this);
        // create menu items;
        itemHome     = new ResideMenuItem(this, R.drawable.icon_home,     "Home");
        itemSettings = new ResideMenuItem(this, R.drawable.icon_settings, "Settings");
        itemAboutUs  = new ResideMenuItem(this, R.drawable.icon_profile,  "About us");

        itemHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent sociometrics = new Intent(SociometricsActivity.this, MainActivity.class);
                startActivity(sociometrics);
                finish();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent settings = new Intent(SociometricsActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });
        itemAboutUs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent aboutus = new Intent(SociometricsActivity.this, AboutActivity.class);
                startActivity(aboutus);
            }
        });

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemAboutUs, ResideMenu.DIRECTION_LEFT);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });


        //for training data
        try
        {
            Log.d("Starting", "Checking up directory");
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "sociofeedback");
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists())
            {
                if (! mediaStorageDir.mkdir())
                {
                    Log.e("DirectoryCreationFailed",mediaStorageDir.toString());


                }
                else
                {
                    Log.i("Directory Creation","Success");
                }
            }
        }
        catch(Exception ex)
        {
            Log.e("Directory Creation",ex.getMessage());
        }
        //filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/"+"aud_data_files/";
        filePath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/"+"sociofeedback/";


        //restore settings
        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = true;
        Boolean restoredPitch = true ;
        Boolean restoredSR = true;
        Boolean restoredMFCC = true;
        String restoredIP = settings.getString(STATE_IP, "IP");

        try{
            mSocket = IO.socket("http://"+restoredIP+":3000");//LWN: 172.22.185.215 Home:
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSettings();

    }

    //restore settings
    private void restoreSettings(){
        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);
        Boolean restoredMFCC = settings.getBoolean(STATE_MFCC, true);
        String restoredIP = settings.getString(STATE_IP, "IP");

        try{
            mSocket = IO.socket("http://"+restoredIP+":3000");//LWN: 172.22.185.215 Home:
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }

        //respond according to settings
        if(restoredVol == true){
            volumeLabel.setVisibility(View.VISIBLE);
            volume.setVisibility(View.VISIBLE);
            textVolume.setVisibility(View.VISIBLE);
            res_textVol.setVisibility(View.VISIBLE);
            lblvolume.setVisibility(View.VISIBLE);
            lblresvol.setVisibility(View.VISIBLE);

            volumeLabel2.setVisibility(View.VISIBLE);
            volume2.setVisibility(View.VISIBLE);
            textVolume2.setVisibility(View.VISIBLE);
            res_textVol2.setVisibility(View.VISIBLE);
            lblvolume2.setVisibility(View.VISIBLE);
            lblresvol2.setVisibility(View.VISIBLE);
        } else {
            volumeLabel.setVisibility(View.INVISIBLE);
            volume.setVisibility(View.INVISIBLE);
            textVolume.setVisibility(View.INVISIBLE);
            res_textVol.setVisibility(View.INVISIBLE);
            lblvolume.setVisibility(View.INVISIBLE);
            lblresvol.setVisibility(View.INVISIBLE);

            volumeLabel2.setVisibility(View.INVISIBLE);
            volume2.setVisibility(View.INVISIBLE);
            textVolume2.setVisibility(View.INVISIBLE);
            res_textVol2.setVisibility(View.INVISIBLE);
            lblvolume2.setVisibility(View.INVISIBLE);
            lblresvol2.setVisibility(View.INVISIBLE);
        }

        if(restoredPitch == true){
            textPitch.setVisibility(View.VISIBLE);
            res_textPitch.setVisibility(View.VISIBLE);
            lblpitch.setVisibility(View.VISIBLE);
            lblrespitch.setVisibility(View.VISIBLE);
            graph.setVisibility(View.VISIBLE);

            textPitch2.setVisibility(View.VISIBLE);
            res_textPitch2.setVisibility(View.VISIBLE);
            lblpitch2.setVisibility(View.VISIBLE);
            lblrespitch2.setVisibility(View.VISIBLE);
        }else{
            textPitch.setVisibility(View.INVISIBLE);
            res_textPitch.setVisibility(View.INVISIBLE);
            lblpitch.setVisibility(View.INVISIBLE);
            lblrespitch.setVisibility(View.INVISIBLE);
            graph.setVisibility(View.INVISIBLE);

            textPitch2.setVisibility(View.INVISIBLE);
            res_textPitch2.setVisibility(View.INVISIBLE);
            lblpitch2.setVisibility(View.INVISIBLE);
            lblrespitch2.setVisibility(View.INVISIBLE);
        }


        if(restoredSR == true){
            lblresspeackingPer.setVisibility(View.VISIBLE);
            res_speakingPer.setVisibility(View.VISIBLE);

            lblresspeackingPer2.setVisibility(View.VISIBLE);
            res_speakingPer2.setVisibility(View.VISIBLE);
        }else{
            lblresspeackingPer.setVisibility(View.INVISIBLE);
            res_speakingPer.setVisibility(View.INVISIBLE);

            lblresspeackingPer2.setVisibility(View.INVISIBLE);
            res_speakingPer2.setVisibility(View.INVISIBLE);
        }

        if(restoredMFCC == true){
            lblmfcc.setVisibility(View.VISIBLE);
            res_mfcc.setVisibility(View.VISIBLE);

            lblmfcc2.setVisibility(View.VISIBLE);
            res_mfcc2.setVisibility(View.VISIBLE);
        }else{
            lblmfcc.setVisibility(View.INVISIBLE);
            res_mfcc.setVisibility(View.INVISIBLE);

            lblmfcc2.setVisibility(View.INVISIBLE);
            res_mfcc2.setVisibility(View.INVISIBLE);
        }
    }


    //onclicklistener for btnFeedback
    View.OnClickListener clicker = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            onFeedback(mStartFeedback);
            mStartFeedback = !mStartFeedback;
        }
    };


    private void onFeedback(boolean start) {
        if (start) {
            Btnfeedback.setImageResource(R.drawable.on);
            startFeedback();
            mSocket.on("new message", onNewMessage);
            mSocket.connect();
            train();

        } else {
            Btnfeedback.setImageResource(R.drawable.off);
            stopFeedback();
        }
    }

    private void stopFeedback(){
        dispatcher2.stop();
        dispatcher2 = null;

        if(count == updateRate -1){
            mToast.cancel();
        }

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }

    private void startFeedback(){
        //real-time - speaker 2 (You)
        dispatcher2 = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0);
        PitchDetectionHandler pdh2 = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                //Calculating MFCC
                float[] mfcc_buffer;
                float[] mfcc_val;
                float sum=0;
                mfcc_buffer = e.getFloatBuffer();
                float bin[] = mfcc.magnitudeSpectrum(mfcc_buffer);
                float fbank[] = mfcc.melFilter(bin, mfcc.getCenterFrequencies());
                float f[] = mfcc.nonLinearTransformation(fbank);
                mfcc_val = mfcc.cepCoefficients(f);

                float min_mfcc = mfcc_val[0];
                //for display purpose,  we take the minimum MFCC
                for (int i = 1; i < mfcc_val.length; i++) {
                    if (mfcc_val[i] < min_mfcc) {
                        min_mfcc = mfcc_val[i];
                    }
                }
                final float finalmfcc_val_float = min_mfcc;

                //Calculating SPL value in dB
                final Double dbSPLValue2 = calculate(e.getFloatBuffer());
                Vol2 = dbSPLValue2 + 85.0;  //+70.0

                //Getting Pitch Frequency in Hz
                pitchInHz2 = result.getPitch();
                //Log.d(TAG, "Volume: " + String.valueOf(Vol2) + "; pitch:" + String.valueOf(pitchInHz2));

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        //update pitch
                        textPitch2.setText("" + pitchInHz2);
                        if (pitchInHz2 > 0) {
                            res_textPitch2.setText("SPEECH DETECTED");
                            textPitch2.setTextColor(Color.parseColor("#FFCC66"));
                            res_textPitch2.setTextColor(Color.parseColor("#FFCC66"));
                            textVolume2.setTextColor(Color.parseColor("#FFCC66"));
                            res_textVol2.setTextColor(Color.parseColor("#FFCC66"));
                            countSpeakingPer2++;

                        } else if (pitchInHz2 == -1) {
                            res_textPitch2.setText("SILENCE");
                            textPitch2.setTextColor(Color.parseColor("#C0C0C0"));
                            res_textPitch2.setTextColor(Color.parseColor("#C0C0C0"));
                            textVolume2.setTextColor(Color.parseColor("#C0C0C0"));
                            res_textVol2.setTextColor(Color.parseColor("#C0C0C0"));
                            pitchInHz2 = 0;
                        }

                        //update conversation state
                        if (checkInterruption()){
                            countIntrup++;
                            ImgConvState.setImageResource(R.drawable.interruption);
                            TextConvState.setText("INTERRUPTION");
                            TextConvState.setTextColor(Color.parseColor("#a71d15"));
                        } else if(checkMutualSilence()){
                            countMutualSil++;
                            ImgConvState.setImageResource(R.drawable.silence);
                            TextConvState.setText("MUTUAL SILIENCE");
                            TextConvState.setTextColor(Color.parseColor("#efe48a"));
                        } else {
                            ImgConvState.setImageResource(R.drawable.conversation);
                            TextConvState.setText("CONVERSATION ON");
                            TextConvState.setTextColor(Color.parseColor("#c0c0c0"));
                        }

                        //update volume
                        textVolume2.setText(String.valueOf(Math.round(dbSPLValue2)));
                        res_textVol2.setText(String.valueOf(Math.round(Vol2)));
                        if(Vol2 >= 0 && Vol2 <= 14){
                            volume2.setProgressColor(Color.parseColor("#8dcdc1"));
                        }else if(Vol2 > 15 && Vol2 <= 25){
                            volume2.setProgressColor(Color.parseColor("#fff5c3"));
                        }else if(Vol2 > 25 && Vol2 <= 50){
                            volume2.setProgressColor(Color.parseColor("#d3e397"));
                        }else if(Vol2 > 50){
                            volume2.setProgressColor(Color.parseColor("#eb6e44"));
                        }
                        volume2.setProgress((float) (Vol2 / 60 * 100));
                        //draw pitch
                        series_aud2.appendData(new DataPoint(lastX++, pitchInHz2), true, 20);
                        //display MFCC
                        res_mfcc2.setText(Float.toString(finalmfcc_val_float));

                        //store data for classification

                        lowLevFeatures[count][3] = (float)Vol2;
                        lowLevFeatures[count][4] = pitchInHz2;
                        lowLevFeatures[count][5] = finalmfcc_val_float;

                        if(count < updateRate -1){
                            count++;
                        }else {
                            lowLevFeatures[count][0] = countIntrup;
                            lowLevFeatures[count][1] = calculateMutualSilience(countMutualSil);
                            lowLevFeatures[count][2]  = calculateSpeakingPercentage(countSpeakingPer2);
                            res_speakingPer.setText(Double.toString(calculateSpeakingPercentage(countSpeakingPer)) + " %");
                            res_speakingPer2.setText(Double.toString(lowLevFeatures[count][2]) + " %");
                            //classificationData(lowLevFeatures);
                            classify(classificationData(lowLevFeatures));
                            count = 0;
                            countSpeakingPer = 0;
                            countSpeakingPer2 = 0;
                            countIntrup = 0;countMutualSil = 0;

                        }


                    }


                });
            }
        };

        //real-time input - speaker 2 (You)
        AudioProcessor p2 = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, bufferSize, pdh2);
        dispatcher2.addAudioProcessor(p2);
        new Thread(dispatcher2, "Audio Dispatcher - real-time").start();

    }

    /* calculate volume in dB  */
    private Double calculate(float[] floatBuffer) {
        double power = 0.0D;
        for (float element : floatBuffer) {
            power += element * element;
        }
        double value = Math.pow(power, 0.5)/ floatBuffer.length;;
        return 20.0 * Math.log10(value);
    }

    /* Interruption */
    private boolean checkInterruption(){
        return (pitchInHz > 0 && pitchInHz2 > 0);
    }

    /* Mutual Silence */
    private boolean checkMutualSilence(){
        return (pitchInHz <= 0 && pitchInHz2 <= 0);
    }

    private float calculateMutualSilience(int cntMutualSil){
        float mutualSilPer;
        mutualSilPer = (float)cntMutualSil/updateRate * 100;  //in number
        DecimalFormat df = new DecimalFormat("#.##");
        mutualSilPer = Float.valueOf(df.format(mutualSilPer));
        return mutualSilPer;
    }

    /* Speaking Percentage */
    private float calculateSpeakingPercentage(int  cntSpeakPer){
        float speakingPer;
        speakingPer = (float)cntSpeakPer/updateRate * 100;  //in100%
        DecimalFormat df = new DecimalFormat("#.##");
        speakingPer = Float.valueOf(df.format(speakingPer));
        return speakingPer;
    }

    /* entropy */
    public static double calculateShannonEntropy(List<String> values) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        // count the occurrences of each value
        for (String sequence : values) {
            if (!map.containsKey(sequence)) {
                map.put(sequence, 0);
            }
            map.put(sequence, map.get(sequence) + 1);
        }
        // calculate the entropy
        double result = 0.0;
        for (String sequence : map.keySet()) {
            double frequency = (double) map.get(sequence) / values.size();
            result -= frequency * (Math.log(frequency) / Math.log(2));
        }
        return result;
    }





    /** real-time - speaker 1 (from Google Glass/another user)
        receive data from the server **/
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String)args[0];
                    String[] parts = data.split(" ");
                    String vol = parts[0];
                    String pitch = parts[1];
                    String MFCC = parts[2];


                    Vol = Double.valueOf(vol);
                    Double dbSPLValue = Vol - 85.0;
                    pitchInHz = Float.valueOf(pitch);

                    textPitch.setText("" + pitchInHz);
                    if (pitchInHz > 0) {
                        res_textPitch.setText("SPEECH DETECTED");
                        textPitch.setTextColor(Color.parseColor("#FFCC66"));
                        res_textPitch.setTextColor(Color.parseColor("#FFCC66"));
                        textVolume.setTextColor(Color.parseColor("#FFCC66"));
                        res_textVol.setTextColor(Color.parseColor("#FFCC66"));
                        countSpeakingPer++;
                    } else if (pitchInHz == -1) {
                        res_textPitch.setText("SILENCE");
                        textPitch.setTextColor(Color.parseColor("#C0C0C0"));
                        res_textPitch.setTextColor(Color.parseColor("#C0C0C0"));
                        textVolume.setTextColor(Color.parseColor("#C0C0C0"));
                        res_textVol.setTextColor(Color.parseColor("#C0C0C0"));
                    }


                    textVolume.setText(String.valueOf(Math.round(dbSPLValue)));
                    res_textVol.setText(String.valueOf(Math.round(Vol)));
                    if (Vol >= 0 && Vol <= 14) {
                        volume.setProgressColor(Color.parseColor("#8dcdc1"));
                    } else if (Vol > 15 && Vol <= 25) {
                        volume.setProgressColor(Color.parseColor("#fff5c3"));
                    } else if (Vol > 25 && Vol <= 50) {
                        volume.setProgressColor(Color.parseColor("#d3e397"));
                    } else if (Vol > 50) {
                        volume.setProgressColor(Color.parseColor("#eb6e44"));
                    }
                    volume.setProgress((float) (Vol / 60 * 100));
                    series_aud.appendData(new DataPoint(lastX++, pitchInHz), true, 20);
                    res_mfcc.setText(MFCC);

                }
            });
        }
    };

    //disconnect socket
    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }

    /**classification**/
    private float[][] classificationData (float[][] LLF){
        float interrupt = LLF[updateRate-1][0];
        float MutualSilience = LLF[updateRate-1][1];
        float speakingPercentage = LLF[updateRate-1][2];
        float Mfcc = LLF[updateRate-1][5];

        //volume
        float avgVol = 0;
        //max-volume
        float maxVol = LLF[0][3];
        for (int i = 1; i < updateRate - 1; i++) {
            if (LLF[i][3] > maxVol) {
                maxVol = LLF[i][3] ;
            }
        }
        //min-volume
        float minVol = maxVol; //get the minimum non-zero volume
        for (int i = 1; i < updateRate - 1; i++) {
            if (LLF[i][3]  < minVol) {
                minVol = LLF[i][3] ;
            }
        }
        //entropy volume
        List<String> volList = new ArrayList<String>();
        for (int index = 0; index < updateRate - 1; index++)
        {
            volList.add(Float.toString(LLF[index][3]));
        }
        float entropyVol = (float)calculateShannonEntropy(volList);

        //pitch
        float avgPitch = 0;
        //max-pitch
        float maxPitch = LLF[0][4];
        for (int i = 1; i < updateRate - 1; i++) {
            if (LLF[i][4] > maxPitch) {
                maxPitch = LLF[i][4];
            }
        }
        //min-pitch
        float minPitch = maxPitch; //get the minimum non-zero pitch
        for (int i = 1; i < updateRate - 1; i++) {
            if (LLF[i][4] < minPitch && LLF[i][4] > 0) {
                minPitch = LLF[i][4];
            }
        }
        //entropy-pitch
        List<String> PitchList = new ArrayList<String>();
        for (int index = 0; index < updateRate - 1; index++)
        {
            volList.add(Float.toString(LLF[index][4]));
        }
        float entropyPitch = (float)calculateShannonEntropy(volList);
        //avg-pitch&vol
        for (int i = 0; i < updateRate -1; i++){
            avgVol = avgVol + LLF[i][3];
            avgPitch = avgPitch + LLF[i][4];
        }

        avgVol = (float) ((avgVol/updateRate)/60.0); //normalized average volume
        avgPitch = (float)((avgPitch/updateRate)/500.0); //normalized average pitch

        //standardise
        float STDinterrupt = (interrupt - (float)1.62755102040816)/(float)2.18832710310961;
        float STDspeakingPercentage = (speakingPercentage - (float)34.5272788654031)/(float)16.1826971728663;
        float STDMutualSilience = (MutualSilience - (float)44.1813425207143)/(float)19.9381502829755;
        float STDminPitch = (minPitch - (float)61.6752329351021)/(float)1.93099709406383;
        float STDmaxPitch = (maxPitch - (float)304.265735510205)/(float)10.2278751108517;
        float STDentropyPitch = (entropyPitch - (float)-1389806.10967541)/(float)747534.543565487;
        float STDavgPitch = (avgPitch - (float)146.619381384184)/(float)23.9101602181699;
        float STDminVol = (minVol - (float)17.6050792722449)/(float)0.949961848829989;
        float STDmaxVol = (maxVol - (float)44.0012289954082)/(float)3.08633542620264;
        float STDentropyVol = (entropyVol - (float)(-1721699.29684694))/(float)310576.031743461;
        float STDavgVol = (avgVol - (float)29.1634195416836)/(float)4.03255748480661;
        float STDMfcc = (Mfcc - (float)(-1.55039924441327))/(float)0.158817693861086;


        float[][] lowLevFeaturesResult = {{STDinterrupt,STDspeakingPercentage, STDMutualSilience,STDminPitch, STDmaxPitch, STDentropyPitch, STDavgPitch, STDminVol, STDmaxVol, STDentropyVol, STDavgVol, STDMfcc}};
        String msg =  "Interrupt: " + String.valueOf(interrupt)
                        + ", speaking percentage: " + String.valueOf(speakingPercentage)
                        + ", mutual silience: " + String.valueOf(MutualSilience)
                        + ", min pitch: " + String.valueOf(minPitch)
                        + ", max pitch: " + String.valueOf(maxPitch)
                        + ", entropy pitch: " + String.valueOf(entropyPitch)
                        + ", mean pitch: " + String.valueOf(avgPitch)
                        + ", min vol: " + String.valueOf(minVol)
                        + ", max vol: " + String.valueOf(maxVol)
                        + ", entropy vol: " + String.valueOf(entropyVol)
                        + ", mean vol: " + String.valueOf(avgVol)
                        + ", mfcc: " + String.valueOf(Mfcc) ;
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return lowLevFeaturesResult;
    }


    private void train() {
        // Svm training
        int kernelType = 2; // Radial basis function
        int cost = 1; // Cost - C 4
        int isProb = 0;
        float gamma = 0.1f; // Gamma -r 0.25

//        //for level of dominance
//        String trainingFileLoc1 = Environment.getExternalStorageDirectory()+"/training_set1";
//        String modelFileLoc1 = Environment.getExternalStorageDirectory()+"/model1";
//        String trainingFileLoc2 = Environment.getExternalStorageDirectory()+"/training_set2";
//        String modelFileLoc2 = Environment.getExternalStorageDirectory()+"/model2";
//        String trainingFileLoc3 = Environment.getExternalStorageDirectory()+"/training_set3";
//        String modelFileLoc3 = Environment.getExternalStorageDirectory()+"/model3";

        String trainingFileLoc1 = filePath+"training_set1";
        String modelFileLoc1 = filePath+"model1";
        String trainingFileLoc2 = filePath+"training_set2";
        String modelFileLoc2 =filePath+"model2";
        String trainingFileLoc3 = filePath+"training_set3";
        String modelFileLoc3 = filePath+"model3";

        //for level of agreement
//        String trainingFileLoc4 = Environment.getExternalStorageDirectory()+"/training_set4";
//        String modelFileLoc4 = Environment.getExternalStorageDirectory()+"/model4";
//        String trainingFileLoc5 = Environment.getExternalStorageDirectory()+"/training_set5";
//        String modelFileLoc5 = Environment.getExternalStorageDirectory()+"/model5";
//        String trainingFileLoc6 = Environment.getExternalStorageDirectory()+"/training_set6";
//        String modelFileLoc6 = Environment.getExternalStorageDirectory()+"/model6";

        String trainingFileLoc4 = filePath+"training_set4";
        String modelFileLoc4 = filePath+"model4";
        String trainingFileLoc5 = filePath+"training_set5";
        String modelFileLoc5 = filePath+"model5";
        String trainingFileLoc6 = filePath+"training_set6";
        String modelFileLoc6 =filePath+"model6";


        if (    trainClassifierNative(trainingFileLoc1, kernelType, cost, gamma, isProb, modelFileLoc1) == -1 ||
                trainClassifierNative(trainingFileLoc2, kernelType, cost, gamma, isProb, modelFileLoc2) == -1 ||
                trainClassifierNative(trainingFileLoc3, kernelType, cost, gamma, isProb, modelFileLoc3) == -1 ||
                trainClassifierNative(trainingFileLoc4, kernelType, cost, gamma, isProb, modelFileLoc4) == -1 ||
                trainClassifierNative(trainingFileLoc5, kernelType, cost, gamma, isProb, modelFileLoc5) == -1 ||
                trainClassifierNative(trainingFileLoc6, kernelType, cost, gamma, isProb, modelFileLoc6) == -1) {
            Log.d(TAG, "training err");
            finish();
        }
        Toast.makeText(this, "Training is done", Toast.LENGTH_SHORT).show();
    }

    /**
     * classify generate labels for features.
     * Return:
     * 	-1: Error
     * 	0: Correct
     */
    public int callSVM(float values[][], int indices[][], int groundTruth[], int isProb, String modelFile,
                       int labels[], double probs[]) {
        // SVM type
        //Log.d(TAG, "callSVM");
        final int C_SVC = 0;
        final int NU_SVC = 1;
        final int ONE_CLASS_SVM = 2;
        final int EPSILON_SVR = 3;
        final int NU_SVR = 4;

        // For accuracy calculation
        int correct = 0;
        int total = 0;
        float error = 0;
        float sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;
        float MSE, SCC, accuracy;

        int num = values.length;
        int svm_type = C_SVC;
        if (num != indices.length)
            return -1;
        // If isProb is true, you need to pass in a real double array for probability array
        int r = doClassificationNative(values, indices, isProb, modelFile, labels, probs);

        // Calculate accuracy
        if (groundTruth != null) {
            if (groundTruth.length != indices.length) {
                Log.d(TAG, "Ground Truth.length != indices.length");
                return -1;

            }
            for (int i = 0; i < num; i++) {
                int predict_label = labels[i];
                int target_label = groundTruth[i];
                if(predict_label == target_label)
                    ++correct;
                error += (predict_label-target_label)*(predict_label-target_label);
                sump += predict_label;
                sumt += target_label;
                sumpp += predict_label*predict_label;
                sumtt += target_label*target_label;
                sumpt += predict_label*target_label;
                ++total;
            }

            if (svm_type==NU_SVR || svm_type==EPSILON_SVR)
            {
                MSE = error/total; // Mean square error
                SCC = ((total*sumpt-sump*sumt)*(total*sumpt-sump*sumt)) / ((total*sumpp-sump*sump)*(total*sumtt-sumt*sumt)); // Squared correlation coefficient
            }
            accuracy = (float)correct/total*100;
            Log.d(TAG, "Classification accuracy is " + accuracy);
        }

        return r;
    }

    private void classify(float values[][]) {
        // Svm classification
        int[][] indices = {
                {1,2,3,4,5,6,7,8,9,10,11,12}
        };
        int[] groundTruth = null;
        int[] labels1 = new int[1];
        int[] labels2 = new int[1];
        int[] labels3 = new int[1];
        int[] labels4 = new int[1];
        int[] labels5 = new int[1];
        int[] labels6 = new int[1];
        double[] probs = new double[1];
        int label_dom = 0;
        int label_agr = 0;
        int isProb = 0; // Not probability prediction
//        String modelFileLoc1 = Environment.getExternalStorageDirectory()+"/model1";
//        String modelFileLoc2 = Environment.getExternalStorageDirectory()+"/model2";
//        String modelFileLoc3 = Environment.getExternalStorageDirectory()+"/model3";
//        String modelFileLoc4 = Environment.getExternalStorageDirectory()+"/model4";
//        String modelFileLoc5 = Environment.getExternalStorageDirectory()+"/model5";
//        String modelFileLoc6 = Environment.getExternalStorageDirectory()+"/model6";

        String modelFileLoc1 = filePath+"model1";
        String modelFileLoc2 = filePath+"model2";
        String modelFileLoc3 = filePath+"model3";
        String modelFileLoc4 = filePath+"model4";
        String modelFileLoc5 = filePath+"model5";
        String modelFileLoc6 = filePath+"model6";



        if (      callSVM(values, indices, groundTruth, isProb, modelFileLoc1, labels1, probs) != 0
                ||callSVM(values, indices, groundTruth, isProb, modelFileLoc2, labels2, probs) != 0
                ||callSVM(values, indices, groundTruth, isProb, modelFileLoc3, labels3, probs) != 0
                ||callSVM(values, indices, groundTruth, isProb, modelFileLoc4, labels4, probs) != 0
                ||callSVM(values, indices, groundTruth, isProb, modelFileLoc5, labels5, probs) != 0
                ||callSVM(values, indices, groundTruth, isProb, modelFileLoc6, labels6, probs) != 0) {
            Log.d(TAG, "Classification is incorrect");
        }
        else {
            String m1 = "",m2 = "", m3 = "", m4 = "",m5 = "", m6 = "", m = "", mm = "";
            Random rand = new Random();
            int value = rand.nextInt(50);
            for (int l : labels1)  //   -> 1v2
                m1 += l + "; ";
            for (int l : labels2)  //   -> 1v3
                m2 += l + "; ";
            for (int l : labels3)  //   -> 2v3
                m3 += l + "; ";

            for (int l : labels4)  //   -> 1v2
                m4 += l + "; ";
            for (int l : labels5)  //   -> 1v3
                m5 += l + "; ";
            for (int l : labels6)  //   -> 2v3
                m6 += l + "; ";

            for (int i = 0; i < labels1.length; i++) {
                if (labels1[i] == labels2[i]) {
                    m += labels1[i] ;//+ ", ";
                    label_dom = labels1[i];
                }else if (labels1[i] == labels3[i]){
                    m += labels1[i] ;//+ ", ";
                    label_dom = labels1[i];
                }else if (labels3[i] == labels2[i]){
                    m += labels2[i] ; // + ", ";
                    label_dom = labels2[i];
                }else {
                    label_dom = rand.nextInt(3)+1;
                    //Integer.toString(label_dom);
                }
            }

            for (int i = 0; i < labels4.length; i++) {
                if (labels4[i] == labels5[i]) {
                    mm += labels4[i] ;//+ ", ";
                    label_agr = labels4[i];
                }else if (labels4[i] == labels6[i]){
                    mm += labels4[i] ;//+ ", ";
                    label_agr = labels4[i];
                }else if (labels5[i] == labels6[i]){
                    mm += labels5[i] ; // + ", ";
                    label_agr = labels5[i];
                }else {
                    label_agr = rand.nextInt(3)+1;
                    //Integer.toString(label_agr);
                }
            }


            if(label_dom == 1){
                m += "-Low dominance";
            }else if(label_dom == 2){
                m += "-Medium dominance";
            }else if (label_dom == 3){
                m += "-High dominance";
            }

            if(label_agr == 1){
                mm += "-Low agreement";
            }else if(label_agr == 2){
                mm += "-Medium agreement";
            }else if (label_agr == 3){
                mm += "-High agreement";
            }
            mToast.makeText(this, "Classification is done, the final result is " + m + ", " + mm, Toast.LENGTH_LONG).show();

        }
    }


    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}
