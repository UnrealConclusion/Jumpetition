
package edu.g.jumpetition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {

    // Views
    private EditText myNameET;
    private TextView opponentNameTV;
    private Button readyB;

    // Wifi
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private Boolean connected = false;

    // Peers
    private String peerName;
    private ArrayList<String> peerNames = new ArrayList<String>();;
    private ArrayList<WifiP2pDevice> peerDevices = new ArrayList<WifiP2pDevice>();
    private ArrayAdapter<String> mAdapter;

    private Handler handler;
    private Communicate communicate = null;

    // Threads
    sendMsg msg;

    public LobbyActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        handler = messageHandler;

        // find views
        myNameET = findViewById(R.id.my_name_text_edit);
        opponentNameTV = findViewById(R.id.opponents_name_text_view);
        readyB = findViewById(R.id.ready_button);

        setupBroadCastReceiver();

        // check permissions
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // permission was not granted so return to main activity
            Intent intent = new Intent();
            setResult(-2, intent);
            finish();
            return;
        }

        // find peers
        mP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Looking for peers", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Discovery Fail", Toast.LENGTH_SHORT).show();
            }
        });

        // setup list view
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        ListView listView = findViewById(R.id.players_list_view);
        listView.setAdapter(mAdapter);

        // connect to a peer
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = peerDevices.get(i);
                WifiP2pConfig config = new WifiP2pConfig();

                if (device != null) {
                    config.deviceAddress = device.deviceAddress;
                }

                mP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connecting with " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Cannot Connect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupBroadCastReceiver(){
        // initial setup
        // Wifi
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mP2pManager.initialize(this, Looper.getMainLooper(), null);
        mReceiver = new WiFiDirectBroadCastReceiver(mP2pManager, mChannel);

        // intent filter setup
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    /* register the receiver on resume
     * */
    @Override
    public void onResume() {
        mReceiver = new WiFiDirectBroadCastReceiver(mP2pManager, mChannel);
        registerReceiver(mReceiver, mIntentFilter);
        super.onResume();
    }

    /* unregister receiver on pause
     * */
    @Override
    public void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    public void readyOnClick(View view) throws InterruptedException {
        if (!connected){
            Toast.makeText(this, "Not Connected to Anyone Yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // check input
        if (myNameET.getText().toString().equals("")){
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (myNameET.getText().length() > 20){
            Toast.makeText(this, "Name is over 20 characters pick something else", Toast.LENGTH_SHORT).show();
            return;
        }

        // let the opponent know your ready + send them your name
        msg = new sendMsg(myNameET.getText().toString());
        msg.start();

        // disable inputs
        myNameET.setEnabled(false);
        readyB.setEnabled(false);

        // start if the opponent is ready
        if (!opponentNameTV.getText().toString().equals("Waiting for Opponent ...")){
            // communicate.interrupt();
            startCompetition();
        }

    }

    private boolean beenCalled = false;
    /* start the jump activity
     * */
    private void startCompetition() {
        System.out.println("Starting now ");
            Intent intent = new Intent(this, JumpActivity.class);
            intent.putExtra("Mode", MainActivity.COMPETITION_MODE);
            intent.putExtra("opponentName", peerName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
    }

    /*-- Listeners ------------------------------------------------------------------------------*/

    /* Searching for peers
     * */
    WifiP2pManager.PeerListListener peerLL = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (wifiP2pDeviceList != null) {

                // no peers if size of list is 0
                if (wifiP2pDeviceList.getDeviceList().size() == 0) {
                    Toast.makeText(getApplicationContext(), "No Peers Near Me", Toast.LENGTH_SHORT).show();
                }

                // check if our list of peers is different and replace it if it is
                if (wifiP2pDeviceList.getDeviceList() != peerDevices) {
                    peerDevices.clear();
                    peerDevices.addAll(wifiP2pDeviceList.getDeviceList());
                }

                // clear everything
                mAdapter.clear();

                // add all name to list view
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    mAdapter.add(device.deviceName);
                }
            }
        }
    };

    /* connect to the peer and run either a client or server thread depending on whether we are the host
     * */
    WifiP2pManager.ConnectionInfoListener connectionIL = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if (wifiP2pInfo != null && !connected){
                final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                if (wifiP2pInfo.groupFormed){
                    // this device is the host start server thread
                    if (wifiP2pInfo.isGroupOwner){
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        Server server = new Server();
                        server.start();
                    }
                    // this device is the client start a client thread
                    else {
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        Client client = new Client(groupOwnerAddress);
                        client.start();
                    }
                    connected = true;
                }
            }
        }
    };

    /*-- Networking -----------------------------------------------------------------------------*/

    /* a server thread for the host
     * */
    private class Server extends Thread{
        @Override
        public void run(){
            try{
                ServerSocket serverSock = new ServerSocket(8002);
                mySocket.setSocket(serverSock.accept());
                mySocket.setStream();
                communicate = new Communicate();
                communicate.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* client thread for the guest
     * */
    public class Client extends Thread{
        private String hostAddress;

        public Client(InetAddress ownerAddress){
            this.hostAddress = ownerAddress.getHostAddress();
        }

        @Override
        public void run(){
            try{
                mySocket.setSocket(new Socket());
                mySocket.getSocket().connect(new InetSocketAddress(hostAddress, 8002), 1000);
                mySocket.setStream();
                communicate = new Communicate();
                communicate.start();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /* Thread Handles reception and sending of messages
     * */
    public class Communicate extends Thread{
        public static final int MESSAGE_NAME = 1;
        private boolean receivedMsg = false;

        @Override
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(mySocket.getSocket()!=null){
                try{
                    if (!receivedMsg){
                        bytes = mySocket.getInputStream().read(buffer);
                        if(bytes > 0){
                            handler.obtainMessage(MESSAGE_NAME, bytes, -1, buffer).sendToTarget();
                            receivedMsg = true;
                        }}
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) throws IOException {
            mySocket.getOutputStream().write(bytes);
            mySocket.getOutputStream().flush();
        }
    }


    private class sendMsg extends Thread{
        String message;

        public sendMsg(String message){
            this.message = message;
        }

        @Override
        public void run(){
            try {
                while(communicate == null){
                    Thread.sleep(1);
                }
                // System.out.println("\nI got to send message\n");
                communicate.write(message.getBytes());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*-------------------------------------------------------------------------------------------*/

    Handler messageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            // System.out.println("\nI got to handle message\n");
            switch (message.what){
                case Communicate.MESSAGE_NAME:
                    byte[] readBuffer = (byte[]) message.obj;
                    String msg = new String(readBuffer, 0, message.arg1);
                    setOpponentName(msg);
                    break;
            }
            return true;
        }
    });

    private void setOpponentName(final String name){
        Handler nameHandler = new Handler(Looper.getMainLooper());
        nameHandler.post(new Runnable() {
            @Override
            public void run() {
                peerName = name;
                String text = name + " [is Ready]";
                opponentNameTV.setText(text);
                if (!readyB.isEnabled()){
                    startCompetition();
                }
            }
        });
    }

}



