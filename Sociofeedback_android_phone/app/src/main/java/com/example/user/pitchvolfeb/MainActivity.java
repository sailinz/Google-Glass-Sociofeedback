package com.example.user.pitchvolfeb;

/**
 * Created by Sailin on 01/2/16.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;


public class MainActivity extends Activity {

//    static final String STATE_VOLUME = "volume selection";
//    static final String STATE_PITCH = "pitch selection";
//    static final String STATE_SPEECHRATE = "speech rate selection";
//    static final String STATE_MFCC = "mfcc selection";
//    static final String STATE_IP = "ip address";
//    private static final String Preferences = "setting";


    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.main);

        ImageView background = (ImageView) findViewById(R.id.background);
        ImageButton settings = (ImageButton) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);

            }
        });
        ImageButton aboutUs = (ImageButton) findViewById(R.id.aboutus);
        aboutUs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent aboutUs = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutUs);
            }
        });
        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent com = new Intent(MainActivity.this, SociometricsActivity.class);
                startActivity(com);
            }
        });


        //"To become a better communicator"
        SecretTextView secretTextView;
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(background, "alpha", 1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(background, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeIn).after(fadeOut);
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();


        secretTextView = (SecretTextView)findViewById(R.id.description);
        secretTextView.setDuration(3000);

        secretTextView.show();    // fade in
        secretTextView.hide();    // fade out
        secretTextView.toggle();


//        SharedPreferences settingsShared = getSharedPreferences(Preferences, 0);
//        Boolean restoredVol = settingsShared .getBoolean(STATE_VOLUME, true);
//        Boolean restoredPitch = settingsShared .getBoolean(STATE_PITCH, true);
//        Boolean restoredSR = settingsShared .getBoolean(STATE_SPEECHRATE, true);
//        Boolean restoredMFCC = settingsShared .getBoolean(STATE_MFCC, true);
//        String restoredIP = settingsShared .getString(STATE_IP, "");

    }




}
