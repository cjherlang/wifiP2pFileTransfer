package com.my.android.wifitest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.android.wifitest.WifiP2P.MyWifiDirectPresenter;
import com.my.android.wifitest.WifiP2P.WifiP2PListener;
import com.my.android.wifitest.WifiP2P.WifiP2PUtils;
import com.my.android.wifitest.adapter.DiscoverDevicesListAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements WifiP2PListener, View.OnClickListener{
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    Context mContext;
    MyWifiDirectPresenter mMyWifiDirectPresenter;
    private BroadcastReceiver receiver = null;
    List<WifiP2pDevice> mDevicesList = null;

    //devices list
    Button mDiscoverBtn;
    ListView mDeviceListView;
    DiscoverDevicesListAdapter mDevicesListAdapter = null;

    //my device info
    TextView mMyDeviceName;
    TextView mMyDeviceStatus;
    TextView mMyDeviceIP;
    Button mDisconnectBtn;

    //receive data and send data
    LinearLayout mDataOperateContainer;
    Button mReceiveBtn;
    Button mSendBtn;
    boolean mIsSendData = false;

    Button mSelectFileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mMyWifiDirectPresenter = new MyWifiDirectPresenter(mContext, this);
        findViewById();
        setup();
    }

    void findViewById(){
        mMyDeviceName = (TextView) findViewById(R.id.my_device_name);
        mMyDeviceStatus = (TextView) findViewById(R.id.my_device_status);
        mMyDeviceIP = (TextView) findViewById(R.id.my_device_ip);
        mDisconnectBtn = (Button) findViewById(R.id.disconnect_btn);
        mDisconnectBtn.setVisibility(View.GONE);
        mDisconnectBtn.setOnClickListener(this);

        mDataOperateContainer = (LinearLayout) findViewById(R.id.data_operate_container);
        mReceiveBtn = (Button) findViewById(R.id.receive_data);
        mReceiveBtn.setOnClickListener(this);
        mSendBtn = (Button) findViewById(R.id.send_data);
        mSendBtn.setOnClickListener(this);

        mDiscoverBtn = (Button) findViewById(R.id.wifi_scan);
        mDiscoverBtn.setVisibility(View.GONE);
        mDiscoverBtn.setOnClickListener(this);
        mDeviceListView = (ListView) findViewById(R.id.wifi_list);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mIsSendData){
                    Toast.makeText(mContext, "正在连接...", Toast.LENGTH_LONG).show();
                    mMyWifiDirectPresenter.connectDevice(mDevicesList.get(i));
                } else {
                    Toast.makeText(mContext, "请等待客户端连接", Toast.LENGTH_LONG).show();
                }
            }
        });

        mSelectFileBtn = (Button) findViewById(R.id.file_select);
        mSelectFileBtn.setVisibility(View.GONE);
        mSelectFileBtn.setOnClickListener(this);
    }

    void setup(){
        if (mDevicesListAdapter == null){
            mDevicesListAdapter = new DiscoverDevicesListAdapter(mContext);
        }
        mDeviceListView.setAdapter(mDevicesListAdapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.wifi_scan){
            mMyWifiDirectPresenter.discoverDevices();
        }

        if (id == R.id.disconnect_btn){
            mMyWifiDirectPresenter.disconnectDevice();
        }

        if (id == R.id.receive_data){
            mIsSendData = false;
            mDataOperateContainer.setVisibility(View.GONE);
            mDiscoverBtn.setVisibility(View.VISIBLE);
        }
        if (id == R.id.send_data){
            mIsSendData = true;
            mDataOperateContainer.setVisibility(View.GONE);
            mDiscoverBtn.setVisibility(View.VISIBLE);
        }

        if (id == R.id.file_select){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = mMyWifiDirectPresenter.getWifiP2PBroadcastReceiver();
        registerReceiver(receiver, mMyWifiDirectPresenter.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_FILE_RESULT_CODE){
            mMyWifiDirectPresenter.sendFileToServer(data);
        }
    }

    /**
     * wifip2p interface implement
     * @param wifiP2pDeviceList
     */
    @Override
    public void onDeviceListGet(List<WifiP2pDevice> wifiP2pDeviceList) {
        mDevicesList = wifiP2pDeviceList;
        mDevicesListAdapter.setDevicesList(wifiP2pDeviceList);
    }

    @Override
    public void onConnected(WifiP2pInfo connectInfo) {
        Toast.makeText(mContext, "main activity connect sucess", Toast.LENGTH_LONG).show();
        if (connectInfo.groupFormed && connectInfo.isGroupOwner) {
            //GroupOwner act as server, receive data
            mMyWifiDirectPresenter.startReceiveFileServer();
        } else if (connectInfo.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button. send file
            mSelectFileBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDisconnected() {
        mDataOperateContainer.setVisibility(View.VISIBLE);
        mDiscoverBtn.setVisibility(View.GONE);
        mSelectFileBtn.setVisibility(View.GONE);
    }

    /**
     * 更新自己的设备信息
     * @param myDevice
     */
    @Override
    public void onUpdateMyDeviceInfo(WifiP2pDevice myDevice) {
        if (myDevice != null){
            mMyDeviceName.setText(myDevice.deviceName);
            mMyDeviceStatus.setText(WifiP2PUtils.getDeviceStatus(myDevice.status));
            mMyDeviceIP.setText(myDevice.deviceAddress);
            if (myDevice.status == WifiP2pDevice.CONNECTED){
                mDisconnectBtn.setVisibility(View.VISIBLE);
                mDataOperateContainer.setVisibility(View.GONE);
            } else {
                mDisconnectBtn.setVisibility(View.GONE);
            }
        }
    }
}
