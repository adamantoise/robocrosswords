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

public interface Downloader {

    public void setContext(Context context);

    public boolean isPuzzleAvailable(Calendar date);

    public String getName();

    public String getFilename(Calendar date);

    public void download(Calendar date) throws IOException;

    public String sourceUrl(Calendar date);

    public boolean isManualDownload();

    public Intent getManualDownloadIntent(Calendar date);
}
