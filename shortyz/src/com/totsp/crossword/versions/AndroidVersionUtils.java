package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.SubMenu;

public interface AndroidVersionUtils {
	
	public static class Factory {
		public static AndroidVersionUtils getInstance(){
			System.out.println("Creating utils for version: "+android.os.Build.VERSION.SDK_INT);
			try{
			switch(android.os.Build.VERSION.SDK_INT){
				case 10:
					return (AndroidVersionUtils) Class.forName("com.totsp.crossword.versions.HoneycombUtil").newInstance();
				case 9: 
					return (AndroidVersionUtils) Class.forName("com.totsp.crossword.versions.GingerbreadUtil").newInstance();
				default:
					return new DefaultUtil();
			}
			} catch(Exception e){
				return new DefaultUtil();
			}
		}
	}
	
	public boolean downloadFile(URL url, File destination, Map<String, String> headers, boolean notification, String title);
	
	public void setContext(Context ctx);
	
	public void finishOnHomeButton(Activity a);
	
	public void onActionBarWithText(MenuItem a);
	

	public void onActionBarWithText(SubMenu reveal);

	public void holographic(Activity playActivity);

}
