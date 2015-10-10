/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.adamrosenfield.wordswithcrosses.BrowseActivity.SortOrder;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;
import com.adamrosenfield.wordswithcrosses.puz.PuzzleMeta;

public class PuzzleDatabaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "crosswords";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CROSSWORDS = "crosswords";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_SOURCE_URL = "source_url";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PERCENT_COMPLETE = "percent_complete";
    public static final String COLUMN_CURRENT_POSITION_ROW = "current_position_row";
    public static final String COLUMN_CURRENT_POSITION_COL = "current_position_col";
    public static final String COLUMN_CURRENT_ORIENTATION_ACROSS = "current_orientation_across";

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    public PuzzleDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        LOG.info("Creating SQLite database");

        db.execSQL(
            "CREATE TABLE " + TABLE_CROSSWORDS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_FILENAME + " TEXT UNIQUE NOT NULL, " +
            COLUMN_ARCHIVED + " INTEGER NOT NULL, " +
            COLUMN_AUTHOR + " TEXT NOT NULL, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_SOURCE + " TEXT NOT NULL, " +
            COLUMN_SOURCE_URL + " TEXT NOT NULL, " +
            COLUMN_DATE + " INTEGER NOT NULL, " +
            COLUMN_PERCENT_COMPLETE + " REAL NOT NULL, " +
            COLUMN_CURRENT_POSITION_ROW + " INTEGER NOT NULL, " +
            COLUMN_CURRENT_POSITION_COL + " INTEGER NOT NULL, " +
            COLUMN_CURRENT_ORIENTATION_ACROSS + " INTEGER NOT NULL)"
            );

        //db.execSQL(
        //    "CREATE UNIQUE INDEX filename_index ON " + TABLE_NAME +
        //    " (" + COLUMN_FILENAME + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        LOG.info("Upgrading SQLite database from version " + oldVersion + " to version " + newVersion);
    }

    /**
     * Workaround for http://code.google.com/p/android/issues/detail?id=16391 -
     * copies the database file to external storage so that it can be downloaded
     * and debugged.
     */
    public void debugCopyDatabaseFileToExternalStorage()
    {
        try
        {
            String dbPath = getReadableDatabase().getPath();
            FileInputStream fis = new FileInputStream(dbPath);
            try
            {
                File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, "crosswords.db");
                LOG.info("Copying " + dbPath + " ==> " + tempFile);
                FileOutputStream fos = new FileOutputStream(tempFile);
                try
                {
                    IO.copyStream(fis, fos);
                }
                finally
                {
                    fos.close();
                }
            }
            finally
            {
                fis.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static class IDAndFilename
    {
        public IDAndFilename(long id, String filename)
        {
            this.id = id;
            this.filename = filename;
        }

        public long id;
        public String filename;
    }

    /**
     * Gets the list of IDs and filenames of all puzzles in the database
     *
     * @return The list of IDs and filenames of all puzzles in the database
     */
    public List<IDAndFilename> getFilenameList()
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
            TABLE_CROSSWORDS,
            new String[]{COLUMN_ID, COLUMN_FILENAME},  // Columns
            null,             // Selection
            null,             // Selection args
            null,             // Group by
            null,             // Having
            COLUMN_FILENAME); // Order by

        ArrayList<IDAndFilename> filenameList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext())
        {
            filenameList.add(new IDAndFilename(cursor.getLong(0), cursor.getString(1)));
        }
        cursor.close();

        return filenameList;
    }

    /**
     * Adds the puzzle at the given path to the database
     *
     * @param path Path of the .puz file to add to the database
     * @param source Name of the puzzle source (e.g. "New York Times")
     * @param sourceUrl Source URL where the puzzle was downloaded from
     * @param dateMillis Source date of the puzzle, in milliseconds since the
     *        epoch
     *
     * @return The puzzle ID of the new puzzle
     */
    public long addPuzzle(File path, String source, String sourceUrl, long dateMillis)
    {
        LOG.info("Adding puzzle to database: " + path);

        Puzzle puz;
        try
        {
            puz = IO.load(path);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            LOG.warning("Failed to load " + path + ", moving to quarantine");
            path.renameTo(new File(WordsWithCrossesApplication.QUARANTINE_DIR, path.getName()));
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_FILENAME, path.getAbsolutePath());
        values.put(COLUMN_ARCHIVED, false);
        values.put(COLUMN_AUTHOR, puz.getAuthor());
        values.put(COLUMN_TITLE, puz.getTitle());
        values.put(COLUMN_SOURCE, source);
        values.put(COLUMN_SOURCE_URL, sourceUrl);
        values.put(COLUMN_DATE, dateMillis);
        values.put(COLUMN_PERCENT_COMPLETE, -1);
        values.put(COLUMN_CURRENT_POSITION_ROW, 0);
        values.put(COLUMN_CURRENT_POSITION_COL, 0);
        values.put(COLUMN_CURRENT_ORIENTATION_ACROSS, true);

        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_CROSSWORDS, null, values);

        if (id == -1)
        {
            LOG.warning("Failed to insert puzzle into database: " + path);
        }

        return id;
    }

    /**
     * Removes the given puzzles from the database
     *
     * @param ids List of puzzle IDs to remove from the database
     */
    public void removePuzzles(List<Long> ids)
    {
        if (ids.isEmpty())
        {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COLUMN_ID + " in (" + TextUtils.join(",", ids) + ")";
        int rowsDeleted = db.delete(TABLE_CROSSWORDS, whereClause, null);

        LOG.info("Deleted " + rowsDeleted + " puzzles from the database (attempted to delete " + ids.size() + " rows)");
    }

    /**
     * Tests if the database contains any puzzles in it
     *
     * @return True if the database has any puzzles in it
     */
    public boolean hasAnyPuzzles()
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT 1 FROM " + TABLE_CROSSWORDS + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        boolean result = (cursor.getCount() > 0);
        cursor.close();

        return result;
    }

    /**
     * Tests if the given filename exists in the database
     *
     * @param filename Filename to test
     *
     * @return True if the given file exists in the database
     */
    public boolean filenameExists(String filename)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT 1 FROM " + TABLE_CROSSWORDS +
            " WHERE " + COLUMN_FILENAME + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(query,  new String[]{filename});
        boolean result = (cursor.getCount() > 0);
        cursor.close();

        return result;
    }

    /**
     * Gets the ID of the puzzle with the given source URL, or -1 if no such
     * puzzle exists.
     *
     * @param url Source URL of the puzzle to query
     *
     * @return The ID of the puzzle with the given source URL, or -1 if no such
     *         puzzle exists
     */
    public long getPuzzleIDForURL(String url)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_CROSSWORDS +
            " WHERE " + COLUMN_SOURCE_URL + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(query,  new String[]{url});
        long id = -1;
        if (cursor.moveToNext())
        {
            id = cursor.getLong(0);
        }
        cursor.close();

        return id;
    }

    /**
     * Tests if a puzzle with the given URL exists in the database
     *
     * @param url Puzzle URL to test
     *
     * @return True if a puzzle with the given URL exists in the database
     */
    public boolean puzzleURLExists(String url)
    {
        return (getPuzzleIDForURL(url) != -1);
    }

    /**
     * Gets the filename for the puzzle with the given ID
     *
     * @param id ID of the puzzle to query
     *
     * @return The filename of the puzzle with the given ID, or null if no such
     *         puzzle exists
     */
    public String getFilename(long id)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COLUMN_FILENAME + " FROM " + TABLE_CROSSWORDS +
            " WHERE " + COLUMN_ID + "=" + id + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        String filename = null;
        if (cursor.moveToNext())
        {
            filename = cursor.getString(0);
        }
        cursor.close();

        return filename;
    }

    /**
     * Gets the puzzle source name for the puzzle with the given ID
     *
     * @param id Puzzle ID to query
     *
     * @return The puzzle source of the given puzzle, or null if no such puzzle
     *         exists
     */
    public String getPuzzleSource(long id)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COLUMN_SOURCE + " FROM " + TABLE_CROSSWORDS +
            " WHERE " + COLUMN_ID + "=" + id + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        String source = null;
        if (cursor.moveToNext())
        {
            source = cursor.getString(0);
        }
        cursor.close();

        return source;
    }

    /**
     * Gets the filename for the puzzle with the given URL
     *
     * @param url Source URL of the puzzle to query
     *
     * @return The filename of the puzzle with the given source URL, or null
     *         if no such puzzle exists
     */
    public String getFilenameForURL(String url)
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + COLUMN_FILENAME + " FROM " + TABLE_CROSSWORDS +
            " WHERE " + COLUMN_SOURCE_URL + "=? LIMIT 1";
        Cursor cursor = db.rawQuery(query,  new String[]{url});
        String filename = null;
        if (cursor.moveToNext())
        {
            filename = cursor.getString(0);
        }
        cursor.close();

        return filename;
    }

    /**
     * Queries the list of all puzzle sources in the database
     *
     * @return The list of all puzzle sources in the database
     */
    public ArrayList<String> querySources()
    {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<String> sourceList = new ArrayList<>();
        Cursor cursor = db.query(
            TABLE_CROSSWORDS,
            new String[]{COLUMN_SOURCE}, // Columns
            null,           // Selection
            null,           // Selection args
            COLUMN_SOURCE,  // Group by
            null,           // Having
            COLUMN_SOURCE); // Order by

        while (cursor.moveToNext())
        {
            sourceList.add(cursor.getString(0));
        }
        cursor.close();

        return sourceList;
    }

    /**
     * Queries the puzzle database for a set of puzzles
     *
     * @param sourceFilter If non-null, specifies the puzzle source to filter by
     * @param archived Specifies whether to return archived or non-archived puzzles
     * @param sortOrder Order to sort the results by
     *
     * @return Array of puzzle meta-information matching the given query
     */
    public ArrayList<PuzzleMeta> queryPuzzles(String sourceFilter, boolean archived, SortOrder sortOrder)
    {
        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_ARCHIVED + "=" + (archived ? "1" : "0");
        String[] args = null;
        if (sourceFilter != null)
        {
            selection += " AND " + COLUMN_SOURCE + "=?";
            args = new String[]{sourceFilter};
        }

        String orderBy = sortOrder.getOrderByClause();

        ArrayList<PuzzleMeta> puzzles = new ArrayList<>();
        String[] columns = new String[] {
            COLUMN_ID, COLUMN_FILENAME, COLUMN_ARCHIVED, COLUMN_AUTHOR,
            COLUMN_TITLE, COLUMN_SOURCE, COLUMN_DATE,
            COLUMN_PERCENT_COMPLETE, COLUMN_SOURCE_URL
        };
        Cursor cursor = db.query(
            TABLE_CROSSWORDS,
            columns,   // Columns
            selection, // Selection
            args,      // Selection args
            null,      // Group by
            null,      // Having
            orderBy);  // Order by

        while (cursor.moveToNext())
        {
            PuzzleMeta puzzle = new PuzzleMeta();
            puzzle.id = cursor.getLong(0);
            puzzle.filename = cursor.getString(1);
            puzzle.archived = (cursor.getInt(2) != 0);
            puzzle.author = cursor.getString(3);
            puzzle.title = cursor.getString(4);
            puzzle.source = cursor.getString(5);
            puzzle.date = Calendar.getInstance();
            puzzle.date.setTimeInMillis(cursor.getLong(6));
            puzzle.percentComplete = cursor.getInt(7);
            puzzle.sourceUrl = cursor.getString(8);
            puzzles.add(puzzle);
        }

        cursor.close();

        sortOrder.sort(puzzles);
        return puzzles;
    }

    /**
     * Gets the list of IDs and filenames of all non-archived puzzles which are
     * older than the given date or 100% solved.
     *
     * @param olderThanDateMillis Date, in milliseconds since the epoch, before
     *          which puzzles will be returned
     *
     * @return List of IDs and filenames
     */
    public List<IDAndFilename> getFilenamesToCleanup(long olderThanDateMillis)
    {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
            TABLE_CROSSWORDS,
            new String[]{COLUMN_ID, COLUMN_FILENAME},
            getCleanupSelectionQuery(olderThanDateMillis),
            null,  // Selection args
            null,  // Group by
            null,  // Having
            null); // Order by
        ArrayList<IDAndFilename> filenameList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext())
        {
            filenameList.add(new IDAndFilename(cursor.getLong(0), cursor.getString(1)));
        }
        cursor.close();

        return filenameList;
    }

    /**
     * Archives all puzzles which are older than the given date or 100% solved.
     *
     * @param olderThanDateMillis Date, in milliseconds since the epoch, before
     *          which puzzles will be archived
     *
     * @return The number of puzzles archived
     */
    public int archivePuzzles(long olderThanDateMillis)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVED, true);

        SQLiteDatabase db = getWritableDatabase();
        return db.update(
            TABLE_CROSSWORDS,
            values,
            getCleanupSelectionQuery(olderThanDateMillis),  // Selection
            null);  // Selection args
    }

    /**
     * Archives or un-archives the given puzzle
     *
     * @param puzzleId Puzzle ID of the puzzle to archive or un-archive
     * @param archive True to archive or false to un-archive
     *
     * @return True if the puzzle was found, or false if no such puzzle was
     *           found
     */
    public boolean archivePuzzle(long puzzleId, boolean archive)
    {
        String selection = COLUMN_ID + "=" + puzzleId;

        ContentValues values = new ContentValues();
        values.put(COLUMN_ARCHIVED, archive);

        SQLiteDatabase db = getWritableDatabase();
        return db.update(
            TABLE_CROSSWORDS,
            values,
            selection,
            null) > 0;
    }

    /**
     * Object representing current solve state of a puzzle which can't be
     * stored directly in the puzzle file format
     */
    public static class SolveState
    {
        public Position position;
        public boolean isOrientationAcross;
    }

    /**
     * Gets any relevant current solve state of a puzzle that's not stored
     * directly in the puzzle file
     *
     * @param puzzleId Puzzle ID to get the solve state of
     *
     * @return True if the given puzzle ID was found, or false otherwise
     */
    public SolveState getPuzzleSolveState(long puzzleId)
    {
        String[] columns = new String[]
        {
            COLUMN_CURRENT_POSITION_ROW,
            COLUMN_CURRENT_POSITION_COL,
            COLUMN_CURRENT_ORIENTATION_ACROSS,
        };

        String selection = COLUMN_ID + "=" + puzzleId;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_CROSSWORDS,
            columns,   // Columns
            selection, // Selection
            null,      // Selection args
            null,      // Group by
            null,      // Having
            null);     // Order by

        if (cursor.moveToNext())
        {
            SolveState solveState = new SolveState();
            int row = cursor.getInt(0);
            int col = cursor.getInt(1);
            solveState.position = new Position(col, row);
            solveState.isOrientationAcross = (cursor.getInt(2) != 0);
            cursor.close();

            return solveState;
        }
        else
        {
            LOG.warning("getPuzzleSolveState: No such puzzle: " + puzzleId);
            return null;
        }
    }

    /**
     * Updates any relevant current solve state about the given puzzle which
     * can't be stored directly in the puzzle file.
     *
     * @param puzzleId Puzzle ID to update
     * @param solveState New solve state to store
     */
    public void updatePuzzleSolveState(long puzzleId, SolveState solveState)
    {
        String selection = COLUMN_ID + "=" + puzzleId;

        ContentValues values = new ContentValues();
        values.put(COLUMN_CURRENT_POSITION_ROW, solveState.position.down);
        values.put(COLUMN_CURRENT_POSITION_COL, solveState.position.across);
        values.put(COLUMN_CURRENT_ORIENTATION_ACROSS, solveState.isOrientationAcross);

        SQLiteDatabase db = getWritableDatabase();
        if (db.update(TABLE_CROSSWORDS, values, selection, null) == 1)
        {
            LOG.info("Updated puzzle solve state");
        }
        else
        {
            LOG.warning("updatePuzzleSolveState: No such puzzle: " + puzzleId);
        }
    }

    /**
     * Updates the percentage complete of the given puzzle
     *
     * @param puzzleId Puzzle ID to update
     * @param percentComplete New percentage complete
     */
    public void updatePercentComplete(long puzzleId, int percentComplete)
    {
        String selection = COLUMN_ID + "=" + puzzleId;

        ContentValues values = new ContentValues();
        values.put(COLUMN_PERCENT_COMPLETE, percentComplete);

        SQLiteDatabase db = getWritableDatabase();
        if (db.update(TABLE_CROSSWORDS, values, selection, null) < 1)
        {
            LOG.warning("updatePercentCompletee: No such puzzle: " + puzzleId);
        }
    }

    /**
     * Gets the selection query used to query for puzzles to archive
     *
     * @param olderThanDateMillis Date, in milliseconds since the epoch, before
     *          which puzzles will be returned
     *
     * @return Query string to be used for selecting puzzles from the database
     */
    private static String getCleanupSelectionQuery(long olderThanDateMillis)
    {
        return COLUMN_ARCHIVED + "=0 AND (" + COLUMN_PERCENT_COMPLETE + "=100 OR " + COLUMN_DATE + "<=" + olderThanDateMillis + ")";
    }
}
