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
    private final String id;
    private Calendar date;
    private String puzzleUrl;
    private String dateUrl;

    private transient Puzzle puzzle;

    public DerStandardPuzzleMetadata(String id) {
        this.id = id;
    }

    public String getPuzzleUrl(String relativeBase) {
        return getUrl(puzzleUrl, relativeBase);
    }

    public void setPuzzleUrl(String puzzleUrl) {
        this.puzzleUrl = puzzleUrl;
    }

    public String getDateUrl(String relativeBase) {
        return getUrl(dateUrl, relativeBase);
    }

    private String getUrl(String url, String relativeBase) {
        if (url.contains("://")) {
            return url;
        } else {
            return relativeBase + url;
        }
    }

    public void setDateUrl(String dateUrl) {
        this.dateUrl = dateUrl;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Calendar getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public void setPuzzle(Puzzle p) {
        this.puzzle = p;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    @Override
    public String toString() {
        return "DerStandardPuzzleMetadata [id=" + id + ", date=" + date + ", puzzleUrl=" + puzzleUrl + ", puzzle=" + puzzle + "]";
    }

}