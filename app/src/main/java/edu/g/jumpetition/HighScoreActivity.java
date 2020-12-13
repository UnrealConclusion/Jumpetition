package edu.g.jumpetition;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class HighScoreActivity<DatabaseOpenHelper> extends AppCompatActivity {

    private ListView scoresLV;
    SimpleCursorAdapter todoAdapter;
    ScoresDatabaseHelper dbHelper;
    static SQLiteDatabase todoDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        scoresLV = findViewById(R.id.high_scores_list_view);

        // start async task
        dbHelper = new ScoresDatabaseHelper(this);
        LoadDB task = new LoadDB();
        task.execute();
    }

    public void MenuOnClick(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // database
        dbHelper = new ScoresDatabaseHelper(this);
        todoDB = dbHelper.getWritableDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();
        todoDB.close();
    }

    private final class LoadDB extends AsyncTask<String, Void, Cursor> {
        @Override
        protected void onPostExecute(Cursor data){
            todoAdapter = new SimpleCursorAdapter(getApplicationContext(),
                    R.layout.high_score_list,
                    data,
                    new String[] {"player", "count", "mode"},
                    new int[] { R.id.player_text_view, R.id.count_text_view, R.id.mode_text_view  }, 0);
            Cursor mCursor = data;
            scoresLV.setAdapter(todoAdapter);
        }

        @Override
        protected Cursor doInBackground(String... args) {
            todoDB = dbHelper.getWritableDatabase();
            return todoDB.query(ScoresDatabaseHelper.TABLE_NAME, ScoresDatabaseHelper.all_columns,
                    null,
                    null,
                    null,
                    null,
                    "count DESC");

        }
    }
}