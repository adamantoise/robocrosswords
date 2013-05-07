package com.adamrosenfield.wordswithcrosses.versions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public interface AndroidVersionUtils {

    public void setContext(Context ctx);

    public boolean downloadFile(URL url, Map<String, String> headers,
            File destination, boolean notification, String title)
            throws IOException;

    public void onFileDownloaded(long id, boolean successful);

    public void finishOnHomeButton(Activity a);

    public void holographic(Activity playActivity);

    public void onActionBarWithText(MenuItem a);

    public void onActionBarWithText(SubMenu reveal);

    public static class Factory {
        private static AndroidVersionUtils INSTANCE;

        public static AndroidVersionUtils getInstance() {
            if (INSTANCE != null){
                return INSTANCE;
            }
            System.out.println("Creating utils for version: " + android.os.Build.VERSION.SDK_INT);

            try {
                int version = android.os.Build.VERSION.SDK_INT;
                if (version < 9) {
                    return INSTANCE = new DefaultUtil();
                } else if (version < 11) {
                    return INSTANCE = (AndroidVersionUtils)Class.forName(
                            "com.adamrosenfield.wordswithcrosses.versions.GingerbreadUtil")
                            .newInstance();
                } else {
                    return INSTANCE = (AndroidVersionUtils)Class.forName(
                            "com.adamrosenfield.wordswithcrosses.versions.HoneycombUtil")
                            .newInstance();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return INSTANCE = new DefaultUtil();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return INSTANCE = new DefaultUtil();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return INSTANCE = new DefaultUtil();
            }
        }
    }

    public View onActionBarCustom(Activity a, int id);

    public void hideWindowTitle(Activity a);

    public void hideActionBar(Activity a);
}
