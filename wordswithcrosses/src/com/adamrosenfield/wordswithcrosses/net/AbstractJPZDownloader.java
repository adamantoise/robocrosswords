package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.JPZIO;

public abstract class AbstractJPZDownloader extends AbstractDownloader {

    protected AbstractJPZDownloader(String baseUrl, String downloaderName) {
        super(baseUrl, downloaderName);
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
            throws IOException {
        return download(date, urlSuffix, headers, JPZIO.NOOP_METADATA_SETTER);
    }

    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers,
            JPZIO.PuzzleMetadataSetter metadataSetter) throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File jpzFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        if (!utils.downloadFile(url, headers, jpzFile, true, getName())) {
            return false;
        }

        try {
            File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
            JPZIO.convertJPZPuzzle(jpzFile, destFile, metadataSetter);
            return true;
        } finally {
            jpzFile.delete();
        }
    }
}
