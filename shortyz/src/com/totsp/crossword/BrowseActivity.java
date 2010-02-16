package com.totsp.crossword;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.preference.PreferenceManager;

import android.util.Log;

import android.view.ContextMenu;

import android.view.ContextMenu.ContextMenuInfo;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.totsp.crossword.net.Downloaders;
import com.totsp.crossword.puz.IO;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;


public class BrowseActivity extends ListActivity {
    private static final int DATE_DIALOG_ID = 0;
    SharedPreferences prefs;
    private File archiveFolder = new File(Environment.getExternalStorageDirectory(),
            "crosswords/archive");
    private Handler handler = new Handler();
    private NotificationManager nm;
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
                System.out.println(year + " " + monthOfYear + " " + dayOfMonth);

                Date d = new Date(year - 1900, monthOfYear, dayOfMonth);
                download(d);

                
            }
        };

    private File contextFile;
    private File crosswordsFolder = new File(Environment.getExternalStorageDirectory(),
            "crosswords");
    private boolean viewArchive;

    
    private void download(final Date d){
    	new Thread(new Runnable() {
            public void run() {
                Downloaders dls = new Downloaders(prefs, nm,
                        BrowseActivity.this);
                dls.download(d);
                handler.post(new Runnable() {
                        public void run() {
                            BrowseActivity.this.render();
                        }
                    });
            }
        }).start();
    }
    
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

        contextFile = (File) getListAdapter().getItem(info.position);
        menu.setHeaderTitle(contextFile.getName());

        MenuItem i = menu.add("Delete");
        menu.add("Archive");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add("Download").setIcon(android.R.drawable.ic_menu_rotate);
        menu.add("Help").setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add("Archive").setIcon(android.R.drawable.ic_menu_view);
        menu.add("Cleanup").setIcon(android.R.drawable.ic_menu_manage);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Download")) {
            showDialog(DATE_DIALOG_ID);

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
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Select Puzzle:");
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        getListView().setOnCreateContextMenuListener(this);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("dlOnStartup", true) ){
        	this.download(new Date());
        }
        this.nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        render();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:

            Date d = new Date();

            return new DatePickerDialog(this, dateSetListener,
                d.getYear() + 1900, d.getMonth(), d.getDate());
        }

        return null;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File puzFile = (File) v.getTag();
        Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this,
                PlayActivity.class);
        this.startActivity(i);
    }

    @Override
    protected void onResume() {
        this.render();
        super.onResume();
    }

    private void render() {
        this.setListAdapter(new FileAdapter(viewArchive ? this.archiveFolder
                                                        : this.crosswordsFolder));

        // getListView().setOnCreateContextMenuListener(this);
    }

    private class FileAdapter extends BaseAdapter {
        SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE\n MMM dd, yyyy");
        PuzzleMeta[] metas;
        File[] puzFiles;

        public FileAdapter(File directory) {
            directory.mkdirs();

            ArrayList<File> files = new ArrayList<File>();

            for (File f : directory.listFiles()) {
                if (f.getName().endsWith(".puz")) {
                    files.add(f);
                }
            }

            puzFiles = files.toArray(new File[files.size()]);
            metas = new PuzzleMeta[puzFiles.length];

            for (int i = 0; i < puzFiles.length; i++) {
                try {
                    metas[i] = IO.meta(puzFiles[i]);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
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

            if (metas[i] != null) {
                date.setText(df.format(metas[i].date));
            } else {
                date.setText(df.format(new Date(puzFiles[i].lastModified())));
            }

            TextView title = (TextView) view.findViewById(R.id.puzzle_name);

            if (metas[i] != null) {
                title.setText(metas[i].source);
            } else {
                title.setText(puzFiles[i].getName());
            }

            ProgressBar bar = (ProgressBar) view.findViewById(R.id.puzzle_progress);

            if (metas[i] != null) {
                bar.setProgress(metas[i].percentComplete);
            } else {
                bar.setProgress(0);
            }

            TextView caption = (TextView) view.findViewById(R.id.puzzle_caption);

            if (metas[i] != null) {
                caption.setText(metas[i].title);
            } else {
                caption.setText("");
            }

            return view;
        }
    }
}
