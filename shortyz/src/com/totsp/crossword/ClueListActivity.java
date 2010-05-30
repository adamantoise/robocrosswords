package com.totsp.crossword;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.view.ScrollingImageView;
import com.totsp.crossword.view.ScrollingImageView.ClickListener;
import com.totsp.crossword.view.ScrollingImageView.Point;


public class ClueListActivity extends Activity {
    private ImaginaryTimer timer;
    private ListView across;
    private ListView down;
    private ScrollingImageView imageView;
    private TabHost tabHost;
    private SharedPreferences prefs;
    private Configuration configuration;
    private KeyboardView keyboardView = null;
    private boolean useNativeKeyboard = false;
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;

        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.configuration = getBaseContext().getResources().getConfiguration();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.timer = new ImaginaryTimer(PlayActivity.BOARD.getPuzzle().getTime());
        timer.start();
        setContentView(R.layout.clue_list);
        
        Keyboard keyboard = new Keyboard(this, R.xml.keyboard);
    	keyboardView = (KeyboardView) this.findViewById(R.id.clueKeyboard);
    	keyboardView.setKeyboard(keyboard);
    	
    	keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener(){

			public void onKey(int primaryCode, int[] keyCodes) {
				System.out.println("Got key "+ ((char) primaryCode)+" "+ primaryCode);
				long eventTime = System.currentTimeMillis();
				KeyEvent event = new KeyEvent(eventTime, eventTime,
					    KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
					    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
				ClueListActivity.this.onKeyDown(primaryCode, event);
			}

			public void onPress(int primaryCode) {
				
			}

			public void onRelease(int primaryCode) {
				
			}

			public void onText(CharSequence text) {
				// TODO Auto-generated method stub
				
			}

			public void swipeDown() {
				// TODO Auto-generated method stub
				
			}

			public void swipeLeft() {
				long eventTime = System.currentTimeMillis();
				KeyEvent event = new KeyEvent(eventTime, eventTime,
					    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
					    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
				ClueListActivity.this.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, event);
			}

			public void swipeRight() {
				long eventTime = System.currentTimeMillis();
				KeyEvent event = new KeyEvent(eventTime, eventTime,
					    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
					    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
				ClueListActivity.this.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, event);
			}

			public void swipeUp() {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	
        
    	this.useNativeKeyboard = prefs.getBoolean("useNativeKeyboard", false);
    	if(this.useNativeKeyboard){
    		keyboardView.setVisibility(View.GONE);
    	}
        
        
        
        this.imageView = (ScrollingImageView) this.findViewById(R.id.miniboard);
        
        this.imageView.setContextMenuListener(new ClickListener(){

			public void onContextMenu(Point e) {
				// TODO Auto-generated method stub
				
			}

			public void onTap(Point e) {
				Word current = PlayActivity.BOARD.getCurrentWord();
				Position p = current.start;
				int box = PlayActivity.RENDERER.findBoxNoScale(e);
				System.out.println("box "+box);
				if(box < current.length){
					if(tabHost.getCurrentTab() == 0)
						p.across += box;
					else
						p.down += box;
				}
				if(!p.equals(PlayActivity.BOARD.getHighlightLetter())){
	                Word old = PlayActivity.BOARD.setHighlightLetter(p);
	                //PlayActivity.BOARD.setAcross(tabHost.getCurrentTab() == 0);
	                ClueListActivity.this.render();
				}
			}
        	
        });
        
        this.tabHost = (TabHost) this.findViewById(R.id.tabhost);
        this.tabHost.setup();

        TabSpec ts = tabHost.newTabSpec("TAB1");

        ts.setIndicator("Across",
            this.getResources().getDrawable(R.drawable.across));

        ts.setContent(R.id.acrossList);

        this.tabHost.addTab(ts);

        ts = this.tabHost.newTabSpec("TAB2");

        ts.setIndicator("Down", this.getResources().getDrawable(R.drawable.down));

        ts.setContent(R.id.downList);
        this.tabHost.addTab(ts);
        this.tabHost.setCurrentTab(PlayActivity.BOARD.isAcross() ? 0 : 1);

        this.across = (ListView) this.findViewById(R.id.acrossList);
        this.down = (ListView) this.findViewById(R.id.downList);

        across.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1,
                PlayActivity.BOARD.getAcrossClues()));
        down.setAdapter(new ArrayAdapter<Clue>(this,
                android.R.layout.simple_list_item_1,
                PlayActivity.BOARD.getDownClues()));

        across.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                    PlayActivity.BOARD.jumpTo(arg2, true);
                    imageView.scrollTo(0, 0);
                    render();
                    if(prefs.getBoolean("snapClue", false)){
                    	across.setSelectionFromTop(arg2, 5);
                    	across.setSelection(arg2);
                    }
                }
            });
        across.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                	if(!PlayActivity.BOARD.isAcross() || PlayActivity.BOARD.getCurrentClueIndex() != arg2 ){
                
	                    PlayActivity.BOARD.jumpTo(arg2, true);
	                    imageView.scrollTo(0, 0);
	                    render();
	                    if(prefs.getBoolean("snapClue", false)){
		                    across.setSelectionFromTop(arg2, 5);
		                    across.setSelection(arg2);
	                    }
                	}
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                   
                }
            }); 
        down.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                	
                    PlayActivity.BOARD.jumpTo(arg2, false);
                    imageView.scrollTo(0, 0);
                    render();
                    if(prefs.getBoolean("snapClue", false)){
	                    down.setSelectionFromTop(arg2, 5);
	                    down.setSelection(arg2);
                    }
                	
                }
            });
        
        down.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                	if(PlayActivity.BOARD.isAcross() || PlayActivity.BOARD.getCurrentClueIndex() != arg2 ){
	                    PlayActivity.BOARD.jumpTo(arg2, false);
	                    imageView.scrollTo(0, 0);
	                    render();
	                    if(prefs.getBoolean("snapClue", false)){
		                    down.setSelectionFromTop(arg2, 5);
		                    down.setSelection(arg2);
	                    }
                	}
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    
                }
            });
        this.render();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Word w = PlayActivity.BOARD.getCurrentWord();
    	Position last = new Position(w.start.across + ( w.across ? w.length -1 :0),
    			w.start.down + (!w.across ? w.length -1 : 0 ) );
    	
        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
        	return false;
        
        case KeyEvent.KEYCODE_BACK:
            System.out.println("BACK!!!");
            this.setResult(0);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
        	if(!PlayActivity.BOARD.getHighlightLetter().equals(PlayActivity.BOARD.getCurrentWord().start)){
        		PlayActivity.BOARD.previousLetter();
        	
	            this.render();
        	}
	            return true;
        	
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        	
        	if(!PlayActivity.BOARD.getHighlightLetter().equals(last)){
	            PlayActivity.BOARD.nextLetter();
	            this.render();
        	}

            return true;

        case KeyEvent.KEYCODE_DEL:
        	w = PlayActivity.BOARD.getCurrentWord();
            PlayActivity.BOARD.deleteLetter();
            Position p = PlayActivity.BOARD.getHighlightLetter();
            if(!w.checkInWord(p.across, p.down) ){
            	PlayActivity.BOARD.setHighlightLetter(w.start);
            }
            this.render();

            return true;
        }

        char c = Character.toUpperCase(  
        		this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO ||
        		this.useNativeKeyboard ? event.getDisplayLabel() :  ((char)keyCode));

        if (PlayActivity.ALPHA.indexOf(c) != -1) {
            PlayActivity.BOARD.playLetter(c);
            Position p = PlayActivity.BOARD.getHighlightLetter();
            if(!PlayActivity.BOARD.getCurrentWord().equals(w) || PlayActivity.BOARD.getBoxes()[p.across][p.down] == null ){
            	PlayActivity.BOARD.setHighlightLetter(last);
            }
            
            this.render();

            return true;
        }

        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            System.out.println("BACK");
            this.finish();

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        timer.stop();
        Puzzle puz = PlayActivity.BOARD.getPuzzle();
        try {
            if ((puz != null) && (PlayActivity.BASE_FILE != null)) {
            	if(puz.getPercentComplete() != 100){
	                this.timer.stop();
	                puz.setTime(timer.getElapsed());
            	}
                IO.save(puz, PlayActivity.BASE_FILE);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        PlayActivity.BOARD.getPuzzle().setTime(timer.getElapsed());
        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.imageView.getWindowToken(), 0);
        }

    }

    private void render() {
    	if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
        	
        	if(this.useNativeKeyboard){
	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	
	            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
	                InputMethodManager.HIDE_IMPLICIT_ONLY);
        	} else {
        		this.keyboardView.setVisibility(View.VISIBLE);
        	}
        	
        } else {
        	this.keyboardView.setVisibility(View.GONE);
        }
        for (Box b : PlayActivity.BOARD.getCurrentWordBoxes()) {
            System.out.print(b + " ");
        }

        System.out.println();
        this.imageView.setBitmap(PlayActivity.RENDERER.drawWord());
    }
}
