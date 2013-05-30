package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.view.PlayboardRenderer;

public class WordsWithCrossesApplication extends Application {

    public static File CROSSWORDS_DIR;
    public static File TEMP_DIR;
    public static File QUARANTINE_DIR;

    public static File CACHE_DIR;
    public static File DEBUG_DIR;

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    public static final String DEVELOPER_EMAIL = "wordswithcrosses@adamrosenfield.com";

    public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

    private static PuzzleDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        File externalStorageDir = new File(
            Environment.getExternalStorageDirectory(),
            "Android/data/" + getPackageName() + "/files");

        CROSSWORDS_DIR = new File(externalStorageDir, "crosswords");
        TEMP_DIR = new File(externalStorageDir, "temp");
        QUARANTINE_DIR = new File(externalStorageDir, "quarantine");

        CACHE_DIR = getCacheDir();
        DEBUG_DIR = new File(CACHE_DIR, "debug");

        makeDirs();

        if (DEBUG_DIR.isDirectory() || DEBUG_DIR.mkdirs()) {
            File infoFile = new File(DEBUG_DIR, "device");
            try {
                PrintWriter writer = new PrintWriter(infoFile);
                try {
                    writer.println("VERSION INT: " + android.os.Build.VERSION.SDK_INT);
                    writer.println("VERSION RELEASE: " + android.os.Build.VERSION.RELEASE);
                    writer.println("MODEL: " + android.os.Build.DEVICE);
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

    public static boolean makeDirs() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        for (File dir : new File[]{CROSSWORDS_DIR, TEMP_DIR, QUARANTINE_DIR, DEBUG_DIR}) {
            if (!dir.isDirectory() && !dir.mkdirs()) {
                LOG.warning("Failed to create directory tree: " + dir);
                return false;
            }
        }

        return true;
    }

    @SuppressLint("WorldReadableFiles")
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

        try {
            ZipOutputStream zos = new ZipOutputStream(
                context.openFileOutput(filename, MODE_WORLD_READABLE));
            try {
                zipDir(DEBUG_DIR.getAbsolutePath(), zos);
            } finally {
                zos.close();
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[] { DEVELOPER_EMAIL });
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    "Words With Crosses Debug Package");
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

    public static void zipDir(String dir2zip, ZipOutputStream zos)
        throws IOException {
        File zipDir = new File(dir2zip);
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[4096];
        int bytesIn = 0;
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                String filePath = f.getPath();
                zipDir(filePath, zos);
                continue;
            }
            FileInputStream fis = new FileInputStream(f);

            ZipEntry anEntry = new ZipEntry(f.getPath());
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            fis.close();
        }
    }

    public static PuzzleDatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }

    public static boolean isLandscape(DisplayMetrics metrics) {
        return metrics.widthPixels > metrics.heightPixels;
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

    public static boolean isMiniTabletish(DisplayMetrics metrics) {
        if (android.os.Build.VERSION.SDK_INT < 11) {
            return false;
        }

        double screenInches = getScreenSizeInInches(metrics);
        return (screenInches > 5.5 && screenInches <= 9.0);
    }
}
