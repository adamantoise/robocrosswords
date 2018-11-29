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

import static com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication.BOARD;
import static com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication.RENDERER;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper.SolveState;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.puz.MovementStrategy;
import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Clue;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.OnBoardChangedListener;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView.ClickListener;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView.RenderScaleListener;
import com.adamrosenfield.wordswithcrosses.view.PlayboardRenderer;
import com.adamrosenfield.wordswithcrosses.view.SeparatedListAdapter;

public class PlayActivity extends WordsWithCrossesActivity {

    /** Extra data tag required by this activity */
    public static final String EXTRA_PUZZLE_ID = "puzzle_id";

    /** Playable non-rebus characters */
    public static final String PLAYABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    private static final int INFO_DIALOG = 0;
    private static final int NOTES_DIALOG = 1;
    private static final int REVEAL_PUZZLE_DIALOG = 2;

    private ProgressDialog loadProgressDialog;

    @SuppressWarnings("rawtypes")
    private AdapterView across;
    @SuppressWarnings("rawtypes")
    private AdapterView down;
    private ListView allClues;
    private ClueListAdapter acrossAdapter;
    private ClueListAdapter downAdapter;
    private SeparatedListAdapter allCluesAdapter;
    private Configuration configuration;
    private Dialog dialog;
    private File baseFile;
    private Handler handler = new Handler();
    private ImaginaryTimer timer;
    private KeyboardView keyboardView = null;
    private Puzzle puz;
    private long puzzleId;
    private CrosswordImageView boardView;
    private TextView clue;
    private TextView timerText;
    private View clueContainer;
    private boolean maybeShowClueContainerOnPuzzleLoad = false;
    private boolean showTimer = false;
    private boolean showingProgressBar = false;

    private UpdateTimeTask updateTimeTask = new UpdateTimeTask();

    private boolean showCount = false;
    private boolean showErrors = false;
    private boolean useNativeKeyboard = false;
    private long lastKey;

    private DisplayMetrics metrics;

    // Saved scale from before we fit to screen
    private float lastBoardScale = 1.0f;
    private boolean fitToScreen = false;

    private boolean hasSetInitialZoom = false;

    private Menu mOptionsMenu;

    private static final int MENU_ID_SHOW_ERRORS = 1;
    private static final int MENU_ID_REVEAL = 2;
    private static final int MENU_ID_REVEAL_LETTER = 3;
    private static final int MENU_ID_REVEAL_WORD = 4;
    private static final int MENU_ID_REVEAL_PUZZLE = 5;
    private static final int MENU_ID_CLUES = 6;
    private static final int MENU_ID_INFO = 7;
    private static final int MENU_ID_HELP = 8;
    private static final int MENU_ID_SETTINGS = 9;
    private static final int MENU_ID_NOTES = 10;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        this.configuration = newConfig;

