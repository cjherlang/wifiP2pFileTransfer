package com.my.android.wifitest.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.my.android.wifitest.R;
import com.my.android.wifitest.WifiP2P.WifiP2PUtils;

import java.util.List;

/**
 * Created by jhchen on 2016/12/26.
 */

public class DiscoverDevicesListAdapter extends BaseAdapter{
    Context mContext = null;
    List<WifiP2pDevice> mDevicesList = null;

    public DiscoverDevicesListAdapter(Context context) {
        this.mContext = context;
    }

    public void setDevicesList(List<WifiP2pDevice> devicesList) {
        this.mDevicesList = devicesList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View contentView = view;
        if (contentView == null){
            contentView = LayoutInflater.from(mContext).inflate(R.layout.devices_list_item, viewGroup, false);
        }
        if (i < mDevicesList.size() && mDevicesList.get(i) != null){
            TextView name = (TextView) contentView.findViewById(R.id.device_name);
            TextView status = (TextView) contentView.findViewById(R.id.device_status);
            TextView ip = (TextView) contentView.findViewById(R.id.device_ip);
            name.setText(mDevicesList.get(i).deviceName);
            status.setText(WifiP2PUtils.getDeviceStatus(mDevicesList.get(i).status));
            ip.setText(mDevicesList.get(i).deviceAddress);
        }
        return contentView;
    }

    @Override
    public int getCount() {
        return mDevicesList != null && !mDevicesList.isEmpty() ? mDevicesList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return mDevicesList != null && !mDevicesList.isEmpty() ? mDevicesList.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
