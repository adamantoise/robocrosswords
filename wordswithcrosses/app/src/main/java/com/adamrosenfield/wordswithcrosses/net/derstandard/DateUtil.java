package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;

public final class DateUtil {
    private DateUtil() {throw new RuntimeException(); }
    
    /* taken from http://stackoverflow.com/a/8912994 */
    public static int daysBetween(Calendar startDate, Calendar endDate) {
        boolean negative = false;
        if (startDate.after(endDate)) {
            Calendar c = startDate;
            startDate = endDate;
            endDate = c;
            negative = true;
        }

        int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
        long endInstant = endDate.getTimeInMillis();
        int presumedDays = (int) ((endInstant - startDate.getTimeInMillis()) / MILLIS_IN_DAY);
        Calendar cursor = (Calendar) startDate.clone();
        cursor.add(Calendar.DAY_OF_YEAR, presumedDays);
        long instant = cursor.getTimeInMillis();
        if (instant == endInstant)
            return presumedDays;

        final int step = instant < endInstant ? 1 : -1;
        do {
            cursor.add(Calendar.DAY_OF_MONTH, step);
            presumedDays += step;
        } while (cursor.getTimeInMillis() != endInstant);
        
        if (negative) {
            presumedDays = -presumedDays;
        }
        
        return presumedDays;
    }
}
