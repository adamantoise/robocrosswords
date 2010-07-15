package com.totsp.crossword;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.net.Downloader;
import com.totsp.crossword.net.Downloaders;
import com.totsp.crossword.net.Scrapers;
import com.totsp.crossword.puz.PuzzleMeta;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.view.SeparatedListAdapter;
import com.totsp.crossword.view.VerticalProgressBar;


public class BrowseActivity extends ListActivity {
    private static final int DOWNLOAD_DIALOG_ID = 0;
    private static final long DAY = 24L * 60L * 60L * 1000L;
    SharedPreferences prefs;

    private Accessor accessor = Accessor.DATE_DESC;
    private BaseAdapter currentAdapter = null;
    private File archiveFolder = new File(Environment.getExternalStorageDirectory(),
            "crosswords/archive");
    private File contextFile;
    private File crosswordsFolder = new File(Environment.getExternalStorageDirectory(),
            "crosswords");
    private FileHandle lastOpenedHandle = null;
    private Handler handler = new Handler();
    private NotificationManager nm;
    private View lastOpenedView = null;
    private boolean viewArchive;
    private Dialog mDownloadDialog;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        File meta = new File(this.contextFile.getParent(),
                contextFile.getName()
                           .substring(0, contextFile.getName().lastIndexOf(".")) +
                ".shortyz");

        if (item.getTitle().equals("Delete")) {
            this.contextFile.delete();

            if (meta.exists()) {
                meta.delete();
            }

            render();

            return true;
        } else if (item.getTitle().equals("Archive")) {
            this.archiveFolder.mkdirs();
            this.contextFile.renameTo(new File(this.archiveFolder,
                    this.contextFile.getName()));
            meta.renameTo(new File(this.archiveFolder, meta.getName()));
            render();

            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
        ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e("com.totsp.crossword", "bad menuInfo", e);

            return;
        }

        contextFile = ((FileHandle) getListAdapter().getItem(info.position)).file;
        menu.setHeaderTitle(contextFile.getName());

        menu.add("Delete");
        menu.add("Archive");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add("Download").setIcon(android.R.drawable.ic_menu_rotate);

        Menu sortMenu = menu.addSubMenu("Sort")
                            .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
        sortMenu.add("By Date (Descending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Date (Ascending)")
                .setIcon(android.R.drawable.ic_menu_day);
        sortMenu.add("By Source").setIcon(android.R.drawable.ic_menu_upload);

