package edu.g.jumpetition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView myScoreTV = findViewById(R.id.my_score_text_view);
        TextView opponentScoreTV = findViewById(R.id.opponent_score_text_view);
        TextView winnerTV = findViewById(R.id.winner_text_view);

        int mode = getIntent().getIntExtra("Mode", 0);
        int myScore = getIntent().getIntExtra("myScore", 0);

        String msText = "My Score: " + myScore;
        myScoreTV.setText(msText);

        // calculate winner
        String result = "PRACTICE MODE";
        if (mode == MainActivity.COMPETITION_MODE){
            int opponentScore = getIntent().getIntExtra("opponentScore", 0);

            String osText = "Their Score: " + opponentScore;
            opponentScoreTV.setText(osText);

            // calculate the result
            if (myScore > opponentScore) { // you win
                result = "YOU WIN!";
            }
            else if (myScore < opponentScore) { // they win
                result = "YOU LOST!";
            }
            else { // its a tie
                result = "IT'S A TIE!";
            }
        }

        // create the database helper
        ScoresDatabaseHelper dbHelper = new ScoresDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // add to database
        ContentValues cv = new ContentValues();
        cv.put(ScoresDatabaseHelper.COLUMN_NAME, "Me");
        cv.put(ScoresDatabaseHelper.COLUMN_COUNT, myScore);

        // insert the opponents info as well
        if (mode == MainActivity.COMPETITION_MODE){
            cv.put(ScoresDatabaseHelper.COLUMN_MODE, "Competition");
            db.insert(ScoresDatabaseHelper.TABLE_NAME, null, cv);
            cv.put(ScoresDatabaseHelper.COLUMN_NAME, getIntent().getStringExtra("opponentName"));
            cv.put(ScoresDatabaseHelper.COLUMN_COUNT, getIntent().getIntExtra("opponentScore", 0));
            cv.put(ScoresDatabaseHelper.COLUMN_MODE, "Competition");
            db.insert(ScoresDatabaseHelper.TABLE_NAME, null, cv);
        }
        else if (mode == MainActivity.PRACTICE_MODE){
            cv.put(ScoresDatabaseHelper.COLUMN_MODE, "Practice");
            db.insert(ScoresDatabaseHelper.TABLE_NAME, null, cv);
        }

        winnerTV.setText(result);

    }

    public void mainMenuOnClick(View view) {
        setResult(RESULT_OK);
        finish();
    }
}