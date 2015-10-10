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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

public class DefaultUtil implements AndroidVersionUtils {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected Context context;
    protected SharedPreferences prefs;

    protected HttpParams mHttpParams;
    protected DefaultHttpClient mHttpClient;

    public DefaultUtil() {
        // Set default connect and recv timeouts to 30 seconds
        mHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(mHttpParams, 30000);
        HttpConnectionParams.setSoTimeout(mHttpParams, 30000);

        mHttpClient = new DefaultHttpClient(mHttpParams);
    }

    public void setContext(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    public void downloadFile(URL url, Map<String, String> headers, File destination, boolean notification, String title) throws IOException {
        downloadFile(url, headers, destination, notification, title, null);
    }

    public void downloadFile(URL url, Map<String, String> headers, File destination, boolean notification, String title, HttpContext httpContext) throws IOException {
        String scrubbedUrl = AbstractDownloader.scrubUrl(url);
        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());
        LOG.info("DefaultUtil: Downloading " + scrubbedUrl + " ==> " + tempFile);
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            downloadHelper(url, scrubbedUrl, headers, httpContext, fos);
        } finally {
            fos.close();
        }

        if (!tempFile.equals(destination) && !tempFile.renameTo(destination)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destination);
        }

        LOG.info("DefaultUtil: Download succeeded: " + scrubbedUrl);
    }

    public String downloadToString(URL url, Map<String, String> headers) throws IOException {
        return downloadToString(url, headers, null);
    }

    public String downloadToString(URL url, Map<String, String> headers, HttpContext httpContext) throws IOException {
        String scrubbedUrl = AbstractDownloader.scrubUrl(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        downloadHelper(url, scrubbedUrl, headers, httpContext, baos);

        return new String(baos.toByteArray());
    }

    private void downloadHelper(URL url, String scrubbedUrl, Map<String, String> headers, HttpContext httpContext, OutputStream output) throws IOException {
        HttpGet httpget;
        try {
            httpget = new HttpGet(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException("Invalid URL: " + url);
        }

        httpget.setHeader("Accept-Encoding", "gzip, deflate");
        for (Entry<String, String> e : headers.entrySet()) {
            httpget.setHeader(e.getKey(), e.getValue());
        }

        HttpResponse response = mHttpClient.execute(httpget, httpContext);

        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (status != 200) {
            LOG.warning("Download failed: " + scrubbedUrl + " status=" + status);
            if (entity != null) {
                entity.consumeContent();
            }

            throw new HTTPException(status);
        }

        if (entity != null) {
            // If we got a compressed entity, create the proper decompression
            // stream wrapper
            InputStream content = entity.getContent();
            Header contentEncoding = entity.getContentEncoding();
            if (contentEncoding != null) {
                if ("gzip".equals(contentEncoding.getValue())) {
                    content = new GZIPInputStream(content);
                } else if ("deflate".equals(contentEncoding.getValue())) {
                    content = new InflaterInputStream(content, new Inflater(true));
                }
            }

            try {
                IO.copyStream(content, output);
            } finally {
                entity.consumeContent();
            }
        }
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
}
