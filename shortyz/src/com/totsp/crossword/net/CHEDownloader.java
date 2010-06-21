package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

public class CHEDownloader extends AbstractDownloader{
	private static final String NAME = "Chronicle of Higher Education";
    NumberFormat nf = NumberFormat.getInstance();

    public CHEDownloader() {
        super("http://chronicle.com/items/biz/puzzles/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        if (date.getDay() != 5) {
            return null;
        }
        return super.download(date, this.createUrlSuffix(date));
    }

	@Override
	protected String createUrlSuffix(Date date) {
		return (date.getYear() + 1900) + nf.format(date.getMonth() + 1) +
        nf.format(date.getDate()) +  ".puz";
	}
}
