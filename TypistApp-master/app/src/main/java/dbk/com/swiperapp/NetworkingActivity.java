package dbk.com.swiperapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class NetworkingActivity extends AppCompatActivity {
   BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothServerSocket bluetoothServerSocket;
    BluetoothSocket bluetoothSocket;
    ArrayList<String> adapter;
    Button hostButton;
    Button joinButton;
    String gameMode;
    Intent boostintent,paraintent;
    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");
    private final static UUID uuid2 = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001b");


    int DISCOVERABLE_DURATION=300;
    int ENABLE_BT_REQUEST_CODE =1;
    int DISCOVERABLE_BT_REQUEST_CODE=2;
    int GAME_SELECTION_REQUEST_CODE=9;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networking);


        joinButton = (Button) findViewById(R.id.joinButton);
        hostButton = (Button) findViewById(R.id.hostButton);
        adapter = new ArrayList<String>();

        check_Bluetooth_functionality_availability();  //also enables ..if available


        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(getApplicationContext(),GameSelectionActivity.class);
            i.putExtra("PLAYER_MODE","MULTIPLAYER");
            startActivityForResult(i, GAME_SELECTION_REQUEST_CODE);
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              discoverDevices();             //Discover Remote Bluetooth devices.
            }
        });



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

//================================================================================================================================
final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // Whenever a remote Bluetooth device is found
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            Log.w("NETWOKING:JOINbutton","found one device");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Add the name and address to an array adapter to show in a ListView
            adapter.add(device.getName() + "\n" + device.getAddress());
            if(adapter.size()>0) {
                Intent i = new Intent(getApplicationContext(), DevicesListActivity.class);
                Log.w("NETWORKING ACTIVITY", "adapter size:" + adapter.size());
                bluetoothAdapter.cancelDiscovery();
                i.putStringArrayListExtra("DEVICE_LIST", adapter);
                startActivity(i);
            }

        }
    }
};


    //===========================================================================================================================
    void check_Bluetooth_functionality_availability() {

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not supported by this Device", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
            }
        }
    }
    //===========================================================================================================================

    protected void makeDiscoverable(){
        // Make local device discoverable for 300secs
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }
    //===========================================================================================================================
    protected void discoverDevices(){
        // To scan for remote Bluetooth devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filter);

        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        if (bluetoothAdapter.startDiscovery()) {
            Toast.makeText(getApplicationContext(), "Discovering other bluetooth devices...",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Discovery failed to start.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //===========================================================================================================================
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=--=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
            } else { // RESULT_CANCELED as user refuse or failed
                Toast.makeText(getApplicationContext(), "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            }
        }
        //--------------------
       if(requestCode==GAME_SELECTION_REQUEST_CODE)
       {
           if(resultCode==Activity.RESULT_OK)
           {
               gameMode =data.getStringExtra("GAME_MODE");
               Toast.makeText(getApplicationContext(), "Selected Game Mode ="+gameMode, Toast.LENGTH_SHORT).show();
               hostButton.setEnabled(false);
               joinButton.setEnabled(false);

               //Now wait for connections...
               //then send game_mode to client
               //i start the desired game mode ---client also starts the desired gamemode
               makeDiscoverable();
               boostintent = new Intent(this,BoostModeActivity.class);
               boostintent.putExtra("PLAYER_MODE", "MULTIPLAYER");
               paraintent = new Intent(this,ParaModeActivity.class);
               paraintent.putExtra("PLAYER_MODE", "MULTIPLAYER");

               ListeningThread t = new ListeningThread(this,gameMode,boostintent,paraintent);
               t.start();
               ProgressDialog progress = new ProgressDialog(this);
               progress.setMessage("Waiting for Clients....");
               progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
               progress.setMax(10);
               progress.show();
           }
           else
           {
               Toast.makeText(getApplicationContext(), "Game Mode Selection Error!.", Toast.LENGTH_SHORT).show();

           }
       }
        //------------------------------------
       else if (requestCode == DISCOVERABLE_BT_REQUEST_CODE){

           if (resultCode == DISCOVERABLE_DURATION){
               Toast.makeText(getApplicationContext(), "Your device is now discoverable by other devices for " +
                               DISCOVERABLE_DURATION + " seconds",
                       Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(getApplicationContext(), "Fail to enable discoverability on your device.",
                       Toast.LENGTH_SHORT).show();
           }
       }
    }
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--=-=--=-=-=-=-=--=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Networking Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://dbk.com.swiperapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Networking Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://dbk.com.swiperapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}






//****************************************************************************************************************************
class ListeningThread extends Thread {
    private final BluetoothServerSocket bluetoothServerSocket;
    BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    Intent boostintent,paraintent;
    String gameMode;
    Activity context;
    private final static UUID uuid = UUID.fromString("fc5ffc49-00e3-4c8b-9cf1-6b72aad1001a");


    public ListeningThread(Activity activity,String gameMode,Intent boostintent,Intent paraintent) {
        this.context = activity;
        this.gameMode=gameMode;
        this.boostintent = boostintent;
        this.paraintent = paraintent;
        BluetoothServerSocket temp = null;
        try {
            temp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(context.getString(R.string.app_name), uuid);

        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothServerSocket = temp;
    }


    public void run() {
        BluetoothSocket bluetoothSocket;
        // This will block while listening until a BluetoothSocket is returned
        // or an exception occurs
        while (true) {
            try {
                bluetoothSocket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection is accepted
            if (bluetoothSocket != null) {
                context.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "A connection has been accepted.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                // Manage the connection in a separate thread\
                try {


                    OutputStream os = bluetoothSocket.getOutputStream();
                    LonelySocket.setSocket(bluetoothSocket);
                    if(gameMode.compareTo("BOOSTMODE")==0) {
                        os.write(9);
                        boostintent.putExtra("HOST_OR_CLIENT","HOST");
                        context.startActivity(boostintent);
                    }
                    else {
                        os.write(1);
                        paraintent.putExtra("HOST_OR_CLIENT","HOST");
                        context.startActivity(paraintent);
                    }

                } catch (IOException e){

                }

                try {
                    bluetoothServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // Cancel the listening socket and terminate the thread
    public void cancel() {
        try {
            bluetoothServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

///-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-==-=-=-=-====================----------------====================
///-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-==-=-=-=-====================----------------====================
///-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-==-=-=-=-====================----------------====================

///-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-==-=-=-=-====================----------------====================
///-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-==-=-=-=-=-=-==-=-=-=-==-=-=-=-====================----------------====================
 /*class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
*/
    /* //Call this from the main activity to send data to the remote device
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    // Call this from the main activity to shutdown the connection
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}*/


//00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000

class LonelySocket
{
    public  static BluetoothSocket socket;

    public static void setSocket(BluetoothSocket BS)
    {
        socket=BS;
    }

    public static BluetoothSocket getSocket()
    {
        return socket;
    }

}
//00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000