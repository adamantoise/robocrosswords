/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.adamrosenfield.wordswithcrosses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.io.JPZIO;

public class HttpDownloadActivity extends WordsWithCrossesActivity {

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

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
        String scheme = uri.getScheme();

        LOG.info("Attempting to download URI: " + uriString);
        LOG.info("MIME type: " + getIntent().getType());

        // Check if the puzzle is already in the database.  If so, skip the
        // download and start playing it.
        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        long existingId = dbHelper.getPuzzleIDForURL(uriString);
        if (existingId != -1) {
            LOG.info("Skipping download for " + uriString + ", already downloaded with ID=" + existingId);
            Intent intent = new Intent(Intent.ACTION_EDIT, null, this, PlayActivity.class);
            intent.putExtra(PlayActivity.EXTRA_PUZZLE_ID, existingId);
            startActivity(intent);
            finish();
            return;
        }

        // If we're opening a content URI (e.g. from an email attachment),
        // query the content provider to get the real filename
        String filename = null;
        if (scheme.equals("content")) {
            filename = getContentFilename(uri);
        }

        // For non-content URIs, or if getting the content filename failed,
        // use the last path component of the URI
        if (filename == null) {
            int slashIndex = uriString.lastIndexOf('/');
            if (slashIndex != -1) {
                filename = uriString.substring(slashIndex + 1);
            } else {
                filename = uriString;
            }
        }

        // Check the filename extension
        String extension = filename;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot != -1) {
            extension = filename.substring(lastDot);
        }
        String lowercaseExtension = extension.toLowerCase(Locale.US);

        if (!lowercaseExtension.equals(".puz") && !lowercaseExtension.equals(".jpz")) {
            LOG.warning("Unknown file extension: " + filename);
            String text = getResources().getString(R.string.unknown_extension) + extension;
            Toast toast = Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        final String filenameRef = filename;

        // For content and file URIs, open the input stream before finish()ing,
        // since otherwise we can sometimes lose the permission to open the
        // content stream.
        InputStream input = null;
        if (!scheme.equals("http") && !scheme.equals("https")) {
            try {
                input = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                notifyDownloadFailed(filenameRef);
                finish();
                return;
            }
        }

        final InputStream inputRef = input;

        final ProgressDialog dialog = new ProgressDialog(this);
        String text = getResources().getString(R.string.downloading_newline) + filenameRef;
        dialog.setMessage(text);
        dialog.setCancelable(false);
        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                doDownload(uri, filenameRef, inputRef);
                dialog.dismiss();
                finish();
            }
        }).start();
    }

    private String getContentFilename(Uri uri) {
        String filename = null;

        Cursor cursor = getContentResolver().query(
            uri,
            new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, // projection
            null,  // selection
            null,  // selectionArgs
            null); // sortOrder
        if (cursor == null) {
            return null;
        }

        if (cursor.moveToNext()) {
            filename = cursor.getString(0);
        }
        cursor.close();

        return filename;
    }

    private void doDownload(Uri uri, final String filename, InputStream input) {
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
                utils.downloadFile(new URL(uriString), EMPTY_MAP, downloadDestFile, true, filename);
            } else {
                // Otherwise, just open the content stream directly and save it
                LOG.info("Copying " + uriString + " ==> " + downloadDestFile);

                try {
                    FileOutputStream fos = new FileOutputStream(downloadDestFile);
                    try {
                        IO.copyStream(input, fos);
                    } finally {
                        fos.close();
                    }
                } finally {
                    input.close();
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
            String source = getResources().getString(R.string.source_download);
            long id = dbHelper.addPuzzle(finalDestFile, source, uriString, System.currentTimeMillis());
            updateLastDatabaseSyncTime();

            // Start playing the puzzle
            Intent intent = new Intent(Intent.ACTION_EDIT, null, this, PlayActivity.class);
            intent.putExtra(PlayActivity.EXTRA_PUZZLE_ID, id);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            notifyDownloadFailed(filename);
        }
    }

    private void notifyDownloadFailed(final String filename) {
        handler.post(new Runnable() {
            public void run() {
                String text = getResources().getString(R.string.download_failed_newline) + filename;
                Toast toast = Toast.makeText(
                    HttpDownloadActivity.this,
                    text,
                    Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private static File getDestFile(String filename) {
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
