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
import java.util.logging.Logger;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper.IDAndFilename;
import com.adamrosenfield.wordswithcrosses.net.Downloader;
import com.adamrosenfield.wordswithcrosses.net.Downloaders;
import com.adamrosenfield.wordswithcrosses.puz.PuzzleMeta;
import com.adamrosenfield.wordswithcrosses.view.SeparatedListAdapter;
import com.adamrosenfield.wordswithcrosses.view.VerticalProgressBar;
import com.adamrosenfield.wordswithcrosses.wordswithcrosses.R;

public class BrowseActivity extends WordsWithCrossesActivity implements OnItemClickListener {

    private static Logger LOG;

    private static final String MENU_ARCHIVES = "Archives";
    private static final int DOWNLOAD_DIALOG_ID = 0;
    private SortOrder sortOrder = SortOrder.DATE_DESC;
    private BaseAdapter currentAdapter = null;
    private Dialog mDownloadDialog;

    /** Puzzle for which a context menu is currently open */
    private PuzzleMeta contextPuzzle = null;

    /** Most recently opened puzzle */
    private PuzzleMeta lastOpenedPuzzle = null;

    private Handler handler = new Handler();
    private List<String> sourceList = new ArrayList<String>();
    private ListView puzzleList;
    private ListView sources;
    private MenuItem archiveMenuItem;
    private NotificationManager nm;
    private View lastOpenedView = null;
    private boolean viewArchive;

    private boolean hasShownSDCardWarning = false;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Delete")) {
            deletePuzzle(contextPuzzle);
            render();

