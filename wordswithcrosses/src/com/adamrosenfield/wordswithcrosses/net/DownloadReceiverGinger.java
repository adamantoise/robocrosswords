package com.adamrosenfield.wordswithcrosses.net;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.adamrosenfield.wordswithcrosses.versions.AndroidVersionUtils;

@TargetApi(11)
public class DownloadReceiverGinger extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager mgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = intent.getLongExtra("extra_download_id", -1);

        if (android.os.Build.VERSION.SDK_INT >= 11 &&
            !"application/x-crossword".equals(mgr.getMimeTypeForDownloadedFile(id))) {
            return;
        }
        Uri uri = null;

        Query q = new Query();
        q.setFilterById(id);
        q.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_FAILED);

        Cursor cursor = mgr.query(q);
        if (cursor.getCount() < 1) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        System.out.println("uriString: " + uriString);
        uri = Uri.parse(uriString);
        cursor.close();

        if (uri == null || !uri.toString().endsWith(".puz")) {
            return;
        }

        System.out.println("===RECEIVED: " + uri);

        AndroidVersionUtils.Factory.getInstance().onFileDownloaded(id, status == DownloadManager.STATUS_SUCCESSFUL);
    }
}
