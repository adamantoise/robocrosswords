/**
 * This file is part of Words With Crosses.
 *
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
 * Erik Agard
 * URL: http://gluttonforpun.blogspot.com
 * Date: Wednesday
 */
public class ErikAgardDownloader extends BloggerDownloader
{
    public ErikAgardDownloader()
    {
        super("Erik Agard", "http://gluttonforpun.blogspot.com");

        // The Dropbox download links don't normally redirect automatically,
        // unless the server detects certain user agents (such as Curl)
        setUserAgent(USER_AGENT_CURL);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY);
    }
}
