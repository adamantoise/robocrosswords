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

package com.adamrosenfield.wordswithcrosses.versions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

@TargetApi(9)
public class GingerbreadUtil extends DefaultUtil {

    protected Context context;

    private static class DownloadingFile
    {
        public boolean succeeded = false;
        public boolean completed = false;
    }

    private static ConcurrentMap<Long, DownloadingFile> waitingDownloads = new ConcurrentSkipListMap<Long, DownloadingFile>();

    private static Map<Long, Boolean> completedDownloads = new HashMap<Long, Boolean>();

    @Override
    public void setContext(Context ctx) {
        this.context = ctx;
    }

    @Override
    public boolean downloadFile(URL url, Map<String, String> headers, File destination, boolean notification,
        String title) throws IOException {
        // Pre-ICS download managers don't support HTTPS
        if ("https".equals(url.getProtocol()) && android.os.Build.VERSION.SDK_INT < 15) {
            LOG.info("HTTPS not supported, not using DownloadManager");
            return super.downloadFile(url,  headers,  destination,  notification,  title);
        }
        DownloadManager mgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        Request request = new Request(Uri.parse(url.toString()));
        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());

        request.setDestinationUri(Uri.fromFile(tempFile));
        LOG.info("Downloading " + url + " ==> " + tempFile);

        for (Entry<String, String> entry : headers.entrySet()) {
            request.addRequestHeader(entry.getKey(), entry.getValue());
        }

        request.setMimeType("application/x-crossword");

        setNotificationVisibility(request, notification);

        request.setTitle(title);
        long id = mgr.enqueue(request);
        Long idObj = id;

        // If the request completed really fast, we're done
        DownloadingFile downloadingFile = new DownloadingFile();
        boolean succeeded = false;
        boolean completed = false;
        synchronized (completedDownloads) {
            Boolean b = completedDownloads.remove(idObj);
            if (b != null) {
                succeeded = b;
                completed = true;
            } else {
                waitingDownloads.put(idObj, downloadingFile);
            }
        }

        // Wait for the request to complete, if it hasn't completed already
        if (!completed) {
            try {
                synchronized (downloadingFile) {
                    if (!downloadingFile.completed) {
                        downloadingFile.wait();
                    }
                    succeeded = downloadingFile.succeeded;
                }
            } catch (InterruptedException e) {
                LOG.warning("Download interrupted: " + url);
                return false;
            }
        }

        LOG.info("Download " + (succeeded ? "succeeded" : "failed") + ": " + url);

        if (succeeded) {
            if (!destination.equals(tempFile) && !tempFile.renameTo(destination)) {
                LOG.warning("Renaming " + tempFile + " to " + destination + " failed");
                return false;
            }
        }

        return succeeded;
    }

    @Override
    public void onFileDownloaded(long id, boolean succeeded) {
        Long idObj = id;
        synchronized (completedDownloads) {
            DownloadingFile downloadingFile = waitingDownloads.remove(idObj);
            if (downloadingFile != null) {
                synchronized (downloadingFile) {
                    downloadingFile.succeeded = succeeded;
                    downloadingFile.completed = true;
                    downloadingFile.notifyAll();
                }
            } else {
                LOG.info("No thread is waiting on download for id=" + id);
                completedDownloads.put(idObj, succeeded);
            }
        }
    }

    protected void setNotificationVisibility(Request reqest, boolean notification) {
    }
}
