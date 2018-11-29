/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import android.text.TextUtils;

import com.adamrosenfield.wordswithcrosses.BrowseActivity;
import com.adamrosenfield.wordswithcrosses.PlayActivity;
import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper;
import com.adamrosenfield.wordswithcrosses.R;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");
    private BrowseActivity context;
    private SharedPreferences prefs;
    private List<Downloader> downloaders = new LinkedList<>();
    private NotificationManager notificationManager;
    private boolean enableNotifications;
    private boolean enableIndividualDownloadNotifications;
    private Intent browseIntent;
    private PendingIntent pendingBrowseIntent;

    private static final int GENERAL_NOTIF_ID = 0;
    private static AtomicInteger nextNotifId = new AtomicInteger(1);

    public Downloaders(BrowseActivity context, boolean includeAll) {
        this.context = context;
        this.notificationManager = context.getNotificationManager();
        this.prefs = context.getPrefs();

        browseIntent = new Intent(Intent.ACTION_EDIT, null, context, BrowseActivity.class);
        pendingBrowseIntent = PendingIntent.getActivity(context, 0, browseIntent, 0);

        if (includeAll || prefs.getBoolean("downloadAVXW", false)) {
            String username = prefs.getString("avxwUsername", "");
            String password = prefs.getString("avxwPassword", "");
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                downloaders.add(new AVXWDownloader(username, password));
            }
        }

        if (includeAll || prefs.getBoolean("downloadAndyKravis", true)) {
            downloaders.add(new AndyKravisDownloader());
        }

        downloaders.add(new BEQDownloader());

        if (includeAll || prefs.getBoolean("downloadCHE", true)) {
            downloaders.add(new CHEDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadCrooked", false)) {
            String username = prefs.getString("crookedUsername", "");
            String password = prefs.getString("crookedPassword", "");
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                downloaders.add(new CrookedDownloader(username, password));
            }
        }

        if (includeAll || prefs.getBoolean("downloadCrosswordNation", false)) {
            String username = prefs.getString("crosswordNationUsername", "");
            String password = prefs.getString("crosswordNationPassword", "");
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                downloaders.add(new CrosswordNationDownloader(username, password));
            }
        }

        if (includeAll || prefs.getBoolean("downloadDerStandard", false)) {
          downloaders.add(new DerStandardDownloader());
        }

        // Devil Cross is no longer published as of 2016-04-01
        //if (includeAll || prefs.getBoolean("downloadDevilCross", true)) {
        //    downloaders.add(new DevilCrossDownloader());
        //}

        if (includeAll || prefs.getBoolean("downloadErikAgard", true)) {
            downloaders.add(new ErikAgardDownloader());
        }

        // "I Swear" crossword is no longer being published as of 2014-01-01
        // TODO: Archives
        //if (includeAll || prefs.getBoolean("downloadISwear", true)) {
        //    downloaders.add(new ISwearDownloader());
        //}

        if (includeAll || prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadJonesin", true)) {
            downloaders.add(new JonesinDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadJoseph", true)) {
            downloaders.add(new JosephDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadLAT", true)) {
            downloaders.add(new LATimesDownloader());
        }

        // Merl Reagle has requested this his puzzles be removed
        //if (includeAll || prefs.getBoolean("downloadMerlReagle", true)) {
        //    downloaders.add(new MerlReagleDownloader());
        //}

        // MGDC is subscription-only as of 2015-11-12
        //downloaders.add(new MGDCDownloader());

        // MGWCC is subscription-only as of 2015-01-01
        //if (includeAll || prefs.getBoolean("downloadMGWCC", true)) {
        //    downloaders.add(new MGWCCDownloader());
        //}

        if (includeAll || prefs.getBoolean("downloadMMMM",  true)) {
            downloaders.add(new MMMMDownloader());
        }

        // Neville Fogarty is no longer published as of 2015-06-05
        //if (includeAll || prefs.getBoolean("downloadNevilleFogarty",  true)) {
        //    downloaders.add(new NevilleFogartyDownloader());
        //}

        // FIXME: NYT temporarily removed until login via a WebView is implemented
        //String nytUsername = prefs.getString("nytUsername", "");
        //String nytPassword = prefs.getString("nytPassword", "");
        //if (!TextUtils.isEmpty(nytUsername) && !TextUtils.isEmpty(nytPassword)) {
        //    if (includeAll || prefs.getBoolean("downloadNYT", false)) {
        //        downloaders.add(new NYTDownloader(nytUsername, nytPassword));
        //    }
        //
        //    if (includeAll || prefs.getBoolean("downloadNYTMini", false)) {
        //        downloaders.add(new NYTMiniDownloader(nytUsername, nytPassword));
        //    }
        //
        //    if (includeAll || prefs.getBoolean("downloadNYTBonus", false)) {
        //        downloaders.add(new NYTBonusDownloader(nytUsername, nytPassword));
        //    }
        //}

        // NYT classic is no longer updating with new puzzles
        //if (includeAll || prefs.getBoolean("downloadNYTClassic", true)) {
        //    downloaders.add(new NYTClassicDownloader());
        //}

        if (includeAll || prefs.getBoolean("downloadNewsday", true)) {
            downloaders.add(new NewsdayDownloader());
        }

        // Patrick Blindauer's free monthly puzzle is no longer published
        // as of 2016-10-01
        //if (includeAll || prefs.getBoolean("downloadPatrickBlindauer",  true)) {
        //    downloaders.add(new PatrickBlindauerDownloader());
        //}

        // People Magazine stopped publishing new puzzles a while back
        //if (includeAll || prefs.getBoolean("downloadPeople", true)) {
        //    downloaders.add(new PeopleScraper());
        //}

        if (includeAll || prefs.getBoolean("downloadPremier", true)) {
            downloaders.add(new PremierDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadSheffer", true)) {
            downloaders.add(new ShefferDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadUniversal", true)) {
            downloaders.add(new UniversalDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadUSAToday", true)) {
            downloaders.add(new USATodayDownloader());
        }

        if (includeAll || prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }

        // CrosSynergy/Washington Post is subscription-only as of 2015-xx-xx
        //if (includeAll || prefs.getBoolean("downloadWaPo", true)) {
        //    downloaders.add(new WaPoDownloader());
        //}

        if (includeAll || prefs.getBoolean("downloadWaPoPuzzler", true)) {
            downloaders.add(new WaPoPuzzlerDownloader());
        }

        enableNotifications = prefs.getBoolean("enableNotifications", true);
        enableIndividualDownloadNotifications = enableNotifications && prefs.getBoolean("enableIndividualDownloadNotifications", true);
    }

    public List<Downloader> getDownloaders(Calendar date) {
        List<Downloader> retVal = new LinkedList<>();

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
        Notification not = enableNotifications ? createDownloadingNotification(contentTitle) : null;
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

            if (d.isManualDownload()) {
                continue;
            }

            int notifId = nextNotifId.incrementAndGet();
            boolean succeeded = false;
            String failureMessage = null;

            try {
                updateDownloadingNotification(not, contentTitle, d.getName());

                if (enableNotifications && notificationManager != null) {
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

                d.download(date);

                LOG.info("Downloaded succeeded: " + filename);
                long id = dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                if (id == -1) {
                    throw new IOException("Failed to load puzzle");
                }

                succeeded = true;
                somethingDownloaded = true;

                if (enableIndividualDownloadNotifications) {
                    postDownloadedNotification(notifId, d.getName(), downloadedFile, id);
                }

                context.postRenderMessage();
            } catch (DownloadException e) {
                LOG.warning("Download failed: " + d.getName());
                e.printStackTrace();
                failureMessage = context.getResources().getString(e.getResource());
            } catch (IOException e) {
                LOG.warning("Download failed: " + d.getName());
                e.printStackTrace();
            }

            // Notify the user about failed downloads.  Don't notify if
            // notifications are disabled, unless there's a non-standard
            // failure message (e.g. invalid username/password) that they
            // should know about.
            if (!succeeded &&
                (enableIndividualDownloadNotifications || failureMessage != null) &&
                notificationManager != null)
            {
                postDownloadFailedNotification(notifId, d.getName(), failureMessage);
            }
        }

        if (notificationManager != null && not != null) {
            notificationManager.cancel(GENERAL_NOTIF_ID);
        }

        if (somethingDownloaded && enableNotifications) {
            postDownloadedGeneral();
        }

        context.updateLastDatabaseSyncTime();
    }

    public void enableIndividualDownloadNotifications(boolean enable) {
        this.enableIndividualDownloadNotifications = enable;
    }

    @SuppressWarnings("deprecation")
    private Notification createDownloadingNotification(String contentTitle) {
        return new Notification(android.R.drawable.stat_sys_download, contentTitle, System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    private void updateDownloadingNotification(Notification not, String contentTitle, String source) {
        if (not != null) {
            String contentText = context.getResources().getString(R.string.downloading_from, source);
            not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);
        }
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
    private void postDownloadFailedNotification(int notifId, String name, String failureMessage) {
        String contentTitle = context.getResources().getString(R.string.download_failed, name);

        String contentText = (failureMessage != null ? failureMessage : name);

        Notification not = new Notification(
                android.R.drawable.stat_notify_error, contentTitle,
                System.currentTimeMillis());
        not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(notifId, not);
        }
    }
}
