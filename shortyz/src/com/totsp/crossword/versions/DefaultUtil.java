package com.totsp.crossword.versions;

import android.app.Activity;

import android.content.Context;

import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;

import com.totsp.crossword.net.AbstractDownloader;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Map;
import java.util.Map.Entry;


public class DefaultUtil implements AndroidVersionUtils {
    public void setContext(Context ctx) {
        // TODO Auto-generated method stub
    }

    public boolean downloadFile(URL url, File destination, Map<String, String> headers, boolean notification,
        String title) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");

            for (Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            System.out.println("Response : " + connection.getResponseCode());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                FileOutputStream fos = new FileOutputStream(destination);
                AbstractDownloader.copyStream(connection.getInputStream(), fos);
                fos.close();

                return true;
            } else {
                throw new RuntimeException();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void finishOnHomeButton(Activity a) {
        // TODO Auto-generated method stub
    }

    public void holographic(Activity playActivity) {
        // TODO Auto-generated method stub
    }

    public void onActionBarWithText(MenuItem a) {
        // TODO Auto-generated method stub
    }

    public void onActionBarWithText(SubMenu reveal) {
        // TODO Auto-generated method stub
    }

	public void storeMetas(Uri uri, PuzzleMeta meta) {
		// TODO Auto-generated method stub
		
	}
}
