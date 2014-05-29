package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.CalendarUtil;
import com.adamrosenfield.wordswithcrosses.net.DerStandardDownloader;

public class DateToIdConverter {
    private final int idZero ;
    private final Calendar dateZero;

    public DateToIdConverter(int idZero, Calendar dateZero) {
        this.idZero = idZero;
        this.dateZero = dateZero;
    }

    public int getId(Calendar date) {
        int daysDifference = daysBetween(dateZero, date);
        return idZero + daysDifference;
    }

    /* taken from http://stackoverflow.com/a/8912994 */
    private static int daysBetween(Calendar startDate, Calendar endDate) {
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
