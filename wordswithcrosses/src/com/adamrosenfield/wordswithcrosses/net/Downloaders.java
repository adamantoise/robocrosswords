package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.adamrosenfield.wordswithcrosses.BrowseActivity;
import com.adamrosenfield.wordswithcrosses.PlayActivity;
import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper;
import com.adamrosenfield.wordswithcrosses.R;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");
    private BrowseActivity context;
    private SharedPreferences prefs;
    private List<Downloader> downloaders = new LinkedList<Downloader>();
    private NotificationManager notificationManager;
    private boolean suppressMessages;
    private Intent browseIntent;
    private PendingIntent pendingBrowseIntent;

    private static final int GENERAL_NOTIF_ID = 0;
    private static AtomicInteger nextNotifId = new AtomicInteger(1);

    public Downloaders(BrowseActivity context, NotificationManager notificationManager) {
        this.context = context;
        this.notificationManager = notificationManager;
        this.prefs = context.getPrefs();

        browseIntent = new Intent(Intent.ACTION_EDIT, null, context, BrowseActivity.class);
        pendingBrowseIntent = PendingIntent.getActivity(context, 0, browseIntent, 0);

        // BEQ has requested that his puzzles not be automatically downloaded
        //if (prefs.getBoolean("downloadBEQ", true)) {
        //    downloaders.add(new BEQDownloader());
        //}

        if (prefs.getBoolean("downloadCHE", true)) {
            downloaders.add(new CHEDownloader());
        }

        if (prefs.getBoolean("downloadISwear", true)) {
            downloaders.add(new ISwearDownloader());
        }

        if (prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }

        if (prefs.getBoolean("downloadJonesin", true)) {
            downloaders.add(new JonesinDownloader());
        }

        if (prefs.getBoolean("downloadJoseph", true)) {
            downloaders.add(new JosephDownloader());
        }

        if (prefs.getBoolean("downloadLat", true)) {
            downloaders.add(new LATimesDownloader());
        }

        if (prefs.getBoolean("downloadMGDC", true)) {
            downloaders.add(new MGDCDownloader());
        }

        if (prefs.getBoolean("downloadMGWCC", true)) {
            downloaders.add(new MGWCCDownloader());
        }

        if (prefs.getBoolean("downloadMerlReagle", true)) {
            downloaders.add(new MerlReagleDownloader());
        }

        if (prefs.getBoolean("downloadMMMM",  true)) {
            downloaders.add(new MMMMDownloader());
        }

        if (prefs.getBoolean("downloadNYT", false)) {
            downloaders.add(new NYTDownloader(
                context,
                context.getHandler(),
                prefs.getString("nytUsername", ""),
                prefs.getString("nytPassword", "")));
        }

        // NYT classic is no longer updating with new puzzles
        //if (prefs.getBoolean("downloadNYTClassic", true)) {
        //    downloaders.add(new NYTClassicDownloader());
        //}

        if (prefs.getBoolean("downloadNewsday", true)) {
            downloaders.add(new NewsdayDownloader());
        }

        if (prefs.getBoolean("downloadPatrickBlindauer",  true)) {
            downloaders.add(new PatrickBlindauerDownloader());
        }

        if (prefs.getBoolean("downloadPeople", true)) {
            downloaders.add(new PeopleScraper());
        }

        if (prefs.getBoolean("downloadPremier", true)) {
            downloaders.add(new PremierDownloader());
        }

        if (prefs.getBoolean("downloadSheffer", true)) {
            downloaders.add(new ShefferDownloader());
        }

        if (prefs.getBoolean("downloadUniversal", true)) {
            downloaders.add(new UniversalDownloader());
        }

        if (prefs.getBoolean("downloadUSAToday", true)) {
            downloaders.add(new USATodayDownloader());
        }

        if (prefs.getBoolean("downloadWaPo", true)) {
            downloaders.add(new WaPoDownloader());
        }

        if (prefs.getBoolean("downloadWaPoPuzzler", true)) {
            downloaders.add(new WaPoPuzzlerDownloader());
        }

        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }

        this.suppressMessages = prefs.getBoolean("suppressMessages", false);
    }

    public List<Downloader> getDownloaders(Calendar date) {
        List<Downloader> retVal = new LinkedList<Downloader>();

        for (Downloader d : downloaders) {
            if (d.isPuzzleAvailable(date)) {
                retVal.add(d);
            }
        }

        return retVal;
    }

    public void download(Calendar date) {
        download(date, getDownloaders(date));
    }

    public void download(Calendar date, List<Downloader> downloaders) {
        date = (Calendar)date.clone();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        String contentTitle = context.getResources().getString(R.string.downloading_puzzles);
        Notification not = createDownloadingNotification(contentTitle);
        boolean somethingDownloaded = false;

        if (!WordsWithCrossesApplication.makeDirs()) {
            return;
        }

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();

        if (downloaders == null || downloaders.size() == 0) {
            downloaders = getDownloaders(date);
        }

        for (Downloader d : downloaders) {
            d.setContext(context);

            int notifId = nextNotifId.incrementAndGet();
            boolean succeeded = false;
            try {
                updateDownloadingNotification(not, contentTitle, d.getName());

                if (!suppressMessages && notificationManager != null) {
                    notificationManager.notify(GENERAL_NOTIF_ID, not);
                }

                String filename = d.getFilename(date);
                File downloadedFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
                if (dbHelper.filenameExists(filename) || downloadedFile.exists()) {
                    LOG.info("Download skipped: " + filename);
                    continue;
                }

                if (downloadedFile.exists()) {
                    LOG.info("File already downloaded but not in database: " + downloadedFile);
                    dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                    somethingDownloaded = true;
                    continue;
                }

                LOG.info("Download beginning: " + filename);

                if (d.download(date)) {
                    LOG.info("Downloaded succeeded: " + filename);
                    succeeded = true;
                    long id = dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                    if (!suppressMessages) {
                        postDownloadedNotification(notifId, d.getName(), downloadedFile, id);
                    }

                    somethingDownloaded = true;
                } else {
                    LOG.warning("Download failed: " + filename);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!succeeded && !suppressMessages && notificationManager != null) {
                postDownloadFailedNotification(notifId, d.getName());
            }
        }

        if (notificationManager != null) {
            notificationManager.cancel(GENERAL_NOTIF_ID);
        }

        if (somethingDownloaded) {
            postDownloadedGeneral();
        }

        context.postRenderMessage();
    }

    public void suppressMessages(boolean b) {
        this.suppressMessages = b;
    }

    @SuppressWarnings("deprecation")
    private Notification createDownloadingNotification(String contentTitle) {
        return new Notification(android.R.drawable.stat_sys_download, contentTitle, System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    private void updateDownloadingNotification(Notification not, String contentTitle, String source) {
        String contentText = context.getResources().getString(R.string.downloading_from, source);
        not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedGeneral() {
        String contentTitle = context.getResources().getString(R.string.downloaded_new_puzzles_title);
        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());

        String contentText = context.getResources().getString(R.string.downloaded_new_puzzles_text);
        not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);

        if (notificationManager != null) {
            notificationManager.notify(GENERAL_NOTIF_ID, not);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedNotification(int notifId, String name, File puzFile, long puzzleId) {
        String contentTitle = context.getResources().getString(R.string.downloaded_puzzle_title, name);

        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(Intent.ACTION_EDIT, null, context, PlayActivity.class);
        notificationIntent.putExtra(PlayActivity.EXTRA_PUZZLE_ID, puzzleId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, puzFile.getName(), contentIntent);

        if (notificationManager != null) {
            notificationManager.notify(notifId, not);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadFailedNotification(int notifId, String name) {
        String contentTitle = context.getResources().getString(R.string.download_failed, name);
        Notification not = new Notification(
                android.R.drawable.stat_notify_error, contentTitle,
                System.currentTimeMillis());
        not.setLatestEventInfo(context, contentTitle, name, pendingBrowseIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(notifId, not);
        }
    }
}
