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
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper.IDAndFilename;
import com.adamrosenfield.wordswithcrosses.net.Downloader;
import com.adamrosenfield.wordswithcrosses.net.Downloaders;
import com.adamrosenfield.wordswithcrosses.puz.PuzzleMeta;
import com.adamrosenfield.wordswithcrosses.view.CustomFastScrollView;
import com.adamrosenfield.wordswithcrosses.view.SeparatedListAdapter;
import com.adamrosenfield.wordswithcrosses.view.VerticalProgressBar;

public class BrowseActivity extends WordsWithCrossesActivity implements OnItemClickListener {

    private String MENU_CROSSWORDS;
    private String MENU_ARCHIVES;
    private String MENU_ARCHIVE;
    private String MENU_UNARCHIVE;
    private String MENU_DELETE;
    private String MENU_DOWNLOAD;
    private String MENU_SORT;
    private String MENU_BYDATE_ASCENDING;
    private String MENU_BYDATE_DESCENDING;
    private String MENU_BYSOURCE;
    private String MENU_BYAUTHOR;
    private String MENU_CLEANUP;
    private String MENU_HELP;
    private String MENU_SETTINGS;
    private String PREF_SENDDEBUGPACKAGE;

    private static final int DOWNLOAD_DIALOG_ID = 0;
    private SortOrder sortOrder = SortOrder.DATE_DESC;
    private BaseAdapter currentAdapter = null;
    private DownloadPickerDialogBuilder downloadPickerDialogBuilder;
    private Dialog mDownloadDialog;

    /** Puzzle for which a context menu is currently open */
    private PuzzleMeta contextPuzzle = null;

    /** Most recently opened puzzle */
    private PuzzleMeta lastOpenedPuzzle = null;

    private Handler handler = new Handler();
    private List<String> sourceList = new ArrayList<>();
    private CustomFastScrollView fastScrollView;
    private ListView puzzleList;
    private ListView sources;
    private MenuItem archiveMenuItem;
    private NotificationManager nm;
    private View lastOpenedView = null;
    private boolean viewArchive;

    private boolean hasShownSDCardWarning = false;

