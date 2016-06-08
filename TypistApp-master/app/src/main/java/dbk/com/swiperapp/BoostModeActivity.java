package dbk.com.swiperapp;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BoostModeActivity extends AppCompatActivity {
BluetoothSocket socket;
EditText boostModeEditText ;
TextView currentWordTextView;
    TextView myScoreTextView;
    TextView hisScoreTextView;
    TextView timerView;
    TextView resultTextView;
    public TextView hisResultTextView;
    TextView textview2;
InputStream inputStream;
ArrayList<String> wordList;
Random random=new Random();
String currentWord;
ArrayList<String> sequence;
int Isequence[]=new int[100];
int score=0;
String playerMode="";
String hostOrClient="";
    CountDownTimer cdTimer;
int count=0;
    OpponentReciever_SenderThread2 t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost_mode);


        boostModeEditText = (EditText)findViewById(R.id.boostModeEditText);
        currentWordTextView=(TextView)findViewById(R.id.currentWordTextView);
        myScoreTextView=(TextView)findViewById(R.id.myScoreTextView);
        hisScoreTextView=(TextView)findViewById(R.id.hisScoreTextView);
        textview2=(TextView)findViewById(R.id.textView2);
        timerView=(TextView)findViewById(R.id.TimerTextView);
        resultTextView=(TextView)findViewById(R.id.myresult);
        hisResultTextView=(TextView)findViewById(R.id.opresult);
        FILE_EXTRACTION_OF_WORDS();

        Intent i = getIntent();
        playerMode=i.getStringExtra("PLAYER_MODE");
        hostOrClient=i.getStringExtra("HOST_OR_CLIENT");

        if(playerMode.compareTo("MULTIPLAYER")==0)
        {
            socket=LonelySocket.getSocket();

            if(hostOrClient.compareTo("HOST")==0)//Host
            {
                generateSequence(socket);


            }
            else//Client
            {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Problem is here :///try making a separate thread which sends all strings one by one(or indexes)..all other
                    //threads wait till this is done...then play game
                     getSequence(socket);


            }
            t =new OpponentReciever_SenderThread2(socket,this);
            t.start();
        }
        else //singlePlayer
        {
           hisScoreTextView.setVisibility(View.INVISIBLE);
           textview2.setVisibility(View.INVISIBLE);
            generateSequence();
            Log.w("singleplayer:", "hgenseq done");

        }


        cdTimer= new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timerView.setText("00:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerView.setText("");
                boostModeEditText.setEnabled(false);
                currentWordTextView.setText("GAME OVER!");
                currentWordTextView.setTextSize(40);
                resultTextView.setText("Your Score:" + score + "wpm");
                if(playerMode.compareTo("MULTIPLAYER")==0) {
                    try {
                        int opScore = socket.getInputStream().read();
                        hisResultTextView.setText("Opponent's Score:" + opScore + "wpm");
                        t.cancel();

                    }catch(IOException ioe){}
                }
            }
        }.start();









        currentWord = sequence.get(0);
        currentWordTextView.setText(currentWord);


        boostModeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String TextViewText = (String) currentWordTextView.getText();
                String EditTextText = s.toString();

                if (TextViewText.compareTo(EditTextText) == 0) {

                    score++;
                    currentWord=sequence.get(score);
                    currentWordTextView.setText(currentWord);
                    currentWordTextView.setTextColor(Color.WHITE);
                    boostModeEditText.setText("");

                    myScoreTextView.setText(""+score);
                    count++;
                }
                else if(TextViewText.length()<=EditTextText.length())
                    currentWordTextView.setTextColor(Color.RED);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });






        }




    void FILE_EXTRACTION_OF_WORDS()
    { AssetManager assetManager = getAssets();
        try {
            inputStream = assetManager.open("words.txt");
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        wordList = new ArrayList<>();
        try {
            while ((line = in.readLine()) != null) {
                String word = line.trim();
                wordList.add(word);
            }
        }catch(IOException ioe)
        {
            Toast toast = Toast.makeText(this, "Could not Save words into buffer", Toast.LENGTH_LONG);
            toast.show();
        }

    }


    void generateSequence()
    {
        sequence=new ArrayList<>();
        for(int i=0;i<100;i++)
           sequence.add(wordList.get(random.nextInt(wordList.size())));


    }

    void generateSequence(BluetoothSocket sock)
    {
        sequence=new ArrayList<>();
        for(int i=0;i<100;i++) {
            Isequence[i] = random.nextInt(wordList.size());
            Log.w("HOST","gen:"+Isequence[i]);
            sequence.add(wordList.get(Isequence[i]));
            try {
                Log.w("HOST","snt:"+intToBytes(Isequence[i]));
                sock.getOutputStream().write(intToBytes(Isequence[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    void getSequence(BluetoothSocket sock)
    {
        sequence=new ArrayList<>();
        byte[] buffer=new byte[4];
        for(int i=0;i<100;i++) {
            try {
                sock.getInputStream().read(buffer);
                Log.w("CLIENT", "recv:" + buffer);
                Isequence[i] = bytesToInt(buffer);
                Log.w("CLIENT","num"+Isequence[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sequence.add(wordList.get(Isequence[i]));
            Log.w("CLIENT",sequence.get(i));

        }

        for(String s:sequence )
            Log.w("CLIENTall:",s);
    }


        public byte[] intToBytes(int my_int)
        {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] int_bytes=new byte[4];
            DataOutputStream out = null;
            try {
                out = new DataOutputStream(bos);
                out.writeInt(my_int);
                out.close();
                int_bytes = bos.toByteArray();
                bos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        return int_bytes;
      }


    public int bytesToInt(byte[] int_bytes)  {
        ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
        DataInputStream ois = new DataInputStream(bis);
        int my_int = 0;
        try {
            my_int = ois.readInt();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return my_int;
    }

}


class OpponentReciever_SenderThread2 extends Thread{
    BluetoothSocket socket;
    BoostModeActivity activity;
    InputStream is;
    OutputStream os;
    int hisScore;
    public OpponentReciever_SenderThread2(BluetoothSocket socket,BoostModeActivity activity) {
        super();
        this.socket = socket;
        this.activity=activity;
    }

    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        }catch(IOException ioe){}


        while(true)
        {                    Log.w("in THREAD :","just");

            try {
                Log.w(" befire skeep :","just");
                Log.w("in THREAD :","wordindex:sent "+activity.score);
                try {
                    os.write(activity.score);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.sleep(1000, 0);
                Log.w("after skeep :", "just");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                hisScore=is.read();
                Log.w("in THREAD :", "Hiswordindex:recieved " + hisScore);

                if (hisScore != -1)
                {  activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.hisScoreTextView.setText(""+hisScore);
                    }
                });
                 }

            }catch(IOException ioe){}
        }
    }


    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }


}