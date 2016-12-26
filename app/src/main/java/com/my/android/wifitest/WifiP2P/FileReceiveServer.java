package com.my.android.wifitest.WifiP2P;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jhchen on 2016/12/23.
 */

public class FileReceiveServer extends AsyncTask<Void, Void, String> {

    private Context context;
    private String mPath;

    /**
     * @param context
     * @param path
     */
    public FileReceiveServer(Context context, String path) {
        this.context = context;
        mPath = path;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(WifiP2PUtils.MY_WIFI_DIRECT_SERVER_PORT);
            Log.d(MyWifiDirectPresenter.TAG, "Server: Socket opened");
            Socket client = serverSocket.accept();
            Log.d(MyWifiDirectPresenter.TAG, "Server: connection done");
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + "wifitest/" + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".jpg");

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.d(MyWifiDirectPresenter.TAG, "server: copying files " + f.toString());
            InputStream inputstream = client.getInputStream();
            WifiP2PUtils.copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return f.getAbsolutePath();
        } catch (IOException e) {
            Log.e(MyWifiDirectPresenter.TAG, e.getMessage());
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
            context.startActivity(intent);
        }

    }

    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        Toast.makeText(context, "Opening a server socket", Toast.LENGTH_LONG);
    }
}
