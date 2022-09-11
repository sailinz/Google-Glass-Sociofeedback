package com.example.user.pitchvolfeb;

/**
 * Created by Sailin on 11/2/16.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;


public class SettingsActivity extends Activity implements View.OnClickListener{
    private SwitchButton volumeChk, pitchChk, speechrateChk, mfccChk;
    private static boolean isChkV, isChkP, isChkSR, isChkMFCC;

    static final String STATE_VOLUME = "volume selection";
    static final String STATE_PITCH = "pitch selection";
    static final String STATE_SPEECHRATE = "speech rate selection";
    static final String STATE_MFCC = "mfcc selection";
    static final String STATE_IP = "ip address";
    private static final String Preferences = "setting";

    private Button BtnDone;

    private Button BtnSubmit;
    private EditText EditTextIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        volumeChk = (SwitchButton) findViewById(R.id.checkbox_v);
        pitchChk = (SwitchButton) findViewById(R.id.checkbox_p);
        speechrateChk = (SwitchButton) findViewById(R.id.checkbox_sr);
        mfccChk = (SwitchButton) findViewById(R.id.checkbox_MFCC);
        EditTextIP = (EditText)findViewById(R.id.IP);
        BtnSubmit = (Button)findViewById(R.id.submit);


        SharedPreferences settings = getSharedPreferences(Preferences, 0);
        Boolean restoredVol = settings.getBoolean(STATE_VOLUME, true);
        Boolean restoredPitch = settings.getBoolean(STATE_PITCH, true);
        Boolean restoredSR = settings.getBoolean(STATE_SPEECHRATE, true);
        Boolean restoredMFCC = settings.getBoolean(STATE_MFCC, true);
        String restoredIP = settings.getString(STATE_IP, "");

        volumeChk.setChecked(restoredVol);
        pitchChk.setChecked(restoredPitch);
        speechrateChk.setChecked(restoredSR);
        mfccChk.setChecked(restoredMFCC);
        EditTextIP.setText(restoredIP);


        BtnDone = (Button)findViewById(R.id.done);
        BtnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStop();
                finish();
            }
        });


        BtnSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if( EditTextIP.getText().toString() != null){
                    SharedPreferences settings = getSharedPreferences(Preferences, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(STATE_IP, EditTextIP.getText().toString());
                    editor.commit();
                }

            }
        });
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
        editor.putBoolean(STATE_MFCC, isChkMFCC);

        // Commit the edits!
        editor.commit();
        Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v){
        int id = v.getId();
        switch (id){
            case R.id.checkbox_v:
                volumeChk.setChecked(!volumeChk.isChecked());
                volumeChk.toggle();
                isChkV=!isChkV;
                break;
            case R.id.checkbox_p:
                pitchChk.setChecked(!pitchChk.isChecked());
                pitchChk.toggle();
                isChkP=!isChkP;
                break;
            case R.id.checkbox_sr:
                speechrateChk.setChecked(!speechrateChk.isChecked());
                speechrateChk.toggle();
                isChkSR=!isChkSR;
                break;
            case R.id.checkbox_MFCC:
                mfccChk.setChecked(!mfccChk.isChecked());
                mfccChk.toggle();
                isChkMFCC=!isChkMFCC;
                break;
            default:break;

        }
    }


}
