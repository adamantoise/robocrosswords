package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

// FIXME: This downloader is broken (though apparently so is their web
// crossword...)

/**
 * Thinks.com
 * URL: http://thinks.com/daily-crossword/puzzles/YYYY-MM/dc1-YYYY-MM-DD.puz
 * Date: Friday
 */
public class ThinksDownloader extends AbstractDownloader {
    private static final String NAME = "Thinks.com";

    public ThinksDownloader() {
        super("http://thinks.com/daily-crossword/puzzles/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "/dc1-" +
                date.get(Calendar.YEAR) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
