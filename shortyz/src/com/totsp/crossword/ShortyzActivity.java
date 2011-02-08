package com.totsp.crossword;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.totsp.crossword.versions.AndroidVersionUtils;

public class ShortyzActivity extends Activity {

	protected SharedPreferences prefs;
	protected AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();
	
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
		doOrientation();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		doOrientation();
	}
	
	private void doOrientation(){
		if("PORT".equals(prefs.getString("orientationLock", "UNLOCKED"))){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if("LAND".equals(prefs.getString("orientationLock", "UNLOCKED"))){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}
	
	protected void showSDCardHelp() {
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("file:///android_asset/sdcard.html"), this,
                HTMLActivity.class);
        this.startActivity(i);
    }

}
