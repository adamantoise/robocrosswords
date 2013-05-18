package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * USA Today
 * URL: http://picayune.uclick.com/comics/usaon/data/usaonYYMMDD-data.xml
 * Date: Monday-Saturday, excluding holidays
 */
public class USATodayDownloader extends UclickDownloader
{
    public USATodayDownloader()
    {
        super("usaon", "USA Today", "USA Today");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
    }
}
