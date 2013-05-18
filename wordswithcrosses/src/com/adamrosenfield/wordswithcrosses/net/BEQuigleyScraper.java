package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Brendan Emmett QUigley
 * URL: http://www.brendanemmettquigley.com/files/[puzzle-name].puz
 * Date: Monday, Thursday
 */
public class BEQuigleyScraper extends AbstractPageScraper {

    public BEQuigleyScraper() {
        super("Brendan Emmett Quigley");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://www.brendanemmettquigley.com/";
    }

    public boolean isPuzzleAvailable(Calendar date) {
        int day = date.get(Calendar.DAY_OF_WEEK);
        return (day == Calendar.MONDAY || day == Calendar.THURSDAY);
    }
}
