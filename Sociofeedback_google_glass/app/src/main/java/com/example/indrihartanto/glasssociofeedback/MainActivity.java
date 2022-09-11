package com.example.indrihartanto.glasssociofeedback;

/**
 * Created by Sailin on 01/2/16.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;


public class MainActivity extends Activity {

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.main);

        ImageView background = (ImageView) findViewById(R.id.background);
        ImageButton userGuide = (ImageButton) findViewById(R.id.userguide);
        ImageButton aboutUs = (ImageButton) findViewById(R.id.aboutus);



        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(background, "alpha",  1f, .3f);
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


        //"To become a better communicator"
        SecretTextView secretTextView;
        secretTextView = (SecretTextView)findViewById(R.id.description);
        secretTextView.setDuration(3000);

        secretTextView.show();    // fade in
        secretTextView.hide();    // fade out
        secretTextView.toggle();

        mGestureDetector = createGestureDetector(this);

    }

    /* Selection of interaction mode via voice trigger*/

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            getMenuInflater().inflate(R.menu.mode, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            switch (item.getItemId()) {
                case R.id.feedback:
                    Intent com = new Intent(MainActivity.this, Combination.class);
                    startActivity(com);
                    break;
                case R.id.settings:
                    Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settings);
                    break;
                case R.id.aboutus:
                    Intent aboutUs = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(aboutUs);
                    break;
                default:
                    return true;
            }
            return true;
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }


    //swipe forward to go to the user guide panel and swipe backward to go to the about us panel
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if  (gesture == Gesture.SWIPE_RIGHT) {
                    Intent help = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(help);
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settings);
                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN){
                    finish();
                    return true;
                } else if (gesture == Gesture.TAP){
                    Intent com = new Intent(MainActivity.this, Combination.class);
                    startActivity(com);
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




}
