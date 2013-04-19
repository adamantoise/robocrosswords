package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.totsp.crossword.puz.PuzzleMeta;

public interface AndroidVersionUtils {
	
	public void storeMetas(Uri uri, PuzzleMeta meta);

	public void setContext(Context ctx);

	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title);

	public void finishOnHomeButton(Activity a);

	public void holographic(Activity playActivity);

	public void onActionBarWithText(MenuItem a);

	public void onActionBarWithText(SubMenu reveal);

	public static class Factory {
		private static AndroidVersionUtils INSTANCE;
		
		public static AndroidVersionUtils getInstance() {
			if(INSTANCE != null){
				return INSTANCE;
			}
			System.out.println("Creating utils for version: "
					+ android.os.Build.VERSION.SDK_INT);

			try {
				switch (android.os.Build.VERSION.SDK_INT) {
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
				
					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.HoneycombUtil")
							.newInstance();
				case 16:
				case 17:
					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.JellyBeanUtil")
							.newInstance();
				case 10:
				case 9:
					System.out.println("Using Gingerbread.");
					return INSTANCE = (AndroidVersionUtils) Class.forName(
							"com.totsp.crossword.versions.GingerbreadUtil")
							.newInstance();

				default:
					return INSTANCE = new DefaultUtil();
				}
			} catch (Exception e) {
				return INSTANCE = new DefaultUtil();
			}
		}
	}

	public View onActionBarCustom(Activity a, int id);
	
	public void hideWindowTitle(Activity a);
	
	public void hideActionBar(Activity a);

	
}
