package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * People Puzzler
 * URL: http://img2.timeinc.net/people/static/puzzler/YYMMDD/codebase/puz_YYMMDD[Name].puz
 * Scraped from: http://www.people.com/people/puzzler/
 * Date: Friday
 *
 * The archives (http://www.people.com/people/archives/puzzler/0,,,00.html) are
 * a pain to scrape, so we don't support them for now.
 */
public class PeopleScraper extends AbstractPageScraper {

    public PeopleScraper() {
        super("People Magazine");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://www.people.com/people/puzzler/";
    }

    public boolean isPuzzleAvailable(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            return false;
        }

        // The archives are a pain to scrape, so we don't support them for now
        Calendar lastFriday = Calendar.getInstance();
        int daysSinceFriday = (lastFriday.get(Calendar.DAY_OF_WEEK) + 7 - Calendar.FRIDAY) % 7;
        lastFriday.add(Calendar.DATE,  -daysSinceFriday);

        return (date.compareTo(lastFriday) >= 0);
    }
}
