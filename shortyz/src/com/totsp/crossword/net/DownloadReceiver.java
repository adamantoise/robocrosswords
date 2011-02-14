package com.totsp.crossword.net;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.totsp.crossword.puz.PuzzleMeta;

public class DownloadReceiver extends BroadcastReceiver {

	public static HashMap<Uri, PuzzleMeta> metas = new HashMap<Uri, PuzzleMeta>();
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		
		DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
		long id = intent.getLongExtra("extra_download_id", -1);
		if(!"application/x-crossword".equals(mgr.getMimeTypeForDownloadedFile(id))){
			return;
		}
		Uri uri = mgr.getUriForDownloadedFile(id);
		if(uri == null){
			return;
		}
		System.out.println("===RECEIVED: "+uri);
		try{
		Downloaders.processDownloadedPuzzle(new File(new URI(uri.toString())), 
			metas.remove(uri));
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
