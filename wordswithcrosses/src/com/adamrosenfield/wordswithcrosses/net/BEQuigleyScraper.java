package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public class BEQuigleyScraper extends AbstractPageScraper {

    private static final int[] DATE_MONDAY_THURSDAY = new int[]{Calendar.MONDAY, Calendar.THURSDAY};

    public BEQuigleyScraper() {
        super("Brendan Emmett Quigley");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://www.brendanemmettquigley.com/";
    }

    public int[] getDownloadDates() {
        return DATE_MONDAY_THURSDAY;
    }
}
