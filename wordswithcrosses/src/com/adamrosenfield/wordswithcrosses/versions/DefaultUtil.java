package com.adamrosenfield.wordswithcrosses.versions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;

public class DefaultUtil implements AndroidVersionUtils {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    public void setContext(Context ctx) {
    }

    public boolean downloadFile(URL url, Map<String, String> headers,
            File destination, boolean notification, String title)
            throws IOException {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient
                .getParams()
                .setParameter(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:20.0) Gecko/20100101 Firefox/20.0");

        HttpGet httpget = new HttpGet(url.toString());
        for (Entry<String, String> e : headers.entrySet()) {
            httpget.setHeader(e.getKey(), e.getValue());
        }

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            IO.copyStream(entity.getContent(), fos);
        } finally {
            fos.close();
        }

        return tempFile.renameTo(destination);
    }

    public void onFileDownloaded(long id, boolean successful) {
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
