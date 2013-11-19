/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        lastFriday.set(Calendar.HOUR, 0);
        lastFriday.set(Calendar.MINUTE, 0);
        lastFriday.set(Calendar.SECOND, 0);
        lastFriday.set(Calendar.MILLISECOND, 0);

        return (date.compareTo(lastFriday) >= 0);
    }
}
