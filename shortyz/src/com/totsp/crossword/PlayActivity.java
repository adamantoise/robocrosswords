package com.totsp.crossword;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.MovementStrategy;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.view.PlayboardRenderer;
import com.totsp.crossword.view.ScrollingImageView;
import com.totsp.crossword.view.ScrollingImageView.ClickListener;
import com.totsp.crossword.view.ScrollingImageView.Point;
import com.totsp.crossword.view.ScrollingImageView.ScaleListener;


public class PlayActivity extends Activity {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private static final int INFO_DIALOG = 0;
    private static final int COMPLETE_DIALOG = 1;
    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Playboard BOARD;
    static PlayboardRenderer RENDERER;
    static File BASE_FILE;
    private AlertDialog completeDialog;
    private Configuration configuration;
    private Dialog dialog;
    private Handler handler = new Handler();
    private ImaginaryTimer timer;
    private Puzzle puz;
    private ScrollingImageView boardView;
    private SharedPreferences prefs;
    private TextView clue;
    private boolean useNativeKeyboard = false;
    private KeyboardView keyboardView = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            
        	if(this.useNativeKeyboard){
        		keyboardView.setVisibility(View.GONE);
	        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	
	            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
	                InputMethodManager.HIDE_NOT_ALWAYS);
        	} else {
        		this.keyboardView.setVisibility(View.VISIBLE);
        	} 
        } else {
        	this.keyboardView.setVisibility(View.GONE);
        }
    }
    
    private MovementStrategy getMovementStrategy(){
    	MovementStrategy movement = null;
    	String stratName = this.prefs.getString("movementStrategy", "MOVE_NEXT_ON_AXIS");
        System.out.println("Movement Strategy:"+stratName);
        if(stratName.equals("MOVE_NEXT_ON_AXIS")){
        	movement = MovementStrategy.MOVE_NEXT_ON_AXIS;
        } else if( stratName.equals("STOP_ON_END")){
        	movement = MovementStrategy.STOP_ON_END;
        } else if(stratName.equals("MOVE_NEXT_CLUE")){
        	movement = MovementStrategy.MOVE_NEXT_CLUE;
        } else if(stratName.equals("MOVE_PARALLEL_WORD")){
        	movement = MovementStrategy.MOVE_PARALLEL_WORD;
        }
        return movement;
        
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.configuration = getBaseContext().getResources().getConfiguration();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        MovementStrategy movement = this.getMovementStrategy();
        
        
        
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        try {
            Uri u = this.getIntent().getData();

            if (u != null) {
                if (u.getScheme().equals("file")) {
                    BASE_FILE = new File(u.getPath());
                    puz = IO.load(BASE_FILE);
                }
            }

            if (puz == null) {
                puz = IO.loadNative(this.getResources()
                                        .openRawResource(R.raw.test));
            }

            
            
            
            
            BOARD = new Playboard(puz, movement);
            RENDERER = new PlayboardRenderer(BOARD, metrics.density);
            
            float scale = prefs.getFloat("scale", metrics.density);
            RENDERER.setScale(scale);
            
            BOARD.setSkipCompletedLetters(this.prefs.getBoolean("skipFilled",
                    false));

            if (puz.getPercentComplete() != 100) {
                this.timer = new ImaginaryTimer(puz.getTime());
                this.timer.start();
            }

            setContentView(R.layout.main);
            
            Keyboard keyboard = new Keyboard(this, R.xml.keyboard);
        	keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
        	keyboardView.setKeyboard(keyboard);
        	
        	keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener(){
        		private long lastSwipe = 0;
				public void onKey(int primaryCode, int[] keyCodes) {
					long eventTime = System.currentTimeMillis();
					if(eventTime - lastSwipe < 500){
						return;
					}
					KeyEvent event = new KeyEvent(eventTime, eventTime,
						    KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
						    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
					PlayActivity.this.onKeyUp(primaryCode, event);
				}

				public void onPress(int primaryCode) {
					
				}

				public void onRelease(int primaryCode) {
					
				}

				public void onText(CharSequence text) {
					// TODO Auto-generated method stub
					
				}

				public void swipeDown() {
					long eventTime = System.currentTimeMillis();
					lastSwipe = eventTime;
					KeyEvent event = new KeyEvent(eventTime, eventTime,
						    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0, 0,
						    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
					PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_DOWN, event);
					
				}

				public void swipeLeft() {
					long eventTime = System.currentTimeMillis();
					lastSwipe = eventTime;
					KeyEvent event = new KeyEvent(eventTime, eventTime,
						    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
						    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
					PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, event);
				}

				public void swipeRight() {
					long eventTime = System.currentTimeMillis();
					lastSwipe = eventTime;
					KeyEvent event = new KeyEvent(eventTime, eventTime,
						    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
						    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
					PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, event);
				}

				public void swipeUp() {
					long eventTime = System.currentTimeMillis();
					lastSwipe = eventTime;
					KeyEvent event = new KeyEvent(eventTime, eventTime,
						    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, 0,
						    KeyEvent.FLAG_SOFT_KEYBOARD|KeyEvent.FLAG_KEEP_TOUCH_MODE);
					PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_UP, event);
				}
        		
        	});
        	
            
        	this.useNativeKeyboard = prefs.getBoolean("useNativeKeyboard", false);
        	if(this.useNativeKeyboard){
        		keyboardView.setVisibility(View.GONE);
        	}
        	
            
            this.clue = (TextView) this.findViewById(R.id.clueLine);

            this.clue.setClickable(true);
            this.clue.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        Intent i = new Intent(PlayActivity.this,
                                ClueListActivity.class);
                        PlayActivity.this.startActivityForResult(i, 0);
                    }
                });

            boardView = (ScrollingImageView) this.findViewById(R.id.board);

            this.registerForContextMenu(boardView);
            boardView.setContextMenuListener(new ClickListener() {
                    public void onContextMenu(final Point e) {
                        handler.post(new Runnable() {
                                public void run() {
                                    Position p = PlayActivity.RENDERER.findBox(e);
                                    Word w = PlayActivity.BOARD.setHighlightLetter(p);
                                    PlayActivity.RENDERER.draw(w);
                                    PlayActivity.this.openContextMenu(boardView);
                                }
                            });
                    }

                    public void onTap(Point e) {
                        Position p = PlayActivity.RENDERER.findBox(e);
                        Word old = PlayActivity.BOARD.setHighlightLetter(p);
                        PlayActivity.this.render(old);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dialog = new Dialog(this);
        dialog.setTitle("Puzzle Info");
        dialog.setContentView(R.layout.puzzle_info_dialog);

        completeDialog = new AlertDialog.Builder(this).create();
        completeDialog.setTitle("Puzzle Complete!");
        completeDialog.setMessage("");
        completeDialog.setButton("OK",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    return;
                }
            });

        this.boardView.setScaleListener( new ScaleListener(){
        	float scale;
        	TimerTask t;
        	Timer renderTimer = new Timer();
        	
			public void onScale(float newScale) {
				this.scale = newScale;
				if(t != null) t.cancel();
				t = new TimerTask(){

					@Override
					public void run() {
						handler.post(new Runnable(){

							public void run() {
								RENDERER.setLogicalScale(scale);
								render();
								boardView.scrollTo(0, 0);
							}
							
						});
						
					}
	        		
	        	};
				renderTimer.schedule(t, 500);
			}
        	
        });
        
        
        this.render();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
        ContextMenuInfo info) {
        System.out.println("CCM " + view);

        if (view == boardView) {
            menu.add("Zoom In");
            menu.add("Zoom Out");
            menu.add("Zoom Reset");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Show Errors").setIcon(android.R.drawable.ic_menu_view)
            .setCheckable(true);

        Menu reveal = menu.addSubMenu("Reveal")
                          .setIcon(android.R.drawable.ic_menu_view);
        menu.add("Clues").setIcon(android.R.drawable.ic_menu_agenda);
        reveal.add("Letter");
        reveal.add("Word");
        reveal.add("Puzzle");
        menu.add("Info").setIcon(android.R.drawable.ic_menu_info_details);
        menu.add("Help").setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings").setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }
    
    private long lastKey;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Word previous;

        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
        	return false;
        
        case KeyEvent.KEYCODE_DPAD_DOWN:
        	if(System.currentTimeMillis() - lastKey > 50){
        		previous = PlayActivity.BOARD.moveDown();
                this.render(previous);
        	}
        	lastKey = System.currentTimeMillis();
            return true;

        case KeyEvent.KEYCODE_DPAD_UP:
        	if(System.currentTimeMillis() - lastKey > 50){
        		previous = PlayActivity.BOARD.moveUp();
                this.render(previous);
        	}
        	lastKey = System.currentTimeMillis();

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
        	if(System.currentTimeMillis() - lastKey > 50){
        		previous = PlayActivity.BOARD.moveLeft();
                this.render(previous);
        	}
        	lastKey = System.currentTimeMillis();
            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
        	if(System.currentTimeMillis() - lastKey > 50){
        		previous = PlayActivity.BOARD.moveRight();
                this.render(previous);
        	}
        	lastKey = System.currentTimeMillis();
            

            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            previous = PlayActivity.BOARD.toggleDirection();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_SPACE:
        	if(prefs.getBoolean("spaceChangesDirection", true)){
	            previous = PlayActivity.BOARD.toggleDirection();
	            this.render(previous);
        	} else {
        		previous = PlayActivity.BOARD.playLetter(' ');
	            this.render(previous);
        	}
        	return true; 
        case KeyEvent.KEYCODE_ENTER:
        	if(prefs.getBoolean("enterChangesDirection", true)){
	            previous = PlayActivity.BOARD.toggleDirection();
	            this.render(previous);
	
	            return true;    
        	} else {
        		previous = PlayActivity.BOARD.getCurrentWord();
        		Position p = PlayActivity.BOARD.getHighlightLetter();
        		if(previous.across){
        			p.across = previous.start.across + previous.length -1;
        		} else {
        			p.down = previous.start.down + previous.length -1;
        		}
        		PlayActivity.BOARD.nextLetter();
        		this.render(previous);
        		return true;
        	}

        case KeyEvent.KEYCODE_DEL:
        	if(System.currentTimeMillis() - lastKey > 50){
	            previous = PlayActivity.BOARD.deleteLetter();
	            this.render(previous);
        	}
        	lastKey = System.currentTimeMillis();
            
            return true;
            
        }

        char c = Character.toUpperCase( this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || 
        		this.useNativeKeyboard ? event.getDisplayLabel() : ((char)keyCode));

        if (ALPHA.indexOf(c) != -1) {
            previous = PlayActivity.BOARD.playLetter(c);
            this.render(previous);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        System.out.println(item.getTitle());

        if (item.getTitle().equals("Letter")) {
            PlayActivity.BOARD.revealLetter();
            this.render();

            return true;
        } else if (item.getTitle().equals("Word")) {
            PlayActivity.BOARD.revealWord();
            this.render();

            return true;
        } else if (item.getTitle().equals("Puzzle")) {
            PlayActivity.BOARD.revealPuzzle();
            this.render();

            return true;
        } else if (item.getTitle().equals("Show Errors") ||
                item.getTitle().equals("Hide Errors")) {
            PlayActivity.BOARD.toggleShowErrors();
            item.setTitle(PlayActivity.BOARD.isShowErrors() ? "Hide Errors"
                                                            : "Show Errors");
            this.render();

            return true;
        } else if (item.getTitle().equals("Settings")) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle().equals("Zoom In")) {
            this.boardView.scrollTo(0, 0);
            float newScale = RENDERER.zoomIn();
            this.prefs.edit().putFloat("scale", newScale).commit();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Out")) {
            this.boardView.scrollTo(0, 0);
            float newScale = RENDERER.zoomOut();
            this.prefs.edit().putFloat("scale", newScale).commit();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Reset")) {
            float newScale = RENDERER.zoomReset();
            this.prefs.edit().putFloat("scale", newScale).commit();
            this.render();
            this.boardView.scrollTo(0, 0);

            return true;
        } else if (item.getTitle().equals("Info")) {
            TextView view = (TextView) dialog.findViewById(R.id.puzzle_info_title);
            view.setText(this.puz.getTitle());
            view = (TextView) dialog.findViewById(R.id.puzzle_info_author);
            view.setText(this.puz.getAuthor());
            view = (TextView) dialog.findViewById(R.id.puzzle_info_copyright);
            view.setText(this.puz.getCopyright());
            view = (TextView) dialog.findViewById(R.id.puzzle_info_time);

            if (timer != null) {
                this.timer.stop();
                view.setText("Elapsed Time: " + this.timer.time());
                this.timer.start();
            } else {
                view.setText("Elapsed Time: " +
                    new ImaginaryTimer(puz.getTime()).time());
            }

            ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.puzzle_info_progress);
            progress.setProgress(this.puz.getPercentComplete());

            this.showDialog(INFO_DIALOG);

            return true;
        } else if (item.getTitle().equals("Clues")) {
            Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
            PlayActivity.this.startActivityForResult(i, 0);

            return true;
        } else if (item.getTitle().equals("Help")) {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("file:///android_asset/playscreen.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);
        }

        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.render();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case INFO_DIALOG:
            return dialog;

        case COMPLETE_DIALOG:
            return completeDialog;

        default:
            return null;
        }
    }

    @Override
    protected void onPause() {
        try {
            if ((puz != null) && (BASE_FILE != null)) {
                if (puz.getPercentComplete() != 100) {
                    this.timer.stop();
                    puz.setTime(timer.getElapsed());
                }

                IO.save(puz, BASE_FILE);
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, null, ioe);
        }

        this.timer = null;

        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(clue.getWindowToken(), 0);
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BOARD.setSkipCompletedLetters(this.prefs.getBoolean("skipFilled", false));
        BOARD.setMovementStrategy(this.getMovementStrategy());
        this.useNativeKeyboard = prefs.getBoolean("useNativeKeyboard", false);
        this.onConfigurationChanged(this.configuration);
        if (puz.getPercentComplete() != 100) {
            timer = new ImaginaryTimer(this.puz.getTime());
            timer.start();
        }
    }

    private void render() {
        render(null);
    }

    private void render(Word previous) {
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
        Clue c = PlayActivity.BOARD.getClue();
        
        if(c.hint == null){
        	PlayActivity.BOARD.toggleDirection();
        	c = PlayActivity.BOARD.getClue();
        	
        }
        
        this.boardView.setBitmap(RENDERER.draw(previous));

        Point topLeft = RENDERER.findPointTopLeft(PlayActivity.BOARD.getHighlightLetter());
        Point bottomRight = RENDERER.findPointBottomRight(PlayActivity.BOARD.getHighlightLetter());

        if (this.prefs.getBoolean("ensureVisible", true)) {
            this.boardView.ensureVisible(bottomRight);
            this.boardView.ensureVisible(topLeft);
        }

        
        this.clue.setText("(" +
            (PlayActivity.BOARD.isAcross() ? "across" : "down") + ") " +
            c.number + ". " + c.hint);

        if ((puz.getPercentComplete() == 100) && (timer != null)) {
            timer.stop();
            puz.setTime(timer.getElapsed());
            this.completeDialog.setMessage("Completed in " + timer.time());
            this.showDialog(COMPLETE_DIALOG);
            this.timer = null;
        }
    }
}
