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

package com.adamrosenfield.wordswithcrosses.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.charset.StandardCharsets;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class NewsdayPlaintextIO
{
    public static void convertNewsdayPuzzle(File inTxtFile, File outPuzFile, Calendar date)
        throws IOException
    {
        FileInputStream fis = new FileInputStream(inTxtFile);
        try
        {
            File tempFile = File.createTempFile("newsday", ".puz.tmp", WordsWithCrossesApplication.TEMP_DIR);
            FileOutputStream fos = new FileOutputStream(tempFile);
            try
            {
                convertNewsdayPuzzle(fis, fos, date);
                if (!tempFile.renameTo(outPuzFile))
                {
                    throw new IOException("Failed to rename " + tempFile + " ==> " + outPuzFile);
                }
            }
            finally
            {
                fos.close();
                tempFile.delete();
            }
        }
        finally
        {
            fis.close();
        }
    }

    public static void convertNewsdayPuzzle(InputStream is, OutputStream os, Calendar date)
        throws IOException
    {
        Puzzle puzzle = new Puzzle();

        try
        {
            Scanner scanner = new Scanner(is, StandardCharsets.ISO_8859_1.name());
            scanner.nextLine();  // "ARCHIVE"
            scanner.nextLine();  // ""
            scanner.nextLine();  // YYMMDD
            scanner.nextLine();  // ""
            puzzle.setTitle(scanner.nextLine());
            scanner.nextLine();  // ""
            puzzle.setAuthor(scanner.nextLine());
            puzzle.setCopyright("\u00a9 " + date.get(Calendar.YEAR) + " Stanley Newman, distributed by Creators Syndicate, Inc.");

            // TODO: Find a non-square puzzle to determine if these are in the
            // correct order
            int width = scanner.nextInt();
            int height = scanner.nextInt();
            if (width <= 0 || height <= 0) {
                throw new IOException("Invalid dimensions: width=" + width + " height=" + height);
            }

            puzzle.setWidth(width);
            puzzle.setHeight(height);

            int numAcrossClues = scanner.nextInt();
            int numDownClues = scanner.nextInt();

            // Read in the solution grid
            Box[][] boxes = new Box[height][width];
            scanner.nextLine();  // ""
            scanner.nextLine();  // ""
            for (int r = 0; r < height; r++)
            {
                String row = scanner.nextLine();
                if (row.length() < width)
                {
                    throw new IOException("Row " + r + " is too short");
                }

                for (int c = 0; c < width; c++)
                {
                    char ch = row.charAt(c);
                    if (ch != '#')
                    {
                        boxes[r][c] = new Box();
                        boxes[r][c].setSolution(ch);
                        boxes[r][c].setResponse(' ');
                    }
                }
            }

            puzzle.setBoxes(boxes);

            scanner.nextLine();

            // Read in the clues
            String[] acrossClues = new String[numAcrossClues];
            String[] downClues = new String[numDownClues];

            for (int i = 0; i < numAcrossClues; i++)
            {
                acrossClues[i] = scanner.nextLine();
            }

            scanner.nextLine();  // ""

            for (int i = 0; i < numDownClues; i++)
            {
                downClues[i] = scanner.nextLine();
            }

            String[] rawClues = new String[numAcrossClues + numDownClues];
            int curClue = 0;
            int curAcrossClue = 0;
            int curDownClue = 0;
            for (int r = 0; r < height; r++)
            {
                for (int c = 0; c < width; c++)
                {
                    if (boxes[r][c] != null)
                    {
                        if ((c == 0 || boxes[r][c-1] == null) &&
                            c < width - 1 &&
                            boxes[r][c+1] != null)
                        {
                            rawClues[curClue++] = acrossClues[curAcrossClue++];
                        }

                        if ((r == 0 || boxes[r-1][c] == null) &&
                            r < height - 1 &&
                            boxes[r+1][c] != null)
                        {
                            rawClues[curClue++] = downClues[curDownClue++];
                        }
                    }
                }
            }

            // There's been at least one malformed puzzle (1/3/2014) which had
            // an extra clue in it.  So ignore extra clues; if there aren't
            // enough clues, the code above will throw an
            // ArrayIndexOutOfBoundsException which will be caught below
            if (curClue < numAcrossClues + numDownClues)
            {
                String[] newRawClues = new String[curClue];
                System.arraycopy(rawClues, 0, newRawClues, 0, curClue);
                rawClues = newRawClues;
            }

            puzzle.setRawClues(rawClues);

            // Save out the puzzle
            IO.save(puzzle, os);
        }
        catch (ArrayIndexOutOfBoundsException | IllegalStateException | NoSuchElementException e)
        {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }
}
