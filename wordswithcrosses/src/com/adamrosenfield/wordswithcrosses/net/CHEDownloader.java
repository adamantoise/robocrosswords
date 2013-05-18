package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Chronicle of Higher Education
 * URL: http://chronicle.com/items/biz/puzzles/YYYYMMDD.puz
 * Date: Friday
 */
public class CHEDownloader extends AbstractDownloader {
    private static final String NAME = "Chronicle of Higher Education";
    NumberFormat nf = NumberFormat.getInstance();

    public CHEDownloader() {
        super("http://chronicle.com/items/biz/puzzles/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
