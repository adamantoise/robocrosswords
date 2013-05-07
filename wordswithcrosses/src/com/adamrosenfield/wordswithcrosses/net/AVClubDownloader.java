package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.io.IO;

/**
 * The Onion AV Club
 * URL: http://herbach.dnsalias.com/Tausig/avYYMMDD.puz
 * Date = Wednesdays
 */
public class AVClubDownloader extends AbstractDownloader {
    public static final String NAME = "The Onion AV Club";
    NumberFormat nf = NumberFormat.getInstance();

    protected AVClubDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_WEDNESDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("av" +
                this.nf.format(date.get(Calendar.YEAR) % 100) +
                this.nf.format(date.get(Calendar.MONTH) + 1) +
                this.nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix) throws IOException {
        URL url;
        try {
            url = new URL(this.baseUrl + urlSuffix);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Referer", this.baseUrl);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            File f = new File(downloadDirectory, this.getFilename(date));
            FileOutputStream fos = new FileOutputStream(f);
            try {
                IO.copyStream(connection.getInputStream(), fos);
            } finally {
                fos.close();
            }

            return true;
        } else {
            return false;
        }
    }
}
