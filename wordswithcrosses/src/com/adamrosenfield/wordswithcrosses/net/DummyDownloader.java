package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.util.Calendar;

import android.content.Context;

/**
 * Does not actually download any puzzles; just adds an "All Available" option to the dropdown.
 */
public class DummyDownloader implements Downloader {
    public void setContext(Context context) {
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return false;
    }

    public String getName() {
        return null;
    }

    public String getFilename(Calendar date) {
        return null;
    }

    public boolean download(Calendar date) throws IOException {
        return false;
    }

    public String sourceUrl(Calendar date) {
        return null;
    }

    @Override
    public String toString() {
        return "All available";
    }
}
