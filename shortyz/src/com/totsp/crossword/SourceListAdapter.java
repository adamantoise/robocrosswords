package com.totsp.crossword;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.totsp.crossword.shortyz.R;

public class SourceListAdapter extends BaseAdapter {
	private static final int transparent = Color.TRANSPARENT;
	private static final int highlight = Color.BLACK;
	static final String ALL_SOURCES = "All Sources";
	private List<String> sources;
	private final Context context;
	public String current = ALL_SOURCES;
	
	public SourceListAdapter(Context context, List<String> sources){
		this.sources = sources;
		this.context = context;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return sources.size() + 1;
	}

	public Object getItem(int i) {
		if(i==0){
			return ALL_SOURCES;
		} else {
			return sources.get(i-1);
		}
	}

	public long getItemId(int i) {
		return i;
	}

	public View getView(int index, View view, ViewGroup group) {
		if(view == null){
			LayoutInflater inflater =
                (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.source_item, null); 
		}
		
		TextView text = (TextView) view;
		String value = index == 0 ? ALL_SOURCES : sources.get(index - 1);
		if(value.equals(this.current)){
			text.setBackgroundColor(highlight);
		} else {
			text.setBackgroundColor(transparent);
		}
		text.setText(value);
		text.setTag(value);
		
		return text;
	}

}
