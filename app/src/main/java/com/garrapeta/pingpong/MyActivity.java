package com.garrapeta.pingpong;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


public class MyActivity extends Activity {



    private World mWorld = new World();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mWorld.init();
    }

    public void onSwingAButtonClicked(View view) {
        mWorld.getPlayer(0).onSwing();
    }

    public void onSwingBButtonClicked(View view) {
        mWorld.getPlayer(1).onSwing();
    }


}
