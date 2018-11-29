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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import okhttp3.OkHttpClient;

public interface AndroidVersionUtils {

    public void setContext(Context context);

    public OkHttpClient getHttpClient();

    public void downloadFile(URL url, Map<String, String> headers, File destination, boolean notification, String title) throws IOException;

    public String downloadToString(URL url, Map<String, String> headers) throws IOException;

    public void onFileDownloaded(long id, boolean successful, int status);

    public void finishOnHomeButton(Activity a);

    public void onActionBarWithText(MenuItem a);

    public void onActionBarWithText(SubMenu reveal);

    public static class Factory {
        private static AndroidVersionUtils INSTANCE;

        public static AndroidVersionUtils getInstance() {
            if (INSTANCE != null){
                return INSTANCE;
            }

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
}
