package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.net.AbstractDownloader;
import com.adamrosenfield.wordswithcrosses.net.DerStandardDownloader;

public class DateToIdEstimator {
    private final DerStandardPuzzleCache cache;

    private final static int ID_ZERO = 7677;
    private final static Calendar DATE_ZERO = AbstractDownloader.createDate(2014, 4, 16);

    public DateToIdEstimator(DerStandardPuzzleCache cache) {
        this.cache = cache;
    }

    public int estimateId(Calendar date) {
        DerStandardPuzzleMetadata dspm = cache.getClosestTo(date);

        int eId = ID_ZERO;
        Calendar eDate = DATE_ZERO;

        if (dspm != null) {
            eId = dspm.getId();
            eDate = dspm.getDate();

            if (DerStandardDownloader.equals(date, eDate)) {
                return dspm.getId();
            }
        }

        int id = estimate(date, eId, eDate);

        return id;
    }

    private int estimate(Calendar date, int baseId, Calendar baseDate) {
        long day1 = baseDate.getTimeInMillis();
        long day2 = date.getTimeInMillis();
        int days = (int) Math.round((day2 - day1) / 86400000D);
        int ids = Math.abs(days) > 6 ? ((int) ((float) days * 6f / 7f)) : days;

        int id = baseId + ids;
        return id;
    }

}
