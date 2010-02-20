package com.totsp.crossword;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.totsp.crossword.puz.IO;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.view.PlayboardRenderer;
import com.totsp.crossword.view.ScrollingImageView;
import com.totsp.crossword.view.ScrollingImageView.ClickListener;
import com.totsp.crossword.view.ScrollingImageView.Point;


public class PlayActivity extends Activity {
	private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
	static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Configuration configuration;
    static  Playboard BOARD;
    static PlayboardRenderer RENDERER;
    private ScrollingImageView boardView;
    private TextView clue;
    private Puzzle puz;
    private File baseFile;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
    }
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        this.render();
    }

   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.configuration = getBaseContext().getResources().getConfiguration();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        try {
        	Uri u = this.getIntent().getData();
        	if(u != null){
        		if(u.getScheme().equals("file")){
        			baseFile = new File(u.getPath());
        			puz = IO.load(baseFile);
        		}
        	}
        	if(puz == null){
        		puz = IO.loadNative(this.getResources()
                                           .openRawResource(R.raw.test));
        	}

            BOARD = new Playboard(puz);
            RENDERER = new PlayboardRenderer(BOARD, metrics.density);
            setContentView(R.layout.main);
            this.clue = (TextView) this.findViewById(R.id.clueLine);
            
            this.clue.setClickable(true);
            this.clue.setOnClickListener(new OnClickListener(){

				public void onClick(View arg0) {
					Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
		        	PlayActivity.this.startActivityForResult(i, 0);
				}
            	
            });
            
            boardView = (ScrollingImageView) this.findViewById(R.id.board);

            this.registerForContextMenu(boardView);
            boardView.setContextMenuListener(new ClickListener() {
                    public void onContextMenu(Point e) {
                        Position p = PlayActivity.RENDERER.findBox(e);
                        PlayActivity.this.BOARD.setHighlightLetter(p);
                        PlayActivity.this.openContextMenu(boardView);
                    }

                    public void onTap(Point e) {
                        Position p = PlayActivity.RENDERER.findBox(e);
                        Word old = PlayActivity.this.BOARD.setHighlightLetter(p);
                        PlayActivity.this.render(old);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        reveal.add("Letter");
        reveal.add("Word");
        reveal.add("Puzzle");
        menu.add("Info").setIcon(android.R.drawable.ic_menu_info_details);
        menu.add("Help").setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings").setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Word previous;

        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_DOWN:
            previous = this.BOARD.moveDown();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_UP:
            previous = this.BOARD.movieUp();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
            previous = this.BOARD.moveLeft();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            previous = this.BOARD.moveRight();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            previous = this.BOARD.toggleDirection();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_SPACE:
            previous = this.BOARD.toggleDirection();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DEL:
            previous = this.BOARD.deleteLetter();
            this.render(previous);

            return true;
        }

        char c = Character.toUpperCase(event.getDisplayLabel());

        if (ALPHA.indexOf(c) != -1) {
            previous = this.BOARD.playLetter(c);
            this.render(previous);

            return true;
        }

        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    protected void onPause() {
    	try{
    		if(puz != null && baseFile != null)
    			IO.save(puz, baseFile);
    	} catch(IOException ioe){
    		LOG.log(Level.SEVERE, null, ioe);
    	}
    	// TODO Auto-generated method stub
    	super.onPause();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	System.out.println(item.getTitle());
        if (item.getTitle().equals("Letter")) {
            this.BOARD.revealLetter();
            this.render();

            return true;
        } else if (item.getTitle().equals("Word")) {
            this.BOARD.revealWord();
            this.render();

            return true;
        } else if (item.getTitle().equals("Puzzle")) {
            this.BOARD.revealPuzzle();
            this.render();

            return true;
        } else if (item.getTitle().equals("Show Errors")) {
            this.BOARD.toggleShowErrors();
            item.setChecked(this.BOARD.isShowErrors());
            this.render();

            return true;
        } else if (item.getTitle().equals("Settings")){
        	Intent i = new Intent(this, PreferencesActivity.class);
        	this.startActivity(i);
        	return true;
        } else if (item.getTitle().equals("Zoom In")) {
            this.boardView.scrollTo(0, 0);
            RENDERER.zoomIn();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Out")) {
            this.boardView.scrollTo(0, 0);
            RENDERER.zoomOut();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Reset")) {
        	RENDERER.zoomReset();
            this.render();
            this.boardView.scrollTo(0, 0);

            return true;
        }

        return false;
    }

    private void render() {
        render(null);
    }

    private void render(Word previous) {
        this.boardView.setBitmap(RENDERER.draw(previous));

        Point topLeft = RENDERER.findPointTopLeft(this.BOARD.getHighlightLetter());
        Point bottomRight = RENDERER.findPointBottomRight(this.BOARD.getHighlightLetter());
        this.boardView.ensureVisible(bottomRight);
        this.boardView.ensureVisible(topLeft);

        Clue c = this.BOARD.getClue();
        this.clue.setText("(" + (this.BOARD.isAcross() ? "across" : "down") +
            ") " + c.number + ". " + c.hint);
    }
}
