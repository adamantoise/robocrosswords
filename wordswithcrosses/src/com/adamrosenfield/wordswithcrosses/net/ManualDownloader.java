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

import android.content.Intent;
import android.net.Uri;

import java.io.IOException;
import java.util.Calendar;

public abstract class ManualDownloader extends AbstractDownloader
{
    public ManualDownloader(String downloaderName)
    {
        super("", downloaderName);
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        throw new IOException("Automatic downloads not supported by ManualDownloader");
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public boolean isManualDownload()
    {
        return true;
    }

    @Override
    public Intent getManualDownloadIntent(Calendar date)
    {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(getManualDownloadUri(date)));
    }

    protected abstract String getManualDownloadUri(Calendar date);
}
