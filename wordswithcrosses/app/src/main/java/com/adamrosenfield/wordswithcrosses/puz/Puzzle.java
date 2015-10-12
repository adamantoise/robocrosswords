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

package com.adamrosenfield.wordswithcrosses.puz;

import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;

import android.util.SparseArray;

import com.adamrosenfield.wordswithcrosses.io.IO;

public class Puzzle {

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    private String author;
    private String copyright;
    private String notes = "";
    private String title;
    private String[] acrossClues;
    private Integer[] acrossCluesLookup;
    private String[] downClues;
    private Integer[] downCluesLookup;
    private int numberOfClues;
    private Calendar pubdate = Calendar.getInstance();
    private Box[][] boxes;
    private Box[] boxesList;
    private String[] rawClues;
    private int height;
    private int width;
    private long playedTime;
    private boolean scrambled;
    public short solutionChecksum;
    private String version = IO.VERSION_STRING;

    // Temporary fields used for unscrambling.
    public int[] unscrambleKey;
    public byte[] unscrambleTmp;
    public byte[] unscrambleBuf;

    public void setAcrossClues(String[] acrossClues) {
        this.acrossClues = acrossClues;
    }

    public String[] getAcrossClues() {
        return acrossClues;
    }

    public void setAcrossCluesLookup(Integer[] acrossCluesLookup) {
        this.acrossCluesLookup = acrossCluesLookup;
    }

