package com.garrapeta.pingpong;

import android.util.Log;

/**
 * One player
 */
public class Player {
    private static final String TAG = "Player";

    private final World mWorld;
    private String mName;
    private int mScore;


    public Player(World world, String name) {
        mWorld = world;
        mName = name;
        mScore = 0;
    }

    public void onSwing() {
        Log.i(TAG, mName + " swings");
        mWorld.onSwing(this, Math.random());
    }

    public void onPointWon() {
        Log.i(TAG, mName + " wins one point");
        mScore++;
    }

    @Override
    public String toString() {
        return "Player{" +
                mName + '\'' +
                '}';
    }


    public String getName() {
        return mName;
    }

    public int getScore() {
        return mScore;
    }

}
