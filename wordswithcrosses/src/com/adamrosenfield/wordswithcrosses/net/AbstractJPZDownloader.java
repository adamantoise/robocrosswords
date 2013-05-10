package com.adamrosenfield.wordswithcrosses.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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
        URL url;
        try {
            url = new URL(this.baseUrl + urlSuffix);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File jpzFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        if (!utils.downloadFile(url, headers, jpzFile, true, getName())) {
            return false;
        }

        try {
            File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
            return convertJPZPuzzle(jpzFile, destFile, date);
        } finally {
            jpzFile.delete();
        }
    }

    private boolean convertJPZPuzzle(File jpzFile, File destFile, Calendar date) throws IOException {
        FileInputStream fis = new FileInputStream(jpzFile);
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
            try {
                return JPZIO.convertJPZPuzzle(fis, dos, date);
            } finally {
                dos.close();
            }
        } finally {
            fis.close();
        }
    }
}
