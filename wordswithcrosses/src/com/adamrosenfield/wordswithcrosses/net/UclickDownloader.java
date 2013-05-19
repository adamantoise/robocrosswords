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
    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File xmlFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        if (!utils.downloadFile(url, headers, xmlFile, true, getName())) {
            return false;
        }

        try {
            File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
            return convertUclickPuzzle(xmlFile, destFile, date);
        } finally {
            xmlFile.delete();
        }
    }

    private boolean convertUclickPuzzle(File xmlFile, File destFile, Calendar date) throws IOException {
        String fullCopyright = "\u00a9 " + date.get(Calendar.YEAR) + " " + copyright;

        FileInputStream fis = new FileInputStream(xmlFile);
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
            try {
                return UclickXMLIO.convertUclickPuzzle(fis, dos, fullCopyright, date);
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
