package com.adamrosenfield.wordswithcrosses.net;

import java.util.logging.Logger;

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

    protected static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager mgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = intent.getLongExtra("extra_download_id", -1);

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
        String mediaType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
        cursor.close();

        LOG.info("Download completed: status=" + status + " reason=" + reason + " mediaType=" + mediaType);

        boolean succeeded = (status == DownloadManager.STATUS_SUCCESSFUL);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            String mimeType = mgr.getMimeTypeForDownloadedFile(id);
            if (!"application/x-crossword".equals(mimeType)) {
                LOG.warning("Bad mime type for downloaded file: " + mimeType);
                succeeded = false;
            }
        }

        AndroidVersionUtils.Factory.getInstance().onFileDownloaded(id, succeeded);
    }
}
