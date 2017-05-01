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

package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.view.PlayboardRenderer;

public class WordsWithCrossesApplication extends Application {

    public static File CROSSWORDS_DIR;
    public static File NON_CROSSWORD_DATA_DIR;
    public static File TEMP_DIR;
    public static File QUARANTINE_DIR;

    public static File CACHE_DIR;
    public static File DEBUG_DIR;

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    public static final String DEVELOPER_EMAIL = "robocrosswords@adamrosenfield.com";

    private static final String PREFERENCES_VERSION_PREF = "preferencesVersion";
    private static final int PREFERENCES_VERSION = 5;

    private static Context mContext;

    public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

    private static PuzzleDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        // Check preferences version and perform any upgrades if necessary
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int prefsVersion = prefs.getInt(PREFERENCES_VERSION_PREF, 0);
        if (prefsVersion != PREFERENCES_VERSION) {
            migratePreferences(prefs, prefsVersion);
        }

        File externalStorageDir = new File(
            Environment.getExternalStorageDirectory(),
            "Android/data/" + getPackageName() + "/files");

        CROSSWORDS_DIR = new File(externalStorageDir, "crosswords");
        NON_CROSSWORD_DATA_DIR = new File(externalStorageDir, "data");
        TEMP_DIR = new File(externalStorageDir, "temp");
        QUARANTINE_DIR = new File(externalStorageDir, "quarantine");

        CACHE_DIR = getCacheDir();
        DEBUG_DIR = new File(CACHE_DIR, "debug");

        makeDirs();

        if (DEBUG_DIR.isDirectory() || DEBUG_DIR.mkdirs()) {
            File infoFile = new File(DEBUG_DIR, "device.txt");
            try {
                PrintWriter writer = new PrintWriter(infoFile);
                try {
                    writer.println("VERSION INT: " + android.os.Build.VERSION.SDK_INT);
                    writer.println("VERSION RELEASE: " + android.os.Build.VERSION.RELEASE);
                    writer.println("MODEL: " + android.os.Build.MODEL);
                    writer.println("DEVICE: " + android.os.Build.DEVICE);
                    writer.println("DISPLAY: " + android.os.Build.DISPLAY);
                    writer.println("MANUFACTURER: " + android.os.Build.MANUFACTURER);
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.warning("Failed to create directory tree: " + DEBUG_DIR);
        }

        dbHelper = new PuzzleDatabaseHelper(this);
    }

    public static Context getContext() {
        return mContext;
    }

    private void migratePreferences(SharedPreferences prefs, int prefsVersion) {
        LOG.info("Upgrading preferences from version " + prefsVersion + " to to version " + PREFERENCES_VERSION);

        SharedPreferences.Editor editor = prefs.edit();

        switch (prefsVersion) {
        case 0:
            editor.putBoolean("enableIndividualDownloadNotifications", !prefs.getBoolean("suppressMessages", false));
            // Fall-through
        case 1:
            editor.putBoolean("showRevealedLetters", !prefs.getBoolean("suppressHints", false));
            // Fall-through
        case 2:
            editor.putString("showKeyboard",
                             prefs.getBoolean("forceKeyboard", false) ? "SHOW" : "AUTO");
            // Fall-through
        case 3:
            try {
                // This is ugly.  But I don't see a clean way of detecting
                // what data type a preference is.
                int clueSize = prefs.getInt("clueSize", 12);
                editor.putString("clueSize", Integer.toString(clueSize));
            } catch (ClassCastException e) {
                // Ignore
            }
            // Fall-through
        case 4:
            editor.putBoolean("downloadNYTBonus", prefs.getBoolean("downloadNYT", false));
            // Fall-through
        }

        editor.putInt(PREFERENCES_VERSION_PREF, PREFERENCES_VERSION);
        editor.commit();
    }

    public static boolean makeDirs() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        for (File dir : new File[]{CROSSWORDS_DIR, NON_CROSSWORD_DATA_DIR, TEMP_DIR, QUARANTINE_DIR, DEBUG_DIR}) {
            if (!dir.isDirectory() && !dir.mkdirs()) {
                LOG.warning("Failed to create directory tree: " + dir);
                return false;
            }
        }

        return true;
    }

    public static Intent sendDebug(Context context) {
        String filename = "debug.zip";
        File zipFile = new File(context.getFilesDir(), filename);
        if (zipFile.exists()) {
            zipFile.delete();
        }

        if (!DEBUG_DIR.exists()) {
            LOG.warning("Can't send debug package, " + DEBUG_DIR + " doesn't exist");
            return null;
        }

        saveLogFile();

        try {
            @SuppressLint("WorldReadableFiles")
            @SuppressWarnings("deprecation")
            ZipOutputStream zos = new ZipOutputStream(
                context.openFileOutput(filename, MODE_WORLD_READABLE));
            try {
                zipDir(DEBUG_DIR.getAbsolutePath(), zos);
            } finally {
                zos.close();
            }

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { DEVELOPER_EMAIL });
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Robo Crosswords Debug Package");
            Uri uri = Uri.fromFile(zipFile);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            LOG.info("Sending debug info: " + uri);
            sendIntent.setType("application/octet-stream");
            return sendIntent;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveLogFile() {
        try {
            // Use logcat to copy the log file into the debug directory
            File logFile = new File(DEBUG_DIR, "wordswithcrosses.log");
            FileOutputStream fos = new FileOutputStream(logFile);
            try {
                Process process = Runtime.getRuntime().exec("logcat -d");
                IO.copyStream(process.getInputStream(), fos);
                process.waitFor();
            } finally {
                fos.close();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void zipDir(String dir2zip, ZipOutputStream zos)
        throws IOException {
        File zipDir = new File(dir2zip);
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[4096];
        for (String filename : dirList) {
            File f = new File(zipDir, filename);
            if (f.isDirectory()) {
                String filePath = f.getPath();
                zipDir(filePath, zos);
                continue;
            }
            FileInputStream fis = new FileInputStream(f);

            ZipEntry anEntry = new ZipEntry(f.getPath());
            zos.putNextEntry(anEntry);
            int bytesIn;
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            fis.close();
        }
    }

    public static PuzzleDatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public static double getScreenSizeInInches(DisplayMetrics metrics) {
        double x = metrics.widthPixels/metrics.xdpi;
        double y = metrics.heightPixels/metrics.ydpi;
        return Math.hypot(x, y);
    }

    public static boolean isTabletish(DisplayMetrics metrics) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            return false;
        }

        double screenInches = getScreenSizeInInches(metrics);
        return (screenInches > 9.0);  // look for a 9" or larger screen.
    }
}
