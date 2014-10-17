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
    private static final int MSG_WAIT_FOR_SERVICE = 2;



    public enum State {
        IDLE,
        WAITING_FOR_SERVICE,
        BEFORE_HIT_TIME,
        IN_HIT_TIME,
    }

    private WorldListener mListener;

    private Handler mHandler;

    private Player[] mPlayers = new Player[]{new Player(this, "A"), new Player(this, "B")};

    private int mActivePlayerIdx;
    private int mServingPlayer;

    private State mState;


    public void setListener(WorldListener listener) {
        mListener = listener;
    }

    public void init() {
        Handler.Callback callback = new Handler.Callback() {

            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIT_TIME_START:
                        onHitTimeStarts((Long) message.obj);
                        break;
                    case MSG_HIT_TIME_END:
                        onHitTimeEnds();
                        break;
                    case MSG_WAIT_FOR_SERVICE:
                        setWaitingForService();
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
        setWaitingForService();
    }

    public boolean isActive(Player player) {
        return getActivePlayer() == player;
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
        mListener.oneActivePlayerChanged(getActivePlayer());
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

    public void setWaitingForService() {
        mState = State.WAITING_FOR_SERVICE;
        setActivePlayer(mServingPlayer);
        Log.i(TAG, "Waiting for service of player: " + getServingPlayer());
        mListener.onWaitingForService(getServingPlayer());
    }

    private void onHitTimeStarts(long time) {
        mState = State.IN_HIT_TIME;
        mListener.onWaitingForSwing(getActivePlayer());
        mHandler.sendEmptyMessageDelayed(MSG_HIT_TIME_END, time);
    }

    private void onHitTimeEnds() {
        if (mState == State.IN_HIT_TIME) {
            onPlayerMiss(getActivePlayer());
        }
        mListener.onBallFallsToGround();
    }

    public void onSwing(Player player, double force) {
        mListener.onSwing(player);
        if (isActive(player)) {
            switch (mState) {
                case WAITING_FOR_SERVICE:
                    onBallHittedInTime(player, force);
                    break;
                case BEFORE_HIT_TIME:
                    onSwungToEarly(player);
                    break;
                case IN_HIT_TIME:
                    onBallHittedInTime(player, force);
                    break;

            }
        } else {
            Log.v(TAG, player + "'s swing ignored");
        }

    }

    private void onBallHittedInTime(Player player, double force) {
        Log.i(TAG, player + " hits the ball");
        mState = State.BEFORE_HIT_TIME;
        mHandler.removeMessages(MSG_HIT_TIME_END);

        long time = getTimeFromForce(force);
        final Message msg = mHandler.obtainMessage(MSG_HIT_TIME_START, time);
        mHandler.sendMessageDelayed(msg, time);
        changeActivePlayer();
        
        mListener.onBallHitted();
    }

    private long getTimeFromForce(double force) {
        return (long) (300 + (2000 * force));
    }

    private void onSwungToEarly(Player player) {
        Log.i(TAG, player + " swings too early");
        mHandler.removeMessages(MSG_HIT_TIME_START);
        mListener.onBallFallsToGround();
        onPlayerMiss(player);
    }

    private void onPlayerMiss(Player player) {
        Log.i(TAG, player + " misses!!");


        if (player == getServingPlayer()) {
            Log.i(TAG, player + " loses the service");
            changeServingPlayer();
        } else {
            getNonActivePlayer().onPointWon();
            mListener.onPointWon(getNonActivePlayer());
        }

        mState = State.IDLE;
        mListener.onIdle();
        mHandler.sendEmptyMessageDelayed(MSG_WAIT_FOR_SERVICE, 3000);
    }

    /**
     * Listener interface
     */
    public static interface WorldListener {

        void onWaitingForService(Player servingPlayer);

        void onPointWon(Player nonActivePlayer);

        void oneActivePlayerChanged(Player activePlayer);

        void onWaitingForSwing(Player activePlayer);

        void onBallHitted();

        void onSwing(Player player);

        void onBallFallsToGround();

        void onIdle();
    }

}
