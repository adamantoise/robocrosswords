package com.totsp.crossword.net;

import android.content.Context;

import android.net.Uri;

import android.os.Environment;

import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.shortyz.ShortyzApplication;
import com.totsp.crossword.versions.AndroidVersionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;


public abstract class AbstractDownloader implements Downloader {
    protected static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    public static File DOWNLOAD_DIR = ShortyzApplication.CROSSWORDS;
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    protected static final Map<String, String> EMPTY_MAP = Collections.EMPTY_MAP;
    protected File downloadDirectory;
    protected String baseUrl;
    private final AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();
    private String downloaderName;

    protected AbstractDownloader(String baseUrl, File downloadDirectory, String downloaderName) {
        this.baseUrl = baseUrl;
        this.downloadDirectory = downloadDirectory;
        this.downloaderName = downloaderName;
    }

    public void setContext(Context ctx) {
        this.utils.setContext(ctx);
    }

    /**
     * Copies the data from an InputStream object to an OutputStream object.
     *
     * @param sourceStream
     *            The input stream to be read.
     * @param destinationStream
     *            The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException
     *                from java.io calls.
     */
    public static int copyStream(InputStream sourceStream, OutputStream destinationStream)
        throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }

        destinationStream.flush();
        destinationStream.close();

        return totalBytes;
    }

    public String createFileName(Date date) {
        return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-" +
        this.downloaderName.replaceAll(" ", "") + ".puz";
    }

    public String sourceUrl(Date date) {
        return this.baseUrl + this.createUrlSuffix(date);
    }

    public String toString() {
        return getName();
    }

    protected abstract String createUrlSuffix(Date date);

    protected File download(Date date, String urlSuffix, Map<String, String> headers) {
        LOG.info("Mkdirs: " + this.downloadDirectory.mkdirs());
        LOG.info("Exist: " + this.downloadDirectory.exists());

        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            System.out.println(url);

            File f = new File(downloadDirectory, this.createFileName(date));
            PuzzleMeta meta = new PuzzleMeta();
            meta.date = date;
            meta.source = getName();
            meta.sourceUrl = url.toString();
            meta.updateable = false;
            
            utils.storeMetas(Uri.fromFile(f), meta);

            if (utils.downloadFile(url, f, headers, true, this.getName())) {
                DownloadReceiver.metas.remove(Uri.fromFile(f));

                return f;
            } else {
                return Downloader.DEFERRED_FILE;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected File download(Date date, String urlSuffix) {
        return this.download(date, urlSuffix, EMPTY_MAP);
    }
}
