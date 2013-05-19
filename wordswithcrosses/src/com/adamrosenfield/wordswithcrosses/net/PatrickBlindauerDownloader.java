package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Patrick Blindauer Free Monthly Puzzle
 * URL: http://www.patrickblindauer.com/Free_Monthly_Puzzles/Mmm_YYYY/[puzzle-name].puz
 * Date: Monday, Thursday
 */
public class PatrickBlindauerDownloader extends AbstractPageScraper
{
    private static final String BLINDAUER_SHORT_MONTHS[] =
    {
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sept",  // At least for 2011 and 2012.  We'll see about 2013...
        "Oct",
        "Nov",
        "Dec"
    };

    public PatrickBlindauerDownloader()
    {
        super("Patrick Blindauer");
    }

    @Override
    protected String getScrapeURL(Calendar date)
    {
        return ("http://www.patrickblindauer.com/Free_Monthly_Puzzles/" +
                BLINDAUER_SHORT_MONTHS[date.get(Calendar.MONTH)] +
                "_" +
                date.get(Calendar.YEAR) +
                "/");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_MONTH) == 1);
    }
}
