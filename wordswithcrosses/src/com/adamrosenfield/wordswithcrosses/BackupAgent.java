package com.adamrosenfield.wordswithcrosses;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

@TargetApi(8)
public class BackupAgent extends BackupAgentHelper {
    static final String PREFS = "com.adamrosenfield.wordswithcrosses.wordswithcrosses_preferences";
    static final String PREFS_BACKUP_KEY = "prefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
