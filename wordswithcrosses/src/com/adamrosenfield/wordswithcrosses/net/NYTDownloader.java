package com.adamrosenfield.wordswithcrosses.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;

/**
 * New York Times URL: http://www.nytimes.com/premium/xword/YYYY/MM/DD/[Mon]DDYY.puz
 * Date = Daily
 */
public class NYTDownloader extends AbstractDownloader {
    private static final String[] MONTHS = new String[] { "Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    public static final String NAME = "New York Times";
    private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=http://select.nytimes.com/premium/xword/puzzles.html";
    NumberFormat nf = NumberFormat.getInstance();
    private Context context;
    private Handler handler;
    private HashMap<String, String> params = new HashMap<String, String>();

    protected NYTDownloader(Context context, Handler handler, String username, String password) {
        super("http://www.nytimes.com/premium/xword/", NAME);
        this.context = context;
        this.handler = handler;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        params.put("is_continue", "false");
        params.put("userid", username);
        params.put("password", password);
    }

    public int[] getDownloadDates() {
        return DATE_DAILY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) + "/" +
                this.nf.format(date.get(Calendar.MONTH) + 1) +
                "/" +
                this.nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                "/" +
                MONTHS[date.get(Calendar.MONTH)] +
                this.nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                this.nf.format(date.get(Calendar.YEAR) % 100) +
                ".puz");
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix) throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        HttpClient client = this.login();
        if (client == null) {
            return false;
        }

        LOG.info("NYT: Downloading " + url);
        HttpGet get = new HttpGet(url.toString());
        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() != 200) {
            LOG.warning("NYT: Download failed: " + response.getStatusLine());
            return false;
        }

        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, getFilename(date));
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            IO.copyStream(response.getEntity().getContent(), fos);
        } finally {
            fos.close();
        }

        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, getFilename(date));
        return tempFile.renameTo(destFile);
    }

    private HttpClient login() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient
                .getParams()
                .setParameter(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:20.0) Gecko/20100101 Firefox/20.0");

        HttpGet httpget = new HttpGet(LOGIN_URL);

        LOG.info("NYT: Logging in");
        HttpResponse response = httpclient.execute(httpget);
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

        HttpPost httpost = new HttpPost(LOGIN_URL);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        for (Entry<String, String> e : this.params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (entity != null) {
            entity.writeTo(baos);

            String resp = new String(baos.toByteArray());

            if (resp.indexOf("Log in to manage") != -1) {
                LOG.warning("NYT: Password error");
                this.handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(
                                context,
                                "New York Times login failure. Is your password correct?",
                                Toast.LENGTH_LONG).show();
                    }
                });

                return null;
            }
        }

        LOG.info("NYT: Logged in");

        return httpclient;
    }
}
