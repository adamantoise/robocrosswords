package com.adamrosenfield.wordswithcrosses.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.logging.Level;

import com.adamrosenfield.wordswithcrosses.io.KingFeaturesPlaintextIO;
import com.adamrosenfield.wordswithcrosses.versions.DefaultUtil;

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
        super("http://" + shortName + ".king-online.com/clues/", DOWNLOAD_DIR, fullName);
        this.fullName = fullName;
        this.author = author;
        this.days = days;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return days;
    }

    public File download(Calendar date) {
        File downloadTo = new File(this.downloadDirectory, this.createFileName(date));

        if (downloadTo.exists()) {
            return null;
        }

        File plainText = downloadToTempFile(date);

        if (plainText == null) {
            return null;
        }

        String copyright = "\u00a9 " + date.get(Calendar.YEAR) + " King Features Syndicate.";

        try {
            InputStream is = new FileInputStream(plainText);
            DataOutputStream os = new DataOutputStream(new FileOutputStream(downloadTo));
            boolean retVal = KingFeaturesPlaintextIO.convertKFPuzzle(is, os, fullName + ", " + df.format(date.getTime()), author,
                    copyright, date);
            os.close();
            is.close();
            plainText.delete();

            if (!retVal) {
                LOG.log(Level.SEVERE, "Unable to convert KFS puzzle into Across Lite format.");
                downloadTo.delete();
                downloadTo = null;
            }
        } catch (Exception ioe) {
            LOG.log(Level.SEVERE, "Exception converting KFS puzzle into Across Lite format.", ioe);
            downloadTo.delete();
            downloadTo = null;
        }

        return downloadTo;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".txt");
    }

    private File downloadToTempFile(Calendar date) {
        DefaultUtil util = new DefaultUtil();
        File downloaded = new File(downloadDirectory, this.createFileName(date));

        try {
            URL url = new URL(this.baseUrl + this.createUrlSuffix(date));
            LOG.log(Level.INFO, this.fullName +" "+url.toExternalForm());
            util.downloadFile(url, downloaded, EMPTY_MAP, false, null);
        } catch (Exception e) {
            e.printStackTrace();
            downloaded.delete();
            downloaded = null;
        }

        if (downloaded == null) {
            LOG.log(Level.SEVERE, "Unable to download plain text KFS file.");

            return null;
        }

        System.out.println("DownloadedKFS: " + downloaded);

        try {
            File tmpFile =  new File(this.tempFolder, "kfs-temp"+System.currentTimeMillis()+".txt"); //File.createTempFile("kfs-temp", "txt");
            downloaded.renameTo(tmpFile);

            return tmpFile;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unable to move KFS file to temporary location.");

            return null;
        }
    }
}
