package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public class KeglerScraper extends AbstractPageScraper {
    public KeglerScraper() {
        super("Kegler's Kryptics");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://www.lafn.org/~keglerron/Block_style/index.html";
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;  // TODO
    }
}
