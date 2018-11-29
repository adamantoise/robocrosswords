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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deprecatedAddPreferencesFromResource(R.xml.preferences);

        // FIXME: NYT temporarily removed until login via a WebView is implemented
        //deprecatedFindPreference("nytLogIn").setOnPreferenceClickListener(
        //    new OnPreferenceClickListener() {
        //        public boolean onPreferenceClick(Preference preference) {
        //            // TODO
        //            return true;
        //        }
        //    });
        //
        //deprecatedFindPreference("nytLogOut").setOnPreferenceClickListener(
        //    new OnPreferenceClickListener() {
        //        public boolean onPreferenceClick(Preference preference) {
        //            // TODO
        //            return true;
        //        }
        //    });

        setUsernameAndPasswordChangeListeners("avxwUsername", "avxwPassword", "downloadAVXW");
        setUsernameAndPasswordChangeListeners("crookedUsername", "crookedPassword", "downloadCrooked");
        setUsernameAndPasswordChangeListeners("crosswordNationUsername", "crosswordNationPassword", "downloadCrosswordNation");

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

    private void setUsernameAndPasswordChangeListeners(String usernamePrefKey, String passwordPrefKey, String downloadPrefKey) {
        setUsernameOrPasswordChangeListener(usernamePrefKey, passwordPrefKey, downloadPrefKey);
        setUsernameOrPasswordChangeListener(passwordPrefKey, usernamePrefKey, downloadPrefKey);
    }

    private void setUsernameOrPasswordChangeListener(String prefKey, String otherPrefKey, String downloadPrefKey) {
        Preference pref = deprecatedFindPreference(prefKey);
        final EditTextPreference otherPref = (EditTextPreference)deprecatedFindPreference(otherPrefKey);
        final CheckBoxPreference downloadPref = (CheckBoxPreference)deprecatedFindPreference(downloadPrefKey);

        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof String) {
                    String strValue = (String)newValue;
                    String otherValue = otherPref.getText();
                    downloadPref.setChecked(!TextUtils.isEmpty(strValue) && !TextUtils.isEmpty(otherValue));
                }

                return true;
            }
        });
    }
}
