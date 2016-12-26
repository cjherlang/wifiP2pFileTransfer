package com.my.android.wifitest.WifiP2P;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.List;

/**
 * Created by jhchen on 2016/12/23.
 */

public interface WifiP2PListener {
    void onDeviceListGet(List<WifiP2pDevice> wifiP2pDeviceList);
    void onConnected(WifiP2pInfo connectInfo);
    void onDisconnected();
    void onUpdateMyDeviceInfo(WifiP2pDevice myDevice);
}
