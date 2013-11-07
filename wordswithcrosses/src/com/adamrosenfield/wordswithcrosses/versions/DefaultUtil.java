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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.net.AbstractDownloader;
import com.adamrosenfield.wordswithcrosses.net.HTTPException;

public class DefaultUtil implements AndroidVersionUtils {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected Context context;
    protected SharedPreferences prefs;

    protected HttpParams mHttpParams;

    public DefaultUtil() {
        // Set default connect and recv timeouts to 30 seconds
        mHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(mHttpParams, 30000);
        HttpConnectionParams.setSoTimeout(mHttpParams, 30000);
    }

    public void setContext(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void downloadFile(URL url, Map<String, String> headers,
            File destination, boolean notification, String title)
            throws IOException {

        String scrubbedUrl = AbstractDownloader.scrubUrl(url);
        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());
        LOG.info("DefaultUtil: Downloading " + scrubbedUrl + " ==> " + tempFile);
        InputStream response = downloadHelper(url, scrubbedUrl, headers);

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            IO.copyStream(response, fos);
        } finally {
            fos.close();
        }

        if (!tempFile.equals(destination) && !tempFile.renameTo(destination)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destination);
        }

        LOG.info("DefaultUtil: Download succeeded: " + scrubbedUrl);
    }

    public String downloadToString(URL url, Map<String, String> headers) throws IOException {
        String scrubbedUrl = AbstractDownloader.scrubUrl(url);
        InputStream response = downloadHelper(url, scrubbedUrl, headers);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IO.copyStream(response, baos);

        return new String(baos.toByteArray());
    }

    private InputStream downloadHelper(URL url, String scrubbedUrl, Map<String, String> headers) throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient(mHttpParams);

        HttpGet httpget = new HttpGet(url.toString());
        for (Entry<String, String> e : headers.entrySet()) {
            httpget.setHeader(e.getKey(), e.getValue());
        }

        HttpResponse response = httpclient.execute(httpget);

        int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            LOG.warning("Download failed: " + scrubbedUrl + " status=" + status);
            throw new HTTPException(status);
        }

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    public void onFileDownloaded(long id, boolean successful, int status) {
    }

    public void finishOnHomeButton(Activity a) {
    }

    public void holographic(Activity playActivity) {
    }

    public void onActionBarWithText(MenuItem a) {
    }

    public void onActionBarWithText(SubMenu reveal) {
    }

    public View onActionBarCustom(Activity a, int id) {
        return null;
    }

    public void hideWindowTitle(Activity a) {
    }

    public void hideActionBar(Activity a) {
    }
}
