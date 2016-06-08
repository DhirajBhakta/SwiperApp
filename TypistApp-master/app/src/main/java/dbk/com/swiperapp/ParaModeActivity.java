package dbk.com.swiperapp;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;



//wordIndex contains the
public class ParaModeActivity extends AppCompatActivity {
    String playerMode="";
    String hostOrClient="";
    ParaGetter paraGetter;
    ArrayList<String>  wordsList;
    TextView paraTextView;
    TextView resultTextView;
    TextView opponentResultTextView;
    TextView timerView;
    Boolean shouldInputBeCleaned;
    Boolean alertSet;
    EditText editText;
    ProgressBar typingProgressBar;
    ProgressBar typingProgressBar2;
    int noWords;
    int recievedParaIndex;
    int wordsIndex = 0;
    CountDownTimer cdTimer;
    OpponentReciever_SenderThread t;

    BluetoothSocket socket;

    protected int refreshPara(String PLAYER_MODE,int PARA_INDEX)
    {
        paraGetter=new ParaGetter(this);
        String para = paraGetter.getRandomPara();                  //get paragraph
        if(PLAYER_MODE.compareTo("MULTIPLAYER")==0)
        {  if(hostOrClient.compareTo("CLIENT")==0)
                para=paraGetter.getPara(PARA_INDEX);
        }
        wordsList = paraGetter.getWords(para);                     //get ARRAYLIST of Words
        paraTextView.setText(para, TextView.BufferType.SPANNABLE);
        typingProgressBar.setProgress(0);
        typingProgressBar2.setProgress(0);
        shouldInputBeCleaned = false;
        alertSet = false;
        wordsIndex = 0;
        noWords = paraGetter.getNoWords();
        cdTimer= new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timerView.setText("00:" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerView.setText("");
                editText.setEnabled(false);
                paraTextView.setText("GAME OVER!");
                paraTextView.setTextSize(50);
                resultTextView.setText("Your Score:"+wordsIndex+"wpm");
                if(playerMode.compareTo("MULTIPLAYER")==0) {
                    try {
                        int opScore = socket.getInputStream().read();
                        opponentResultTextView.setText("Opponent's Score:" + opScore+"wpm");
                        t.cancel();

                    }catch(IOException ioe){}
                }
            }
        }.start();
     return  paraGetter.lineNumber;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paramode);


        Intent recvIntent = getIntent();
        playerMode=recvIntent.getStringExtra("PLAYER_MODE");
        hostOrClient=recvIntent.getStringExtra("HOST_OR_CLIENT");



        paraTextView = (TextView)findViewById(R.id.paraTextView);
        resultTextView=(TextView)findViewById(R.id.resultTextView);
        opponentResultTextView=(TextView)findViewById(R.id.opponentResultTextView);
        editText = (EditText) findViewById(R.id.editText);
        timerView = (TextView) findViewById(R.id.timerView);
        typingProgressBar = (ProgressBar) findViewById(R.id.typingProgressBar);
        typingProgressBar2=(ProgressBar)findViewById(R.id.typingProgressBar2);


        if(playerMode.compareTo("MULTIPLAYER")==0)
        {
            socket = LonelySocket.getSocket();
            if(hostOrClient.compareTo("CLIENT")==0) {
                try {
                    recievedParaIndex = socket.getInputStream().read();
                    refreshPara(playerMode,recievedParaIndex);
                }catch (IOException ioe){}
            }
            else
            { try {
                int paraindex = refreshPara(playerMode, recievedParaIndex);
                socket.getOutputStream().write(paraindex);
            }catch(IOException ioe){}

            }
            t = new OpponentReciever_SenderThread(socket,this);
            t.start();
        }
        else  //SINGLE_PLAYER
        {   typingProgressBar2.setVisibility(View.INVISIBLE);
            refreshPara(playerMode,-1);
        }

        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().replace(" ","").equals(wordsList.get(wordsIndex)))
                {
                    shouldInputBeCleaned = true;
                    wordsIndex++;
                    typingProgressBar.setProgress((int)(100 * (float)wordsIndex/noWords));

                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(shouldInputBeCleaned == true)
                {
                    shouldInputBeCleaned = false;
                    editText.setText("");
                    updateColor(paraTextView, paraGetter.getNextIndex(wordsIndex-1));
                }
                else if (!wordsList.get(wordsIndex).contains(s.toString().replace(" ","")))
                {
                    wrongAlert(paraTextView, paraGetter.getCurrentIndex() , paraGetter.lookaheadIndex(wordsIndex ));

                }



            }
        });
        //updateColor(paraTextView, paraGetter.getNextIndex(1));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public static void updateColor(TextView t,int lastindex)
    {
        Spannable wordtoSpan = new SpannableString(t.getText());
        wordtoSpan.setSpan(new ForegroundColorSpan(Color.rgb(34,139,34)), 0, lastindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        t.setText(wordtoSpan);
    }
    public static void wrongAlert(TextView t, int startindex, int endindex)
    {
        Spannable wordtoSpan = new SpannableString(t.getText());
        wordtoSpan.setSpan(new ForegroundColorSpan(Color.rgb(139,34,34)), startindex, endindex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        t.setText(wordtoSpan);
    }

}
/*
class InputChecker implements Runnable
{
    Thread inputCheckerThread;
    EditText inputText;
    TextView paraTextView;
    ArrayList<String> wordsList;
    ParaGetter paraGetter;
    int wordsIndex ;
    InputChecker(EditText inputText,TextView paraTextView, ArrayList<String> wordsList, ParaGetter paraGetter )
    {
        this.inputText = inputText;
        this.paraTextView = paraTextView;
        this.wordsList = wordsList;
        inputCheckerThread = new Thread(this, "Input Checker Thread ");
        this.wordsIndex = 0;
        this.paraGetter = paraGetter;

    }

    public void start()
    {
        inputCheckerThread.start();
    }
    @Override
    public void run() {
        while(true)
        {
            if (inputText.getText().equals(wordsList.get(wordsIndex))) {
                ParaModeActivity.updateColor(paraTextView, paraGetter.getNextIndex(wordsIndex));
                inputText.setText(" ");
                wordsIndex++;
            }
        }
    }
}*/


//000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
class OpponentReciever_SenderThread extends Thread{
    BluetoothSocket socket;
ParaModeActivity activity;
    InputStream is;
    OutputStream os;
    int hisWordIndex;
    public OpponentReciever_SenderThread(BluetoothSocket socket,ParaModeActivity activity) {
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
                    Log.w("in THREAD :","wordindex:sent "+activity.wordsIndex);
                    try {
                        os.write(activity.wordsIndex);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(1000, 0);
                    Log.w("after skeep :", "just");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    hisWordIndex=is.read();
                    Log.w("in THREAD :", "Hiswordindex:recieved " + hisWordIndex);

                    if (hisWordIndex != -1)
                {
                    activity.typingProgressBar2.setProgress((int)(100*(float)hisWordIndex/activity.noWords));
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