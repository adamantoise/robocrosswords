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

import java.util.logging.Logger;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

public class DownloadReceiverGinger extends BroadcastReceiver {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager mgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = intent.getLongExtra("extra_download_id", -1);

        Query q = new Query();
        q.setFilterById(id);
        q.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_FAILED);

        Cursor cursor = mgr.query(q);
        if (cursor.getCount() < 1) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
        String mediaType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
        cursor.close();

        LOG.info("Download completed: id=" + id + " status=" + status + " reason=" + reason + " mediaType=" + mediaType);

        boolean succeeded = (status == DownloadManager.STATUS_SUCCESSFUL);

        AndroidVersionUtils.Factory.getInstance().onFileDownloaded(id, succeeded, reason);
    }
}
