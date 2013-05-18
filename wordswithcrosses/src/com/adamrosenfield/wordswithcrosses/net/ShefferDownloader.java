package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Sheffer Crosswords
 * URL: http://sheffer.king-online.com/clues/YYYYMMDD.txt
 * Date: Monday-Saturday
 */
public class ShefferDownloader extends KFSDownloader
{
    public ShefferDownloader()
    {
        super("sheffer", "Sheffer Crosswords", "Eugene Sheffer");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
    }
}
