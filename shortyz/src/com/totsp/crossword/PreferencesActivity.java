package com.totsp.crossword;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import com.totsp.crossword.shortyz.R;


public class PreferencesActivity extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference release = (Preference) findPreference("releaseNotes");
        release.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference arg0) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"), PreferencesActivity.this,
		                HTMLActivity.class);
				PreferencesActivity.this.startActivity(i);
				return true;
			}
        	
        });
    }
}
