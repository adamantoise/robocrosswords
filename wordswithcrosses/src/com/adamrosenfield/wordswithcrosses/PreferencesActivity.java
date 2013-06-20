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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deprecatedAddPreferencesFromResource(R.xml.preferences);

        Preference subscribeNyt = deprecatedFindPreference("nytSubscribe");
        subscribeNyt.setOnPreferenceClickListener(new OpenHTMLClickListener("http://www.nytimes.com/puzzle"));

        Preference morePuzzleLinks = deprecatedFindPreference("morePuzzleLinks");
        morePuzzleLinks.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/puzzle-links.html"));

        Preference release = deprecatedFindPreference("releaseNotes");
        release.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/release.html"));

        Preference license = deprecatedFindPreference("license");
        license.setOnPreferenceClickListener(new OpenHTMLClickListener("file:///android_asset/license.html"));

        Preference sourceCode = deprecatedFindPreference("sourceCode");
        sourceCode.setOnPreferenceClickListener(new OpenHTMLClickListener("http://github.com/adamantoise/wordswithcrosses"));

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
            Intent intent;

            // Open the URI in either our HTML activity for local files, or in
            // the user's browser for other URL schemes
            if (uri.getScheme().equals("file")) {
                intent = new Intent(Intent.ACTION_VIEW, uri, PreferencesActivity.this, HTMLActivity.class);
            } else {
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }

            PreferencesActivity.this.startActivity(intent);

            return true;
        }
    }
}
