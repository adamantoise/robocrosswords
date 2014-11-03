/**
 * This file is part of Words With Crosses.
 * 
 * Copyright (this file) 2014 Wolfgang Groiss
 * 
 * This file is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 **/

package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.io.Serializable;
import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class DerStandardPuzzleMetadata implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private final int id;
    private final Calendar date;
    private final String puzzleUrl;
    
    private boolean puzzleAvailable = false;
    private boolean solutionAvailable = false;

    private transient Puzzle puzzle;

    public DerStandardPuzzleMetadata(int id, String puzzleUrl, Calendar date) {
        this.id = id;
        this.date = date;
        this.puzzleUrl = puzzleUrl;
    }

    public String getPuzzleUrl(String relativeBase) {
        return getUrl(puzzleUrl, relativeBase);
    }

    private String getUrl(String url, String relativeBase) {
        if (url.contains("://")) {
            return url;
        } else {
            return relativeBase + url;
        }
    }

    public Calendar getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setPuzzle(Puzzle p) {
        this.puzzle = p;
        refreshPuzzleTitle();
    }

    private void refreshPuzzleTitle() {
        if (puzzle == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder("Nr. ");
        sb.append(id);
        
        if (!solutionAvailable) {
            sb.append(" [no Solution]");
        }

        puzzle.setTitle(sb.toString());
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public boolean isPuzzleAvailable() {
        return puzzle != null;
    }

    public boolean isSolutionAvailable() {
        return solutionAvailable;
    }

    public void setSolutionAvailable(boolean solutionAvailable) {
        this.solutionAvailable = solutionAvailable;
        refreshPuzzleTitle();
    }

    @Override
    public String toString() {
        return "DerStandardPuzzleMetadata [id=" + id + 
                                        ", date=" + date + 
                                        ", puzzleUrl=" + puzzleUrl + 
                                        ", puzzleAvailable=" + puzzleAvailable + 
                                        ", solutionAvailable=" + solutionAvailable + 
                                        "]";
    }


}
