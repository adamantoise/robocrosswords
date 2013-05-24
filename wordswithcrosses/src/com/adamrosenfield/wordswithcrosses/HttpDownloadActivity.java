package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import com.adamrosenfield.wordswithcrosses.io.IO;
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

        // Check if the puzzle is already in the database.  If so, skip the
        // download and start playing it.
        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        String existingFilename = dbHelper.getFilenameForURL(uriString);
        if (existingFilename != null) {
            LOG.info("Skipping download for " + uriString + ", already downloaded at " + existingFilename);
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.fromFile(new File(existingFilename)), this, PlayActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(
                this,
                "Downloading\n" + filename,
                Toast.LENGTH_SHORT);
            toast.show();

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Downloading...\n" + filename);
            dialog.setCancelable(false);

            new Thread(new Runnable() {
                public void run() {
                    doDownload(uri, filename);
                }
            }).start();
        }

        finish();
    }

    private void doDownload(Uri uri, final String filename) {
        String scheme = uri.getScheme();
        String uriString = uri.toString();
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

            if (scheme.equals("http") || scheme.equals("https")) {
                // If we're opening a HTTP(S) URI, download the file
                utils.setContext(this);
                if (!utils.downloadFile(new URL(uriString), EMPTY_MAP, downloadDestFile, true, filename)) {
                    throw new IOException("Download failed: " + uriString);
                }
            } else {
                // Otherwise, just open the content stream directly and save it
                LOG.info("Copying " + uriString + " ==> " + downloadDestFile);
                InputStream is = getContentResolver().openInputStream(uri);
                try {
                    FileOutputStream fos = new FileOutputStream(downloadDestFile);
                    try {
                        IO.copyStream(is, fos);
                    } finally {
                        fos.close();
                    }
                } finally {
                    is.close();
                }
            }

            // If it's a JPZ file, convert it
            if (isJpz) {
                LOG.info("Converting JPZ file " + downloadDestFile + " ==> " + finalDestFile);
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
            notifyDownloadFailed(filename);
        }
    }

    private void notifyDownloadFailed(final String filename) {
        handler.post(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(
                    HttpDownloadActivity.this,
                    "Unable to download\n" + filename,
                    Toast.LENGTH_LONG);
                toast.show();
            }
        });
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
