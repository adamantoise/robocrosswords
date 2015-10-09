/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
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

package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import android.text.TextUtils;

import com.adamrosenfield.wordswithcrosses.CalendarUtil;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * Wall Street Journal
 * URL: http://blogs.wsj.com/applets/[gny|wsj]xwdYYYYMMDD.dat
 * Date: Monday-Saturday
 */
public class WSJDownloader extends AbstractDownloader {

    private static final String NAME = "Wall Street Journal";

    /**
     * Up through 2015-09-11, the WSJ was weekly on Fridays.  From 2015-09-19
     * and later, it's daily except for Sundays.
     */
    private static final Calendar DAILY_START_DATE = CalendarUtil.createDate(2015, 9, 19);

    public WSJDownloader() {
        super("http://blogs.wsj.com/applets/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        if (date.before(DAILY_START_DATE)) {
            return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
        } else {
            return (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
        }
    }

    @Override
    public void download(Calendar date) throws IOException {
        String url = baseUrl + createUrlSuffix(date);
        String puzzleData = downloadUrlToString(url);

        Puzzle puzzle = convertPuzzle(puzzleData, date);

        String destFilename = getFilename(date);
        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, destFilename);
        IO.save(puzzle, destFile);
    }

    private Puzzle convertPuzzle(String puzzleData, Calendar date) throws IOException {
        Puzzle puzzle = new Puzzle();
        puzzle.setDate(date);

        Scanner scanner = new Scanner(puzzleData);

        String dimensionsLine = scanner.nextLine();
        String[] dimensions = dimensionsLine.split("\\|");
        if (dimensions.length != 2) {
            LOG.warning("WSJ: Unable to parse dimensions: " + dimensionsLine);
            throw new IOException("Unable to parse dimensions");
        }

        // TODO: Figure out if these are correctly ordered (have not yet seen
        // a non-square grid)
        int width = Integer.parseInt(dimensions[0]);
        int height = Integer.parseInt(dimensions[1]);
        puzzle.setWidth(width);
        puzzle.setHeight(height);

        // TODO: Handle rebuses??
        String solution = scanner.nextLine();
        if (solution.length() != width * height) {
            LOG.warning("WSJ: Solution is wrong length: " + solution.length());
            throw new IOException("Unable to parse solution");
        }

        Box[][] boxes = new Box[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                char cell = solution.charAt(r * width + c);
                if (cell != '+') {
                    boxes[r][c] = new Box();
                    boxes[r][c].setSolution(cell);
                    boxes[r][c].setResponse(' ');
                }
            }
        }
        puzzle.setBoxes(boxes);

        // Clues are pipe-delimited in triplets, as
        // "<clueNumber>|<acrossClue>|<downClue>|....".  If a given clue
        // number doesn't have an across or down clue for that number, then
        // it's given as the empty string.
        String cluesLine = scanner.nextLine();
        String[] cluePieces = cluesLine.split("\\|", -1);
        ArrayList<String> rawClues = new ArrayList<>();
        for (int i = 0; i < cluePieces.length - 2; i += 3) {
            // Skip the clue number and collect the across and down clues, if
            // present
            if (!TextUtils.isEmpty(cluePieces[i + 1])) {
                rawClues.add(cluePieces[i + 1]);
            }

            if (!TextUtils.isEmpty(cluePieces[i + 2])) {
                rawClues.add(cluePieces[i + 2]);
            }
        }

        puzzle.setNumberOfClues(rawClues.size());
        puzzle.setRawClues(rawClues.toArray(new String[rawClues.size()]));

        String metaLine = scanner.nextLine();
        String[] metaParts = metaLine.split("\\|");
        puzzle.setTitle(metaParts[0]);
        puzzle.setAuthor(metaParts[1]);

        // TODO: Puzzle notes and copyright?  Those don't seem to be present.

        return puzzle;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        String prefix;
        if (date.before(DAILY_START_DATE) || date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            prefix = "wsjxwd";
        } else {
            prefix = "gnyxwd";
        }

        return (prefix +
                date.get(Calendar.YEAR) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".dat");
    }
}
