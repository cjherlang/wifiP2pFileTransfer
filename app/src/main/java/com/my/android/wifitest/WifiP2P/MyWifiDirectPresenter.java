package com.my.android.wifitest.WifiP2P;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by jhchen on 2016/12/23
 */

public class MyWifiDirectPresenter implements WifiP2PBroadcastReceiver.WifiP2PBroadcastReceiverListener{
    public static final String TAG = "MyWifiDirectPresenter";
    private Context mContext = null;

    private WifiManager mWifiManager = null;
    private WifiP2pManager mWifiP2PManager = null;
    private WifiP2PListener mWifiP2PListener = null;

    private List<WifiP2pDevice> mDiscoverPeersList = new ArrayList<WifiP2pDevice>();
    WifiP2pInfo mConnectWifiP2pInfo;
    private WifiP2pManager.Channel mP2PWifiChannel = null;
    private IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2PBroadcastReceiver mWifiP2PBroadcastReceiver = null;

    public MyWifiDirectPresenter(Context context, WifiP2PListener wifiP2PListener) {
        mContext = context;
        if (wifiP2PListener == null){
            return;
        }
        mWifiP2PListener = wifiP2PListener;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiP2PManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

        mP2PWifiChannel = mWifiP2PManager.initialize(context, context.getMainLooper(), null);

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        if (!isWifiCanUse()){
            setWifiCanUse(true);
        }
    }

    public List<WifiP2pDevice> getDiscoverPeersList() {
        return mDiscoverPeersList;
    }

    public WifiP2pManager.Channel getP2PWifiChannel() {
        return mP2PWifiChannel;
    }

    public WifiP2PBroadcastReceiver getWifiP2PBroadcastReceiver() {
        mWifiP2PBroadcastReceiver = new WifiP2PBroadcastReceiver(mContext, mWifiP2PManager, mP2PWifiChannel, this);
        return mWifiP2PBroadcastReceiver;
    }

    public IntentFilter getIntentFilter() {
        return mIntentFilter;
    }

    public boolean isWifiCanUse() {
        return mWifiManager.isWifiEnabled();
    }

    public void setWifiCanUse(boolean wifiCanUse) {
        if(wifiCanUse) {
            openWifi();
        } else {
            closeWifi();
        }
    }

    public boolean openWifi(){
        if (!mWifiManager.isWifiEnabled()){
            return mWifiManager.setWifiEnabled(true);
        } else {
            return true;
        }
    }

    public boolean closeWifi(){
        if (mWifiManager.isWifiEnabled()){
            return mWifiManager.setWifiEnabled(false);
        }{
            return true;
        }
    }

    @Override
    public void updateMyDeviceInfo(WifiP2pDevice myDevice){
        mWifiP2PListener.onUpdateMyDeviceInfo(myDevice);
    }

    public void discoverDevices(){
        if (!isWifiCanUse()){
            openWifi();
        }
        if(isWifiCanUse()){
            mWifiP2PManager.discoverPeers(mP2PWifiChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    //handle in broadcast
                }

                @Override
               public void onFailure(int i) {
                    Toast.makeText(mContext, "未找到设备，请重新查找", Toast.LENGTH_LONG);
                }
            });
        } else {
            Toast.makeText(mContext, "请打开wifi", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 取得发现的设备列表
     * @param wifiP2pDeviceList
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        mDiscoverPeersList.clear();
        mDiscoverPeersList.addAll(wifiP2pDeviceList.getDeviceList());
        mWifiP2PListener.onDeviceListGet(mDiscoverPeersList);
    }

    public void stopDiscoverDevices(){
        mWifiP2PManager.stopPeerDiscovery(mP2PWifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    public void connectDevice(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.groupOwnerIntent = 0;
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mWifiP2PManager.connect(mP2PWifiChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //handle in broadcast
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(mContext, "连接失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        mConnectWifiP2pInfo = wifiP2pInfo;
        mWifiP2PListener.onConnected(mConnectWifiP2pInfo);
    }

    public void disconnectDevice(){
        mWifiP2PManager.removeGroup(mP2PWifiChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(mContext, "断开连接失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDisconnected() {
        mWifiP2PListener.onDisconnected();
    }

    public void startReceiveFileServer(){
        new FileReceiveServer(mContext, "").execute();
    }

    public void sendFileToServer(Intent data){
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        Intent serviceIntent = new Intent(mContext, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                mConnectWifiP2pInfo.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, WifiP2PUtils.MY_WIFI_DIRECT_SERVER_PORT);
        mContext.startService(serviceIntent);
    }
}
