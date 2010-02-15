package com.totsp.crossword;

import android.app.ListActivity;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.ContextMenu;

import android.view.ContextMenu.ContextMenuInfo;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.totsp.crossword.puz.IO;
import com.totsp.crossword.puz.PuzzleMeta;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;


public class BrowseActivity extends ListActivity {
    private File contextFile;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Delete")) {
            this.contextFile.delete();
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        getListView().setOnCreateContextMenuListener(this);
        render();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File puzFile = (File) v.getTag();
        Intent i = new Intent(Intent.ACTION_EDIT, Uri.fromFile(puzFile), this,
                PlayActivity.class);
        this.startActivity(i);
    }

    private void render() {
        this.setListAdapter(new FileAdapter(
                new File(Environment.getExternalStorageDirectory(), "crosswords")));

        // getListView().setOnCreateContextMenuListener(this);
    }

    private class FileAdapter extends BaseAdapter {
        SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEE\n MMM dd, yyyy");
        PuzzleMeta[] metas;
        File[] puzFiles;

        public FileAdapter(File directory) {
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
