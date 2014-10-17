package com.garrapeta.pingpong;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class PingPongActivity extends Activity implements World.WorldListener {

    private static final String TAG = PingPongActivity.class.getSimpleName();

    private TextView   mStateTextView;
    private TextView[] mNamesTextViews;
    private TextView[] mScoresTextViews;

    private World mWorld = new World();

    private SoundPool mSoundPool;
    private int mSoundBallPaddle;
    private int mSoundBallTable;
    private int mSoundBallToGround;
    private int mSoundSwing;
    private int mSoundStart;
    private int mSoundWrong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pingpong);
        bindViews();

        mWorld.setListener(this);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int id, int status) {
                if (mSoundWrong == id) {
                    mWorld.init();
                    initPlayersGui();
                }
            }
        });

        mSoundStart = mSoundPool.load(this, R.raw.whistle, 5);
        mSoundBallPaddle = mSoundPool.load(this, R.raw.ball_2_paddle, 10);
        mSoundBallTable = mSoundPool.load(this, R.raw.ball_2_table, 10);
        mSoundBallToGround = mSoundPool.load(this, R.raw.ball_2_ground, 5);
        mSoundSwing = mSoundPool.load(this, R.raw.swing, 1);
        mSoundWrong = mSoundPool.load(this, R.raw.wrong, 1);
    }



    private void initPlayersGui() {
        for (int i = 0; i < 2; i++) {
            mScoresTextViews[i].setText(String.valueOf(mWorld.getPlayer(i).getScore()));
            mNamesTextViews[i].setText(mWorld.getPlayer(i).getName());
        }
    }

    private void bindViews() {
        mStateTextView = (TextView) findViewById(R.id.state_textview);
        mNamesTextViews = new TextView[] {
                (TextView) findViewById(R.id.playerNameA),
                (TextView) findViewById(R.id.playerNameB)
        };
        mScoresTextViews = new TextView[] {
                (TextView) findViewById(R.id.scoreA),
                (TextView) findViewById(R.id.scoreB)
        };
    }

    public void onSwingAButtonClicked(View view) {
        mWorld.getPlayer(0).onSwing();
    }

    public void onSwingBButtonClicked(View view) {
        mWorld.getPlayer(1).onSwing();
    }

    @Override
    public void oneActivePlayerChanged(Player activePlayer) {
        for (int i = 0; i < 2; i++) {
            boolean active = mWorld.getPlayer(i) == mWorld.getActivePlayer();
            mNamesTextViews[i].setTextColor(active ? Color.RED : Color.BLACK);
        }
    }


    @Override
    public void onIdle() {
        mStateTextView.setText("");
    }

    @Override
    public void onWaitingForService(Player servingPlayer) {
        playSound(mSoundStart);
        mStateTextView.setText("Waiting for service: " + servingPlayer);
        mStateTextView.setTextColor(Color.BLACK);
    }

    @Override
    public void onWaitingForSwing(Player player) {
        mStateTextView.setText("Waiting for SWING from: " + player);
        mStateTextView.setTextColor(Color.RED);
        playSound(mSoundBallTable);
    }

    @Override
    public void onBallHitted() {
        mStateTextView.setText("Ball sent to: " + mWorld.getActivePlayer());
        mStateTextView.setTextColor(Color.BLACK);
        playSound(mSoundBallPaddle);
    }


    @Override
    public void onSwing(Player player) {
        playSound(mSoundSwing);
    }

    @Override
    public void onBallFallsToGround() {
        playSound(mSoundBallToGround);
    }

    private void playSound(int soundId) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;

        mSoundPool.play(soundId, volume, volume, 1, 0, 1f);
    }



    @Override
    public void onPointWon(Player nonActivePlayer) {
        playSound(mSoundWrong);
        initPlayersGui();
    }


}
