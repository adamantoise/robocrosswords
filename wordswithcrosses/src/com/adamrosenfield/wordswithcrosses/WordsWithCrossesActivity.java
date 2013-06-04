package com.adamrosenfield.wordswithcrosses;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;

import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

public abstract class WordsWithCrossesActivity extends Activity {
	protected AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();
	protected SharedPreferences prefs;

	private boolean useUserOrientation = true;

	protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    // Preference key for the time of the last database sync
    protected static final String PREF_LAST_DB_SYNC_TIME = "last_db_sync_time";

    public WordsWithCrossesActivity() {
        // No-op
    }

    public WordsWithCrossesActivity(boolean useUserOrientation) {
        this.useUserOrientation = useUserOrientation;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}

		StatFs stats = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		long bytesFree = (long)stats.getAvailableBlocks() * (long)stats.getBlockSize();
		//LOG.info("Avail blocks: " + stats.getAvailableBlocks());
		//LOG.info("Block size: " + stats.getBlockSize());
		//LOG.info("Bytes free: " + bytesFree);

		if (bytesFree < 1024L * 1024L) {
			showSDCardFull();
			finish();

			return;
		}

		if (useUserOrientation) {
		    doOrientation();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			showSDCardHelp();
			finish();

			return;
		}

	    if (useUserOrientation) {
	        doOrientation();
	    }
	}

	protected void showHTMLPage(String pageName) {
	    Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("file:///android_asset/" + pageName), this,
                HTMLActivity.class);
        this.startActivity(i);
	}

	protected void showSDCardFull() {
	    showHTMLPage("sdcard-full.html");
	}

	protected void showSDCardHelp() {
	    showHTMLPage("sdcard.html");
	}

	protected void showWelcomePage() {
	    showHTMLPage("welcome.html");
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

	public SharedPreferences getPrefs()
	{
	    return prefs;
	}

    /**
     * Updates our record of the last time we synced the database with the
     * file system
     */
    public void updateLastDatabaseSyncTime() {
        long folderTimestamp = WordsWithCrossesApplication.CROSSWORDS_DIR.lastModified();
        Editor e = prefs.edit();
        e.putLong(PREF_LAST_DB_SYNC_TIME, folderTimestamp);
        e.commit();
    }
}