    /** Number of threads which are currently downloading puzzles */
    private int downloadingThreads = 0;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(MENU_DELETE)) {
            deletePuzzle(contextPuzzle);
            render();

            return true;
        } else if (item.getTitle().equals(MENU_ARCHIVE)) {
            archivePuzzle(contextPuzzle, true);
            render();

            return true;
        } else if (item.getTitle().equals(MENU_UNARCHIVE)) {
            archivePuzzle(contextPuzzle, false);
            render();

            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        } catch (ClassCastException e) {
            Log.e("wordswithcrosses", "bad menuInfo", e);
            return;
        }

        // Ignore selection if user selected a separator
        Object item = puzzleList.getAdapter().getItem(info.position);
        if (!(item instanceof PuzzleMeta)) {
            return;
        }

        contextPuzzle = ((PuzzleMeta)item);

        menu.setHeaderTitle(contextPuzzle.title);

        menu.add(MENU_DELETE);
        archiveMenuItem = menu.add(viewArchive ? MENU_UNARCHIVE : MENU_ARCHIVE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        System.setProperty("http.keepAlive", "false");

        utils.onActionBarWithText(menu.add(MENU_DOWNLOAD).setIcon(android.R.drawable.ic_menu_rotate));

        SubMenu sortMenu = menu.addSubMenu(MENU_SORT)
                               .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        sortMenu.add(MENU_BYDATE_DESCENDING)
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add(MENU_BYDATE_ASCENDING)
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add(MENU_BYSOURCE)
                .setIcon(android.R.drawable.ic_menu_upload);
        sortMenu.add(MENU_BYAUTHOR)
                .setIcon(android.R.drawable.ic_menu_edit);
        utils.onActionBarWithText(sortMenu);

        menu.add(MENU_CLEANUP)
            .setIcon(android.R.drawable.ic_menu_manage);
        menu.add(MENU_ARCHIVES)
            .setIcon(android.R.drawable.ic_menu_view);
        menu.add(MENU_HELP)
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add(MENU_SETTINGS)
            .setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        lastOpenedView = v;
        lastOpenedPuzzle = ((PuzzleMeta)v.getTag());

        // This can happen when selecting a section header with the keyboard
        if (lastOpenedPuzzle == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_EDIT, null, this, PlayActivity.class);
        intent.putExtra(PlayActivity.EXTRA_PUZZLE_ID, lastOpenedPuzzle.id);
        this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(MENU_DOWNLOAD)) {
            deprecatedShowDialog(DOWNLOAD_DIALOG_ID);

            return true;
        } else if (item.getTitle().equals(MENU_SETTINGS)) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle().equals(MENU_CROSSWORDS) ||
                   item.getTitle().equals(MENU_ARCHIVES)) {
            viewArchive = !viewArchive;
            item.setTitle(viewArchive ? MENU_CROSSWORDS : MENU_ARCHIVES);

            if (archiveMenuItem != null) {
                archiveMenuItem.setTitle(viewArchive ? MENU_UNARCHIVE : MENU_ARCHIVE);
            }

            render();

            return true;
        } else if (item.getTitle().equals(MENU_CLEANUP)) {
            this.cleanup();

            return true;
        } else if (item.getTitle().equals(MENU_HELP)) {
            showHTMLPage("filescreen.html");
        } else if (item.getTitle().equals(MENU_BYSOURCE)) {
            sortOrder = SortOrder.SOURCE_ASC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals(MENU_BYAUTHOR)) {
            sortOrder = SortOrder.AUTHOR_ASC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals(MENU_BYDATE_ASCENDING)) {
            sortOrder = SortOrder.DATE_ASC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals(MENU_BYDATE_DESCENDING)) {
            sortOrder = SortOrder.DATE_DESC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals(PREF_SENDDEBUGPACKAGE)){
            Intent i = WordsWithCrossesApplication.sendDebug(this);
            if (i != null) {
                this.startActivity(i);
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    void deprecatedShowDialog(int dialog) {
        showDialog(dialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == RESULT_OK) && (mDownloadDialog != null) && mDownloadDialog.isShowing()) {
            // If the user hit close in the browser download activity, we close the dialog.
            mDownloadDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MENU_CROSSWORDS = getResources().getString(R.string.menu_crosswords);
        MENU_ARCHIVES = getResources().getString(R.string.menu_archives);
        MENU_ARCHIVE = getResources().getString(R.string.menu_archive);
        MENU_UNARCHIVE = getResources().getString(R.string.menu_unarchive);
        MENU_DELETE = getResources().getString(R.string.menu_delete);
        MENU_DOWNLOAD = getResources().getString(R.string.menu_download);
        MENU_SORT = getResources().getString(R.string.menu_sort);
        MENU_BYDATE_ASCENDING = getResources().getString(R.string.menu_bydate_asc);
        MENU_BYDATE_DESCENDING = getResources().getString(R.string.menu_bydate_desc);
        MENU_BYSOURCE = getResources().getString(R.string.menu_bysource);
        MENU_BYAUTHOR = getResources().getString(R.string.menu_byauthor);
        MENU_CLEANUP = getResources().getString(R.string.menu_cleanup);
        MENU_HELP = getResources().getString(R.string.menu_help);
        MENU_SETTINGS = getResources().getString(R.string.menu_settings);
        PREF_SENDDEBUGPACKAGE = getResources().getString(R.string.pref_send_debug_package);

        this.setTitle("Puzzles - " + getResources().getString(R.string.app_name));
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        this.setContentView(R.layout.browse);
        this.fastScrollView = (CustomFastScrollView)this.findViewById(R.id.fastScrollView);
        this.puzzleList = (ListView)this.findViewById(R.id.puzzleList);
        this.puzzleList.setOnCreateContextMenuListener(this);
        this.puzzleList.setOnItemClickListener(this);
        this.sources = (ListView)this.findViewById(R.id.sourceList);
        upgradePreferences();
        this.nm = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        sortOrder = SortOrder.values()[prefs.getInt("sort", SortOrder.DATE_DESC.ordinal())];

        // Check that the SD card is mounted
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!hasShownSDCardWarning) {
                showSDCardHelp();
                hasShownSDCardWarning = true;
            } else {
                render();
            }
        } else if (!WordsWithCrossesApplication.getDatabaseHelper().hasAnyPuzzles() &&
                   TextUtils.isEmpty(prefs.getString("welcome_shown_release", "")))
        {
            // If this is the first time the user has launched the app, start
            // downloading a bunch of starter puzzles and show the welcome
            // page
            if (WordsWithCrossesApplication.makeDirs()) {
                updateLastDatabaseSyncTime();
                downloadStarterPuzzles();
            }

            // Also don't show the the release notes the first time the user
            // launched the app
            try {
                PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                Editor e = prefs.edit();
                e.putString("welcome_shown_release", pkgInfo.versionName);
                e.commit();
            } catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            showWelcomePage();
        } else {
            // Look up what the latest version of the app is which has shown
            // the welcome screen
            boolean showReleaseNotes = false;
            try {
                PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                String welcomeShownRelease = prefs.getString("welcome_shown_release", "");
                if (!welcomeShownRelease.equals(pkgInfo.versionName)) {
                    showReleaseNotes = true;

                    Editor e = prefs.edit();
                    e.putString("welcome_shown_release", pkgInfo.versionName);
                    e.commit();
                }
            } catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (showReleaseNotes) {
                // Show the release notes if this is the first time the user
                // launched this version of the app
                showHTMLPage("release.html");
            } else if (needDatabaseSync()) {
                // If there are files in the crosswords directory which aren't
                // known in the database, sync the database with the file
                // system (on a background thread)
                new Thread(new Runnable() {
                    public void run() {
                        syncDatabase();
                    }
                }).start();
            } else {
                // Normal case -- database is in sync and user has launched this
                // version before.  Try to download new puzzles if necessary.
                render();
                checkDownload();
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DOWNLOAD_DIALOG_ID:

            DownloadPickerDialogBuilder.OnDownloadSelectedListener downloadButtonListener = new DownloadPickerDialogBuilder.OnDownloadSelectedListener() {
                    public void onDownloadSelected(Calendar date, List<Downloader> downloaders, int selected) {
                        List<Downloader> toDownload = new LinkedList<>();

                        if (selected == 0) {
                            // Download all available.
                            toDownload.addAll(downloaders);
                            toDownload.remove(0);
                        } else {
                            // Only download selected.
                            Downloader downloader = downloaders.get(selected);
                            if (downloader.isManualDownload()) {
                                // Try to initiate the manual download
                                Intent intent = downloader.getManualDownloadIntent(date);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    // TODO: Display AlertDialog
                                }
                                return;
                            }
                            toDownload.add(downloader);
                        }

                        download(date, toDownload);
                    }
                };

            Calendar now = Calendar.getInstance();

            downloadPickerDialogBuilder = new DownloadPickerDialogBuilder(
                this,
                downloadButtonListener,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));

            mDownloadDialog = downloadPickerDialogBuilder.getInstance();

            return mDownloadDialog;
        }

        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
        case DOWNLOAD_DIALOG_ID:
            downloadPickerDialogBuilder.updatePuzzleSelect(null);
            break;
        }
    }

    /**
     * Called when the "Show All" checkbox in the download dialog is clicked
     */
    public void onDownloadDialogShowAllClicked(View view) {
        boolean checked = ((CheckBox)view).isChecked();
        downloadPickerDialogBuilder.showAllPuzzles(checked);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.currentAdapter == null) {
            this.render();
        } else {
            if (lastOpenedPuzzle != null) {
                VerticalProgressBar bar = (VerticalProgressBar)lastOpenedView.findViewById(R.id.puzzle_progress);
                bar.setPercentComplete(lastOpenedPuzzle.percentComplete);
            }
        }

        this.checkDownload();
    }

    private SeparatedListAdapter buildList() {
        // Get source filter, if any
        String sourceMatch = null;
        if (sources != null) {
            sourceMatch = ((SourceListAdapter) sources.getAdapter()).current;

            if (SourceListAdapter.ALL_SOURCES.equals(sourceMatch)) {
                sourceMatch = null;
            }
        }

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        ArrayList<PuzzleMeta> puzzles = dbHelper.queryPuzzles(sourceMatch, viewArchive, sortOrder);

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);
        FileAdapter lastAdapter = null;
        String lastGroup = null;

        for (PuzzleMeta puzzle : puzzles) {
            String group = sortOrder.getGroup(puzzle);
            if (lastGroup == null || !lastGroup.equals(group)) {
                lastAdapter = new FileAdapter();
                lastGroup = group;
                adapter.addSection(lastGroup, lastAdapter);
            }

            lastAdapter.addPuzzle(puzzle);
        }

        if (sources != null) {
            ArrayList<String> newSourceList = dbHelper.querySources();
            this.sourceList.clear();
            this.sourceList.addAll(newSourceList);

            this.handler.post(new Runnable() {
                public void run(){
                    ((SourceListAdapter)sources.getAdapter()).notifyDataSetInvalidated();
                }
            });
        }

        return adapter;
    }

    /**
     * Checks if the database needs to be synced with the file system
     *
     * @return True if the database needs to be synced
     */
    private boolean needDatabaseSync() {
        long folderTimestamp = WordsWithCrossesApplication.CROSSWORDS_DIR.lastModified();
        long lastDBSync = prefs.getLong(PREF_LAST_DB_SYNC_TIME, 0);
        return (folderTimestamp > lastDBSync);
    }

    /**
     * Syncs the database with the file system.  Any puzzle files found which
     * are not in the database are added to the database, and any puzzles in
     * the database which are missing in the file system are deleted from the
     * database.
     */
    private void syncDatabase() {
        long startTime = System.currentTimeMillis();

        // Get the list of .puz files in the crosswords directory
        File[] fileList = WordsWithCrossesApplication.CROSSWORDS_DIR.listFiles(
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".puz");
                }
            });
        if (fileList == null) {
            LOG.warning("Unable to enumerate directory: " + WordsWithCrossesApplication.CROSSWORDS_DIR);
            return;
        }

        // Sort the list of filenames
        ArrayList<String> filenameList = new ArrayList<>(fileList.length);
        for (File file : fileList) {
            filenameList.add(file.getAbsolutePath());
        }
        Collections.sort(
            filenameList,
            new Comparator<String>() {
                public int compare(String s1, String s2) {
                    return s1.compareTo(s2);
                }
            });

        // Get the list of filenames in the database
        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        List<PuzzleDatabaseHelper.IDAndFilename> dbFileList = dbHelper.getFilenameList();

        ArrayList<String> filesToAdd = new ArrayList<>();
        ArrayList<Long> filesToRemove = new ArrayList<>();

        // Pseudo-merge the two sorted lists to reconcile them
        int filenameIndex = 0;
        int dbIndex = 0;
        while (filenameIndex < filenameList.size() && dbIndex < dbFileList.size()) {
            int cmp = filenameList.get(filenameIndex).compareTo(dbFileList.get(dbIndex).filename);
            if (cmp == 0) {
                // File exists in both the file system and database, we're good
                filenameIndex++;
                dbIndex++;
            } else if (cmp < 0) {
                // File exists in the file system but not in the database, so
                // add it to the database
                filesToAdd.add(filenameList.get(filenameIndex));
                filenameIndex++;
            } else {
                // File exists in the database but not in the file system, so
                // remove it from the database
                filesToRemove.add(dbFileList.get(dbIndex).id);
                dbIndex++;
            }
        }

        while (filenameIndex < filenameList.size()) {
            filesToAdd.add(filenameList.get(filenameIndex));
            filenameIndex++;
        }

        while (dbIndex < dbFileList.size()) {
            filesToRemove.add(dbFileList.get(dbIndex).id);
            dbIndex++;
        }

        // Update the database accordingly
        String source = getResources().getString(R.string.source_unknown);
        for (String filename : filesToAdd) {
            File file = new File(filename);
            dbHelper.addPuzzle(file, source, "", file.lastModified());
        }

        dbHelper.removePuzzles(filesToRemove);

        updateLastDatabaseSyncTime();

        long durationMs = System.currentTimeMillis() - startTime;
        LOG.info("Database sync took " + durationMs + " ms");

        postRenderMessage();
    }

    private void checkDownload() {
        synchronized (this) {
            if (downloadingThreads > 0) {
                // No automatic downloads while other downloads are pending
                return;
            }
        }
        long lastDL = prefs.getLong("dlLast", 0);

        if (prefs.getBoolean("dlOnStartup", true) &&
                ((System.currentTimeMillis() - (long) (12 * 60 * 60 * 1000)) > lastDL)) {
            this.download(Calendar.getInstance(), null);
            prefs.edit()
                 .putLong("dlLast", System.currentTimeMillis())
                 .commit();
        }
    }

    private void cleanup() {
        long cleanupValue = Long.parseLong(prefs.getString("cleanupAge", "2")) + 1;
        long maxAge = (cleanupValue == 0) ? 0 : (System.currentTimeMillis() - (cleanupValue * 24 * 60 * 60 * 1000));
        boolean deleteOnCleanup = prefs.getBoolean("deleteOnCleanup", false);

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();

        if (deleteOnCleanup) {
            // Get list of puzzles to delete
            List<IDAndFilename> puzzles = dbHelper.getFilenamesToCleanup(maxAge);
            ArrayList<Long> ids = new ArrayList<>();

            // Delete the puzzles from the file system
            for (IDAndFilename idAndFilename : puzzles) {
                LOG.info("Deleting puzzle: " + idAndFilename.filename);
                ids.add(idAndFilename.id);

                File puzzleFile = new File(idAndFilename.filename);
                if (!puzzleFile.delete()) {
                    LOG.warning("Failed to delete puzzle: " + idAndFilename.filename);
                }
            }

            // Delete the puzzles form the database
            dbHelper.removePuzzles(ids);

            updateLastDatabaseSyncTime();
        } else {
            // Just mark the puzzles as archived
            int numArchived = dbHelper.archivePuzzles(maxAge);
            LOG.info("Archived " + numArchived + " puzzles");
        }

        render();
    }

    /**
     * Deletes the given puzzle from the file system and the database
     *
     * @param puzzle Puzzle to delete
     */
    private void deletePuzzle(PuzzleMeta puzzle) {
        LOG.info("Deleting puzzle: " + puzzle.filename);

        // Delete the puzzle from the file system
        File puzzleFile = new File(puzzle.filename);
        if (!puzzleFile.delete()) {
            LOG.warning("Failed to delete puzzle: " + puzzle.filename);
        }

        // Delete the puzzle from the database
        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        ArrayList<Long> idsToRemove = new ArrayList<>(1);
        idsToRemove.add(puzzle.id);
        dbHelper.removePuzzles(idsToRemove);
    }

    /**
     * Archives or un-archives the given puzzle
     *
     * @param puzzle Puzzle to archive or un-archive
     * @param archive True to archive or false to un-archive
     */
    private void archivePuzzle(PuzzleMeta puzzle, boolean archive) {
        LOG.info((archive ? "Archiving " : "Un-archiving ") + puzzle.filename);

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        dbHelper.archivePuzzle(puzzle.id, archive);
    }

    private void download(final Calendar date, final List<Downloader> downloaders) {
        new Thread(new Runnable() {
            public void run() {
                BrowseActivity.this.internalDownload(date, downloaders);
            }
        }).start();
    }

    private void internalDownload(Calendar date, List<Downloader> downloaders) {
        synchronized (this) {
            downloadingThreads++;
        }

        Downloaders dls = new Downloaders(this, false);
        dls.download(date, downloaders);

        synchronized (this) {
            downloadingThreads--;
        }

        postRenderMessage();
    }

    private void downloadStarterPuzzles() {
        new Thread(new Runnable() {
            public void run() {
                BrowseActivity.this.internalDownloadStarterPuzzles();
            }
        }).start();
    }

    private void internalDownloadStarterPuzzles() {
        synchronized (this) {
            downloadingThreads++;
        }

        Downloaders dls = new Downloaders(this, false);
        dls.enableIndividualDownloadNotifications(false);

        Calendar now = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Calendar date = (Calendar)now.clone();
            date.add(Calendar.DAY_OF_MONTH, -i);
            dls.download(date);

            postRenderMessage();
        }

        synchronized (this) {
            downloadingThreads--;
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public NotificationManager getNotificationManager() {
        return nm;
    }

    private void render() {
        if ((this.sources != null) && (this.sources.getAdapter() == null)) {
            final SourceListAdapter adapter = new SourceListAdapter(this, this.sourceList);
            this.sources.setAdapter(adapter);
            this.sources.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
                        String selected = (String) view.getTag();
                        adapter.current = selected;
                        adapter.notifyDataSetInvalidated();
                        render();
                    }
                });
        }

        puzzleList.setAdapter(buildList());
        fastScrollView.listItemsChanged();
    }

    /**
     * Posts a message to this object to render on the UI thread
     */
    public void postRenderMessage() {
        handler.post(new Runnable() {
            public void run() {
                BrowseActivity.this.render();
            }
        });
    }

    private void upgradePreferences() {
        if (this.prefs.getString("keyboardType", null) == null) {
            if (this.prefs.getBoolean("useNativeKeyboard", false)) {
                this.prefs.edit()
                          .putString("keyboardType", "NATIVE")
                          .commit();
            } else {
                Configuration config = getBaseContext().getResources().getConfiguration();

                if ((config.navigation == Configuration.NAVIGATION_NONAV) ||
                    (config.navigation == Configuration.NAVIGATION_UNDEFINED)) {
                    this.prefs.edit()
                              .putString("keyboardType", "CONDENSED_ARROWS")
                              .commit();
                } else {
                    this.prefs.edit()
                              .putString("keyboardType", "CONDENSED")
                              .commit();
                }
            }
        }
    }

    /**
     * Enumeration of supported puzzle sort orders
     */
    public enum SortOrder {
        // Do not reorder these, otherwise preference values (stored as the
        // enum's ordinal values) will be wrong
        DATE_ASC,
        DATE_DESC,
        SOURCE_ASC,

        AUTHOR_ASC {
            @Override
            public void sort(List<PuzzleMeta> puzzles) {
                for (PuzzleMeta puzzle : puzzles) {
                    puzzle.canonicalizeAuthor();
                }

                Collections.sort(puzzles, new Comparator<PuzzleMeta>() {
                    public int compare(PuzzleMeta puzzle1, PuzzleMeta puzzle2) {
                        int result = puzzle1.canonicalAuthor.compareToIgnoreCase(puzzle2.canonicalAuthor);
                        if (result != 0) {
                            return result;
                        }

                        result = puzzle1.date.compareTo(puzzle2.date);
                        if (result != 0) {
                            return result;
                        }

                        return puzzle1.source.compareTo(puzzle2.source);
                    }
                });
            }
        };

        public static final SimpleDateFormat DATE_FORMAT
            = new SimpleDateFormat("EEEE MMMM dd, yyyy", Locale.getDefault());

        /**
         * Gets the "ORDER BY" clause to be used for database queries with this
         * sort order
         *
         * @return The "ORDER BY" clause
         */
        public String getOrderByClause() {
            switch (this)
            {
            case DATE_ASC:
                return PuzzleDatabaseHelper.COLUMN_DATE + " ASC," + PuzzleDatabaseHelper.COLUMN_SOURCE + " ASC";
            case DATE_DESC:
                return PuzzleDatabaseHelper.COLUMN_DATE + " DESC," + PuzzleDatabaseHelper.COLUMN_SOURCE + " ASC";
            case SOURCE_ASC:
                return PuzzleDatabaseHelper.COLUMN_SOURCE + " ASC," + PuzzleDatabaseHelper.COLUMN_DATE + " ASC";
            case AUTHOR_ASC:
                // Author sorting is more complicated, so it's done in code
                // instead of in SQL
                return null;
            default:
                throw new IllegalArgumentException("Invalid sort order: " + this);
            }
        }

        /**
         * Gets the group under which the given puzzle should be sorted
         *
         * @param puzzle Puzzle to sort
         *
         * @return Group under which the given puzzle should be sorted
         */
        public String getGroup(PuzzleMeta puzzle) {
            switch (this)
            {
            case DATE_ASC:
            case DATE_DESC:
                return DATE_FORMAT.format(puzzle.date.getTime());

            case SOURCE_ASC:
                return puzzle.source;

            case AUTHOR_ASC:
                return puzzle.canonicalAuthor;

            default:
                throw new IllegalArgumentException("Invalid sort order: " + this);
            }
        }

        /**
         * For SortOrders which cannot by sorted by an "ORDER BY" clause in
         * SQL, this Sorts the given list of puzzles according to this
         * SortOrder.  Otherwise, this does nothing.
         */
        public void sort(List<PuzzleMeta> puzzles) {
        }
    }

    private static final SimpleDateFormat FILE_ADAPTER_DATE_FORMAT
        = new SimpleDateFormat("EEEE\nMMM dd, yyyy", Locale.getDefault());

    private class FileAdapter extends BaseAdapter {
        private final ArrayList<PuzzleMeta> puzzles = new ArrayList<>();

        public FileAdapter() {
        }

        public int getCount() {
            return puzzles.size();
        }

        public Object getItem(int i) {
            return puzzles.get(i);
        }

        public long getItemId(int arg) {
            return arg;
        }

        public View getView(int i, View view, ViewGroup group) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.puzzle_list_item, null);
            }

            PuzzleMeta puzzle = puzzles.get(i);
            view.setTag(puzzle);

            TextView date = (TextView)view.findViewById(R.id.puzzle_date);

            date.setText(FILE_ADAPTER_DATE_FORMAT.format(puzzle.date.getTime()));

            if (sortOrder == SortOrder.DATE_ASC ||
                sortOrder == SortOrder.DATE_DESC) {
                date.setVisibility(View.GONE);
            } else {
                date.setVisibility(View.VISIBLE);
            }

            TextView title = (TextView)view.findViewById(R.id.puzzle_name);
            title.setText(puzzle.source);

            VerticalProgressBar bar = (VerticalProgressBar)view.findViewById(R.id.puzzle_progress);
            bar.setPercentComplete(puzzle.percentComplete);

            TextView caption = (TextView)view.findViewById(R.id.puzzle_caption);
            caption.setText(puzzle.title);

            return view;
        }

        public void addPuzzle(PuzzleMeta puzzle) {
            puzzles.add(puzzle);
        }
    }
}
