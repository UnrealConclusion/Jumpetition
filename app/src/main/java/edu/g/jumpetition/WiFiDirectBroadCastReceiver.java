package edu.g.jumpetition;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class WiFiDirectBroadCastReceiver extends BroadcastReceiver {
    final static int LOBBY_ACTIVITY = 1;
    final static int JUMP_ACTIVITY = 2;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private Context mContext;
    private int activityNum;

    public WiFiDirectBroadCastReceiver(WifiP2pManager mWifiP2pManager, WifiP2pManager.Channel mChannel, Context mContext, int activityNum) {
        this.mWifiP2pManager = mWifiP2pManager;
        this.mChannel = mChannel;
        this.mContext = mContext;
        this.activityNum = activityNum;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        assert action != null;

        // detect changes in the WIFI state
        if (action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
            int isEnabled = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (isEnabled != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Turn the Wifi on", Toast.LENGTH_SHORT).show();
            }
        }

        // listen for peers
        else if (action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
            if (mWifiP2pManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (activityNum == LOBBY_ACTIVITY){
                    mWifiP2pManager.requestPeers(mChannel, ((LobbyActivity) context).peerLL);
                }
                /*else if (activityNum == JUMP_ACTIVITY){
                    mWifiP2pManager.requestPeers(mChannel, ((JumpActivity) context).peerLL);
                }*/
            }
        }

        else if (action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)){
            if (mWifiP2pManager == null) {
                return;
            }
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (info.isConnected()){
                if (activityNum == LOBBY_ACTIVITY){
                    mWifiP2pManager.requestConnectionInfo(mChannel, ((LobbyActivity) context).connectionIL);
                }
                /*else if (activityNum == JUMP_ACTIVITY){
                    mWifiP2pManager.requestConnectionInfo(mChannel, ((JumpActivity) context).connectionIL);
                }*/
            }
        }
        else if (action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)){

        }
    }
}