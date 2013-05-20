package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.io.JPZIO;

public class HttpDownloadActivity extends WordsWithCrossesActivity {

    private static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

    private Handler handler = new Handler();

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

    private void doDownload(String uriString, final String filename) {
        try {
            File finalDestFile = getDestFile(filename);
            File downloadDestFile = finalDestFile;

            // If we're asked to download a JPZ file, download it to a temporary
            // file
            boolean isJpz = false;
            if (filename.toLowerCase(Locale.US).endsWith(".jpz")) {
                isJpz = true;
                downloadDestFile = new File(WordsWithCrossesApplication.TEMP_DIR,
                        filename + "-" + System.currentTimeMillis());
            }

            utils.setContext(this);
            if (!utils.downloadFile(new URL(uriString), EMPTY_MAP, downloadDestFile, true, filename)) {
                throw new IOException("Download failed: " + uriString);
            }

            // If it's a JPZ file, convert it
            if (isJpz) {
                try {
                    JPZIO.convertJPZPuzzle(downloadDestFile, finalDestFile);
                } finally {
                    downloadDestFile.delete();
                }
            }

            // Add the puzzle to the database
            PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
            dbHelper.addPuzzle(finalDestFile,  "Downloaded puzzles", uriString, System.currentTimeMillis());
            updateLastDatabaseSyncTime();

            // Start playing the puzzle
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.fromFile(finalDestFile), this, PlayActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();

            handler.post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(
                        HttpDownloadActivity.this,
                        "Unabled to download from\n" + filename,
                        Toast.LENGTH_LONG);
                    toast.show();
                }
            });
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

        return new File(WordsWithCrossesApplication.CROSSWORDS_DIR,
                prefix + "-" + System.currentTimeMillis() + ".puz");
    }
}
