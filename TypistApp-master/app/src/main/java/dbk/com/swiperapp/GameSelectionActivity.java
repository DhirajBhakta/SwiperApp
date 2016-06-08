package dbk.com.swiperapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;

public class GameSelectionActivity extends AppCompatActivity {

    TextView gameModeTitle;
    Button paraModeButton;
    Button boostModeButton;
    String playerMode;
    Intent recvIntent;
    Vibrator V ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);
        gameModeTitle = (TextView) findViewById(R.id.modeTitle);
        paraModeButton = (Button) findViewById(R.id.paraModeButton);
        boostModeButton = (Button)findViewById(R.id.boostModeButton);
        V = (Vibrator)getSystemService(this.VIBRATOR_SERVICE);

        //The Below Intent will tell the GameSelection ACtivity whether its singlePlayer or MultiPlayer...
        recvIntent = getIntent();
        playerMode = recvIntent.getStringExtra("PLAYER_MODE");


        paraModeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                V.vibrate(100);
                paraModeCall();
            }
        });
        boostModeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                V.vibrate(500);
                boostModeCall();
            }
        });

    }

    protected void paraModeCall()
    {
        if(playerMode.compareTo("SINGLEPLAYER")==0) {
            Intent i = new Intent(this, ParaModeActivity.class);
            i.putExtra("PLAYER_MODE","SINGLEPLAYER");
            startActivity(i);
        }
        else if(playerMode.compareTo("MULTIPLAYER")==0)
        {
           recvIntent.putExtra("GAME_MODE","PARAMODE");
           setResult(Activity.RESULT_OK,recvIntent);
           finish();

        }
    }


    protected void boostModeCall()
    {            Log.w("GAME_SELECTION", "boostMode..playerMode:"+playerMode);

        if(playerMode.compareTo("SINGLEPLAYER")==0) {
            Intent i = new Intent(this, BoostModeActivity.class);
            i.putExtra("PLAYER_MODE","SINGLEPLAYER");
            startActivity(i);
        }
        else if(playerMode.compareTo("MULTIPLAYER")==0)
        {Log.w("GAME_SEL","boostmode fin");
            recvIntent.putExtra("GAME_MODE","BOOSTMODE");
            setResult(Activity.RESULT_OK,recvIntent);
            finish();
            Log.w("GAME_SEL","boostmode fin");

        }
    }
}
