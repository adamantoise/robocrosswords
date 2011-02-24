package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;

import com.totsp.crossword.puz.PuzzleMeta;


public interface AndroidVersionUtils {
	
	public void storeMetas(Uri uri, PuzzleMeta meta);
	
    public void setContext(Context ctx);

    public boolean downloadFile(URL url, File destination, Map<String, String> headers, boolean notification,
        String title);

    public void finishOnHomeButton(Activity a);

    public void holographic(Activity playActivity);

    public void onActionBarWithText(MenuItem a);

    public void onActionBarWithText(SubMenu reveal);

    public static class Factory {
        public static AndroidVersionUtils getInstance() {
            System.out.println("Creating utils for version: " + android.os.Build.VERSION.SDK_INT);

            try {
                switch (android.os.Build.VERSION.SDK_INT) {
                case 11:
                    return (AndroidVersionUtils) Class.forName("com.totsp.crossword.versions.HoneycombUtil")
                                                      .newInstance();
                case 10:
                case 9:
                	System.out.println("Using Gingerbread.");
                    return (AndroidVersionUtils) Class.forName("com.totsp.crossword.versions.GingerbreadUtil")
                                                      .newInstance();

                default:
                    return new DefaultUtil();
                }
            } catch (Exception e) {
                return new DefaultUtil();
            }
        }
    }
}
