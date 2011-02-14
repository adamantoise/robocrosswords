package com.totsp.crossword;

import android.app.Application;

import android.os.Environment;

import com.totsp.crossword.io.IO;

import java.io.File;


public class ShortyzApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            IO.TEMP_FOLDER = new File(Environment.getExternalStorageDirectory(), "crossword/temp");
            IO.TEMP_FOLDER.mkdirs();
        }
    }
}
