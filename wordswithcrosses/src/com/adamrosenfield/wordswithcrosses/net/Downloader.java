package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.util.Calendar;

import android.content.Context;

public interface Downloader {

    public void setContext(Context context);

    public boolean isPuzzleAvailable(Calendar date);

    public String getName();

    public String getFilename(Calendar date);

    public boolean download(Calendar date) throws IOException;

    public String sourceUrl(Calendar date);
}
