package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public class CruScraper extends AbstractPageScraper {
    public CruScraper() {
        super("Cryptic Cru Workshop Archive");
    }

    @Override
    protected String getScrapeURL(Calendar date) {
        return "http://world.std.com/~wij/puzzles/cru/";
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;  // TODO
    }
}
