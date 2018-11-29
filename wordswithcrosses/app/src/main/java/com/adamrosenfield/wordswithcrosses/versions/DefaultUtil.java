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
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DefaultUtil implements AndroidVersionUtils {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected Context context;
    protected SharedPreferences prefs;

    protected OkHttpClient mHttpClient;

    public DefaultUtil() {
        // Set default connect and recv timeouts to 30 seconds
        mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public void setContext(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public OkHttpClient getHttpClient() {
        return mHttpClient;
    }

    public void downloadFile(URL url, Map<String, String> headers, File destination, boolean notification, String title) throws IOException {
        String scrubbedUrl = AbstractDownloader.scrubUrl(url);
        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());
        LOG.info("DefaultUtil: Downloading " + scrubbedUrl + " ==> " + tempFile);
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            downloadHelper(url, scrubbedUrl, headers, fos);
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        downloadHelper(url, scrubbedUrl, headers, baos);

        return new String(baos.toByteArray());
    }

    private void downloadHelper(URL url, String scrubbedUrl, Map<String, String> headers, OutputStream output) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
            .url(url)
            .header("Accept-Encoding", "gzip, deflate");

        for (Entry<String, String> e : headers.entrySet()) {
            requestBuilder.header(e.getKey(), e.getValue());
        }

        try (Response response = mHttpClient.newCall(requestBuilder.build()).execute()) {
            int status = response.code();
            if (status != 200) {
                LOG.warning("Download failed: " + scrubbedUrl + " status=" + status);          throw new HTTPException(status);
            }

            ResponseBody body = response.body();

            // If we got a compressed entity, create the proper decompression
            // stream wrapper
            InputStream content = body.byteStream();
            String contentEncoding = response.header("Content-Encoding");
            if ("gzip".equals(contentEncoding)) {
                content = new GZIPInputStream(content);
            } else if ("deflate".equals(contentEncoding)) {
                content = new InflaterInputStream(content, new Inflater(true));
            }

            IO.copyStream(content, output);
        }
    }

    public void onFileDownloaded(long id, boolean successful, int status) {
    }

    public void finishOnHomeButton(Activity a) {
    }

    public void onActionBarWithText(MenuItem a) {
    }

    public void onActionBarWithText(SubMenu reveal) {
    }

    public View onActionBarCustom(Activity a, int id) {
        return null;
    }
}
