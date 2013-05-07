package com.adamrosenfield.wordswithcrosses.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Map;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.UclickXMLIO;

/**
 * Uclick XML Puzzles
 * URL: http://picayune.uclick.com/comics/[puzzle]/data/[puzzle]YYMMDD-data.xml
 * crnet (Newsday) = Daily
 * usaon (USA Today) = Monday-Saturday (not holidays)
 * fcx (Universal) = Daily
 * lacal (LA Times Sunday Calendar) = Sunday
 */
public class UclickDownloader extends AbstractDownloader {
    NumberFormat nf = NumberFormat.getInstance();
    private String copyright;
    private String fullName;
    private String shortName;
    private int[] days;

    public UclickDownloader(String shortName, String fullName, String copyright, int[] days) {
        super("http://picayune.uclick.com/comics/" + shortName + "/data/", DOWNLOAD_DIR, fullName);
        this.shortName = shortName;
        this.fullName = fullName;
        this.copyright = copyright;
        this.days = days;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return days;
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        URL url;
        try {
            url = new URL(this.baseUrl + urlSuffix);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

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
                nf.format(date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                "-data.xml");
    }
}
