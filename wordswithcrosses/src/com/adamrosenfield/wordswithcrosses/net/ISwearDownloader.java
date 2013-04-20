package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * http://wij.theworld.com/puzzles/dailyrecord/DR110401.puz
 * @author robert.cooper
 */
public class ISwearDownloader extends AbstractDownloader {
    private static final String NAME = "I Swear";
    NumberFormat nf = NumberFormat.getInstance();
    
    public ISwearDownloader(){
    	super("http://wij.theworld.com/puzzles/dailyrecord/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }
    
    public int[] getDownloadDates() {
        return DATE_FRIDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("DR" +
                (date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }

	public String getName() {
		return NAME;
	}

	public File download(Calendar date) {
		return super.download(date, this.createUrlSuffix(date));
	}
    
    
}
