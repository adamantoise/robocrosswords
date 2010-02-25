package com.totsp.crossword.net;

import java.io.File;

import java.text.NumberFormat;

import java.util.Date;


//http://herbach.dnsalias.com/Tausig/vv100212.puz
public class InkwellDownloader extends AbstractDownloader {
    private static final String NAME = "InkWell.com";
    NumberFormat nf = NumberFormat.getInstance();

    public InkwellDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", DOWNLOAD_DIR, NAME);
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

        String name = "vv" + (date.getYear() - 100) +
            nf.format(date.getMonth() + 1) + nf.format(date.getDate()) +
            ".puz";

        return super.download(date, name);
    }
}
