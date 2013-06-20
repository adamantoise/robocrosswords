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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Map;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.KingFeaturesPlaintextIO;

/**
 * King Features Syndicate Puzzles
 * URL: http://[puzzle].king-online.com/clues/YYYYMMDD.txt
 * premier = Sunday
 * joseph = Monday-Saturday
 * sheffer = Monday-Saturday
 */
public abstract class KFSDownloader extends AbstractDownloader {
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    private String author;
    private String fullName;

    public KFSDownloader(String shortName, String fullName, String author) {
        super("http://" + shortName + ".king-online.com/clues/", fullName);
        this.fullName = fullName;
        this.author = author;
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File txtFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        if (!utils.downloadFile(url, headers, txtFile, true, getName())) {
            return false;
        }

        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
        String copyright = "\u00a9 " + date.get(Calendar.YEAR) + " King Features Syndicate.";

        boolean succeeded = false;

        FileInputStream fis = new FileInputStream(txtFile);
        try {
            FileOutputStream fos = new FileOutputStream(destFile);
            try {
                succeeded = KingFeaturesPlaintextIO.convertKFPuzzle(
                    fis, fos, fullName + ", " + df.format(date.getTime()), author,
                    copyright, date);
            } finally {
                fos.close();
            }
        } finally {
            fis.close();
            txtFile.delete();
        }

        return succeeded;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".txt");
    }
}
