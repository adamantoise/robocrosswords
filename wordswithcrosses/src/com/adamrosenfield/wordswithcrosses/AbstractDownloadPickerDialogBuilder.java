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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.adamrosenfield.wordswithcrosses.net.Downloader;
import com.adamrosenfield.wordswithcrosses.net.Downloaders;
import com.adamrosenfield.wordswithcrosses.net.DummyDownloader;

public abstract class AbstractDownloadPickerDialogBuilder {
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final BrowseActivity mActivity;
    private Dialog mDialog;
    protected List<Downloader> mAvailableDownloaders;

    protected Spinner mPuzzleSelect;
    private ArrayAdapter<Downloader> mAdapter;
    
    protected final ScrollView layout;

    private boolean mShowAll = false;
    
    final OnDownloadSelectedListener downloadButtonListener;
    
    public AbstractDownloadPickerDialogBuilder(BrowseActivity activity, OnDownloadSelectedListener downloadButtonListener, int layoutId, int rootId) {
        mActivity = activity;
        this.downloadButtonListener = downloadButtonListener;

        LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = (ScrollView)inflater.inflate(layoutId, (ViewGroup)mActivity.findViewById(rootId));
        
        mPuzzleSelect = (Spinner)layout.findViewById(R.id.puzzleSelect);

        mAdapter = new ArrayAdapter<Downloader>(mActivity, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPuzzleSelect.setAdapter(mAdapter);

        updatePuzzleSelect(null);

        OnClickListener clickHandler = createDownloadButtonClickListener();

        AlertDialog.Builder builder = (new AlertDialog.Builder(mActivity)).setPositiveButton("Download", clickHandler)
                                       .setNegativeButton("Cancel", null)
                                       .setTitle("Download Puzzles");

        builder.setView(layout);
        mDialog = builder.create();
    }
    
    protected abstract OnClickListener createDownloadButtonClickListener();
    protected abstract Calendar getCurrentDate();

    public Dialog getInstance() {
        return mDialog;
    }

    public void updatePuzzleSelect(String toSelect) {
        mAvailableDownloaders = new Downloaders(mActivity, mShowAll).getDownloaders(getCurrentDate());
        mAvailableDownloaders.add(0, new DummyDownloader(mActivity));

        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();

        int indexToSelect = 0, i =0;
        for (Downloader downloader : mAvailableDownloaders) {
            mAdapter.add(downloader);
            if (downloader.toString().equals(toSelect)) {
                indexToSelect = i;
            }

            i++;
        }
        mAdapter.notifyDataSetChanged();

        mPuzzleSelect.setSelection(indexToSelect, false);
    }

    public void showAllPuzzles(boolean showAll) {
        mShowAll = showAll;

        String curPuzzle = mPuzzleSelect.getSelectedItem().toString();
        updatePuzzleSelect(curPuzzle);
    }
    
    protected Set<Downloader> getSelectedDownloaders() {
        int which = mPuzzleSelect.getSelectedItemPosition();
        
        Set<Downloader> toDownload = which == 0 ?
                new HashSet<Downloader>(mAvailableDownloaders) : 
                Collections.singleton(mAvailableDownloaders.get(which));
                
        return toDownload;
    }

    
    public interface OnDownloadSelectedListener {
        void onDownloadSelected(Calendar date, Set<Downloader> downloaders);
        void onDownloadSelected(Calendar from, Calendar to, Set<Downloader> downloaders);
    }


}
