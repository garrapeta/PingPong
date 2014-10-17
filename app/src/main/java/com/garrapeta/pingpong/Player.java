package com.garrapeta.pingpong;

import android.util.Log;

/**
 * One player
 */
public class Player {
    private static final String TAG = "Player";

    private final World mWorld;
    private String mName;

    public Player(World world, String name) {
        mWorld = world;
        mName = name;
    }

    public void onSwing() {
        Log.i(TAG, mName + " swings");
        mWorld.onSwing(this);
    }

    public void onPointWon() {
        Log.i(TAG, mName + " wins one point");
    }

    @Override
    public String toString() {
        return "Player{" +
                "mName='" + mName + '\'' +
                '}';
    }


}
