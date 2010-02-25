package com.totsp.crossword.net;

import java.io.File;

import java.text.NumberFormat;

import java.util.Date;


public class ThinksDownloader extends AbstractDownloader {
    private static final String NAME = "Thinks.com";
    NumberFormat nf = NumberFormat.getInstance();

    public ThinksDownloader() {
        super("http://thinks.com/daily-crossword/puzzles/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public String getName() {
        return NAME;
    }

    public File download(Date date) {
        String name = (date.getYear() + 1900) + "-" +
            nf.format(date.getMonth() + 1) + "/" + "dc1-" +
            (date.getYear() + 1900) + "-" + nf.format(date.getMonth() + 1) +
            "-" + nf.format(date.getDate()) + ".puz";

        return super.download(date, name);
    }
}
