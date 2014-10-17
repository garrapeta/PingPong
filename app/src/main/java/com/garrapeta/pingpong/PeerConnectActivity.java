package com.garrapeta.pingpong;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class PeerConnectActivity extends Activity implements WiFiDirectBroadcastReceiver.WiFiDirectBroadcastReceiverListener {

    public static final String TAG = WiFiDirectBroadcastReceiver.TAG;

    private Button mConnectButton;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private BroadcastReceiver mWiFiDirectBroadcastReceiver;
    private IntentFilter mWiFiDirectIntentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);


        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setEnabled(false);

        setupWifiP2P();
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mWiFiDirectBroadcastReceiver, mWiFiDirectIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWiFiDirectBroadcastReceiver);
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

    public void onConnectClicked(View view) {
        discoverPeers();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWifiP2PActive() {
        mConnectButton.setEnabled(true);
    }

    @Override
    public void onWifiP2PNotActive() {
        mConnectButton.setEnabled(false);
    }


    private void discoverPeers() {
        Log.i(TAG, "Discovering peers....");

        mWifiP2pManager.discoverPeers(mWifiP2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // action done in onReceive with WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
                Log.i(TAG, "Discovering peers succeeded");
            }

            @Override
            public void onFailure(int reasonCode) {
                onError("Could not discover peers. Reason: " + reasonCode);
            }
        });
    }
}
