package com.adamrosenfield.wordswithcrosses.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
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
public class KFSDownloader extends AbstractDownloader {
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    NumberFormat nf = NumberFormat.getInstance();
    private String author;
    private String fullName;
    private int[] days;

    public KFSDownloader(String shortName, String fullName, String author, int[] days) {
        super("http://" + shortName + ".king-online.com/clues/", fullName);
        this.fullName = fullName;
        this.author = author;
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
        try {
            FileInputStream is = new FileInputStream(txtFile);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
            succeeded = KingFeaturesPlaintextIO.convertKFPuzzle(
                    is, dos, fullName + ", " + df.format(date.getTime()), author,
                    copyright, date);
            dos.close();
            is.close();
            txtFile.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return succeeded;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".txt");
    }
}
