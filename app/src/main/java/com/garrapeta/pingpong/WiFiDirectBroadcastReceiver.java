package com.garrapeta.pingpong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver implements WifiP2pManager.PeerListListener, WifiP2pManager.ActionListener {

    public static final String TAG = WiFiDirectBroadcastReceiver.class.getSimpleName();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiverListener mListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WiFiDirectBroadcastReceiverListener listener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Action received: " + action);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            onP2PStateActionChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            onP2PPeersActionChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }



    private void onP2PStateActionChanged(Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Log.i(TAG, "P2P wifi active.");
            mListener.onWifiP2PActive();
        } else {
            onError("P2P wifi not enabled!");
            mListener.onWifiP2PNotActive();
        }
    }

    private void onP2PPeersActionChanged(Intent intent) {
        // request available peers from the wifi p2p manager. This is an
        // asynchronous call and the calling activity is notified with a
        // callback on PeerListListener.onPeersAvailable()
        if (mManager != null) {
            mManager.requestPeers(mChannel, this);
        }
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
            Log.i(TAG, "Peer found: " + device.deviceName);
            connectTo(device);
        }
    }

    private void connectTo(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, this);
    }

    @Override
    public void onSuccess() {
        Log.i(TAG, "Connection established! ");

    }

    @Override
    public void onFailure(int reason) {
        onError("Could not connect to peer: " + reason);
    }


    private void onError(String message) {
        Log.e(TAG, message);
        mListener.onError(message);
    }

    public static interface WiFiDirectBroadcastReceiverListener {
        void onError(String message);

        void onWifiP2PActive();

        void onWifiP2PNotActive();
    }
}
