package com.adamrosenfield.wordswithcrosses.net;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import android.content.Context;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

public abstract class AbstractDownloader implements Downloader {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

    protected String baseUrl;
    private String downloaderName;

    protected final AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();

    protected static final String[] SHORT_MONTHS = new String[] {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    protected static final NumberFormat DEFAULT_NF;

    static {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        DEFAULT_NF = nf;
    }

    protected AbstractDownloader(String baseUrl, String downloaderName) {
        this.baseUrl = baseUrl;
        this.downloaderName = downloaderName;
    }

    public void setContext(Context ctx) {
        this.utils.setContext(ctx);
    }

    public String getFilename(Calendar date) {
        return (date.get(Calendar.YEAR) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                "-" +
                this.downloaderName.replaceAll(" ", "").replace("/", "_") +
                ".puz");
    }

    public String sourceUrl(Calendar date) {
        return this.baseUrl + this.createUrlSuffix(date);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return downloaderName;
    }

    protected abstract String createUrlSuffix(Calendar date);

    public boolean download(Calendar date) throws IOException {
        return download(date, createUrlSuffix(date));
    }

    protected boolean download(Calendar date, String urlSuffix) throws IOException {
        return download(date, urlSuffix, EMPTY_MAP);
    }

    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
        return utils.downloadFile(url, headers, destFile, true, getName());
    }

    protected String downloadUrlToString(String url) throws IOException {
        URL u = new URL(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IO.copyStream(u.openStream(), baos);

        return new String(baos.toByteArray());
    }
}
