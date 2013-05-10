package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public class PeopleScraper extends AbstractPageScraper {

    public PeopleScraper() {
        super("People Magazine");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://www.people.com/people/puzzler/";
    }

    public int[] getDownloadDates() {
        // TODO: Verify
        return DATE_FRIDAY;
    }
}
