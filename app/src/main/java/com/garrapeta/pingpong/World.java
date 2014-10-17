package com.garrapeta.pingpong;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Game world
 */
public class World {
    private static final String TAG = "World";

    private static final int MSG_HIT_TIME_START = 0;
    private static final int MSG_HIT_TIME_END = 1;

    private enum State {
        WAITING_FOR_SERVICE,
        BEFORE_HIT_TIME,
        IN_HIT_TIME,
    }
    private Handler mHandler;

    private Player[] mPlayers = new Player[]{new Player(this, "A"), new Player(this, "B")};

    private int mActivePlayerIdx;
    private int mServingPlayer;

    private State mState;


    public void init() {
        Handler.Callback callback = new Handler.Callback() {


            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIT_TIME_START:
                        onHitTimeStarts();
                        break;
                    case MSG_HIT_TIME_END:
                        onHitTimeEnds();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown message.what: " + message.what);
                }
                return true;
            }

        };

        mHandler = new Handler(callback);

        setServingPlayer(0);
        setActivePlayer(0);
        setWaitingForService(true);
    }

    public boolean isActive(Player player) {
        return getActivePlayer() == player;
    }

    private void onHitTimeStarts() {
        Log.i(TAG, "Swing NOW!!!");
        mState = State.IN_HIT_TIME;
        mHandler.sendEmptyMessageDelayed(MSG_HIT_TIME_END, 1000);
    }

    private void onHitTimeEnds() {
        onPlayerMiss(getActivePlayer());
    }


    public int getOpositePlayerIdx(int idx) {
        return (idx + 1) % mPlayers.length;
    }

    public Player getPlayer(int idx) {
        return mPlayers[idx];
    }

    public Player getActivePlayer() {
        return getPlayer(mActivePlayerIdx);
    }

    public void setActivePlayer(int idx) {
        mActivePlayerIdx = idx;
        Log.i(TAG, "Active player is " + getActivePlayer());
    }

    public Player getNonActivePlayer() {
        return getPlayer(getOpositePlayerIdx(mActivePlayerIdx));
    }

    public Player getServingPlayer() {
        return getPlayer(mServingPlayer);
    }

    private void changeServingPlayer() {
        setServingPlayer(getOpositePlayerIdx(mServingPlayer));
    }

    private void setServingPlayer(int servingPlayer) {
        mServingPlayer = servingPlayer;
        Log.i(TAG, "Serving player is " + getServingPlayer());
    }

    private void changeActivePlayer() {
        mActivePlayerIdx = (mActivePlayerIdx + 1) % mPlayers.length;
    }

    public void setWaitingForService(boolean waitingForService) {
        mState = State.WAITING_FOR_SERVICE;
        if (waitingForService) {
            Log.i(TAG, "Waiting for service of player: " + getServingPlayer());
            setActivePlayer(mServingPlayer);
        }
    }

    public void onSwing(Player player) {
        if (isActive(player)) {
            switch (mState) {
                case WAITING_FOR_SERVICE:
                    onBallHittedInTime(player);
                    break;
                case BEFORE_HIT_TIME:
                    onSwungToEarly(player);
                    break;
                case IN_HIT_TIME:
                    onBallHittedInTime(player);
                    break;
            }
        } else {
            Log.v(TAG, player + "'s swing ignored");
        }

    }

    private void onBallHittedInTime(Player player) {
        Log.i(TAG, player + " hits the ball");
        mState = State.BEFORE_HIT_TIME;
        setWaitingForService(false);
        changeActivePlayer();
        mHandler.removeMessages(MSG_HIT_TIME_END);
        mHandler.sendEmptyMessageDelayed(MSG_HIT_TIME_START, 2000);
    }

    private void onSwungToEarly(Player player) {
        Log.i(TAG, player + " swings too early");
        onPlayerMiss(player);
    }

    private void onPlayerMiss(Player player) {
        Log.i(TAG, player + " misses!!");


        if (player == getServingPlayer()) {
            Log.i(TAG, player + " loses the service");
            changeServingPlayer();
        } else {
            getNonActivePlayer().onPointWon();
        }

        setWaitingForService(true);
    }



}
