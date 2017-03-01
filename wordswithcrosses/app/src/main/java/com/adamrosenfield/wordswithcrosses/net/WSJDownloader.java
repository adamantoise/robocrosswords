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
 * URL: http://herbach.dnsalias.com/wsj/wsjYYMMDD.puz
 * Date: Monday-Saturday
 */
public class WSJDownloader extends AbstractDownloader {

    private static final String NAME = "Wall Street Journal";

    private static final String V1_BASE_URL = "http://blogs.wsj.com/applets/";
    private static final String V2_BASE_URL = "http://herbach.dnsalias.com/wsj/wsj";

    /**
     * Up through 2015-09-11, the WSJ was weekly on Fridays.  From 2015-09-19
     * and later, it's daily except for Sundays.
     */
    private static final Calendar DAILY_START_DATE = CalendarUtil.createDate(2015, 9, 19);

    /**
     * Prior to this date, the puzzles were at:
     *   http://blogs.wsj.com/applets/gnyxwdYYYYMMDD.dat (Monday-Friday)
     *   http://blogs.wsj.com/applets/wsjxwdYYYYMMDD.dat (Saturday)
     */
    private static final Calendar V2_START_DATE = CalendarUtil.createDate(2017, 2, 13);

    public WSJDownloader() {
        super("", NAME);
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
        if (date.before(V2_START_DATE)) {
            String url = baseUrl + createUrlSuffix(date);
            String puzzleData = downloadUrlToString(url);

            Puzzle puzzle = convertPuzzle(puzzleData, date);

            String destFilename = getFilename(date);
            File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, destFilename);
            IO.save(puzzle, destFile);
        } else {
            super.download(date);
        }
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
        if (date.before(V2_START_DATE)) {
            if (date.before(DAILY_START_DATE) || date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                prefix = "wsjxwd";
            } else {
                prefix = "gnyxwd";
            }

            return (V1_BASE_URL +
                    prefix +
                    date.get(Calendar.YEAR) +
                    DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                    DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                    ".dat");
        } else {
            return (V2_BASE_URL +
                    DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                    DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                    DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                    ".puz");
        }
    }
}
