package com.my.android.wifitest.WifiP2P;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;


/**
 * Created by jhchen on 2016/12/23.
 */

public class WifiP2PBroadcastReceiver extends BroadcastReceiver{
    interface WifiP2PBroadcastReceiverListener extends  WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {
        public void updateMyDeviceInfo(WifiP2pDevice myDevice);
        void onDisconnected();
    }

    private Context mContext;
    private WifiP2pManager mWifiP2PManager;
    private WifiP2pManager.Channel mChannel;

    //MyWifiP2PManager中处理广播中的各种回调
    private WifiP2PBroadcastReceiverListener mWifiP2PBroadcastReceiverListener;


    public WifiP2PBroadcastReceiver( Context context, WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2PBroadcastReceiverListener wifiP2PBroadcastReceiverListener) {
        super();
        mContext = context;
        mWifiP2PManager = manager;
        mChannel = channel;
        mWifiP2PBroadcastReceiverListener = wifiP2PBroadcastReceiverListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
            } else {

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            mWifiP2PManager.requestPeers(mChannel, mWifiP2PBroadcastReceiverListener);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                mWifiP2PManager.requestConnectionInfo(mChannel, mWifiP2PBroadcastReceiverListener);
            } else {
                // It's a disconnect
                mWifiP2PBroadcastReceiverListener.onDisconnected();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice myDevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mWifiP2PBroadcastReceiverListener.updateMyDeviceInfo(myDevice);
        }
    }
}
