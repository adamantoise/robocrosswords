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

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.net.Downloader;

/**
 * Custom dialog for choosing puzzles to download.
 */
public class DownloadPickerDialogBuilder extends AbstractDownloadPickerDialogBuilder {
    private TextView mDateLabel;
    private int mDayOfMonth;
    private int mMonthOfYear;
    private int mYear;
    private OnDateChangedListener dateChangedListener;   

    public DownloadPickerDialogBuilder(BrowseActivity activity, final OnDownloadSelectedListener downloadButtonListener, int year, int monthOfYear, int dayOfMonth) {
        super(activity, downloadButtonListener, R.layout.download_dialog, R.id.download_root); 
       
        mYear = year;
        mMonthOfYear = monthOfYear;
        mDayOfMonth = dayOfMonth;

        mDateLabel = (TextView)layout.findViewById(R.id.dateLabel);
        updateDateLabel();

        dateChangedListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String curPuzzle = mPuzzleSelect.getSelectedItem().toString();
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                updateDateLabel();
                updatePuzzleSelect(curPuzzle);
            }
        };

        DatePicker datePicker = (DatePicker)layout.findViewById(R.id.datePicker);
        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);

    }


    
    
    @Override
    protected OnClickListener createDownloadButtonClickListener() {
        return new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                downloadButtonListener.onDownloadSelected(getCurrentDate(), getSelectedDownloaders());
            }
        };
    }




    @Override
    protected Calendar getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonthOfYear, mDayOfMonth);
        return calendar;
    }

    private void updateDateLabel() {
        mDateLabel.setText(DATE_FORMAT.format(getCurrentDate().getTime()));
    }





   
}
