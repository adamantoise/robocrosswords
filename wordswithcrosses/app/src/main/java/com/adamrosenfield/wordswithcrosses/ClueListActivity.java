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

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;
import com.adamrosenfield.wordswithcrosses.view.ClueImageView;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView.ClickListener;

public class ClueListActivity extends WordsWithCrossesActivity {
    private Configuration configuration;
    private File baseFile;
    private ImaginaryTimer timer;
    private KeyboardView keyboardView = null;
    private ListView across;
    private ListView down;
    private Puzzle puz;
    private ClueImageView imageView;
    private TabHost tabHost;
    private boolean useNativeKeyboard = false;
    private boolean hasSetInitialZoom = false;

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;
        try {
            if (this.prefs.getBoolean("forceKeyboard", false)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
                    || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        utils.holographic(this);
        utils.finishOnHomeButton(this);
        try {
            configuration = getBaseContext().getResources().getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.device_configuration_error),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        // Not sure how this can happen, but it's happened at least once
        final Playboard board = WordsWithCrossesApplication.BOARD;
        if (board == null) {
            LOG.warning("ClueListActivity: BOARD is null!");
            finish();
            return;
        }

        this.timer = new ImaginaryTimer(board.getPuzzle().getTime());

        Uri u = this.getIntent().getData();

        if (u != null) {
            if (u.getScheme().equals("file")) {
                baseFile = new File(u.getPath());
            }
        }

        puz = board.getPuzzle();
        timer.start();
        setContentView(R.layout.clue_list);

        int keyboardType = getKeyboardTypePreference();
        useNativeKeyboard = (keyboardType == -1);
        keyboardView = (KeyboardView)this.findViewById(R.id.clueKeyboard);

        if (!useNativeKeyboard) {
            Keyboard keyboard = new Keyboard(this, keyboardType);
            keyboardView.setKeyboard(keyboard);
        } else {
            keyboardView.setVisibility(View.GONE);
        }

        keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
            private long lastSwipe = 0;

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                long eventTime = System.currentTimeMillis();

                if ((eventTime - lastSwipe) < 500) {
                    return;
                }

                KeyEvent event = new KeyEvent(
                    eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                ClueListActivity.this.onKeyDown(primaryCode, event);
            }

                @Override
                public void onPress(int primaryCode) {}

                @Override
                public void onRelease(int primaryCode) {}

                @Override
                public void onText(CharSequence text) {}

                @Override
                public void swipeDown() {}

                @Override
                public void swipeLeft() {
                    long eventTime = System.currentTimeMillis();
                    lastSwipe = eventTime;

                    KeyEvent event = new KeyEvent(
                        eventTime, eventTime,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                    ClueListActivity.this.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, event);
                }

                @Override
                public void swipeRight() {
                    long eventTime = System.currentTimeMillis();
                    lastSwipe = eventTime;

                    KeyEvent event = new KeyEvent(
                        eventTime, eventTime,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                    ClueListActivity.this.onKeyDown(
                        KeyEvent.KEYCODE_DPAD_RIGHT, event);
                }

                @Override
                public void swipeUp() {
                }
            });

        imageView = (ClueImageView)this.findViewById(R.id.miniboard);
        imageView.setUseNativeKeyboard(useNativeKeyboard);

        imageView.setClickListener(new ClickListener() {
            @Override
            public void onClick(Position pos) {
                if (pos == null) {
                    return;
                }
                Word current = board.getCurrentWord();
                int newAcross = current.start.across;
                int newDown = current.start.down;
                int box = pos.across;

                if (box >= current.length) {
                    return;
                }

                if (tabHost.getCurrentTab() == 0) {
                    newAcross += box;
                } else {
                    newDown += box;
                }

                Position newPos = new Position(newAcross, newDown);

                if (!newPos.equals(board.getCursorPosition())) {
                    board.setCursorPosition(newPos);
                    render();
                }
            }

            @Override
            public void onDoubleClick(Position pos) {
                // No-op
            }

            @Override
            public void onLongClick(Position pos) {
                // No-op
            }
        });

        this.tabHost = (TabHost)this.findViewById(R.id.tabhost);
        this.tabHost.setup();

        TabSpec ts = tabHost.newTabSpec("TAB1");

        ts.setIndicator(getResources().getString(R.string.across),
            getResources().getDrawable(R.drawable.across));

        ts.setContent(R.id.acrossList);

        this.tabHost.addTab(ts);

        ts = this.tabHost.newTabSpec("TAB2");

        ts.setIndicator(getResources().getString(R.string.down),
            getResources().getDrawable(R.drawable.down));

        ts.setContent(R.id.downList);
        this.tabHost.addTab(ts);

        this.tabHost.setCurrentTab(board.isCursorAcross() ? 0 : 1);

        this.across = (ListView) this.findViewById(R.id.acrossList);
        this.down = (ListView) this.findViewById(R.id.downList);

        across.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, board
                        .getAcrossClues()));
        across.setFocusableInTouchMode(true);
        down.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, board
                        .getDownClues()));
        across.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parent.setSelected(true);
                board.jumpTo(position, true);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    across.setSelectionFromTop(position, 5);
                    across.setSelection(position);
                }
            }
        });
        across.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!board.isCursorAcross() || (board.getCurrentClueIndex() != position)) {
                    board.jumpTo(position, true);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        across.setSelectionFromTop(position, 5);
                        across.setSelection(position);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        down.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                board.jumpTo(position, false);
                imageView.setTranslate(0.0f, 0.0f);
                render();

                if (prefs.getBoolean("snapClue", false)) {
                    down.setSelectionFromTop(position, 5);
                    down.setSelection(position);
                }
            }
        });

        down.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (board.isCursorAcross() || (board.getCurrentClueIndex() != position)) {
                    board.jumpTo(position, false);
                    imageView.setTranslate(0.0f, 0.0f);
                    render();

                    if (prefs.getBoolean("snapClue", false)) {
                        down.setSelectionFromTop(position, 5);
                        down.setSelection(position);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        this.render();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasSetInitialZoom) {
            imageView.fitToHeight();
            hasSetInitialZoom = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Playboard board = WordsWithCrossesApplication.BOARD;
        Word w = board.getCurrentWord();
        Position last = new Position(w.start.across
                + (w.across ? (w.length - 1) : 0), w.start.down
                + ((!w.across) ? (w.length - 1) : 0));

        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_BACK:
            this.setResult(0);

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:

            if (!board.getCursorPosition().equals(w.start)) {
                board.moveToPreviousLetterStopAtEndOfWord();

                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:

            if (!board.getCursorPosition().equals(last)) {
                board.moveToNextLetterStopAtEndOfWord();
                this.render();
            }

            return true;

        case KeyEvent.KEYCODE_DEL:
            w = board.getCurrentWord();
            board.deleteLetter();

            Position p = board.getCursorPosition();

            if (!w.checkInWord(p.across, p.down)) {
                board.setCursorPosition(w.start);
            }

            this.render();

            return true;

        case KeyEvent.KEYCODE_SPACE:

            if (!prefs.getBoolean("spaceChangesDirection", true)) {
                board.playLetter(' ');

                Position curr = board.getCursorPosition();

                if (!board.getCurrentWord().equals(w) || (board.getBoxes()[curr.down][curr.across] == null)) {
                    board.setCursorPosition(last);
                }

                this.render();

                return true;
            }
        }

        char c = Character
                .toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
                        .getDisplayLabel() : ((char) keyCode));

        if (PlayActivity.PLAYABLE_CHARS.indexOf(c) != -1) {
            board.playLetter(c);

            Position p = board.getCursorPosition();

            if (!board.getCurrentWord().equals(w) || (board.getBoxes()[p.down][p.across] == null)) {
                board.setCursorPosition(last);
            }

            this.render();

            if (puz.isSolved() && (timer != null)) {
                timer.stop();
                puz.setTime(timer.getElapsed());
                this.timer = null;
                Intent i = new Intent(ClueListActivity.this, PuzzleFinishedActivity.class);
                this.startActivity(i);

            }

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            this.finish();

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if ((puz != null) && (baseFile != null)) {
                if ((timer != null) && !puz.isSolved()) {
                    timer.stop();
                    puz.setTime(timer.getElapsed());
                    timer = null;
                }

                IO.save(puz, baseFile);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (shouldShowKeyboard(configuration)) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(imageView.getWindowToken(), 0);
            }
        }
    }

    private void render() {
        if (shouldShowKeyboard(configuration)) {
            if (useNativeKeyboard) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            } else {
                keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            keyboardView.setVisibility(View.GONE);
        }

        imageView.render();
    }
}
