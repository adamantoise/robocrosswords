package com.totsp.crossword;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.TextView;

import com.totsp.crossword.net.Downloaders;
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
	private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Configuration configuration;
    private Playboard board;
    private PlayboardRenderer renderer;
    private ScrollingImageView boardView;
    private TextView clue;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Zoom In")) {
            this.boardView.scrollTo(0, 0);
            this.renderer.zoomIn();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Out")) {
            this.boardView.scrollTo(0, 0);
            this.renderer.zoomOut();
            this.render();

            return true;
        } else if (item.getTitle().equals("Zoom Reset")) {
            this.renderer.zoomReset();
            this.render();
            this.boardView.scrollTo(0, 0);

            return true;
        }

        return false;
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
            Puzzle puz = IO.loadNative(this.getResources()
                                           .openRawResource(R.raw.test));

            board = new Playboard(puz);
            this.renderer = new PlayboardRenderer(board, metrics.density);
            setContentView(R.layout.main);
            this.clue = (TextView) this.findViewById(R.id.clueLine);
            boardView = (ScrollingImageView) this.findViewById(R.id.board);

            this.registerForContextMenu(boardView);
            boardView.setContextMenuListener(new ClickListener() {
                    public void onContextMenu(Point e) {
                        Position p = PlayActivity.this.renderer.findBox(e);
                        PlayActivity.this.board.setHighlightLetter(p);
                        PlayActivity.this.openContextMenu(boardView);
                    }

                    public void onTap(Point e) {
                        Position p = PlayActivity.this.renderer.findBox(e);
                        Word old = PlayActivity.this.board.setHighlightLetter(p);
                        PlayActivity.this.render(old);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.render();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        new Thread(new Runnable(){

			public void run() {
				Downloaders dls = new Downloaders(prefs, nm, PlayActivity.this);
				dls.download(new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)));
			}
        	
        }).start();
        
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
            previous = this.board.moveDown();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_UP:
            previous = this.board.movieUp();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
            previous = this.board.moveLeft();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            previous = this.board.moveRight();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            previous = this.board.toggleDirection();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_SPACE:
            previous = this.board.toggleDirection();
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_DEL:
            previous = this.board.deleteLetter();
            this.render(previous);

            return true;
        }

        char c = Character.toUpperCase(event.getDisplayLabel());

        if (ALPHA.indexOf(c) != -1) {
            previous = this.board.playLetter(c);
            this.render(previous);

            return true;
        }

        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getTitle().equals("Letter")) {
            this.board.revealLetter();
            this.render();

            return true;
        } else if (item.getTitle().equals("Word")) {
            this.board.revealWord();
            this.render();

            return true;
        } else if (item.getTitle().equals("Puzzle")) {
            this.board.revealPuzzle();
            this.render();

            return true;
        } else if (item.getTitle().equals("Show Errors")) {
            this.board.toggleShowErrors();
            item.setChecked(this.board.isShowErrors());
            this.render();

            return true;
        } else if (item.getTitle().equals("Settings")){
        	Intent i = new Intent(this, PreferencesActivity.class);
        	this.startActivity(i);
        	return true;
        }

        return false;
    }

    private void render() {
        render(null);
    }

    private void render(Word previous) {
        this.boardView.setBitmap(this.renderer.draw(previous));

        Point topLeft = this.renderer.findPointTopLeft(this.board.getHighlightLetter());
        Point bottomRight = this.renderer.findPointBottomRight(this.board.getHighlightLetter());
        this.boardView.ensureVisible(bottomRight);
        this.boardView.ensureVisible(topLeft);

        Clue c = this.board.getClue();
        this.clue.setText("(" + (this.board.isAcross() ? "across" : "down") +
            ") " + c.number + ". " + c.hint);
    }
}
