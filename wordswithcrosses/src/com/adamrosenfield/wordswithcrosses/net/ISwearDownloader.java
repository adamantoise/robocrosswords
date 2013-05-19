package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Daily Record "I Swear" crossword
 * URL: http://wij.theworld.com/puzzles/dailyrecord/DR110401.puz
 * Date: Friday
 *
 * @author robert.cooper
 */
public class ISwearDownloader extends AbstractDownloader {
    private static final String NAME = "I Swear";

    public ISwearDownloader(){
        super("http://wij.theworld.com/puzzles/dailyrecord/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("DR" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
