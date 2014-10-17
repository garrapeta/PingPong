package com.garrapeta.pingpong;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
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

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private BroadcastReceiver mWiFiDirectBroadcastReceiver;
    private IntentFilter mWiFiDirectIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
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

        setupWifiP2P();

    }


    private void setupWifiP2P() {
        mWiFiDirectIntentFilter = new IntentFilter();
        mWiFiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mWiFiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mWiFiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mWiFiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
        mWiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager, mWifiP2pChannel, this);
    }


    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mWiFiDirectBroadcastReceiver, mWiFiDirectIntentFilter);
        mWifiP2pManager.discoverPeers(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e(WiFiDirectBroadcastReceiver.TAG, "OK");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(WiFiDirectBroadcastReceiver.TAG, "KO " + reasonCode);
            }
        });
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWiFiDirectBroadcastReceiver);
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
