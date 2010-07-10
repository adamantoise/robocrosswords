package com.totsp.crossword;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.DatePicker.OnDateChangedListener;

import com.totsp.crossword.net.Downloader;
import com.totsp.crossword.net.Downloaders;
import com.totsp.crossword.net.DummyDownloader;
import com.totsp.crossword.shortyz.R;

/**
 * Custom dialog for choosing puzzles to download.
 */
public class DownloadPickerDialog {
	private static DateFormat df = new SimpleDateFormat("EEEE,");
	private Context mContext;
	private int mYear;
	private int mMonthOfYear;
	private int mDayOfMonth;
	private TextView mDateLabel;
	private Dialog mDialog;
	private Spinner mPuzzleSelect;
	private Downloaders mDownloaders;
	private List<Downloader> mAvailableDownloaders;
	
	public interface OnDownloadSelectedListener {
		void onDownloadSelected(Date date, List<Downloader> availableDownloaders, int selected);
	}
	
	private OnDateChangedListener dateChangedListener = new DatePicker.OnDateChangedListener() {	
		public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mYear = year;
			mMonthOfYear = monthOfYear;
			mDayOfMonth = dayOfMonth;
			updateDateLabel();
			updatePuzzleSelect();
		}
	};
	
	public DownloadPickerDialog(Context c, final OnDownloadSelectedListener downloadButtonListener,
			int year, int monthOfYear, int dayOfMonth, Downloaders dls) {
		mContext = c;
		
		mYear = year;
		mMonthOfYear = monthOfYear;
		mDayOfMonth = dayOfMonth;
		
		mDownloaders = dls;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.download_dialog, null);
        
        mDateLabel = (TextView) layout.findViewById(R.id.dateLabel);
        updateDateLabel();

        DatePicker datePicker = (DatePicker) layout.findViewById(R.id.datePicker);
        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);
        
        mPuzzleSelect = (Spinner) layout.findViewById(R.id.puzzleSelect);
        updatePuzzleSelect();
        
        OnClickListener clickHandler = new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				downloadButtonListener.onDownloadSelected(getCurrentDate(), mAvailableDownloaders,
						mPuzzleSelect.getSelectedItemPosition());
			}
        };
        
        AlertDialog.Builder builder = (new AlertDialog.Builder(mContext))
        	.setPositiveButton("Download", clickHandler)
        	.setNegativeButton("Cancel", null)
        	.setTitle("Download Puzzles");
        
        builder.setView(layout);
        mDialog = builder.create();
	}
	
	private void updateDateLabel() {
		mDateLabel.setText(df.format(getCurrentDate()));
	}
	
	private void updatePuzzleSelect() {
		mAvailableDownloaders = mDownloaders.getDownloaders(getCurrentDate());
		mAvailableDownloaders.add(0, new DummyDownloader());
		ArrayAdapter<Downloader> adapter = new ArrayAdapter<Downloader>(mContext,
				android.R.layout.simple_spinner_item, mAvailableDownloaders);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mPuzzleSelect.setAdapter(adapter);
	}
	
	public Dialog getInstance() {
		return mDialog;
	}
	
	private Date getCurrentDate() {
		return new Date(mYear - 1900, mMonthOfYear, mDayOfMonth);
	}
}
