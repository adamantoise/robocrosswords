package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import android.content.Context;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

public abstract class AbstractDownloader implements Downloader {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

    protected String baseUrl;
    private String downloaderName;

    protected final AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();

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
                (date.get(Calendar.MONTH) + 1) +
                "-" +
                date.get(Calendar.DAY_OF_MONTH) +
                "-" +
                this.downloaderName.replaceAll(" ", "") +
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
}
