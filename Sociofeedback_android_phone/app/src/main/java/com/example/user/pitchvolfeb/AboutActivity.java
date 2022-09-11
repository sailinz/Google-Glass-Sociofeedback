package com.example.user.pitchvolfeb;

/**
 * Created by indrihartanto on 11/2/15.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends Activity {

    private Button BtnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        BtnDone = (Button)findViewById(R.id.done);
        BtnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }



}


