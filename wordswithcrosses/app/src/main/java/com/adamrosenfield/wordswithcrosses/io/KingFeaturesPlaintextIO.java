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

package com.adamrosenfield.wordswithcrosses.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import android.util.SparseArray;

import com.adamrosenfield.wordswithcrosses.io.charset.MacRoman;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * Converts a puzzle from the plaintext format used by King Features Syndicate
 * puzzles to the Across Lite .puz format.  The format is:
 *
 * -Grid shape and clue numbers (redundant)
 * -Solution grid
 * -Across Clues
 * -Down Clues
 *
 * Each section begins with a { character, and each line except the last in a section
 * ends with a | character.  The charset used is Mac Roman.
 *
 * For an example puzzle in this format, see:
 * http://joseph.king-online.com/clues/20130528.txt
 */
public class KingFeaturesPlaintextIO {

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    /**
     * Take an InputStream containing a plaintext puzzle to a OutputStream containing
     * the generated .puz file.  Returns true if the process succeeded, or false if it fails
     * (for example, if the plaintext file is not in a valid format).
     */
    public static boolean convertKFPuzzle(InputStream is, OutputStream os,
            String title, String author, String copyright, Calendar date) {
        Puzzle puz = new Puzzle();

        Scanner scanner = new Scanner(new InputStreamReader(is, new MacRoman()));

        if (!scanner.hasNextLine()) {
            LOG.warning("KFIO: File empty.");
            return false;
        }

        String line = scanner.nextLine();
        if (!line.startsWith("{") || !scanner.hasNextLine()) {
            LOG.warning("KFIO: First line format incorrect.");
            return false;
        }

        // Skip over redundant grid information.
        line = scanner.nextLine();
        while (!line.startsWith("{")) {
            if (!scanner.hasNextLine()) {
                LOG.warning("KFIO: Unexpected EOF - Grid information.");
                return false;
            }
            line = scanner.nextLine();
        }

        if (line.length() < 3) {
            LOG.warning("KFIO: Line too short");
            return false;
        }

        // Process solution grid.
        List<char[]> solGrid = new ArrayList<>();
        line = line.substring(1, line.length()-2);
        String[] rowString = line.split(" ");
        int width = rowString.length;
        do {
            if (line.endsWith(" |")) {
                line = line.substring(0, line.length()-2);
            }
            rowString = line.split(" ");
            if (rowString.length != width) {
                LOG.warning("KFIO: Not a square grid.");
                return false;
            }

            char[] row = new char[width];
            for (int x = 0; x < width; x++) {
                if (rowString[x].length() == 0) {
                    LOG.warning("KFIO: Empty solution cell");
                    return false;
                }

                row[x] = rowString[x].charAt(0);
            }
            solGrid.add(row);

            if (!scanner.hasNextLine()) {
                LOG.warning("KFIO: Unexpected EOF - Solution grid.");
                return false;
            }
            line = scanner.nextLine();
        } while (!line.startsWith("{"));

        // Convert solution grid into Box grid.
        int height = solGrid.size();
        puz.setWidth(width);
        puz.setHeight(height);
        Box[][] boxes = new Box[height][width];
        for (int r = 0; r < height; r++) {
            char[] row = solGrid.get(r);
            for (int c = 0; c < width; c++) {
                if (row[c] != '#') {
                    boxes[r][c] = new Box();
                    boxes[r][c].setSolution(row[c]);
                    boxes[r][c].setResponse(' ');
                }
            }
        }

        puz.setBoxes(boxes);

        // Process clues.
        SparseArray<String> acrossNumToClueMap = new SparseArray<>();
        line = line.substring(1);
        int clueNum;
        int lastClueNum = 0;
        do {
            if (line.endsWith(" |")) {
                line = line.substring(0, line.length()-2);
            }
            clueNum = 0;
            int i = 0;
            while (i < line.length() && line.charAt(i) != '.') {
                if (clueNum != 0) {
                    clueNum *= 10;
                }
                clueNum += line.charAt(i) - '0';
                i++;
            }

            if (i < line.length()) {
                lastClueNum = clueNum;
                String clue = line.substring(i+2).trim();
                acrossNumToClueMap.put(clueNum, clue);
            } else {
                // Shouldn't happen.  If it does, we're probably in trouble.
                LOG.warning("KFIO: Failed to parse across clue number in line: \"" + line + "\"");
            }

            if (!scanner.hasNextLine()) {
                LOG.warning("KFIO: Unexpected EOF - Across clues.");
                return false;
            }
            line = scanner.nextLine();
        } while (!line.startsWith("{"));

        int maxClueNum = lastClueNum;

        SparseArray<String> downNumToClueMap = new SparseArray<>();
        line = line.substring(1);
        boolean finished = false;
        do {
            if (line.endsWith(" |")) {
                line = line.substring(0, line.length()-2);
            } else {
                finished = true;
            }
            clueNum = 0;
            int i = 0;
            while (i < line.length() && line.charAt(i) != '.') {
                if (clueNum != 0) {
                    clueNum *= 10;
                }
                clueNum += line.charAt(i) - '0';
                i++;
            }

            if (i < line.length() - 1) {
                lastClueNum = clueNum;
                String clue = line.substring(i+2).trim();
                downNumToClueMap.put(clueNum, clue);
            } else {
                // Ignore malformed lines -- this has happened at least once,
                // on the 8/16/13 Sheffer puzzle
                LOG.warning("KFIO: Failed to parse down clue number in line: \"" + line + "\"");
            }

            if (!finished) {
                if (!scanner.hasNextLine()) {
                    LOG.warning("KFIO: Unexpected EOF - Down clues.");
                    return false;
                }
                line = scanner.nextLine();
            }
        } while (!finished);

        maxClueNum = Math.max(maxClueNum, lastClueNum);

        // Convert clues into raw clues format.
        int numberOfClues = acrossNumToClueMap.size() + downNumToClueMap.size();
        puz.setNumberOfClues(numberOfClues);
        String[] rawClues = new String[numberOfClues];
        int i = 0;
        for (clueNum = 1; clueNum <= maxClueNum; clueNum++) {
            String clue = acrossNumToClueMap.get(clueNum);
            if (clue != null) {
                rawClues[i] = clue;
                i++;
            }

            clue = downNumToClueMap.get(clueNum);
            if (clue != null) {
                rawClues[i] = clue;
                i++;
            }
        }
        puz.setRawClues(rawClues);

        // Set puzzle information
        puz.setTitle(title);
        puz.setAuthor(author);
        puz.setDate(date);
        puz.setCopyright(copyright);

        try {
            IO.save(puz, os);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
