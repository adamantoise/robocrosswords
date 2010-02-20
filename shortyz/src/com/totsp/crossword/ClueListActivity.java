package com.totsp.crossword;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.view.ScrollingImageView;


public class ClueListActivity extends Activity {
    private ListView across;
    private ListView down;
    private TabHost tabHost;
    private ScrollingImageView imageView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.clue_list);
        this.imageView = (ScrollingImageView) this.findViewById(R.id.miniboard);
        
        this.tabHost = (TabHost) this.findViewById(R.id.tabhost);
        this.tabHost.setup();
        
        TabSpec ts = tabHost.newTabSpec("TAB1");

        ts.setIndicator("Across", this.getResources().getDrawable(R.drawable.across));

        ts.setContent(R.id.acrossList);

        this.tabHost.addTab(ts);

        ts = this.tabHost.newTabSpec("TAB2");

        ts.setIndicator("Down", this.getResources().getDrawable(R.drawable.down));

        ts.setContent(R.id.downList);
        this.tabHost.addTab(ts);
        this.tabHost.setCurrentTab(0);
        
        this.across = (ListView) this.findViewById(R.id.acrossList);
        this.down = (ListView) this.findViewById(R.id.downList);
        
        
        
        
        across.setAdapter(new ArrayAdapter<Clue>(this, android.R.layout.simple_list_item_1, PlayActivity.BOARD.getAcrossClues()));
        down.setAdapter(new ArrayAdapter<Clue>(this, android.R.layout.simple_list_item_1, PlayActivity.BOARD.getDownClues()));

        across.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				PlayActivity.BOARD.jumpTo(arg2, true);
				render();
			}
        	
        });
        across.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				PlayActivity.BOARD.jumpTo(arg2, true);
				render();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        down.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				PlayActivity.BOARD.jumpTo(arg2, false);
				render();
				
			}
        	
        });
        down.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				PlayActivity.BOARD.jumpTo(arg2, false);
				render();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
    }
    
    private void render(){
    	for(Box b :PlayActivity.BOARD.getCurrentWordBoxes()){
    	System.out.print(b+" ");
    	}
    	System.out.println();
    	this.imageView.setBitmap(PlayActivity.RENDERER.drawWord());
    }
    
   
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
    	switch (keyCode) {

        case KeyEvent.KEYCODE_BACK:
        	System.out.println("BACK");
        	this.finish();
        	return true;
    	}
    	return super.onKeyUp(keyCode, event);
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Word previous;

        switch (keyCode) {

        case KeyEvent.KEYCODE_BACK:
        	System.out.println("BACK!!!");
        	this.setResult(0);
        	return true;
        
        case KeyEvent.KEYCODE_DPAD_LEFT:
        	PlayActivity.BOARD.previousLetter();
        	this.render();
        	return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        	PlayActivity.BOARD.nextLetter();
        	this.render();
        	return true;
        case KeyEvent.KEYCODE_DEL:
            previous = PlayActivity.BOARD.deleteLetter();
            this.render();

            return true;
        }

        char c = Character.toUpperCase(event.getDisplayLabel());

        if (PlayActivity.ALPHA.indexOf(c) != -1) {
            previous = PlayActivity.BOARD.playLetter(c);
            this.render();

            return true;
        }

        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }
    
}
