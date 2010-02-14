package com.totsp.crossword.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.totsp.crossword.PlayActivity;

public class Downloaders {

	private ArrayList<Downloader> downloaders = new ArrayList<Downloader>();
	private NotificationManager notificationManager;
	private Context context;
	
	public Downloaders(SharedPreferences prefs,
			NotificationManager notificationManager,
			Context context) {
		this.notificationManager = notificationManager;
		this.context = context;
		
		if (prefs.getBoolean("downloadGlobe", true)) {
			downloaders.add(new BostonGlobeDownloader());
		}
		if(prefs.getBoolean("downloadThinks", true)){
			downloaders.add(new ThinksDownloader());
		}
		if(prefs.getBoolean("downloadChron", true)){
			downloaders.add(new ChronDownloader() );
		}
	}

	public void download(Date date){
		int i=1;
		Notification not = new Notification(android.R.drawable.stat_sys_download, "Downloading Puzzles", System.currentTimeMillis());
		String contentTitle = "Downloading Puzzles";
		
		for(Downloader d : downloaders){
			String contentText = "Downloading from "+d.getName();
			Intent notificationIntent = new Intent(context, PlayActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			not.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			this.notificationManager.notify(0, not);
			File downloaded = d.download(date);
		}
	}
}
