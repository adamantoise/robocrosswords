package com.totsp.crossword.net;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.totsp.crossword.PlayActivity;

public class Scrapers {
	
	private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private ArrayList<AbstractPageScraper> scrapers = new ArrayList<AbstractPageScraper>();
    private Context context;
    private NotificationManager notificationManager;
    private boolean supressMessages;
    public Scrapers(SharedPreferences prefs,
        NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
        
        if (prefs.getBoolean("scrapeBEQ", true)) {
            scrapers.add(new BEQuigleyScraper());
        }
        
        this.supressMessages = prefs.getBoolean("supressMessages", false);
    }
    
    
    public void scrape(){
    	 int i = 1;
         String contentTitle = "Downloading Puzzles";

         Notification not = new Notification(android.R.drawable.stat_sys_download,
                 contentTitle, System.currentTimeMillis());
         for(AbstractPageScraper scraper : scrapers ){
        	 String contentText = "Downloading from " + scraper.getSourceName();
             Intent notificationIntent = new Intent(context, PlayActivity.class);
             PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                     notificationIntent, 0);
             not.setLatestEventInfo(context, contentTitle, contentText,
                 contentIntent);

             if (this.notificationManager != null) {
                 this.notificationManager.notify(0, not);
             }
             
             List<File> downloaded = scraper.scrape();
             if(!this.supressMessages){
	             for(File f: downloaded){
	            	 postDownloadedNotification(i++, scraper.getSourceName(), f);
	             }
             }
             
             
         }
         if (this.notificationManager != null) {
             this.notificationManager.cancel(0);
         }
    }
    
    private void postDownloadedNotification(int i, String name, File puzFile) {
        String contentTitle = "Downloaded Puzzle From " + name;
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
