package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Premier Crosswords
 * URL: http://premier.king-online.com/clues/YYYYMMDD.txt
 * Date: Sunday
 */
public class PremierDownloader extends KFSDownloader
{
    public PremierDownloader()
    {
        super("premier", "Premier Crosswords", "Frank Longo");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }
}
