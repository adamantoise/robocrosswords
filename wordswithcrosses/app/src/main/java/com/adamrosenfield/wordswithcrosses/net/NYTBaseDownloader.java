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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/*
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
*/

import com.adamrosenfield.wordswithcrosses.R;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;

public abstract class NYTBaseDownloader extends AbstractDownloader {
    private static final String BASE_URL = "https://www.nytimes.com/svc/crosswords/v2/puzzle/";
    private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=https://www.nytimes.com/crosswords/index.html";

    //private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:53.0) Gecko/20100101 Firefox/53.0";

    //private HashMap<String, String> params = new HashMap<>();

    protected NYTBaseDownloader(String name, String username, String password) {
        this(BASE_URL, name, username, password);
    }

    protected NYTBaseDownloader(String baseUrl, String name, String username, String password) {
        super(baseUrl, name);
        //params.put("is_continue", "false");
        //params.put("userid", username);
        //params.put("password", password);
    }

    @Override
    protected void download(Calendar date, String urlSuffix) throws IOException {
        /*
        login();

        URL url = new URL(this.baseUrl + urlSuffix);
        LOG.info("NYT: Downloading " + url);

        HttpGet get = new HttpGet(url.toString());
        get.setHeader("User-Agent", USER_AGENT);

        HttpResponse response = utils.getHttpClient().execute(get);

        int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            LOG.warning("NYT: Download failed: " + response.getStatusLine());

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                entity.writeTo(baos);

                String resp = new String(baos.toByteArray());
                LOG.warning(resp);
            }

            throw new IOException("Download failed: status " + status);
        }

        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, getFilename(date));
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            IO.copyStream(response.getEntity().getContent(), fos);
        } finally {
            fos.close();
        }

        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, getFilename(date));
        if (!tempFile.renameTo(destFile)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destFile);
        }
        */
        throw new IOException("not implemented");
    }

    protected void login() throws IOException {
    /*
        HttpClient httpClient = utils.getHttpClient();

        HttpGet httpget = new HttpGet(LOGIN_URL);
        httpget.setHeader("User-Agent", USER_AGENT);

        LOG.info("NYT: Logging in");
        HttpResponse response = httpClient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);

            String resp = new String(baos.toByteArray());
            String tok = "name=\"token\" value=\"";
            String expires = "name=\"expires\" value=\"";
            int tokIndex = resp.indexOf(tok);

            if (tokIndex != -1) {
                params.put(
                        "token",
                        resp.substring(tokIndex + tok.length(),
                                resp.indexOf("\"", tokIndex + tok.length())));
            } else {
                LOG.warning("NYT: Failed to parse token in login page");
            }

            int expiresIndex = resp.indexOf(expires);

            if (expiresIndex != -1) {
                params.put(
                        "expires",
                        resp.substring(
                                expiresIndex + expires.length(),
                                resp.indexOf("\"",
                                        expiresIndex + expires.length())));
            } else {
                LOG.warning("NYT: Failed to parse expires in login page");
            }
        }

        HttpPost httpPost = new HttpPost(LOGIN_URL);
        httpPost.setHeader("User-Agent", USER_AGENT);

        List<NameValuePair> nvps = new ArrayList<>();

        for (Entry<String, String> e : this.params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpClient.execute(httpPost);
        entity = response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (entity != null) {
            entity.writeTo(baos);

            String resp = new String(baos.toByteArray());

            if (resp.contains("The email address and password you entered don't match an NYTimes account. Please try again.")) {
                LOG.warning("NYT: Password error");
                throw new DownloadException(R.string.login_failed);
            }
        }

        LOG.info("NYT: Logged in");
    */
        throw new IOException("Not implemented");
    }
}
