package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.util.Calendar;

import android.content.Context;


public interface Downloader {
    // These lists must be sorted for binary search.
    public static final int[] DATE_SUNDAY = new int[] { Calendar.SUNDAY };
    public static final int[] DATE_MONDAY = new int[] { Calendar.MONDAY };
    public static final int[] DATE_TUESDAY = new int[] { Calendar.TUESDAY };
    public static final int[] DATE_WEDNESDAY = new int[] { Calendar.WEDNESDAY };
    public static final int[] DATE_THURSDAY = new int[] { Calendar.THURSDAY };
    public static final int[] DATE_FRIDAY = new int[] { Calendar.FRIDAY };
    public static final int[] DATE_SATURDAY = new int[] { Calendar.SATURDAY };
    public static final int[] DATE_DAILY = new int[] {
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    };
    public static final int[] DATE_NO_SUNDAY = new int[] {
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    };

    public static final File DEFERRED_FILE = new File(".");

    public void setContext(Context context);

    public int[] getDownloadDates();

    public String getName();

    public String createFileName(Calendar date);

    public File download(Calendar date);

    public String sourceUrl(Calendar date);
}
