package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;

public class ConfigurableDailyDateToIdConverter implements DateToIdConverter {
    private final int idZero ;
    private final Calendar dateZero;

    public ConfigurableDailyDateToIdConverter(int idZero, Calendar dateZero) {
        this.idZero = idZero;
        this.dateZero = dateZero;
    }

    public int getId(Calendar date) {
        int daysDifference = DateUtil.daysBetween(dateZero, date);
        return idZero + daysDifference;
    }

   

}
