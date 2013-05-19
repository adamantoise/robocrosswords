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

        Preference release = deprecatedFindPreference("releaseNotes");
        release.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/release.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference license = deprecatedFindPreference("license");
        license.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/license.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference subscribeNyt = deprecatedFindPreference("nytSubscribe");
        subscribeNyt.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com/puzzle"));
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

        Preference scrapeInfo = deprecatedFindPreference("aboutScrapes");
        scrapeInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/scrapes.html"),
                            PreferencesActivity.this, HTMLActivity.class);
                    PreferencesActivity.this.startActivity(i);

                    return true;
                }
            });

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
}
