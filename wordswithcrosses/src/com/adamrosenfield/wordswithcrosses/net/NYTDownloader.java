package com.adamrosenfield.wordswithcrosses.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.io.IO;

/**
 * New York Times URL: http://select.nytimes.com/premium/xword/[Mon]DDYY.puz
 * Date = Daily
 */
public class NYTDownloader extends AbstractDownloader {
    private static final String[] MONTHS = new String[] { "Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    public static final String NAME = "New York Times";
    private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=http://select.nytimes.com/premium/xword/puzzles.html";
    NumberFormat nf = NumberFormat.getInstance();
    private Context context;
    private Handler handler = new Handler();
    private HashMap<String, String> params = new HashMap<String, String>();

    protected NYTDownloader(Context context, String username, String password) {
        super("http://select.nytimes.com/premium/xword/", DOWNLOAD_DIR, NAME);
        this.context = context;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        params.put("is_continue", "true");
        params.put("SAVEOPTION", "YES");
        params.put("URI",
                "http://select.nytimes.com/premium/xword/puzzles.html");
        params.put("OQ", "");
        params.put("OP", "");
        params.put("userid", username);
        params.put("password", password);
        params.put("USERID", username);
        params.put("PASSWORD", password);
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
        URL url;
        try {
            url = new URL(this.baseUrl + urlSuffix);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        HttpClient client = this.login();

        HttpGet fetchIndex = new HttpGet("http://select.nytimes.com/premium/xword/puzzles.html");
        HttpResponse indexResponse = client.execute(fetchIndex);

        FileOutputStream fos = new FileOutputStream(downloadDirectory.getAbsolutePath()
                + "/debug/xword-puzzles.html");
        try {
            IO.copyStream(indexResponse.getEntity().getContent(), fos);
        } finally {
            fos.close();
        }

        HttpGet get = new HttpGet(url.toString());
        get.addHeader("Referer",
                "http://select.nytimes.com/premium/xword/puzzles.html");

        HttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() != 200) {
            return false;
        }

        File f = new File(downloadDirectory, this.getFilename(date));
        fos = new FileOutputStream(f);
        try {
            IO.copyStream(response.getEntity().getContent(), fos);
        } finally {
            fos.close();
        }

        fos = new FileOutputStream(downloadDirectory.getAbsolutePath() + "/debug/debug.puz");
        try {
            IO.copyStream(new FileInputStream(f), fos);
        } finally {
            fos.close();
        }

        return true;
    }

    private HttpClient login() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient
                .getParams()
                .setParameter(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:20.0) Gecko/20100101 Firefox/20.0");

        HttpGet httpget = new HttpGet(LOGIN_URL);

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());

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
                System.out.println("Got token: " + params.get("token"));
            }

            int expiresIndex = resp.indexOf(expires);

            if (expiresIndex != -1) {
                params.put(
                        "expires",
                        resp.substring(
                                expiresIndex + expires.length(),
                                resp.indexOf("\"",
                                        expiresIndex + expires.length())));
                System.out.println("Got expires: " + params.get("expires"));
            }
        }

        System.out.println("Initial set of cookies:");

        List<Cookie> cookies = httpclient.getCookieStore().getCookies();

        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        HttpPost httpost = new HttpPost(LOGIN_URL);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        for (Entry<String, String> e : this.params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
            System.out.println(e.getKey() + "=" + e.getValue());
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (entity != null) {
            entity.writeTo(baos);

            new File(this.downloadDirectory, "debug/").mkdirs();

            FileOutputStream fos = new FileOutputStream(
                    this.downloadDirectory.getAbsolutePath() + "/debug/authresp.html");
            try {
                IO.copyStream(new ByteArrayInputStream(baos.toByteArray()), fos);
            } finally {
                fos.close();
            }

            String resp = new String(baos.toByteArray());

            if (resp.indexOf("Log in to manage") != -1) {
                System.out.println("=================== Password error\n"
                        + resp);
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

        System.out.println("Post logon cookies:");
        cookies = httpclient.getCookieStore().getCookies();

        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        return httpclient;
    }
}
