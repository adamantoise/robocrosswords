package com.adamrosenfield.wordswithcrosses.net;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

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

        Query q = new Query();
        q.setFilterById(id);
        q.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL | DownloadManager.STATUS_FAILED);

        Cursor cursor = mgr.query(q);
        if (cursor.getCount() < 1) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
        cursor.close();

        System.out.println("Download completed: status=" + status + " reason=" + reason);

        AndroidVersionUtils.Factory.getInstance().onFileDownloaded(id, status == DownloadManager.STATUS_SUCCESSFUL);
    }
}
