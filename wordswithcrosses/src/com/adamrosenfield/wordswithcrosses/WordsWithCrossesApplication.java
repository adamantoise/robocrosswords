package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.view.PlayboardRenderer;

public class WordsWithCrossesApplication extends Application {

    public static File CROSSWORDS_DIR;

    public static File TEMP_DIR;

    public static File CACHE_DIR;
    public static File DEBUG_DIR;

    private static Logger LOG;

    public static final String DEVELOPER_EMAIL = "adam@adamrosenfield.com";

    public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

    private static PuzzleDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        LOG = Logger.getLogger(getPackageName());

        File externalStorageDir = new File(
            Environment.getExternalStorageDirectory(),
            "Android/data/" + getPackageName() + "/files");

        CROSSWORDS_DIR = new File(externalStorageDir, "crosswords");

        TEMP_DIR = new File(externalStorageDir, "temp");

        CACHE_DIR = getCacheDir();
        DEBUG_DIR = new File(CACHE_DIR, "debug");

        makeDirs();

        if (DEBUG_DIR.mkdirs()) {
            File info = new File(DEBUG_DIR, "device");
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(info));
                writer.println("VERSION INT: " + android.os.Build.VERSION.SDK_INT);
                writer.println("VERSION RELEASE: " + android.os.Build.VERSION.RELEASE);
                writer.println("MODEL: " + android.os.Build.DEVICE);
                writer.println("DISPLAY: " + android.os.Build.DISPLAY);
                writer.println("MANUFACTURER: " + android.os.Build.MANUFACTURER);
                writer.close();
            } catch (Exception e) {
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

        if (!CROSSWORDS_DIR.mkdirs()) {
            LOG.warning("Failed to create directory tree: " + CROSSWORDS_DIR);
            return false;
        }

        return true;
    }

    public static Intent sendDebug() {
        File zip = new File(CACHE_DIR, "debug.stz");
        if (zip.exists()) {
            zip.delete();
        }

        if (DEBUG_DIR.exists()) {
            try {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
                zipDir(DEBUG_DIR.getAbsolutePath(), zos);
                zos.close();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[] { DEVELOPER_EMAIL });
                sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                        "Words With Crosses Debug Package");
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zip));
                sendIntent.setType("application/octet-stream");
                return sendIntent;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void zipDir(String dir2zip, ZipOutputStream zos)
        throws FileNotFoundException, IOException {
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

    public static boolean isLandscape(DisplayMetrics metrics){
        return metrics.widthPixels > metrics.heightPixels;
    }

    public static boolean isTabletish(DisplayMetrics metrics) {
        switch (android.os.Build.VERSION.SDK_INT) {
        case 12:
        case 11:
        case 13:
        case 14:
        case 15:
        case 16:
            double x = Math.pow(metrics.widthPixels/metrics.xdpi,2);
            double y = Math.pow(metrics.heightPixels/metrics.ydpi,2);
            double screenInches = Math.sqrt(x+y);
            System.out.println("SCREEN SIZE: "+(screenInches));
            if (screenInches > 9) { // look for a 9" or larger screen.
                return true;
            } else {
                return false;
            }
        default:
            return false;
        }
    }

    public static boolean isMiniTabletish(DisplayMetrics metrics) {
        switch (android.os.Build.VERSION.SDK_INT) {
        case 12:
        case 11:
        case 13:
        case 14:
        case 15:
        case 16:
            double x = Math.pow(metrics.widthPixels/metrics.xdpi,2);
            double y = Math.pow(metrics.heightPixels/metrics.ydpi,2);
            double screenInches = Math.sqrt(x+y);
            System.out.println("SCREEN SIZE: "+(screenInches));
            if (screenInches > 5.5 && screenInches <= 9) {
                return true;
            } else {
                return false;
            }
        default:
            return false;
        }
    }
}
