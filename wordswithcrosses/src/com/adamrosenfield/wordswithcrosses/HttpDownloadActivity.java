package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

public class HttpDownloadActivity extends WordsWithCrossesActivity {

    private static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

    public HttpDownloadActivity() {
        super(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Superclass will show the SD card help if the SD card is not mounted
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            finish();
            return;
        }

        final Uri uri = getIntent().getData();
        final String uriString = uri.toString();
        final String filename = uriString.substring(uriString.lastIndexOf('/') + 1);

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading...\n" + filename);
        dialog.setCancelable(false);

        new Thread(new Runnable() {
            public void run() {
                doDownload(uriString, filename);
            }
        }).start();

        finish();
    }

    private void doDownload(String uriString, String filename) {
        try {
            File destFile = getDestFile(filename);
            utils.setContext(this);
            if (!utils.downloadFile(new URL(uriString), EMPTY_MAP, destFile, true, filename)) {
                throw new IOException("Download failed: " + uriString);
            }

            // Add the puzzle to the database
            PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
            dbHelper.addPuzzle(destFile,  "Downloaded puzzles", uriString, System.currentTimeMillis());
            updateLastDatabaseSyncTime();

            // Start playing the puzzle
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.fromFile(destFile), this, PlayActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();

            Toast toast = Toast.makeText(this, "Unabled to download from\n" + filename, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private static File getDestFile(String filename) throws IOException {
        String prefix;
        int dot = filename.lastIndexOf('.');
        if (dot != -1) {
            prefix = filename.substring(0, dot);
        } else {
            prefix = filename;
        }

        return File.createTempFile(prefix, ".puz", WordsWithCrossesApplication.CROSSWORDS_DIR);
    }
}