    public Integer[] getAcrossCluesLookup() {
        return acrossCluesLookup;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setBoxes(Box[][] boxes) {
        this.boxes = boxes;

        int clueCount = 1;

        for (int r = 0; r < boxes.length; r++) {
            boolean tickedClue = false;

            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] == null) {
                    continue;
                }

                if (((r == 0) || (boxes[r - 1][c] == null)) &&
                        (((r + 1) < boxes.length) && (boxes[r + 1][c] != null))) {
                    boxes[r][c].setDown(true);

                    if ((r == 0) || (boxes[r - 1][c] == null)) {
                        boxes[r][c].setClueNumber(clueCount);
                        tickedClue = true;
                    }
                }

                if (((c == 0) || (boxes[r][c - 1] == null)) &&
                        (((c + 1) < boxes[r].length) &&
                        (boxes[r][c + 1] != null))) {
                    boxes[r][c].setAcross(true);

                    if ((c == 0) || (boxes[r][c - 1] == null)) {
                        boxes[r][c].setClueNumber(clueCount);
                        tickedClue = true;
                    }
                }

                if (tickedClue) {
                    clueCount++;
                    tickedClue = false;
                }
            }
        }
    }

    public Box[][] getBoxes() {
        return (boxes == null) ? this.buildBoxes() : boxes;
    }

    public void setBoxesList(Box[] value) {
        this.boxesList = value;
    }

    public Box[] getBoxesList() {
        Box[] result = new Box[boxes.length * boxes[0].length];
        int i = 0;

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                result[i++] = boxes[r][c];
            }
        }

        return result;
    }

    /**
     * Initialize the temporary unscramble buffers.  Returns the scrambled solution.
     */
    public byte[] initializeUnscrambleData() {
        unscrambleKey = new int[4];
        unscrambleTmp = new byte[9];

        byte[] solution = getSolutionDown();
        unscrambleBuf = new byte[solution.length];

        return solution;
    }

    private byte[] getSolutionDown() {
        StringBuilder ans = new StringBuilder();
        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                if (boxes[r][c] != null) {
                    ans.append(boxes[r][c].getSolution());
                }
            }
        }
        return ans.toString().getBytes();
    }

    public void setUnscrambledSolution(byte[] solution) {
        int i = 0;
        for (int c = 0; c < width; c++) {
            for (int r = 0; r < height; r++) {
                if (boxes[r][c] != null) {
                    boxes[r][c].setSolution((char) solution[i++]);
                }
            }
        }
        setScrambled(false);
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setDate(Calendar date) {
        this.pubdate = date;
    }

    public Calendar getDate() {
        return pubdate;
    }

    public void setDownClues(String[] downClues) {
        this.downClues = downClues;
    }

    public String[] getDownClues() {
        return downClues;
    }

    public void setDownCluesLookup(Integer[] downCluesLookup) {
        this.downCluesLookup = downCluesLookup;
    }

    public Integer[] getDownCluesLookup() {
        return downCluesLookup;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Invalid height: " + height);
        }
        this.height = height;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNumberOfClues(int numberOfClues) {
        this.numberOfClues = numberOfClues;
    }

    public int getNumberOfClues() {
        return numberOfClues;
    }

    public int getPercentComplete() {
        return (int)(100 * getFractionComplete());
    }

    public double getFractionComplete() {
        int total = 0;
        int correct = 0;

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] != null) {
                    total++;

                    if (boxes[r][c].getResponse() == boxes[r][c].getSolution()) {
                        correct++;
                    }
                }
            }
        }

        if (total > 0) {
            return (double)correct / total;
        } else {
            LOG.warning("getFractionComplete(): Puzzle is empty?");
            return -1;
        }
    }

    public boolean isSolved() {
        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] != null && boxes[r][c].getResponse() != boxes[r][c].getSolution()) {
                        return false;
                }
            }
        }

        return true;
    }

    public void setRawClues(String[] rawClues) {
        this.rawClues = rawClues;
    }

    /**
     * Helper function to set rawClues from the maps of across and down clues.
     * Performs a merge of the two sorted clue maps.
     */
    public void setRawClues(SparseArray<String> acrossClues, SparseArray<String> downClues) {
        int numAcross = acrossClues.size();
        int numDown = downClues.size();
        rawClues = new String[numAcross + numDown];

        int i = 0, j = 0;
        while (i < numAcross && j < numDown) {
            int clue1 = acrossClues.keyAt(i);
            int clue2 = downClues.keyAt(j);
            if (clue1 <= clue2) {
                rawClues[i + j] = acrossClues.valueAt(i);
                i++;
            } else {
                rawClues[i + j] = downClues.valueAt(j);
                j++;
            }
        }

        while (i < numAcross) {
            rawClues[i + j] = acrossClues.valueAt(i);
            i++;
        }
        while (j < numDown) {
            rawClues[i + j] = downClues.valueAt(j);
        }
    }

    public String[] getRawClues() {
        return rawClues;
    }

    public void setTime(long time) {
        this.playedTime = time;
    }

    public long getTime() {
        return this.playedTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setVersion(String version) {
        this.version = version;
    }

     public String getVersion() {
         return version;
     }

    public void setScrambled(boolean scrambled) {
        this.scrambled = scrambled;
    }

    public boolean isScrambled() {
        return scrambled;
    }

    public void setSolutionChecksum(short checksum) {
        this.solutionChecksum = checksum;
    }

    public short getSolutionChecksum() {
        return solutionChecksum;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Invalid width: " + width);
        }
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    public Box[][] buildBoxes() {
        int i = 0;
        boxes = new Box[this.height][this.width];

        for (int r = 0; r < this.height; r++) {
            for (int c = 0; c < this.width; c++) {
                boxes[r][c] = boxesList[i++];
            }
        }

        return boxes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Puzzle other = (Puzzle) obj;

        if (!Arrays.equals(acrossClues, other.acrossClues)) {
            return false;
        }

        if (!Arrays.equals(acrossCluesLookup, other.acrossCluesLookup)) {
            return false;
        }

        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }

        Box[][] b1 = boxes;
        Box[][] b2 = other.boxes;

        for (int r = 0; r < b1.length; r++) {
            for (int c = 0; c < b1[r].length; c++) {
                if (!b1[r][c].equals(b2[r][c])) {
                    return false;
                }
            }
        }

        if (copyright == null) {
            if (other.copyright != null) {
                return false;
            }
        } else if (!copyright.equals(other.copyright)) {
            return false;
        }

        if (!Arrays.equals(downClues, other.downClues)) {
            return false;
        }

        if (!Arrays.equals(downCluesLookup, other.downCluesLookup)) {
            return false;
        }

        if (height != other.height) {
            return false;
        }

        if (notes == null) {
            if (other.notes != null) {
                return false;
            }
        } else if (!notes.equals(other.notes)) {
            return false;
        }

        if (getNumberOfClues() != other.getNumberOfClues()) {
            return false;
        }

        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }

        if (width != other.width) {
            return false;
        }

        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }

        if (scrambled != other.scrambled) {
            return false;
        }

        if (solutionChecksum != other.solutionChecksum) {
            return false;
        }

        return true;
    }

    public String findAcrossClue(int clueNumber) {
        int clueIndex = Arrays.binarySearch(acrossCluesLookup, clueNumber);
        return (clueIndex >= 0 ? acrossClues[clueIndex] : null);
    }

    public String findDownClue(int clueNumber) {
        int clueIndex = Arrays.binarySearch(this.downCluesLookup, clueNumber);
        return (clueIndex >= 0 ? downClues[clueIndex] : null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(acrossClues);
        result = (prime * result) + Arrays.hashCode(acrossCluesLookup);
        result = (prime * result) + ((author == null) ? 0 : author.hashCode());
        result = (prime * result) + Arrays.hashCode(boxes);
        result = (prime * result) +
            ((copyright == null) ? 0 : copyright.hashCode());
        result = (prime * result) + Arrays.hashCode(downClues);
        result = (prime * result) + Arrays.hashCode(downCluesLookup);
        result = (prime * result) + height;
        result = (prime * result) + ((notes == null) ? 0 : notes.hashCode());
        result = (prime * result) + getNumberOfClues();
        result = (prime * result) + ((title == null) ? 0 : title.hashCode());
        result = (prime * result) + ((version == null) ? 0 : version.hashCode());
        result = (prime * result) + width;

        return result;
    }

    @Override
    public String toString() {
        return "Puzzle " + boxes.length + " x " + boxes[0].length + " " +
        this.title;
    }
}
