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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.UclickXMLIO;

/**
 * Uclick XML Puzzles
 * URL: http://picayune.uclick.com/comics/[puzzle]/data/[puzzle]YYMMDD-data.xml
 * usaon (USA Today) = Monday-Saturday (not holidays)
 * fcx (Universal) = Daily
 * lacal (LA Times Sunday Calendar) = Sunday
 */
public abstract class UclickDownloader extends AbstractDownloader {
    private String copyright;
    //private String fullName;
    private String shortName;

    public UclickDownloader(String shortName, String fullName, String copyright) {
        super("http://picayune.uclick.com/comics/" + shortName + "/data/", fullName);
        this.shortName = shortName;
        //this.fullName = fullName;
        this.copyright = copyright;
    }

    @Override
    protected void download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File xmlFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        utils.downloadFile(url, headers, xmlFile, true, getName());

        try {
            File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
            convertUclickPuzzle(xmlFile, destFile, date);
        } finally {
            xmlFile.delete();
        }
    }

    private void convertUclickPuzzle(File xmlFile, File destFile, Calendar date) throws IOException {
        String fullCopyright = "\u00a9 " + date.get(Calendar.YEAR) + " " + copyright;

        FileInputStream fis = new FileInputStream(xmlFile);
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
            try {
                if (!UclickXMLIO.convertUclickPuzzle(fis, dos, fullCopyright, date)) {
                    throw new IOException("Failed to convert Uclick puzzle");
                }
            } finally {
                dos.close();
            }
        } finally {
            fis.close();
        }
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (this.shortName +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                "-data.xml");
    }
}
