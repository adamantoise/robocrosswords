package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;

public interface DateToIdConverter {
    static final int NONE = -1;

    int getId(Calendar date);

}