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

import java.util.Calendar;
import java.util.Locale;

import com.adamrosenfield.wordswithcrosses.R;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

public class PuzzleMeta {

    public long id;
    public String filename;
    public boolean archived;
    public String author;
    public String canonicalAuthor;
    public String title;
    public String source;
    public Calendar date;
    public int percentComplete;
    public String sourceUrl;

    @Override
    public String toString() {
        return new StringBuilder("id: ")
        .append(id)
        .append(" filename: ")
        .append(filename)
        .append(" archived: ")
        .append(archived)
        .append(" author: ")
        .append(author)
        .append(" title: ")
        .append(title)
        .append(" source: ")
        .append(source)
        .append(" sourceUrl: ")
        .append(sourceUrl)
        .append(" date: " )
        .append(date)
        .append(" percentCompelete: ")
        .append(percentComplete)
        .toString();
    }

    /**
     * Initializes the canonicalAuthor field with a canonicalized version of
     * the author field:
     *
     * - Any leading and trailing spaces are removed
     * - Any leading "By " is removed
     * - Initial character is capitalized
     */
    public void canonicalizeAuthor() {
        canonicalAuthor = author.trim();

        if (canonicalAuthor.length() > 3 &&
            canonicalAuthor.substring(0, 3).compareToIgnoreCase("by ") == 0)
        {
            canonicalAuthor = canonicalAuthor.substring(3);
        }

        if (canonicalAuthor.length() > 0) {
            canonicalAuthor = canonicalAuthor.substring(0, 1).toUpperCase(Locale.US) + canonicalAuthor.substring(1);
        } else {
            canonicalAuthor = WordsWithCrossesApplication.getContext().getResources().getString(R.string.author_unknown);
        }
    }
}
