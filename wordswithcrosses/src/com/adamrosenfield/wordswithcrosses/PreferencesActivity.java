package com.adamrosenfield.wordswithcrosses;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.adamrosenfield.wordswithcrosses.wordswithcrosses.R;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deprecatedAddPreferencesFromResource(R.xml.preferences);

        Preference morePuzzleLinks = deprecatedFindPreference("morePuzzleLinks");
        morePuzzleLinks.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/puzzle-links.html"));

        Preference release = deprecatedFindPreference("releaseNotes");
        release.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/release.html"));

        Preference license = deprecatedFindPreference("license");
        license.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/license.html"));

        Preference subscribeNyt = deprecatedFindPreference("nytSubscribe");
        subscribeNyt.setOnPreferenceClickListener(new OpenHTMLClickListener("http://www.nytimes.com/puzzle"));

        Preference sendDebug = deprecatedFindPreference("sendDebug");
        sendDebug.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference preference) {
				startActivity(WordsWithCrossesApplication.sendDebug(PreferencesActivity.this));
				return true;
			}
        });
    }

    @SuppressWarnings("deprecation")
    private void deprecatedAddPreferencesFromResource(int resource) {
        addPreferencesFromResource(resource);
    }

    @SuppressWarnings("deprecation")
    private Preference deprecatedFindPreference(String preference) {
        return findPreference(preference);
    }

    private class OpenHTMLClickListener implements OnPreferenceClickListener {

        private String url;

        public OpenHTMLClickListener(String url) {
            this.url = url;
        }

        public boolean onPreferenceClick(Preference preference) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri,
                PreferencesActivity.this, HTMLActivity.class);
            PreferencesActivity.this.startActivity(intent);

            return true;
        }
    }
}
