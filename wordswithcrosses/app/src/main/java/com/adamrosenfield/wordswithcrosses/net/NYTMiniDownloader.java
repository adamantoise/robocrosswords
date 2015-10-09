/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2015 Adam Rosenfield
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

package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.io.charset.StandardCharsets;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * New York Times
 * URL: http://www.nytimes.com/crosswords/game/mini/YYYY/MM/DD
 * Date: Daily
 */
public class NYTMiniDownloader extends NYTBaseDownloader
{
    private static final String BASE_URL = "http://www.nytimes.com/crosswords/game/mini/";

    public static final String NAME = "New York Times Mini Puzzle";

    private static final String PUZZLE_REGEX = "type=\"text/javascript\">window.preload = '([^']*)'";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    public NYTMiniDownloader(String username, String password)
    {
        super(BASE_URL, NAME, username, password);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return true;
    }

    @Override
    protected void download(Calendar date, String urlSuffix) throws IOException
    {
        login();

        String scrapeUrl = baseUrl + createUrlSuffix(date);
        String scrapedPage = downloadUrlToString(scrapeUrl);

        Matcher matcher = PUZZLE_PATTERN.matcher(scrapedPage);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle data in page: " + scrapeUrl);
            throw new IOException("Failed to scrape puzzle URL");
        }

        String base64Data = matcher.group(1);
        String jsonData = new String(Base64.decode(base64Data, Base64.DEFAULT), StandardCharsets.UTF_8);
        String destFilename = getFilename(date);
        convertPuzzle(jsonData, date, destFilename);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return (date.get(Calendar.YEAR) +
                "/" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "/" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)));
    }

    private static void convertPuzzle(String jsonData, Calendar date, String destFilename) throws IOException
    {
        Puzzle puzzle = new Puzzle();
        try
        {
            JSONArray rootArray = new JSONArray(jsonData);
            JSONObject rootObject = rootArray.getJSONObject(0);
            JSONObject puzzleMetaObject = rootObject.getJSONObject("puzzle_meta");
            int width = puzzleMetaObject.getInt("width");
            int height = puzzleMetaObject.getInt("height");

            puzzle.setWidth(width);
            puzzle.setHeight(height);
            puzzle.setAuthor(puzzleMetaObject.getString("author"));
            puzzle.setCopyright(puzzleMetaObject.getString("copyright"));
            puzzle.setDate(date);
            puzzle.setNotes(getPuzzleNotes(puzzleMetaObject.getJSONArray("notes")));

            String title = puzzleMetaObject.getString("title");
            if (TextUtils.isEmpty(title)) {
                title = puzzleMetaObject.getString("printDate");
            }
            puzzle.setTitle(title);

            JSONObject puzzleDataObject = rootObject.getJSONObject("puzzle_data");
            JSONArray layoutArray = puzzleDataObject.getJSONArray("layout");
            JSONArray answersArray = puzzleDataObject.getJSONArray("answers");

            Box[][] boxes = new Box[height][width];
            for (int r = 0; r < height; r++)
            {
                for (int c = 0; c < width; c++)
                {
                    int index = r * width + c;
                    if (layoutArray.getInt(index) != 0)
                    {
                        boxes[r][c] = new Box();
                        boxes[r][c].setSolution(answersArray.getString(index));
                        boxes[r][c].setResponse(' ');
                    }
                }
            }

            puzzle.setBoxes(boxes);

            JSONObject cluesObject = puzzleDataObject.getJSONObject("clues");
            JSONArray acrossCluesArray = cluesObject.getJSONArray("A");
            JSONArray downCluesArray = cluesObject.getJSONArray("D");

            SparseArray<String> acrossClues = new SparseArray<>();
            SparseArray<String> downClues = new SparseArray<>();
            for (int i = 0; i < acrossCluesArray.length(); i++)
            {
                JSONObject clueObject = acrossCluesArray.getJSONObject(i);
                acrossClues.put(clueObject.getInt("clueNum"), clueObject.getString("value"));
            }
            for (int i = 0; i < downCluesArray.length(); i++)
            {
                JSONObject clueObject = downCluesArray.getJSONObject(i);
                downClues.put(clueObject.getInt("clueNum"), clueObject.getString("value"));
            }

            puzzle.setNumberOfClues(acrossCluesArray.length() + downCluesArray.length());
            puzzle.setRawClues(acrossClues, downClues);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            LOG.warning("JSON exception parsing puzzle data: " + e.getMessage());
            throw new IOException("JSON exception: " + e.getMessage());
        }

        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, destFilename);
        IO.save(puzzle, destFile);
    }

    private static String getPuzzleNotes(JSONArray notesArray) throws JSONException
    {
        int length = notesArray.length();
        if (length == 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(notesArray.getString(0));
        for (int i = 1; i < length; i++)
        {
            sb.append("\n")
              .append(notesArray.getString(i));
        }

        return sb.toString();
    }
}
