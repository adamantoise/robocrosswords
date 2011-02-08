package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;

public class HoneycombUtil implements AndroidVersionUtils {
	
	public void holographic(Activity a){
		ActionBar bar = a.getActionBar();
		Theme current = a.getTheme();
		a.setTheme(android.R.style.Theme_Holo);
		Theme changed = a.getTheme();
		System.out.println("========== IS HOLO? "+current.equals(changed));
		System.out.println("========== BAR "+(bar != null));
		if(bar != null){
			bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		}
	}
	
	public void finishOnHomeButton(final Activity a) {
		
		View home = a.findViewById(android.R.id.home);
		home.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				a.finish();
			}
			
		});
	}

	public void onActionBarWithText(MenuItem a){
		a.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT +MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	public void setContext(Context ctx) {
		// TODO Auto-generated method stub
		
	}

	public boolean downloadFile(URL url, File destination,
			Map<String, String> headers, boolean notification, String title) {
		return false;
	}

	public void onActionBarWithText(SubMenu a) {
		this.onActionBarWithText(a.getItem());
		
	}

}
