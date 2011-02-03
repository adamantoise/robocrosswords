package com.totsp.crossword;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.shortyz.R;

public class ClueListAdapter extends BaseAdapter{

	private Clue[] clues;
	private boolean across;
	private Context context;
	private HashMap<Integer, Box[]> cache = new HashMap<Integer, Box[]>();
	
	public ClueListAdapter(Context context, Clue[] clues, boolean across){
		this.clues = clues;
		this.context = context;
		this.across = across;
	}
	
	public int getCount() {
		return clues.length;
	}

	public Object getItem(int i) {
		return clues[i];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {
		if(view == null){
			LayoutInflater inflater =
                (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.clue_detail_item, null); 
		}
		
		TextView line = (TextView) view.findViewById(R.id.clueLine);
		TextView word = (TextView) view.findViewById(R.id.clueWord);
		Clue c = this.clues[position];
		line.setText( c.number+". "+c.hint);
		Box[] boxes = this.cache.get(c.number);
		if(boxes == null){
			boxes = PlayActivity.BOARD.getWordBoxes(c.number, across);
			cache.put(c.number, boxes);
		}
		StringBuilder sb = new StringBuilder();
		for(Box b: boxes){
			if(b == null){
				continue;
			}
			sb.append( b.getResponse() == ' ' ? '_': b.getResponse()).append(' ');
		}
		sb.append(" ["+boxes.length+"]");
		word.setText(sb);
		return view;
	}
	
	public int indexOf(Clue clue){
		return Arrays.binarySearch(clues, clue, new Comparator<Clue>(){

			public int compare(Clue arg0, Clue arg1) {
				return ((Integer)arg0.number).compareTo(arg1.number);
			}
			
		});
	}

}
