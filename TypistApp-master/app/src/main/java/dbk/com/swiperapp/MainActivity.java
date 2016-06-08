package dbk.com.swiperapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Vibrator;



public class MainActivity extends AppCompatActivity {

    Button singlePlayerButton;
    Button multiPlayerButton;
    public static String GAME_MODE = "dbk.com.swiperapp.GAME_MODE";
    Vibrator V;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        singlePlayerButton = (Button) findViewById(R.id.singlePlayerButton);
        multiPlayerButton = (Button) findViewById(R.id.multiPlayerButton);
        V=(Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        singlePlayerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                V.vibrate(100);
                singlePlayerCall();
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////////////////
        multiPlayerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                V.vibrate(500);
                multiPlayerCall();
            }
        });
       ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
    //END_OF_ONCREATE








    protected void singlePlayerCall()
    {
        Intent i = new Intent(this, GameSelectionActivity.class);
        i.putExtra("PLAYER_MODE","SINGLEPLAYER");
        startActivity(i);
    }
    protected void multiPlayerCall()
    {   /*MULTI_PLAYER is quite different,
          it first calls NETWORKING_ACTIVITY ..to host/join,
                                             ..Blutooth enabled
          Then, after host--> GAME_SELECTION_ACTIVITY ...to select game mode (burst/para)
          Once the Game mode is set ,  -->host sends The MODE to client
          after client recieves the MSG,
                host starts the desired MODE game..
                client starts the same MODE game
                */
        Intent i = new Intent(this, NetworkingActivity.class);
        startActivity(i);
    }

}
