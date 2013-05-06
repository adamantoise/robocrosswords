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
    protected boolean download(Calendar date, String urlSuffix) {
        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            HttpClient client = this.login();

            HttpGet fetchIndex = new HttpGet(
                    "http://select.nytimes.com/premium/xword/puzzles.html");
            HttpResponse indexResponse = client.execute(fetchIndex);
            AbstractDownloader.copyStream(indexResponse.getEntity()
                    .getContent(),
                    new FileOutputStream(downloadDirectory.getAbsolutePath()
                            + "/debug/xword-puzzles.html"));

            HttpGet get = new HttpGet(url.toString());
            get.addHeader("Referer",
                    "http://select.nytimes.com/premium/xword/puzzles.html");

            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                File f = new File(downloadDirectory, this.getFilename(date));
                FileOutputStream fos = new FileOutputStream(f);
                AbstractDownloader.copyStream(
                        response.getEntity().getContent(), fos);
                fos.close();

                AbstractDownloader.copyStream(
                        new FileInputStream(f),
                        new FileOutputStream(downloadDirectory
                                .getAbsolutePath() + "/debug/debug.puz"));

                return true;
            } else {
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private HttpClient login() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient
                .getParams()
                .setParameter(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");

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
            copyStream(
                    new ByteArrayInputStream(baos.toByteArray()),
                    new FileOutputStream(this.downloadDirectory
                            .getAbsolutePath() + "/debug/authresp.html"));

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
