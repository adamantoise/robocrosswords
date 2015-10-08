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

import java.io.IOException;
import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.net.HTTPException;
import com.adamrosenfield.wordswithcrosses.R;

/**
 * Common base class functionality for crosswords downloaded from
 * xwordhub.com.  These include the following crosswords:
 *
 * - American Values Club xword
 * - CRooked Crossword
 * - Crossword Nation
 */
public abstract class XWordHubDownloader extends AbstractDownloader
{
    // ATTENTION
    //
    // If you are going to use this code as a basis for writing your own
    // downloaders for AVXW/CRooked/XWord Nation, PLEASE OBTAIN A SEPARATE
    // PARTNER ACCOUNT by contacting the support crew:
    //
    // <support@avxword.com>
    // <support@crookedcrosswords.com>
    // <support@xwordnation.com>
    //
    // They can set you up with your own credentials for accessing the
    // download API.  They're really nice and friendly.
    private static final String PARTNER_ID = "60";
    private static final String PARTNER_NAME = "crosses_android";
    private static final String BASE_URL = "https://puzzles.xwordhub.com:11443/download?partner=" + PARTNER_ID + "&app=" + PARTNER_NAME;

    public XWordHubDownloader(String downloaderName, String shortName, String username, String password)
    {
        super(BASE_URL +
              "&brand=" + shortName +
              "&username=" + username +
              "&password=" + password,
              downloaderName);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return
            "&id=" +
            date.get(Calendar.YEAR) +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void download(Calendar date, String urlSuffix) throws IOException
    {
        try
        {
            super.download(date, urlSuffix);
        }
        catch (HTTPException e)
        {
            if (e.getStatus() == 401)
            {
                throw new DownloadException(R.string.login_failed);
            }
            else
            {
                // TODO: Parse the entity for the error code
                throw e;
            }
        }
    }

}