        menu.add("Cleanup").setIcon(android.R.drawable.ic_menu_manage);
        menu.add("Archive").setIcon(android.R.drawable.ic_menu_view);
        menu.add("Help").setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings").setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Download")) {
            showDialog(DOWNLOAD_DIALOG_ID);

            return true;
        } else if (item.getTitle().equals("Settings")) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle().equals("Crosswords") ||
                item.getTitle().equals("Archive")) {
            this.viewArchive = !viewArchive;
            item.setTitle(viewArchive ? "Crosswords" : "Archive");
            render();

            return true;
        } else if (item.getTitle().equals("Cleanup")) {
            this.cleanup();

            return true;
        } else if (item.getTitle().equals("Help")) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("file:///android_asset/filescreen.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);
        } else if (item.getTitle().equals("By Source")) {
            this.accessor = Accessor.SOURCE;
            prefs.edit().putInt("sort", 2).commit();
            this.render();
        } else if (item.getTitle().equals("By Date (Ascending)")) {
            this.accessor = Accessor.DATE_ASC;
            prefs.edit().putInt("sort", 1).commit();
            this.render();
        } else if (item.getTitle().equals("By Date (Descending)")) {
            this.accessor = Accessor.DATE_DESC;
            prefs.edit().putInt("sort", 0).commit();
            this.render();
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Environment.MEDIA_MOUNTED.equals(
                    Environment.getExternalStorageState())) {
            showSDCardHelp();
            finish();

            return;
        }

        this.setTitle("Select Puzzle:");
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        getListView().setOnCreateContextMenuListener(this);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        upgradePreferences();
        this.nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        switch (prefs.getInt("sort", 0)) {
        case 2:
            this.accessor = Accessor.SOURCE;

            break;

        case 1:
            this.accessor = Accessor.DATE_ASC;

            break;

        default:
            this.accessor = Accessor.DATE_DESC;
        }

        if (!crosswordsFolder.exists()) {
            this.downloadTen();

            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("file:///android_asset/welcome.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);

            return;
        } else if (prefs.getBoolean("release_2.2.17", true)) {
            Editor e = prefs.edit();
            e.putBoolean("release_2.2.17", false);
            e.commit();

            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("file:///android_asset/release.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);

            return;
        }

        this.checkDownload();

        render();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DOWNLOAD_DIALOG_ID:      
            DownloadPickerDialog.OnDownloadSelectedListener downloadButtonListener = 
            		new DownloadPickerDialog.OnDownloadSelectedListener() {
				public void onDownloadSelected(Date d, List<Downloader> downloaders, int selected) {
					List<Downloader> toDownload = new LinkedList<Downloader>();
					boolean scrape;
					if (selected == 0) {
						// Download all available.
						toDownload.addAll(downloaders);
						toDownload.remove(0);
						scrape = true;
					} else {
						// Only download selected.
						toDownload.add(downloaders.get(selected));
						scrape = false;
					}
					download(d, toDownload, scrape);
				}
			};
			
            Date d = new Date();
            
            DownloadPickerDialog dpd = new DownloadPickerDialog(this, downloadButtonListener,
                    d.getYear() + 1900, d.getMonth(), d.getDate(), new Downloaders(prefs, nm, this));
            
            mDownloadDialog = dpd.getInstance();

            return mDownloadDialog;
        }

        return null;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        lastOpenedView = v;
        lastOpenedHandle = ((FileHandle) v.getTag());

        File puzFile = lastOpenedHandle.file;
        Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this,
                PlayActivity.class);
        this.startActivity(i);
    }

    @Override
    protected void onResume() {
        if (this.currentAdapter == null) {
            this.render();
        } else {
            if (lastOpenedHandle != null) {
                try {
                    lastOpenedHandle.meta = IO.meta(lastOpenedHandle.file);

                    VerticalProgressBar bar = (VerticalProgressBar) lastOpenedView.findViewById(R.id.puzzle_progress);

                    if (lastOpenedHandle.meta.updateable) {
                        bar.setPercentComplete(-1);
                    } else {
                        bar.setPercentComplete(lastOpenedHandle.getProgress());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        this.checkDownload();
        super.onResume();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK && mDownloadDialog != null && mDownloadDialog.isShowing()) {
    		// If the user hit close in the browser download activity, we close the dialog.
    		mDownloadDialog.dismiss();
    	}
    }

    private SeparatedListAdapter buildList(final Dialog dialog, File directory,
        Accessor accessor) {
        directory.mkdirs();

        long incept = System.currentTimeMillis();
        ArrayList<FileHandle> files = new ArrayList<FileHandle>();
        FileHandle[] puzFiles = null;

        if (!directory.exists()) {
            showSDCardHelp();

            return new SeparatedListAdapter(this);
        }

        for (File f : directory.listFiles()) {
            // if this is taking a while and we are off the EDT, pop up the dialog.
            if ((dialog != null) &&
                    ((System.currentTimeMillis() - incept) > 2000) &&
                    !dialog.isShowing()) {
                handler.post(new Runnable() {
                        public void run() {
                            try {
                                dialog.show();
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }

            if (f.getName().endsWith(".puz")) {
                PuzzleMeta m = null;

                try {
                    m = IO.meta(f);

                    //System.out.println("Read meta for "+f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                files.add(new FileHandle(f, m));
            }
        }

        puzFiles = files.toArray(new FileHandle[files.size()]);

        try {
            Arrays.sort(puzFiles, accessor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);
        String lastHeader = null;
        ArrayList<FileHandle> current = new ArrayList<FileHandle>();

        for (FileHandle handle : puzFiles) {
            String check = accessor.getLabel(handle);

            if (!((lastHeader == null) || lastHeader.equals(check))) {
                FileAdapter fa = new FileAdapter();
                fa.puzFiles = current.toArray(new FileHandle[current.size()]);
                adapter.addSection(lastHeader, fa);
                current = new ArrayList<FileHandle>();
            }

            lastHeader = check;
            current.add(handle);
        }

        if (lastHeader != null) {
            FileAdapter fa = new FileAdapter();
            fa.puzFiles = current.toArray(new FileHandle[current.size()]);
            adapter.addSection(lastHeader, fa);
            current = new ArrayList<FileHandle>();
        }

        return adapter;
    }

    private void checkDownload() {
        long lastDL = prefs.getLong("dlLast", 0);

        if (prefs.getBoolean("dlOnStartup", true) &&
                ((System.currentTimeMillis() - (long) (12 * 60 * 60 * 1000)) > lastDL)) {
            this.download(new Date(), null, true);
            prefs.edit().putLong("dlLast", System.currentTimeMillis()).commit();
        }
    }

    private void cleanup() {
        File directory = new File(Environment.getExternalStorageDirectory(),
                "crosswords");
        ArrayList<FileHandle> files = new ArrayList<FileHandle>();
        FileHandle[] puzFiles = null;

        for (File f : directory.listFiles()) {
            if (f.getName().endsWith(".puz")) {
                PuzzleMeta m = null;

                try {
                    m = IO.meta(f);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                files.add(new FileHandle(f, m));
            }
        }

        puzFiles = files.toArray(new FileHandle[files.size()]);

        ArrayList<FileHandle> toCleanup = new ArrayList<FileHandle>();
        Arrays.sort(puzFiles);
        files.clear();

        for (FileHandle h : puzFiles) {
            if (h.getProgress() == 100) {
                toCleanup.add(h);
            } else {
                files.add(h);
            }
        }

        for (int i = 9; i < files.size(); i++) {
            toCleanup.add(files.get(i));
        }

        for (FileHandle h : toCleanup) {
            File meta = new File(directory,
                    h.file.getName()
                          .substring(0, h.file.getName().lastIndexOf(".")) +
                    ".shortyz");

            if (prefs.getBoolean("deleteOnCleanup", false)) {
                h.file.delete();
                meta.delete();
            } else {
                h.file.renameTo(new File(this.archiveFolder, h.file.getName()));
                meta.renameTo(new File(this.archiveFolder, meta.getName()));
            }
        }

        render();
    }

    private void download(final Date d, final List<Downloader> downloaders, final boolean scrape) {
        final Downloaders dls =  new Downloaders(prefs, nm, this);
    	new Thread(new Runnable() {
                public void run() {
                	
                	dls.download(d, downloaders);
                	
                	if (scrape) {
	                    Scrapers scrapes = new Scrapers(prefs, nm,
	                            BrowseActivity.this);
	                    scrapes.scrape();
                	}

                    handler.post(new Runnable() {
                            public void run() {
                                BrowseActivity.this.render();
                            }
                        });
                }
            }).start();
    }

    private void downloadTen() {
        new Thread(new Runnable() {
                public void run() {
                    Downloaders dls = new Downloaders(prefs, nm,
                            BrowseActivity.this);
                    Scrapers scrapes = new Scrapers(prefs, nm,
                            BrowseActivity.this);
                    scrapes.scrape();

                    Date d = new Date();

                    for (int i = 0; i < 10; i++) {
                        d = new Date(d.getTime() - DAY);
                        dls.download(d);
                        handler.post(new Runnable() {
                                public void run() {
                                    BrowseActivity.this.render();
                                }
                            });
                    }
                }
            }).start();
    }

    private void render() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);

        final File directory = viewArchive ? BrowseActivity.this.archiveFolder
                                           : BrowseActivity.this.crosswordsFolder;
        directory.mkdirs();
        //Only spawn a thread if there are a lot of puzzles.
        // Using SDK rev as a proxy to decide whether you have a slow processor or not.
        System.out.println("SDK VERSION: "+ android.os.Build.VERSION.SDK_INT);
        if ((android.os.Build.VERSION.SDK_INT >= 5 && directory.exists() && 
                directory.list().length > 500) ||
                (android.os.Build.VERSION.SDK_INT < 5 && directory.exists()  && directory.list().length > 160)) {
            Runnable r = new Runnable() {
                    public void run() {
                        currentAdapter = BrowseActivity.this.buildList(dialog,
                                directory, BrowseActivity.this.accessor);
                        BrowseActivity.this.handler.post(new Runnable() {
                                public void run() {
                                    BrowseActivity.this.setListAdapter(currentAdapter);

                                    if (dialog.isShowing()) {
                                        dialog.hide();
                                    }
                                }
                            });
                    }
                };

            new Thread(r).start();
        } else {
            this.setListAdapter(this.buildList(null, directory, accessor));
        }
    }

    private void showSDCardHelp() {
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse("file:///android_asset/sdcard.html"), this,
                HTMLActivity.class);
        this.startActivity(i);
    }

    private class FileAdapter extends BaseAdapter {
        SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE\n MMM dd, yyyy");
        FileHandle[] puzFiles;

        public FileAdapter() {
        }

        public int getCount() {
            return puzFiles.length;
        }

        public Object getItem(int i) {
            return puzFiles[i];
        }

        public long getItemId(int arg0) {
            return arg0;
        }

        public View getView(int i, View view, ViewGroup group) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) BrowseActivity.this.getApplicationContext()
                                                                              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.puzzle_list_item, null);
            }

            view.setTag(puzFiles[i]);

            TextView date = (TextView) view.findViewById(R.id.puzzle_date);

            date.setText(df.format(puzFiles[i].getDate()));

            if (accessor == Accessor.SOURCE) {
                date.setVisibility(View.VISIBLE);
            } else {
                date.setVisibility(View.GONE);
            }

            TextView title = (TextView) view.findViewById(R.id.puzzle_name);

            title.setText(puzFiles[i].getTitle());

            VerticalProgressBar bar = (VerticalProgressBar) view.findViewById(R.id.puzzle_progress);

            bar.setPercentComplete(puzFiles[i].getProgress());

            TextView caption = (TextView) view.findViewById(R.id.puzzle_caption);

            caption.setText(puzFiles[i].getCaption());

            return view;
        }
    }
    
    
    private void upgradePreferences(){
    	
    	if(this.prefs.getString("keyboardType", null) == null){
    		if(this.prefs.getBoolean("useNativeKeyboard", false)){
    			this.prefs.edit().putString("keyboardType", "NATIVE").commit();
    		} else {
	    		Configuration config = getBaseContext().getResources().getConfiguration();
	    		if(config.navigation == Configuration.NAVIGATION_NONAV || config.navigation == Configuration.NAVIGATION_UNDEFINED){
	    			this.prefs.edit().putString("keyboardType", "CONDENSED_ARROWS").commit();
	    		} else {
	    			this.prefs.edit().putString("keyboardType", "CONDENSED").commit();
	    		}
    		}
    		
    		
    	}
    	
    }
    
}
