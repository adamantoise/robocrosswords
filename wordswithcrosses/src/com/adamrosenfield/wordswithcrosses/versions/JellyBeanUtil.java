package com.adamrosenfield.wordswithcrosses.versions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.adamrosenfield.wordswithcrosses.net.AbstractDownloader;

public class JellyBeanUtil extends HoneycombUtil {

    @Override
    public boolean downloadFile(URL url, Map<String, String> headers,
            File destination, boolean notification, String title) {

        // XXX
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
        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            FileOutputStream fos = new FileOutputStream(destination);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            AbstractDownloader.copyStream(entity.getContent(), baos);
            if(url.toExternalForm().indexOf("crnet") != -1){
                System.out.println(new String(baos.toByteArray()));
            }
            AbstractDownloader.copyStream(new ByteArrayInputStream(baos.toByteArray()), fos);
            fos.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
