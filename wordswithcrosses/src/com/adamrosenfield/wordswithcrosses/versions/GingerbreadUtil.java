package com.adamrosenfield.wordswithcrosses.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

@TargetApi(9)
public class GingerbreadUtil extends DefaultUtil {

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    protected Context context;

    private static class DownloadingFile
    {
        public boolean succeeded = false;
    }

    private static ConcurrentMap<Long, DownloadingFile> waitingDownloads = new ConcurrentSkipListMap<Long, DownloadingFile>();

    @Override
    public void setContext(Context ctx) {
        this.context = ctx;
    }

    @Override
    public boolean downloadFile(URL url, Map<String, String> headers, File destination, boolean notification,
        String title) {
        DownloadManager mgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        Request request = new Request(Uri.parse(url.toString()));
        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destination.getName());

        request.setDestinationUri(Uri.fromFile(tempFile));
        LOG.info("Downloading " + url + " ==> " + tempFile);

        for (Entry<String, String> entry : headers.entrySet()) {
            request.addRequestHeader(entry.getKey(), entry.getValue());
        }

        request.setMimeType("application/x-crossword");

        setNotificationVisibility(request, notification);

        request.setTitle(title);
        long id = mgr.enqueue(request);

        // Wait for the request to complete
        DownloadingFile downloadingFile = new DownloadingFile();
        waitingDownloads.put(id, downloadingFile);

        boolean succeeded;
        try {
            synchronized (downloadingFile) {
                downloadingFile.wait();
                succeeded = downloadingFile.succeeded;
            }
        } catch (InterruptedException e) {
            LOG.warning("Download interrupted: " + url);
            return false;
        }

        LOG.info("Download " + (succeeded ? "succeeded" : "failed") + ": " + url);

        if (succeeded) {
            if (!destination.equals(tempFile) && !tempFile.renameTo(destination)) {
                LOG.warning("Renaming " + tempFile + " to " + destination + " failed");
                return false;
            }
        }

        return succeeded;
    }

    @Override
    public void onFileDownloaded(long id, boolean succeeded) {
        DownloadingFile downloadingFile = waitingDownloads.remove(id);
        if (downloadingFile != null) {
            synchronized (downloadingFile) {
                downloadingFile.succeeded = succeeded;
                downloadingFile.notifyAll();
            }
        } else {
            LOG.warning("No thread is waiting on download for id=" + id);
        }
    }

    @Override
    public void finishOnHomeButton(Activity a) {
    }

    @Override
    public void holographic(Activity playActivity) {
    }

    @Override
    public void onActionBarWithText(MenuItem a) {
    }

    @Override
    public void onActionBarWithText(SubMenu reveal) {
    }

    @Override
    public void hideWindowTitle(Activity a) {
        a.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void hideActionBar(Activity a) {
    }

    protected void setNotificationVisibility(Request reqest, boolean notification) {
    }
}
