package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Joseph Crosswords
 * URL: http://joseph.king-online.com/clues/YYYYMMDD.txt
 * Date: Monday-Saturday
 */
public class JosephDownloader extends KFSDownloader
{
    public JosephDownloader()
    {
        super("joseph", "Joseph Crosswords", "Thomas Joseph");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
    }
}
