package com.totsp.crossword.shortyz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.view.PlayboardRenderer;

public class ShortyzApplication extends Application {

	public static final boolean DEBUG = false;
	public static File DEBUG_DIR;
	public static File CROSSWORDS = new File(
			Environment.getExternalStorageDirectory(), "crosswords");

	@Override
	public void onCreate() {
		super.onCreate();

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			IO.TEMP_FOLDER = new File(CROSSWORDS, "temp");
			IO.TEMP_FOLDER.mkdirs();
		}
		if (DEBUG) {
			DEBUG_DIR = new File(CROSSWORDS, "debug");
			DEBUG_DIR.mkdirs();
			File info = new File(DEBUG_DIR, "device");
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(info));
				writer.println("VERSION INT: "
						+ android.os.Build.VERSION.SDK_INT);
				writer.println("VERSION STRING: "
						+ android.os.Build.VERSION.SDK);
				writer.println("VERSION RELEASE: "
						+ android.os.Build.VERSION.RELEASE);
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
	}
	
	
	public static Playboard BOARD;
    public static PlayboardRenderer RENDERER;

	public static Intent sendDebug() {
		File zip = new File(CROSSWORDS, "debug.stz");
		File debug = new File(CROSSWORDS, "debug");
		if (zip.exists()) {
			zip.delete();
		}

		if (debug.exists())
			try {
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
						zip));
				zipDir(debug.getAbsolutePath(), zos);
				zos.close();
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { "kebernet@gmail.com" });
				sendIntent.putExtra(Intent.EXTRA_SUBJECT,
						"Shortyz Debug Package");
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zip));
				sendIntent.setType("application/octet-stream");
				return sendIntent;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		return null;
	}

	public static void zipDir(String dir2zip, ZipOutputStream zos) {
		try {
			File zipDir = new File(dir2zip);
			String[] dirList = zipDir.list();
			byte[] readBuffer = new byte[2156];
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isTabletish(DisplayMetrics metrics) {
		switch (android.os.Build.VERSION.SDK_INT) {
		case 12:
		case 11:
		case 13:
			return true;
		case 14:
			int pixels = metrics.heightPixels > metrics.widthPixels ? metrics.heightPixels : metrics.widthPixels;
			float dpi = metrics.xdpi > metrics.ydpi ? metrics.xdpi : metrics.ydpi;
			if(pixels / dpi > 5){ // look for a 5" or larger screen.
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}
	}
}
