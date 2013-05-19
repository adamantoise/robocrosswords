package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Chronicle of Higher Education
 * URL: http://chronicle.com/items/biz/puzzles/YYYYMMDD.puz
 * Date: Friday
 */
public class CHEDownloader extends AbstractDownloader {
    private static final String NAME = "Chronicle of Higher Education";

    public CHEDownloader() {
        super("http://chronicle.com/items/biz/puzzles/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
