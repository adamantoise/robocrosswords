package com.adamrosenfield.wordswithcrosses.view;

import com.adamrosenfield.wordswithcrosses.DownloadPickerDialogBuilder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class DownloadPickerView extends ScrollView
{
    private DownloadPickerDialogBuilder mDownloadPicker;

    public DownloadPickerView(Context context)
    {
        super(context);
    }
    
    public DownloadPickerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public DownloadPickerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setDownloadPickerDialogBuilder(DownloadPickerDialogBuilder downloadPickerDialogBuilder)
    {
        mDownloadPicker = downloadPickerDialogBuilder;
    }
    
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right,  bottom);
        
        mDownloadPicker.onViewLayout();
    }
}
