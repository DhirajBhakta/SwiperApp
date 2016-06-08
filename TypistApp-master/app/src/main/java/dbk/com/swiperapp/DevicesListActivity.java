package dbk.com.swiperapp;

import android.app.Activity;
import android.app.LauncherActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class DevicesListActivity extends AppCompatActivity {
ListView listView;
ArrayList<String> deviceList;
ArrayAdapter adapter;
    Activity act =this;
    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");

    BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_list);

        Log.w("DEVICE_LIST_ACTIVITY", "Inside the Activity!");
        listView =(ListView)findViewById(R.id.listView);

        Intent recvIntent=getIntent();
        Log.w("DEVICE_LIST_ACTIVITY","Inside the Activity!");
        deviceList=recvIntent.getStringArrayListExtra("DEVICE_LIST");
        Log.w("DEVICE_LIST_ACTIVITY","Devicelist size"+deviceList.size());
        for(String S:deviceList)
          Log.w("DEVICE_LIST_ACTIVITY", S);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);
                String MAC = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);
                // Initiate a connection request in a separate thread
                Intent boostIntent = new Intent(getApplicationContext(),BoostModeActivity.class);
                boostIntent.putExtra("PLAYER_MODE", "MULTIPLAYER");
                Intent paraIntent = new Intent(getApplicationContext(),ParaModeActivity.class);
                paraIntent.putExtra("PLAYER_MODE", "MULTIPLAYER");
                ConnectThread t = new ConnectThread(bluetoothDevice,act,boostIntent,paraIntent);
                t.start();
            }
        });

    }

}


 class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
     Intent boostIntent,paraIntent;
     Activity act;
     private final static UUID MY_UUID = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");


     public ConnectThread(BluetoothDevice device,Activity act,Intent boostIntent,Intent paraIntent) {
        BluetoothSocket tmp = null;
        mmDevice = device;
         this.boostIntent=boostIntent;
         this.paraIntent=paraIntent;
         this.act = act;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        }
        mmSocket = tmp;
    }

    public void run() {

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
           LonelySocket.setSocket(mmSocket);
        // Do work to manage the connection (in a separate thread)
        try {
            InputStream is = mmSocket.getInputStream();
            int gameMode=is.read();
            Log.w("gamemode:",""+gameMode);
            if(gameMode==9)
            {//startBoost_mode
                boostIntent.putExtra("HOST_OR_CLIENT","CLIENT");
                act.startActivity(boostIntent);
            }
            else
            {//startPara_Mode
               paraIntent.putExtra("HOST_OR_CLIENT", "CLIENT");
               act.startActivity(paraIntent);
            }
        }catch(IOException ioe){}
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}