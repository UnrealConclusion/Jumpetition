package edu.g.jumpetition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    final static int COMPETITION_MODE = 1;
    final static int PRACTICE_MODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void newCompetitionOnClick(View view) {
        Intent intent = new Intent(this, LobbyActivity.class);
        startActivity(intent);
    }

    /* Start Competition for Single Player*/
    public void practiceOnClick(View view) {
        Intent intent = new Intent(this, JumpActivity.class);
        intent.putExtra("Mode", PRACTICE_MODE);
        startActivity(intent);
    }

    public void highScoreOnClick(View view) {
        Intent intent = new Intent(this, HighScoreActivity.class);
        startActivity(intent);
    }
}