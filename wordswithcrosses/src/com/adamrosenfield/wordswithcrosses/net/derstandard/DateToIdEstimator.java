package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Date;

import com.adamrosenfield.wordswithcrosses.net.DerStandardDownloader;

public class DateToIdEstimator {
    private final DerStandardPuzzleCache cache;
    
    private final static int  ID_ZERO = 7677;
    private final static Date DATE_ZERO = new Date(114, 4, 16);
    
    public DateToIdEstimator(DerStandardPuzzleCache cache) {
        this.cache = cache;
    }

    public int estimateId(Date date) {
        DerStandardPuzzleMetadata dspm = cache.getClosestTo(date);
        
        int eId = ID_ZERO; 
        Date eDate = DATE_ZERO;
        
        if (dspm != null) {
            eId = dspm.getId();
            eDate = dspm.getDate().getTime();
            
            if (DerStandardDownloader.equals(date, eDate)) {
                return dspm.getId();
            }
        }
        
        int id = estimate(date, eId, eDate);
        
        return id;
    }

    private int estimate(Date date, int baseId, Date baseDate) {
        long day1 = baseDate.getTime();
        long day2 = date.getTime();
        int days = (int) Math.round((day2 - day1) / 86400000D);        
        int ids = days > 6 ? ((int) ((float) days * 6f / 7f)) : days;
        
        int id = baseId + ids;
        return id;
    }

}
