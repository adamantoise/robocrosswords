package com.totsp.crossword.net;

import java.io.File;

import java.text.NumberFormat;

import java.util.Date;


/**
 * Jonesin' Crosswords Downloader
 * URL: http://herbach.dnsalias.com/Jonesin/jzYYMMDD.puz
 * Date = Thursdays (usually posted previous Monday)
 */
public class JonesinDownloader extends AbstractDownloader {
    private static final String NAME = "Jonesin' Crosswords";
    NumberFormat nf = NumberFormat.getInstance();

    public JonesinDownloader() {
        super("http://herbach.dnsalias.com/Jonesin/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        if (date.getDay() != 4) {
            return null;
        }
        return super.download(date, this.createUrlSuffix(date));
    }

	@Override
	protected String createUrlSuffix(Date date) {
		return "jz" + (date.getYear() - 100) +
        nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
        ".puz";
	}
}
