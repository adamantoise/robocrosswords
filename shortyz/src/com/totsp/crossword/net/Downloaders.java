package com.totsp.crossword.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.totsp.crossword.BrowseActivity;
import com.totsp.crossword.PlayActivity;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;


public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private ArrayList<Downloader> downloaders = new ArrayList<Downloader>();
    private Context context;
    private NotificationManager notificationManager;
    private boolean supressMessages;
    public Downloaders(SharedPreferences prefs,
        NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;

        if (prefs.getBoolean("downloadGlobe", true)) {
            downloaders.add(new BostonGlobeDownloader());
        }

        if (prefs.getBoolean("downloadThinks", true)) {
            downloaders.add(new ThinksDownloader());
        }

        if (prefs.getBoolean("downloadChron", true)) {
            downloaders.add(new ChronDownloader());
        }

        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }

        if (prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }

        if (prefs.getBoolean("downloadLat", true)) {
            downloaders.add(new LATDownloader());
        }
        if (prefs.getBoolean("downloadAvClub", true)) {
            downloaders.add(new AVClubDownloader());
        }
        if (prefs.getBoolean("downloadPhilly", true)) {
            downloaders.add(new PhillyDownloader());
        }
        if(prefs.getBoolean("downloadCHE", true)){
        	downloaders.add(new CHEDownloader());
        }

        if (prefs.getBoolean("downloadNYT", false)) {
            downloaders.add(new NYTDownloader(prefs.getString("nytUsername", ""),
                    prefs.getString("nytPassword", "")));
        }
        this.supressMessages = prefs.getBoolean("supressMessages", false);
    }

    public void download(Date date) {
        int i = 1;
        String contentTitle = "Downloading Puzzles";

        Notification not = new Notification(android.R.drawable.stat_sys_download,
                contentTitle, System.currentTimeMillis());
        boolean somethingDownloaded = false;
        for (Downloader d : downloaders) {
            String contentText = "Downloading from " + d.getName();
            Intent notificationIntent = new Intent(context, PlayActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);
            not.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

            if (this.notificationManager != null) {
                this.notificationManager.notify(0, not);
            }

            File downloaded = new File(Environment.getExternalStorageDirectory(),
                    "crosswords/" + d.createFileName(date));
            File archived  = new File(Environment.getExternalStorageDirectory(),
                    "crosswords/archive/" + d.createFileName(date));

            System.out.println(downloaded.getAbsolutePath()+" "+downloaded.exists() + " OR "+archived.getAbsolutePath()+" "+archived.exists());
            if(d instanceof NYTDownloader && downloaded.exists() ){
            	NYTDownloader updater = (NYTDownloader) d;
            	File updated = updater.update(date);
            	if(updated != null){
            		this.postUpdatedNotification(i, d.getName(), downloaded);
            	}
            	continue;
            	
            } else if (downloaded.exists() || archived.exists()) {
                continue;
            } 

            downloaded = d.download(date);

            if (downloaded != null) {
                try {
                    Puzzle puz = IO.load(downloaded);
                    puz.setDate(date);
                    puz.setSource(d.getName());
                    IO.save(puz, downloaded);
                    if(!this.supressMessages){
                    	this.postDownloadedNotification(i, d.getName(), downloaded);
                    }
                    somethingDownloaded = true;
                } catch (Exception ioe) {
                    LOG.log(Level.WARNING, "Exception reading " + downloaded,
                        ioe);
                    downloaded.delete();
                }
            }

            i++;
        }

        if (this.notificationManager != null) {
            this.notificationManager.cancel(0);
        }
        if( somethingDownloaded && this.supressMessages){
        	this.postDownloadedGeneral();
        }
    }

    private void postDownloadedNotification(int i, String name, File puzFile) {
        String contentTitle = "Downloaded " + name;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
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
    
    private void postDownloadedGeneral() {
        String contentTitle = "Downloaded new puzzles!" ;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
               null, context, BrowseActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, "News Puzzles Were Downloaded",
            contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(0, not);
        }
    }
    
    private void postUpdatedNotification(int i, String name, File puzFile) {
        String contentTitle = "Updated " + name;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
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
