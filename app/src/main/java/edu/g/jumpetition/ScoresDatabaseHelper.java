package edu.g.jumpetition;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoresDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "HighScores.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "scores";
    public static final String COLUMN_NAME = "player";
    public static final String COLUMN_MODE = "mode";
    public static final String COLUMN_COUNT = "count";
    public static final String[] all_columns = {"_id", "player", "mode", "count"};

    public ScoresDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create database
        final String SQL_CREATE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_COUNT + " INTEGER NOT NULL, " +
                COLUMN_MODE + " INTEGER NOT NULL " +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE);

        /*
        // insert some values
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, "Me");
        cv.put(COLUMN_COUNT, 15);
        cv.put(COLUMN_MODE, "Practice");
        sqLiteDatabase.insert(TABLE_NAME, null, cv);

        cv.put(COLUMN_NAME, "Bob");
        cv.put(COLUMN_COUNT, 25);
        cv.put(COLUMN_MODE, "Competition");
        sqLiteDatabase.insert(TABLE_NAME, null, cv);
         */
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}