        if (shouldShowKeyboard(configuration)) {
            if (this.useNativeKeyboard) {
                keyboardView.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        showTimer = prefs.getBoolean("showTimer", false);
        updateTimeTask.updateTime();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.configuration = getBaseContext().getResources().getConfiguration();

        if (prefs.getBoolean("showProgressBar", false)) {
            requestWindowFeature(Window.FEATURE_PROGRESS);
            showingProgressBar = true;
        }

        utils.finishOnHomeButton(this);

        // Must be called after all calls to requestWindowFeature()
        setContentView(R.layout.play);

        setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE);

        if (prefs.getBoolean("fullScreen", false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        puzzleId = getIntent().getLongExtra(EXTRA_PUZZLE_ID,  -1);
        if (puzzleId == -1) {
            LOG.warning(EXTRA_PUZZLE_ID + " extra must be specified");
            puzzleLoadFailed(null);
            return;
        }

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        final String filename = dbHelper.getFilename(puzzleId);
        if (filename == null) {
            LOG.warning("Invalid puzzle ID: " + puzzleId);
            puzzleLoadFailed(null);
            return;
        }

        // Initialize this here so that onPause() has a reference to a View
        // even if the puzzle hasn't finished loading yet
        clue = (TextView)this.findViewById(R.id.clueLine);

        // Hide the clue container while we load the puzzle, then maybe
        // show it again later
        clueContainer = findViewById(R.id.clueContainer);
        maybeShowClueContainerOnPuzzleLoad = (clueContainer.getVisibility() != View.GONE);
        clueContainer.setVisibility(View.GONE);

        baseFile = new File(filename);

        if (BOARD != null && BOARD.getPuzzleID() == puzzleId) {
            puz = BOARD.getPuzzle();
        }

        if (puz != null) {
            postPuzzleLoaded();
        } else {
            // Show a progress dialog while the puzzle is loaded
            loadProgressDialog = new ProgressDialog(this);
            loadProgressDialog.setMessage(getResources().getString(R.string.loading_puzzle));
            loadProgressDialog.setCancelable(false);
            loadProgressDialog.show();

            // Load the puzzle on a background thread
            new Thread(new Runnable() {
                public void run() {
                    final Puzzle newPuzzle = loadPuzzle();

                    // Do stuff on the UI thread after the puzzle is loaded
                    handler.post(new Runnable() {
                        public void run() {
                            if (loadProgressDialog != null) {
                                loadProgressDialog.dismiss();
                                loadProgressDialog = null;

                                if (newPuzzle != null) {
                                    puz = newPuzzle;
                                    postPuzzleLoaded();
                                } else {
                                    puzzleLoadFailed(filename);
                                }
                            }
                        }
                        });
                }
            }).start();
        }
    }

    private void puzzleLoadFailed(String filename) {
        String text = getResources().getString(R.string.load_puzzle_failed);
        if (filename != null) {
            text += filename;
        }

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }

    private Puzzle loadPuzzle() {
        try {
            return IO.load(baseFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void postPuzzleLoaded() {
        if (mOptionsMenu != null) {
            maybeAddNotesMenuItem();
        }

        initPlayboard();
        initKeyboard();
        initClueLists();
        handleOnResume();
    }

    private void initPlayboard() {
        BOARD = new Playboard(puz, puzzleId, getMovementStrategy());
        RENDERER = new PlayboardRenderer(BOARD);

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        SolveState solveState = dbHelper.getPuzzleSolveState(puzzleId);
        if (solveState != null) {
            BOARD.setSolveState(solveState);
        }

        BOARD.setOnBoardChangedListener(new OnBoardChangedListener() {
            public void onBoardChanged() {
                updateProgressBar();
            }
        });

        if (maybeShowClueContainerOnPuzzleLoad && android.os.Build.VERSION.SDK_INT < 11) {
            clueContainer.setVisibility(View.VISIBLE);
        } else {
            clueContainer.setVisibility(View.GONE);
            View clueLine = utils.onActionBarCustom(this, R.layout.clue_line_only);
            if (clueLine != null) {
                clue = (TextView)clueLine.findViewById(R.id.clueLine);
                timerText = (TextView)clueLine.findViewById(R.id.timerText);
            }
        }
        clue.setClickable(true);
        clue.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
                i.setData(Uri.fromFile(baseFile));
                PlayActivity.this.startActivityForResult(i, 0);
            }
        });

        if (clueContainer.getVisibility() != View.GONE &&
            !TextUtils.isEmpty(puz.getNotes()))
        {
            View notesButton = findViewById(R.id.notesButton);
            notesButton.setVisibility(View.VISIBLE);
        }

        boardView = (CrosswordImageView)findViewById(R.id.board);
        boardView.setBoard(BOARD, metrics);

        this.registerForContextMenu(boardView);

        boardView.setClickListener(new ClickListener() {
            public void onClick(Position pos) {
                setCursorPositionAndRender(pos);
            }

            public void onDoubleClick(Position pos) {
                if (prefs.getBoolean("doubleTap",  false)) {
                    if (fitToScreen) {
                        boardView.setRenderScale(lastBoardScale);
                    } else {
                        lastBoardScale = boardView.getRenderScale();
                        boardView.fitToScreen();
                    }

                    fitToScreen = !fitToScreen;
                }

                setCursorPositionAndRender(pos);
            }

            public void onLongClick(Position pos) {
                setCursorPositionAndRender(pos);
                openContextMenu(boardView);
            }
        });

        boardView.setRenderScaleListener(new RenderScaleListener() {
            public void onRenderScaleChanged(float renderScale) {
                fitToScreen = false;
            }
        });
    }

    private void setCursorPositionAndRender(Position pos) {
        Word prevWord = null;

        if (pos != null) {
            prevWord = BOARD.getCurrentWord();

            boolean toggleDirection = (BOARD.getCursorPosition().equals(pos));
            BOARD.setCursorPosition(pos);

            if (toggleDirection) {
                BOARD.toggleDirection();
            }
        }

        render(prevWord);
    }

    private void initKeyboard() {
        updateKeyboardFromPrefs();
        keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
            private long lastSwipe = 0;

            public void onKey(int primaryCode, int[] keyCodes) {
                long eventTime = System.currentTimeMillis();

                if ((eventTime - lastSwipe) < 500) {
                    return;
                }

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 primaryCode,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(primaryCode, event);
            }

            public void onPress(int primaryCode) {
            }

            public void onRelease(int primaryCode) {
            }

            public void onText(CharSequence text) {
            }

            public void swipeDown() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime, eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_DOWN,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_DOWN, event);
            }

            public void swipeLeft() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_LEFT,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, event);
            }

            public void swipeRight() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_RIGHT,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, event);
            }

            public void swipeUp() {
                long eventTime = System.currentTimeMillis();
                lastSwipe = eventTime;

                KeyEvent event =
                    new KeyEvent(eventTime,
                                 eventTime,
                                 KeyEvent.ACTION_DOWN,
                                 KeyEvent.KEYCODE_DPAD_UP,
                                 0, 0, 0, 0,
                                 KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_UP, event);
            }
        });
    }

    private void updateKeyboardFromPrefs() {
        int keyboardType = getKeyboardTypePreference();
        useNativeKeyboard = (keyboardType == -1);
        keyboardView = (KeyboardView)findViewById(R.id.playKeyboard);

        if (!useNativeKeyboard) {
            Keyboard keyboard = new Keyboard(this, keyboardType);
            keyboardView.setKeyboard(keyboard);
        } else {
            keyboardView.setVisibility(View.GONE);
        }

        boardView.setUseNativeKeyboard(useNativeKeyboard);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initClueLists() {
        this.across = (AdapterView) this.findViewById(R.id.acrossList);
        this.down = (AdapterView) this.findViewById(R.id.downList);

        if ((this.across == null) && (this.down == null)) {
            this.across = (AdapterView) this.findViewById(R.id.acrossListGal);
            this.down = (AdapterView) this.findViewById(R.id.downListGal);
        }

        if ((across != null) && (down != null)) {
            across.setAdapter(this.acrossAdapter = new ClueListAdapter(this,
                    BOARD.getAcrossClues(), true));
            down.setAdapter(this.downAdapter = new ClueListAdapter(this, BOARD
                    .getDownClues(), false));
            across.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    parent.setSelected(true);
                    BOARD.jumpTo(position, true);
                    render();
                }
            });
            across.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!BOARD.isCursorAcross() || (BOARD.getCurrentClueIndex() != position)) {
                        BOARD.jumpTo(position, true);
                        render();
                    }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });
            down.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    parent.setSelected(true);
                    BOARD.jumpTo(position, false);
                    render();
                }
            });

            down.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (BOARD.isCursorAcross() || (BOARD.getCurrentClueIndex() != position)) {
                        BOARD.jumpTo(position, false);
                        render();
                    }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });
            down.scrollTo(0, 0);
            across.scrollTo(0, 0);
        }
        this.allClues = (ListView) this.findViewById(R.id.allClues);
        if (this.allClues != null) {
            this.allCluesAdapter = new SeparatedListAdapter(this);
            this.allCluesAdapter.addSection(
                    getResources().getString(R.string.across),
                    this.acrossAdapter = new ClueListAdapter(this, BOARD
                            .getAcrossClues(), true));
            this.allCluesAdapter.addSection(
                    getResources().getString(R.string.down),
                    this.downAdapter = new ClueListAdapter(this, BOARD
                            .getDownClues(), false));
            allClues.setAdapter(this.allCluesAdapter);

            allClues.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    boolean across = position <= BOARD.getAcrossClues().length +1;
                    int index = position - 1;
                    if (index > BOARD.getAcrossClues().length) {
                        index = index - BOARD.getAcrossClues().length - 1;
                    }
                    parent.setSelected(true);
                    BOARD.jumpTo(index, across);
                    render();
                }
            });
            allClues.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                        int position, long id) {
                    boolean across = position <= BOARD.getAcrossClues().length +1;
                    int index = position - 1;
                    if (index > BOARD.getAcrossClues().length) {
                        index = index - BOARD.getAcrossClues().length - 1;
                    }
                    if (!BOARD.isCursorAcross() == across && BOARD.getCurrentClueIndex() != index) {
                        parent.setSelected(true);
                        BOARD.jumpTo(index, across);
                        render();
                    }
                }

                public void onNothingSelected(AdapterView<?> view) {
                }
            });

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasSetInitialZoom) {
            float zoom = 0.0f;
            String zoomStr = prefs.getString("initialZoom", "0");
            if (zoomStr != null) {
                try {
                    zoom = Float.parseFloat(zoomStr);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            if (Math.abs(zoom) < 0.01f) {
                boardView.fitToScreen();
            } else {
                boardView.setRenderScale(zoom);
            }

            hasSetInitialZoom = true;
            render();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);

        if (view == boardView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.playactivity_context_menu, menu);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mOptionsMenu = menu;

        int showItemStr = (showErrors ? R.string.menu_hide_errors : R.string.menu_show_errors);
        MenuItem showItem = menu.add(Menu.NONE, MENU_ID_SHOW_ERRORS, Menu.NONE, showItemStr)
            .setIcon(android.R.drawable.ic_menu_view);

        SubMenu reveal = menu.addSubMenu(Menu.NONE, MENU_ID_REVEAL, Menu.NONE, R.string.menu_reveal)
            .setIcon(android.R.drawable.ic_menu_view);
        reveal.add(Menu.NONE, MENU_ID_REVEAL_LETTER, Menu.NONE, R.string.menu_reveal_letter);
        reveal.add(Menu.NONE, MENU_ID_REVEAL_WORD,   Menu.NONE, R.string.menu_reveal_word);
        reveal.add(Menu.NONE, MENU_ID_REVEAL_PUZZLE, Menu.NONE, R.string.menu_reveal_puzzle);

        menu.add(Menu.NONE, MENU_ID_CLUES, Menu.NONE, R.string.menu_clues)
            .setIcon(android.R.drawable.ic_menu_agenda);
        menu.add(Menu.NONE, MENU_ID_INFO, Menu.NONE, R.string.menu_info)
            .setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(Menu.NONE, MENU_ID_HELP, Menu.NONE, R.string.menu_help)
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add(Menu.NONE, MENU_ID_SETTINGS, Menu.NONE, R.string.menu_settings)
            .setIcon(android.R.drawable.ic_menu_preferences);

        maybeAddNotesMenuItem();

        if (WordsWithCrossesApplication.isTabletish(metrics)) {
            utils.onActionBarWithText(showItem);
            utils.onActionBarWithText(reveal);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        showErrors = prefs.getBoolean("showErrors", false);
        int showItemStr = (showErrors ? R.string.menu_hide_errors : R.string.menu_show_errors);
        menu.findItem(MENU_ID_SHOW_ERRORS).setTitle(showItemStr);

        return true;
    }

    private void maybeAddNotesMenuItem() {
        if (puz != null && !TextUtils.isEmpty(puz.getNotes())) {
            MenuItem notesItem = mOptionsMenu.add(Menu.NONE, MENU_ID_NOTES, Menu.NONE, R.string.menu_notes).setIcon(R.drawable.ic_action_paste);
            utils.onActionBarWithText(notesItem);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Ignore double-presses and bad hardware which report key events for
        // both the software keyboard and the hardware keyboard
        long now = System.currentTimeMillis();
        if (now < lastKey + 50) {
            return true;
        }
        lastKey = now;

        Position prevCursorPos;
        Word prevWord;

        switch (keyCode) {
        case KeyEvent.KEYCODE_SEARCH:
            prevWord = BOARD.moveToNextWord(MovementStrategy.MOVE_NEXT_CLUE);
            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_BACK:
            finish();
            return true;

        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_DPAD_DOWN:
            prevCursorPos = BOARD.getCursorPosition();
            prevWord = BOARD.moveDown();
            toggleDirectionIfCursorDidNotMove(prevCursorPos);

            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_DPAD_UP:
            prevCursorPos = BOARD.getCursorPosition();
            prevWord = BOARD.moveUp();
            toggleDirectionIfCursorDidNotMove(prevCursorPos);

            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:
            prevCursorPos = BOARD.getCursorPosition();
            prevWord = BOARD.moveLeft();
            toggleDirectionIfCursorDidNotMove(prevCursorPos);

            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            prevCursorPos = BOARD.getCursorPosition();
            prevWord = BOARD.moveRight();
            toggleDirectionIfCursorDidNotMove(prevCursorPos);

            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_DPAD_CENTER:
            prevWord = BOARD.getCurrentWord();
            BOARD.toggleDirection();
            render(prevWord);
            return true;

        case KeyEvent.KEYCODE_SPACE:
            if (prefs.getBoolean("spaceChangesDirection", true)) {
                prevWord = BOARD.getCurrentWord();
                BOARD.toggleDirection();
                render(prevWord);
            } else {
                prevWord = BOARD.playLetter(' ');
                render(prevWord);
            }

            return true;

        case KeyEvent.KEYCODE_ENTER:
            if (prefs.getBoolean("enterChangesDirection", true)) {
                prevWord = BOARD.getCurrentWord();
                BOARD.toggleDirection();
                render(prevWord);
            } else {
                prevWord = BOARD.moveToNextWord(MovementStrategy.MOVE_NEXT_CLUE);
                render(prevWord);
            }

            return true;

        case KeyEvent.KEYCODE_DEL:
            prevWord = BOARD.deleteLetter();
            render(prevWord);
            return true;
        }

        char c = Character
                .toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
                        .getDisplayLabel() : ((char) keyCode));

        if (PLAYABLE_CHARS.indexOf(c) != -1) {
            prevWord = BOARD.playLetter(c);
            this.render(prevWord);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void toggleDirectionIfCursorDidNotMove(Position prevCursorPos) {
        if (BOARD.getCursorPosition().equals(prevCursorPos)) {
            BOARD.toggleDirection();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId())
        {
        case MENU_ID_REVEAL_LETTER:
            BOARD.revealLetter();
            render();
            return true;

        case MENU_ID_REVEAL_WORD:
            BOARD.revealWord();
            render();
            return true;

        case MENU_ID_REVEAL_PUZZLE:
            deprecatedShowDialog(REVEAL_PUZZLE_DIALOG);
            return true;

        case MENU_ID_SHOW_ERRORS:
            showErrors = !showErrors;
            BOARD.setShowErrors(showErrors);
            int showErrorsStr = (showErrors ? R.string.menu_hide_errors : R.string.menu_show_errors);
            item.setTitle(showErrorsStr);
            prefs.edit().putBoolean("showErrors", showErrors).commit();
            render();
            return true;

        case MENU_ID_SETTINGS:
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;

        case MENU_ID_INFO:
            deprecatedShowDialog(INFO_DIALOG);

            return true;

        case MENU_ID_CLUES:
            intent = new Intent(PlayActivity.this, ClueListActivity.class);
            intent.setData(Uri.fromFile(baseFile));
            PlayActivity.this.startActivityForResult(intent, 0);

            return true;

        case MENU_ID_HELP:
            showHTMLPage("playscreen.html");
            return true;

        case MENU_ID_NOTES:
            onNotesClicked(null);
            return true;

        case R.id.context_zoom_in:
            boardView.zoomIn();
            fitToScreen = false;
            render();
            return true;

        case R.id.context_zoom_out:
            boardView.zoomOut();
            fitToScreen = false;
            render();
            return true;

        case R.id.context_fit_to_screen:
            lastBoardScale = boardView.getRenderScale();
            boardView.fitToScreen();
            fitToScreen = true;
            this.render();

            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @SuppressWarnings("deprecation")
    private void deprecatedShowDialog(int dialog) {
        showDialog(dialog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.render();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        // This can get called if the OS is attempting to restore any dialogs
        // which were previously present but the app was killed while it was in
        // the background.
        if (puz == null) {
            return null;
        }

        switch (id) {
        case INFO_DIALOG:
            // This is weird. I don't know why a rotate resets the dialog.
            // Whatevs.
            return createInfoDialog();

        case NOTES_DIALOG:
            return createNotesDialog();

        case REVEAL_PUZZLE_DIALOG:
            return createRevealPuzzleDialog();

        default:
            return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);

        if (id == INFO_DIALOG) {
            if (timer != null) {
                timer.stop();
            }
            TextView timeText = (TextView)dialog.findViewById(R.id.puzzle_info_time);
            updateElapsedTime(timeText);

            ProgressBar progress = (ProgressBar)dialog.findViewById(R.id.puzzle_info_progress);
            progress.setProgress((int)(puz.getFractionComplete() * 10000));

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    if (timer != null) {
                        timer.start();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        try {
            if ((puz != null) && (baseFile != null)) {
                if (!puz.isSolved() && (timer != null)) {
                    timer.stop();
                    puz.setTime(timer.getElapsed());
                    timer = null;
                }

                IO.save(puz, baseFile);

                PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
                dbHelper.updatePercentComplete(puzzleId, puz.getPercentComplete());
                SolveState solveState = BOARD.getSolveState();
                dbHelper.updatePuzzleSolveState(puzzleId, solveState);
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, null, ioe);
        }

        timer = null;

        if ((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
                || (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(clue.getWindowToken(), 0);
            }
        }

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (timer != null) {
            timer.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (puz != null) {
            handleOnResume();
        }
    }

    private void handleOnResume() {
        setTitle(getResources().getString(R.string.app_name) +
                 " - " +
                 puz.getTitle() +
                 " - " +
                 puz.getAuthor() +
                 " - " +
                 puz.getCopyright());

        BOARD.setSkipCompletedLetters(prefs.getBoolean("skipFilled", false));
        BOARD.setMovementStrategy(getMovementStrategy());

        showErrors = prefs.getBoolean("showErrors", false);
        BOARD.setShowErrors(showErrors);

        RENDERER.setHintHighlight(prefs.getBoolean("showRevealedLetters", true));

        updateClueSize();

        String clickSlopStr = prefs.getString("touchSensitivity", "3");
        try {
            int clickSlop = Integer.parseInt(clickSlopStr);
            boardView.setClickSlop(clickSlop);
        } catch (NumberFormatException e) {
            // Ignore
        }

        updateKeyboardFromPrefs();

        this.showCount = prefs.getBoolean("showCount", false);
        this.onConfigurationChanged(this.configuration);

        if (!puz.isSolved()) {
            timer = new ImaginaryTimer(puz.getTime());
            timer.start();
        }

        showTimer = prefs.getBoolean("showTimer", false);
        updateTimeTask.updateTime();

        updateProgressBar();
        render();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (loadProgressDialog != null) {
            loadProgressDialog.dismiss();
            loadProgressDialog = null;
        }

        super.onDestroy();
    }

    public void onNotesClicked(View notesButton) {
        if (!TextUtils.isEmpty(puz.getNotes())) {
            deprecatedShowDialog(NOTES_DIALOG);
        }
    }

    private Dialog createNotesDialog() {
        String notes = puz.getNotes();

        AlertDialog.Builder notesDialogBuilder = new AlertDialog.Builder(this);
        notesDialogBuilder
            .setTitle(getResources().getString(R.string.dialog_notes_title))
            .setMessage(notes)
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return notesDialogBuilder.create();
    }

    private Dialog createRevealPuzzleDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
            .setTitle(getResources().getString(R.string.reveal_puzzle_title))
            .setMessage(getResources().getString(R.string.reveal_puzzle_body))
            .setPositiveButton(
                android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BOARD.revealPuzzle();
                        render();
                    }
                })
            .setNegativeButton(
                getResources().getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return dialogBuilder.create();
    }

    private void updateClueSize() {
        String clueSizeStr = prefs.getString("clueSize", "12");
        try
        {
            setClueSize(Integer.parseInt(clueSizeStr));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
    }

    private void setClueSize(int dps) {
        this.clue.setTextSize(TypedValue.COMPLEX_UNIT_SP, dps);

        if ((acrossAdapter != null) && (downAdapter != null)) {
            acrossAdapter.setTextSize(dps);
            acrossAdapter.notifyDataSetInvalidated();
            downAdapter.setTextSize(dps);
            downAdapter.notifyDataSetInvalidated();
        }
    }

    private MovementStrategy getMovementStrategy() {
        String strategyName = prefs.getString("movementStrategy", MovementStrategy.MOVE_NEXT_ON_AXIS.toString());

        try {
            return MovementStrategy.valueOf(strategyName);
        } catch (IllegalArgumentException e) {
            LOG.warning("Invalid movement strategy: " + strategyName);
            return MovementStrategy.MOVE_NEXT_ON_AXIS;
        }
    }

    private Dialog createInfoDialog() {
        if (dialog == null) {
            dialog = new Dialog(this);
        }

        dialog.setTitle(getResources().getString(R.string.dialog_info_title));
        dialog.setContentView(R.layout.puzzle_info_dialog);

        TextView view = (TextView) dialog.findViewById(R.id.puzzle_info_title);
        view.setText(this.puz.getTitle());
        view = (TextView) dialog.findViewById(R.id.puzzle_info_author);
        view.setText(this.puz.getAuthor());
        view = (TextView) dialog.findViewById(R.id.puzzle_info_copyright);
        view.setText(this.puz.getCopyright());

        return dialog;
    }

    private void updateElapsedTime(TextView view) {
        String elapsedStr = getResources().getString(R.string.elapsed_time);
        if (timer != null) {
            view.setText(elapsedStr + " " + timer.time());
        } else {
            view.setText(elapsedStr + " " + new ImaginaryTimer(puz.getTime()).time());
        }
    }

    private void render() {
        render(null);
    }

    private void render(Word previous) {
        if (puz == null) {
            return;
        }

        if (shouldShowKeyboard(configuration)) {
            if (this.useNativeKeyboard) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        Clue c = BOARD.getClue();

        if (c.hint == null) {
            BOARD.toggleDirection();
            c = BOARD.getClue();
        }

        this.boardView.render(previous);
        this.boardView.requestFocus();

        // If we jumped to a new word, ensure the first letter is visible.
        // Otherwise, insure that the current letter is visible. Only necessary
        // if the cursor is currently off screen.
        if (this.prefs.getBoolean("ensureVisible", true)) {
            if ((previous != null) && previous.equals(BOARD.getCurrentWord())) {
                boardView.ensureVisible(BOARD.getCursorPosition());
            } else {
                boardView.ensureVisible(BOARD.getCurrentWordStart());
            }
        }

        String dirStr = getResources().getString(BOARD.isCursorAcross() ? R.string.across : R.string.down);
        StringBuilder clueText = new StringBuilder();
        clueText.append("(")
                .append(dirStr)
                .append(") ")
                .append(c.number)
                .append(". ")
                .append(c.hint);

        if (showCount) {
            clueText.append(" [")
                    .append(BOARD.getCurrentWord().length)
                    .append("]");
        }
        clue.setText(clueText.toString());

        if (this.allClues != null) {
            if (BOARD.isCursorAcross()) {
                ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
                        .get(0);
                cla.setActiveDirection(BOARD.isCursorAcross());
                cla.setHighlightClue(c);
                this.allCluesAdapter.notifyDataSetChanged();
                this.allClues.setSelectionFromTop(cla.indexOf(c) + 1,
                        (this.allClues.getHeight() / 2) - 50);
            } else {
                ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
                        .get(1);
                cla.setActiveDirection(!BOARD.isCursorAcross());
                cla.setHighlightClue(c);
                this.allCluesAdapter.notifyDataSetChanged();
                this.allClues.setSelectionFromTop(
                        cla.indexOf(c) + BOARD.getAcrossClues().length + 2,
                        (this.allClues.getHeight() / 2) - 50);
            }
        }

        if (this.down != null) {
            this.downAdapter.setHighlightClue(c);
            this.downAdapter.setActiveDirection(!BOARD.isCursorAcross());
            this.downAdapter.notifyDataSetChanged();

            if (!BOARD.isCursorAcross() && !c.equals(this.down.getSelectedItem())) {
                if (this.down instanceof ListView) {
                    ((ListView) this.down).setSelectionFromTop(
                            this.downAdapter.indexOf(c),
                            (down.getHeight() / 2) - 50);
                } else {
                    // Gallery
                    this.down.setSelection(this.downAdapter.indexOf(c));
                }
            }
        }

        if (this.across != null) {
            this.acrossAdapter.setHighlightClue(c);
            this.acrossAdapter.setActiveDirection(BOARD.isCursorAcross());
            this.acrossAdapter.notifyDataSetChanged();

            if (BOARD.isCursorAcross() && !c.equals(this.across.getSelectedItem())) {
                if (across instanceof ListView) {
                    ((ListView) this.across).setSelectionFromTop(
                            this.acrossAdapter.indexOf(c),
                            (across.getHeight() / 2) - 50);
                } else {
                    // Gallery view
                    this.across.setSelection(this.acrossAdapter.indexOf(c));
                }
            }
        }

        if (puz.isSolved() && (timer != null)) {
            timer.stop();
            puz.setTime(timer.getElapsed());
            timer = null;

            Intent intent = new Intent(PlayActivity.this, PuzzleFinishedActivity.class);
            startActivity(intent);

        }
        this.boardView.requestFocus();
    }

    private void updateProgressBar() {
        if (showingProgressBar) {
            getWindow().setFeatureInt(Window.FEATURE_PROGRESS, (int)(puz.getFractionComplete() * 10000));
        }
    }

    private class UpdateTimeTask implements Runnable {
        private boolean isScheduled = false;

        public void run() {
            isScheduled = false;
            updateTime();
        }

        public void updateTime() {
            if (showTimer) {
                String timeElapsed;
                if (timer != null) {
                    timeElapsed = timer.time();
                } else {
                    timeElapsed = new ImaginaryTimer(puz.getTime()).time();
                }

                if (timerText != null) {
                    timerText.setVisibility(View.VISIBLE);
                    timerText.setText(timeElapsed);
                } else {
                    setTitle(timeElapsed);
                }

                if (!isScheduled) {
                    isScheduled = true;
                    handler.postDelayed(this, 1000);
                }
            } else {
                if (timerText != null) {
                    timerText.setVisibility(View.GONE);
                } else {
                    setTitle(getResources().getString(R.string.app_name));
                }
            }
        }
    }
}
