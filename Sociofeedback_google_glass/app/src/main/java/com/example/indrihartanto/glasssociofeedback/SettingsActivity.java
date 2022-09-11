package com.example.indrihartanto.glasssociofeedback;

/**
 * Created by Sailin on 11/2/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class SettingsActivity extends Activity {
    private CheckBox volumeChk, pitchChk, speechrateChk;
    private GestureDetector settingGesture;
    private static boolean isChkV, isChkP, isChkSR;

    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    static final String STATE_SPEECHRATE = "speech rate selection";
    private static final String Preferences = "setting";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        volumeChk = (CheckBox) findViewById(R.id.checkbox_v);
        pitchChk = (CheckBox) findViewById(R.id.checkbox_p);
        //speechrateChk = (CheckBox) findViewById(R.id.checkbox_sr);
        settingGesture = createGestureDetector(this);

        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        //Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);

        volumeChk.setChecked(restoredVol);
        pitchChk.setChecked(restoredPitch);
        //speechrateChk.setChecked(restoredSR);

    }

    @Override
    protected void onStop(){
        super.onStop();
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(STATE_VOLUME, isChkV);
        editor.putBoolean(STATE_PITCH, isChkP);
        editor.putBoolean(STATE_SPEECHRATE, isChkSR);

        // Commit the edits!
        editor.commit();
        Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
    }



    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.SWIPE_LEFT) {
                    if (volumeChk.isFocused()) {
                        volumeChk.setSelected(false);
                        pitchChk.requestFocus();
                    } else if (pitchChk.isFocused()) {
                        pitchChk.setSelected(false);
                        speechrateChk.requestFocus();
                    } else if (speechrateChk.isEnabled()) {
                        speechrateChk.setSelected(false);
                        volumeChk.requestFocus();
                    }
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    if (speechrateChk.isEnabled()) {
                        speechrateChk.setSelected(false);
                        volumeChk.requestFocus();
                    } else if (pitchChk.isFocused()) {
                        pitchChk.setSelected(false);
                        speechrateChk.requestFocus();
                    } else if (volumeChk.isFocused()) {
                        volumeChk.setSelected(false);
                        pitchChk.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        return gestureDetector;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (settingGesture != null) {
            return settingGesture.onMotionEvent(event);
        }
        return false;
    }


//    public void onCheckboxClicked(View view) {
//        // Is the view now checked?
//        boolean checked = ((CheckBox) view).isChecked();
//
//        // Check which checkbox was clicked
//        switch(view.getId()) {
//            case R.id.checkbox_v:
//                if (checked) {
//                    // enable volume detection
//                    isChkV = true;
//                }else {
//                    // disable volume detection
//                    isChkV = false;
//                }
//                break;
//            case R.id.checkbox_p:
//                if (checked) {
//                    // enable pitch detection
//                    isChkP = true;
//                }else{
//                    // disable pitch detection
//                    isChkP = false;
//                }
//                break;
//            case R.id.checkbox_sr:
//                if (checked) {
//                    // enable speech rate detection
//                    isChkSR = true;
//                }else {
//                    // disable speech rate detection
//                    isChkSR = false;
//                }
//                break;
//            // TODO: Veggie sandwich
//        }
//
//    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_v:
                isChkV = checked;
                break;
            case R.id.checkbox_p:
                isChkP = checked;
                break;
//            case R.id.checkbox_sr:
//                isChkSR = checked;
//                break;
//            // TODO: Veggie sandwich
        }
    }


}
