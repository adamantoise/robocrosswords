package com.adamrosenfield.wordswithcrosses;

import java.util.logging.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class PuzzleDatabaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "crosswords";
    public static final int DATABASE_VERSION = 0;

    public static final String TABLE_NAME = "crosswords";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_SOURCE_URL = "source_url";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PERCENT_COMPLETE = "percent_complete";
    public static final String COLUMN_CURRENT_POSITION_ROW = "current_position_row";
    public static final String COLUMN_CURRENT_POSITION_COL = "current_position_col";
    public static final String COLUMN_CURRENT_ORIENTATION_ACROSS = "current_orientation_across";

    private final Logger LOG;

    public PuzzleDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        LOG = Logger.getLogger(context.getPackageName());
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        LOG.info("Creating SQLite database");

        db.execSQL(
            "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_FILENAME + " TEXT NOT NULL, " +
            COLUMN_AUTHOR + " TEXT NOT NULL, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_SOURCE + " TEXT NOT NULL, " +
            COLUMN_SOURCE_URL + " TEXT NOT NULL, " +
            COLUMN_DATE + " INTEGER NOT NULL, " +
            COLUMN_PERCENT_COMPLETE + " REAL NOT NULL, " +
            COLUMN_CURRENT_POSITION_ROW + " INTEGER NOT NULL, " +
            COLUMN_CURRENT_POSITION_COL + " INTEGER NOT NULL, " +
            COLUMN_CURRENT_ORIENTATION_ACROSS + " INTEGER NOT NULL"
            );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        LOG.info("Upgrading SQLite database from version " + oldVersion + " to version " + newVersion);
    }
}
