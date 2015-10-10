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

import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;

import com.adamrosenfield.wordswithcrosses.R;

/**
 * Does not actually download any puzzles; just adds an "All Available" option to the dropdown.
 */
public class DummyDownloader implements Downloader {

    private Context mContext;

    public DummyDownloader(Context context) {
        mContext = context;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return false;
    }

    public String getName() {
        return null;
    }

    public String getFilename(Calendar date) {
        return null;
    }

    public void download(Calendar date) throws IOException {
    }

    public String sourceUrl(Calendar date) {
        return null;
    }

    public boolean isManualDownload() {
        return false;
    }

    public Intent getManualDownloadIntent(Calendar date) {
        return null;
    }

    @Override
    public String toString() {
        return mContext.getResources().getString(R.string.source_all_available);
    }
}
