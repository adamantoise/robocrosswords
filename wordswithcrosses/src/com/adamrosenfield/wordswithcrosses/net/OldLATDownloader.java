package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Los Angeles Times
 * URL: http://www.cruciverb.com/puzzles/lat/latYYMMDD.puz
 * Date = Daily
 */
public class OldLATDownloader extends AbstractDownloader {
    public static final String NAME = "Los Angeles Times";
    NumberFormat nf = NumberFormat.getInstance();

    protected OldLATDownloader() {
        super("http://www.cruciverb.com/puzzles/lat/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_DAILY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("lat" +
                this.nf.format(date.get(Calendar.YEAR) % 100) +
                this.nf.format(date.get(Calendar.MONTH) + 1) +
                this.nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix) throws IOException {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Referer", "http://www.cruciverb.com/puzzles.php?op=showarch&pub=lat");
        headers.put("User-Agent",
            "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.0 Safari/534.13");

        return super.download(date, urlSuffix, headers);
    }
}
