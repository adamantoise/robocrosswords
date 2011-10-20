package com.totsp.crossword.versions;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.view.MenuItem;
import android.view.SubMenu;

import com.totsp.crossword.net.DownloadReceiver;
import com.totsp.crossword.puz.PuzzleMeta;


public class GingerbreadUtil extends DefaultUtil {
    protected Context ctx;

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public boolean xdownloadFile(URL url, File destination, Map<String, String> headers, boolean notification,
        String title) {
        DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);

        Request request = new Request(Uri.parse(url.toString()));
        request.setDestinationUri(Uri.fromFile(destination));
        System.out.println("====REQUESTING " + Uri.fromFile(destination));

        for (Entry<String, String> entry : headers.entrySet()) {
            request.addRequestHeader(entry.getKey(), entry.getValue());
        }

        request.setMimeType("application/x-crossword");
        
        request.setTitle(title);
        mgr.enqueue(request);

        return false;
    }

    public void finishOnHomeButton(Activity a) {
    }

    public void holographic(Activity playActivity) {
    }

    public void onActionBarWithText(MenuItem a) {
    }

    public void onActionBarWithText(SubMenu reveal) {
    }

	public void storeMetas(Uri uri, PuzzleMeta meta) {
		DownloadReceiver.metas.put(uri, meta);
		
	}
}
