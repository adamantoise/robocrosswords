package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.adamrosenfield.wordswithcrosses.BrowseActivity;
import com.adamrosenfield.wordswithcrosses.PlayActivity;
import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.wordswithcrosses.R;

public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");
    private BrowseActivity context;
    private SharedPreferences prefs;
    private List<Downloader> downloaders = new LinkedList<Downloader>();
    private NotificationManager notificationManager;
    private boolean suppressMessages;

    public Downloaders(BrowseActivity context, NotificationManager notificationManager) {
        this.context = context;
        this.notificationManager = notificationManager;
        this.prefs = context.getPrefs();

        if (prefs.getBoolean("downloadThinks", true)) {
            downloaders.add(new ThinksDownloader());
        }
        if (prefs.getBoolean("downloadWaPo", true)) {
            downloaders.add(new WaPoDownloader());
        }

        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }

        if (prefs.getBoolean("downloadWaPoPuzzler", true)) {
            downloaders.add(new WaPoPuzzlerDownloader());
        }

        if (prefs.getBoolean("downloadNYTClassic", true)) {
            downloaders.add(new NYTClassicDownloader());
        }

        if (prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }

        if (prefs.getBoolean("downloadJonesin", true)) {
            downloaders.add(new JonesinDownloader());
        }

        if (prefs.getBoolean("downloadLat", true)) {
//          downloaders.add(new UclickDownloader("tmcal", "Los Angeles Times", "Rich Norris", Downloader.DATE_NO_SUNDAY));
            downloaders.add(new LATimesDownloader());
        }

        if (prefs.getBoolean("downloadPhilly", true)) {
            downloaders.add(new PhillyDownloader());
        }

        if (prefs.getBoolean("downloadCHE", true)) {
            downloaders.add(new CHEDownloader());
        }

        if (prefs.getBoolean("downloadJoseph", true)) {
            downloaders.add(new KFSDownloader("joseph", "Joseph Crosswords",
                    "Thomas Joseph", Downloader.DATE_NO_SUNDAY));
        }

        if (prefs.getBoolean("downloadSheffer", true)) {
            downloaders.add(new KFSDownloader("sheffer", "Sheffer Crosswords",
                    "Eugene Sheffer", Downloader.DATE_NO_SUNDAY));
        }

        if (prefs.getBoolean("downloadPremier", true)) {
            downloaders.add(new KFSDownloader("premier", "Premier Crosswords",
                    "Frank Longo", Downloader.DATE_SUNDAY));
        }

        if (prefs.getBoolean("downloadNewsday", true)) {
            downloaders.add(new UclickDownloader("crnet", "Newsday",
                    "Stanley Newman, distributed by Creators Syndicate, Inc.",
                    Downloader.DATE_DAILY));
        }

        if (prefs.getBoolean("downloadUSAToday", true)) {
            downloaders.add(new UclickDownloader("usaon", "USA Today",
                    "USA Today", Downloader.DATE_NO_SUNDAY));
        }

        if (prefs.getBoolean("downloadUniversal", true)) {
            downloaders.add(new UclickDownloader("fcx", "Universal Crossword",
                    "uclick LLC", Downloader.DATE_DAILY));
        }

        if (prefs.getBoolean("downloadLACal", true)) {
            downloaders.add(new UclickDownloader("lacal",
                    "LAT Sunday Calendar", "Los Angeles Times",
                    Downloader.DATE_SUNDAY));
        }

        if (prefs.getBoolean("downloadISwear", true)) {
            downloaders.add(new ISwearDownloader());
        }

        if (prefs.getBoolean("downloadNYT", false)) {
            downloaders.add(new NYTDownloader(context, prefs.getString(
                    "nytUsername", ""), prefs.getString("nytPassword", "")));
        }

        //if (prefs.getBoolean("scrapeBEQ", true)) {
        //    scrapers.add(new BEQuigleyScraper());
        //}

        if (prefs.getBoolean("scrapeCru", false)) {
            downloaders.add(new CruScraper());
        }

        if (prefs.getBoolean("scrapeKegler", false)) {
            downloaders.add(new KeglerScraper());
        }

        if (prefs.getBoolean("scrapePeople", true)) {
            downloaders.add(new PeopleScraper());
        }

        this.suppressMessages = prefs.getBoolean("suppressMessages", false);
    }

    public List<Downloader> getDownloaders(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        List<Downloader> retVal = new LinkedList<Downloader>();

        for (Downloader d : downloaders) {
            if (arrayContains(d.getDownloadDates(), dayOfWeek)) {
                retVal.add(d);
            }
        }

        return retVal;
    }

    private static boolean arrayContains(int[] array, int key) {
        for (int x : array) {
            if (x == key) {
                return true;
            }
        }

        return false;
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

        int i = 1;
        for (Downloader d : downloaders) {
            d.setContext(context);

            try {
                updateDownloadingNotification(not, contentTitle, d.getName());

                if (!suppressMessages && notificationManager != null) {
                    notificationManager.notify(0, not);
                }

                String filename = d.getFilename(date);
                File downloadedFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
                if (dbHelper.filenameExists(filename) || downloadedFile.exists()) {
                    LOG.info("Download skipped: " + filename);
                    continue;
                }

                LOG.info("Download beginning: " + filename);

                if (d.download(date)) {
                    LOG.info("Downloaded succeeded: " + filename);
                    dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                    if (!suppressMessages) {
                        postDownloadedNotification(i, d.getName(), downloadedFile);
                    }

                    somethingDownloaded = true;
                } else {
                    LOG.warning("Download failed: " + filename);
                }

                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (notificationManager != null) {
            notificationManager.cancel(0);
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
        String contentText = context.getResources().getString(R.string.downloading_from);
        contentText = contentText.replace("${SOURCE}", source);
        Intent notificationIntent = new Intent(context, PlayActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedGeneral() {
        String contentTitle = context.getResources().getString(R.string.downloaded_new_puzzles_title);
        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT, null,
                context, BrowseActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        String contentText = context.getResources().getString(R.string.downloaded_new_puzzles_text);
        not.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(0, not);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedNotification(int i, String name, File puzFile) {
        String contentTitle = context.getResources().getString(R.string.downloaded_new_puzzles_title);
        contentTitle = contentTitle.replace("${SOURCE}", name);
        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
                Uri.fromFile(puzFile), context, PlayActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, puzFile.getName(),
                contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(i, not);
        }
    }
}