            return true;
        } else if (item.getTitle().equals("Archive")) {
            archivePuzzle(contextPuzzle, true);
            render();

            return true;
        } else if (item.getTitle().equals("Un-archive")) {
            archivePuzzle(contextPuzzle, false);
            render();

            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        } catch (ClassCastException e) {
            Log.e("com.adamrosenfield.wordswithcrosses", "bad menuInfo", e);
            return;
        }

        contextPuzzle = ((PuzzleMeta)puzzleList.getAdapter().getItem(info.position));

        menu.setHeaderTitle(contextPuzzle.title);

        menu.add("Delete");
        archiveMenuItem = menu.add(viewArchive ? "Un-archive" : "Archive");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        System.setProperty("http.keepAlive", "false");
        utils.onActionBarWithText(menu.add("Download").setIcon(android.R.drawable.ic_menu_rotate));

        SubMenu sortMenu = menu.addSubMenu("Sort")
                               .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        sortMenu.add("By Date (Descending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Date (Ascending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Source")
                .setIcon(android.R.drawable.ic_menu_upload);
        utils.onActionBarWithText(sortMenu);

        menu.add("Cleanup")
            .setIcon(android.R.drawable.ic_menu_manage);
        menu.add(MENU_ARCHIVES)
            .setIcon(android.R.drawable.ic_menu_view);
        menu.add("Help")
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings")
            .setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        lastOpenedView = v;
        lastOpenedPuzzle = ((PuzzleMeta)v.getTag());

        File puzFile = new File(lastOpenedPuzzle.filename);
        Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this, PlayActivity.class);
        this.startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Download")) {
            deprecatedShowDialog(DOWNLOAD_DIALOG_ID);

            return true;
        } else if (item.getTitle().equals("Settings")) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle().equals("Crosswords") ||
                   item.getTitle().equals(MENU_ARCHIVES)) {
            viewArchive = !viewArchive;
            item.setTitle(viewArchive ? "Crosswords" : MENU_ARCHIVES);

            if (archiveMenuItem != null) {
                archiveMenuItem.setTitle(viewArchive ? "Un-archive" : "Archive");
            }

            render();

            return true;
        } else if (item.getTitle().equals("Cleanup")) {
            this.cleanup();

            return true;
        } else if (item.getTitle().equals("Help")) {
            showHTMLPage("filescreen.html");
        } else if (item.getTitle().equals("By Source")) {
            sortOrder = SortOrder.SOURCE_ASC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals("By Date (Ascending)")) {
            sortOrder = SortOrder.DATE_ASC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if (item.getTitle().equals("By Date (Descending)")) {
            sortOrder = SortOrder.DATE_DESC;
            prefs.edit()
                 .putInt("sort", sortOrder.ordinal())
                 .commit();
            render();
        } else if("Send Debug Package".equals(item.getTitle())){
        	Intent i = WordsWithCrossesApplication.sendDebug();
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
        if ((resultCode == RESULT_OK) && (mDownloadDialog != null) && mDownloadDialog.isShowing()) {
            // If the user hit close in the browser download activity, we close the dialog.
            mDownloadDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LOG = Logger.getLogger(getPackageName());

        this.setTitle("Puzzles - Words With Crosses");
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        this.setContentView(R.layout.browse);
        this.puzzleList = (ListView) this.findViewById(R.id.puzzleList);
        this.puzzleList.setOnCreateContextMenuListener(this);
        this.puzzleList.setOnItemClickListener(this);
        this.sources = (ListView) this.findViewById(R.id.sourceList);
        upgradePreferences();
        this.nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        sortOrder = SortOrder.values()[prefs.getInt("sort", SortOrder.DATE_DESC.ordinal())];

        // Check that the SD card is mounted
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!hasShownSDCardWarning) {
                showSDCardHelp();
                hasShownSDCardWarning = true;
            } else {
                render();
            }
        } else if (!WordsWithCrossesApplication.getDatabaseHelper().hasAnyPuzzles()) {
            // If this is the first time the user has launched the app, start
            // downloading a bunch of starter puzzles and show the welcome
            // page
            if (WordsWithCrossesApplication.makeDirs()) {
                updateLastDatabaseSyncTime();
                downloadStarterPuzzles();
            }

            showWelcomePage();
        } else {
            // Look up what the latest version of the app is which has shown
            // the welcome screen
            boolean showWelcome = false;
            try {
                PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                String welcomeShownRelease = prefs.getString("welcome_shown_release", "");
                if (!welcomeShownRelease.equals(pkgInfo.versionName)) {
                    showWelcome = true;

                    Editor e = prefs.edit();
                    e.putString("welcome_shown_release", pkgInfo.versionName);
                    e.commit();
                }
            } catch(PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (showWelcome) {
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
                        List<Downloader> toDownload = new LinkedList<Downloader>();
                        System.out.println(selected + " of " + downloaders.size());

                        if (selected == 0) {
                            // Download all available.
                            toDownload.addAll(downloaders);
                            toDownload.remove(0);
                        } else {
                            // Only download selected.
                            toDownload.add(downloaders.get(selected));
                        }

                        download(date, toDownload);
                    }
                };

            Calendar now = Calendar.getInstance();

            DownloadPickerDialogBuilder dpd = new DownloadPickerDialogBuilder(this, downloadButtonListener,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH),
                    new Provider<Downloaders>() {
                        public Downloaders get() {
                            return new Downloaders(BrowseActivity.this, nm);
                        }
                    });

            mDownloadDialog = dpd.getInstance();

            return mDownloadDialog;
        }

        return null;
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

            this.handler.post(new Runnable(){
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
        ArrayList<String> filenameList = new ArrayList<String>(fileList.length);
        for (int i = 0; i < fileList.length; i++) {
            filenameList.add(fileList[i].getAbsolutePath());
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

        ArrayList<String> filesToAdd = new ArrayList<String>();
        ArrayList<Integer> filesToRemove = new ArrayList<Integer>();

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
        for (String filename : filesToAdd) {
            File file = new File(filename);
            dbHelper.addPuzzle(file, "", "", file.lastModified());
        }

        dbHelper.removePuzzles(filesToRemove);

        updateLastDatabaseSyncTime();

        long durationMs = System.currentTimeMillis() - startTime;
        LOG.info("Database sync took " + durationMs + " ms");

        postRenderMessage();
    }

    private void checkDownload() {
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
            ArrayList<Integer> ids = new ArrayList<Integer>();

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
        dbHelper.removePuzzles(new ArrayList<Integer>(puzzle.id));
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
        Downloaders dls = new Downloaders(this, nm);
        dls.download(date, downloaders);

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
        Downloaders dls = new Downloaders(this, nm);
        dls.suppressMessages(true);

        Calendar now = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            Calendar date = (Calendar)now.clone();
            date.add(Calendar.DAY_OF_MONTH, -i);
            dls.download(date);

            postRenderMessage();
        }
    }

    private void render() {
        if ((this.sources != null) && (this.sources.getAdapter() == null)) {
            final SourceListAdapter adapter = new SourceListAdapter(this, this.sourceList);
            this.sources.setAdapter(adapter);
            this.sources.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> list, View view, int arg2, long arg3) {
                        String selected = (String) view.getTag();
                        adapter.current = selected;
                        adapter.notifyDataSetInvalidated();
                        render();
                    }
                });
        }

        this.puzzleList.setAdapter(this.buildList());
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

    public static interface Provider<T> {
        T get();
    }

    /**
     * Enumeration of supported puzzle sort orders
     */
    public static enum SortOrder {
        DATE_ASC,
        DATE_DESC,
        SOURCE_ASC;

        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEEEEEEE MMM dd, yyyy");

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
            if (this == SOURCE_ASC) {
                return puzzle.source;
            } else {
                return DATE_FORMAT.format(puzzle.date.getTime());
            }
        }
    }

    private static final SimpleDateFormat FILE_ADAPTER_DATE_FORMAT = new SimpleDateFormat("EEEEEEEEE\n MMM dd, yyyy");

    private class FileAdapter extends BaseAdapter {
        private ArrayList<PuzzleMeta> puzzles = new ArrayList<PuzzleMeta>();

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
                LayoutInflater inflater = (LayoutInflater)BrowseActivity.this.getApplicationContext()
                                                                             .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.puzzle_list_item, null);
            }

            PuzzleMeta puzzle = puzzles.get(i);
            view.setTag(puzzle);

            TextView date = (TextView)view.findViewById(R.id.puzzle_date);

            date.setText(FILE_ADAPTER_DATE_FORMAT.format(puzzle.date.getTime()));

            if (sortOrder == SortOrder.SOURCE_ASC) {
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
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
