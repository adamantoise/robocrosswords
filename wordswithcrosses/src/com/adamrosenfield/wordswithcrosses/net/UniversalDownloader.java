package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Universal Crossword
 * URL: http://picayune.uclick.com/comics/fcs/data/fcsYYMMDD-data.xml
 * Date: Monday-Saturday, excluding holidays
 */
public class UniversalDownloader extends UclickDownloader
{
    public UniversalDownloader()
    {
        super("fcx", "Universal Crossword", "uclick LLC");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return true;
    }
}
