package com.totsp.crossword.net;

import java.io.File;

import java.text.NumberFormat;

import java.util.Date;

/**
 * Houston Chronicle
 * URL: http://www.chron.com/apps/games/xword/puzzles/csYYYYMMDD.puz
 * Date = Daily
 */
public class ChronDownloader extends AbstractDownloader {
    private static final String NAME = "Houston Chronicle";
    NumberFormat nf = NumberFormat.getInstance();

    public ChronDownloader() {
        super("http://www.chron.com/apps/games/xword/puzzles/", DOWNLOAD_DIR,
            NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public String getName() {
        return NAME;
    }
    
    public int[] getDownloadDates() {
    	return DATE_DAILY;
    }

    public File download(Date date) {
        return super.download(date, this.createUrlSuffix(date));
    }

	@Override
	protected String createUrlSuffix(Date date) {
		return "cs" + (date.getYear() - 100) +
        nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
        ".puz";
	}
}
