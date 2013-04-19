package com.totsp.crossword;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

import com.totsp.crossword.versions.AndroidVersionUtils;

public class ShortyzActivity extends Activity {
	protected AndroidVersionUtils utils = AndroidVersionUtils.Factory
			.getInstance();
	protected SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}
		StatFs stats = new StatFs(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		System.out.println("Avail blocks" + stats.getAvailableBlocks());
		System.out.println("BLock size " + stats.getBlockSize());
		System.out.println("Bytes Free" + (long) stats.getAvailableBlocks()
				* (long) stats.getBlockSize());
		System.out.println("Megs free "+ (((long) stats.getAvailableBlocks()
				* (long) stats.getBlockSize())/1024L/1024L));
		
		if ( (long) stats.getAvailableBlocks() * (long) stats.getBlockSize() < 1024L * 1024L) {
			showSDCardFull();
			finish();

			return;
		}
		doOrientation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}
		doOrientation();
	}

	protected void showSDCardFull() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("file:///android_asset/sdcard-full.html"), this,
				HTMLActivity.class);
		this.startActivity(i);
	}

	protected void showSDCardHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("file:///android_asset/sdcard.html"), this,
				HTMLActivity.class);
		this.startActivity(i);
	}

	private void doOrientation() {
		if ("PORT".equals(prefs.getString("orientationLock", "UNLOCKED"))) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if ("LAND"
				.equals(prefs.getString("orientationLock", "UNLOCKED"))) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}
}
