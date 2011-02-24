package com.totsp.crossword;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

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

import android.util.DisplayMetrics;
import android.util.TypedValue;

import android.view.ContextMenu;

import android.view.ContextMenu.ContextMenuInfo;

import android.view.KeyEvent;
import android.view.Menu;
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

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.MovementStrategy;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.view.PlayboardRenderer;
import com.totsp.crossword.view.ScrollingImageView;
import com.totsp.crossword.view.ScrollingImageView.ClickListener;
import com.totsp.crossword.view.ScrollingImageView.Point;
import com.totsp.crossword.view.ScrollingImageView.ScaleListener;

import java.io.File;
import java.io.IOException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlayActivity extends ShortyzActivity {
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private static final int INFO_DIALOG = 0;
    private static final int COMPLETE_DIALOG = 1;
    private static final int REVEAL_PUZZLE_DIALOG = 2;
    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Playboard BOARD;
    static PlayboardRenderer RENDERER;
    private AdapterView across;
    private AdapterView down;
    private AlertDialog completeDialog;
    private AlertDialog revealPuzzleDialog;
    private ClueListAdapter acrossAdapter;
    private ClueListAdapter downAdapter;
    private Configuration configuration;
    private Dialog dialog;
    private File baseFile;
    private Handler handler = new Handler();
    private ImaginaryTimer timer;
    private KeyboardView keyboardView = null;
    private MovementStrategy movement = null;
    private Puzzle puz;
    private ScrollingImageView boardView;
    private TextView clue;
    private boolean fitToScreen;
    private boolean runTimer = false;
    private Runnable updateTimeTask = new Runnable() {
            public void run() {
                if (timer != null) {
                    getWindow()
                        .setTitle(timer.time());
                    getWindow()
                        .setFeatureInt(Window.FEATURE_PROGRESS, puz.getPercentComplete() * 100);
                }

                if (runTimer) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

    private boolean showCount = false;
    private boolean showErrors = false;
    private boolean useNativeKeyboard = false;
    private long lastKey;
    private long lastTap = 0;
    private long resumedOn;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        this.configuration = newConfig;

        if (this.prefs.getBoolean("forceKeyboard", false) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            if (this.useNativeKeyboard) {
                keyboardView.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        this.runTimer = prefs.getBoolean("runTimer", false);

        if (runTimer) {
            this.handler.post(this.updateTimeTask);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils.holographic(this);
        this.configuration = getBaseContext()
                                 .getResources()
                                 .getConfiguration();
        this.showErrors = this.prefs.getBoolean("showErrors", false);
        setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE);

        MovementStrategy movement = this.getMovementStrategy();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager()
            .getDefaultDisplay()
            .getMetrics(metrics);

        if (!prefs.getBoolean("showTimer", false)) {
            if (android.os.Build.VERSION.SDK_INT < 10) {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
        } else {
            requestWindowFeature(Window.FEATURE_PROGRESS);
        }

        if (prefs.getBoolean("fullScreen", false)) {
            getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        try {
            Uri u = this.getIntent()
                        .getData();

            if (u != null) {
                if (u.getScheme()
                         .equals("file")) {
                    baseFile = new File(u.getPath());
                    puz = IO.load(baseFile);
                }
            }

            if (puz == null) {
                throw new IOException();
            }

            BOARD = new Playboard(puz, movement);
            RENDERER = new PlayboardRenderer(BOARD, metrics.density);

            float scale = prefs.getFloat("scale", metrics.density);

            if (scale > 2.5f) {
                scale = 2.5f;
                prefs.edit()
                     .putFloat("scale", 2.5f)
                     .commit();
            } else if (scale < .5f) {
                scale = .25f;
                prefs.edit()
                     .putFloat("scale", .25f)
                     .commit();
            } else if (scale == Float.NaN) {
                scale = 1f;
                prefs.edit()
                     .putFloat("scale", 1f)
                     .commit();
            }

            RENDERER.setScale(scale);

            BOARD.setSkipCompletedLetters(this.prefs.getBoolean("skipFilled", false));

            if (puz.getPercentComplete() != 100) {
                this.timer = new ImaginaryTimer(puz.getTime());
                this.timer.start();
                this.runTimer = prefs.getBoolean("showTimer", false);

                if (runTimer) {
                    this.handler.post(this.updateTimeTask);
                }
            }

            setContentView(R.layout.play);

            int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString("keyboardType", "")) ? R.xml.keyboard_dpad
                                                                                              : R.xml.keyboard;
            Keyboard keyboard = new Keyboard(this, keyboardType);
            keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
            keyboardView.setKeyboard(keyboard);
            this.useNativeKeyboard = "NATIVE".equals(prefs.getString("keyboardType", ""));

            if (this.useNativeKeyboard) {
                keyboardView.setVisibility(View.GONE);
            }

            keyboardView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                    private long lastSwipe = 0;

                    public void onKey(int primaryCode, int[] keyCodes) {
                        long eventTime = System.currentTimeMillis();

                        if ((eventTime - lastSwipe) < 500) {
                            return;
                        }

                        KeyEvent event = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0,
                                0, KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
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

                        KeyEvent event = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_DOWN, event);
                    }

                    public void swipeLeft() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, event);
                    }

                    public void swipeRight() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, event);
                    }

                    public void swipeUp() {
                        long eventTime = System.currentTimeMillis();
                        lastSwipe = eventTime;

                        KeyEvent event = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, 0,
                                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
                        PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_UP, event);
                    }
                });

            this.clue = (TextView) this.findViewById(R.id.clueLine);

            this.clue.setClickable(true);
            this.clue.setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
                        i.setData(Uri.fromFile(baseFile));
                        PlayActivity.this.startActivityForResult(i, 0);
                    }
                });

            boardView = (ScrollingImageView) this.findViewById(R.id.board);

            this.registerForContextMenu(boardView);
            boardView.setContextMenuListener(new ClickListener() {
                    public void onContextMenu(final Point e) {
                        handler.post(new Runnable() {
                                public void run() {
                                    try {
                                        Position p = PlayActivity.RENDERER.findBox(e);
                                        Word w = PlayActivity.BOARD.setHighlightLetter(p);
                                        PlayActivity.RENDERER.draw(w);
                                        PlayActivity.this.openContextMenu(boardView);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                    }

                    public void onTap(Point e) {
                        try {
                            if (prefs.getBoolean("doubleTap", false) && ((System.currentTimeMillis() - lastTap) < 300)) {
                                if (fitToScreen) {
                                    PlayActivity.RENDERER.setScale(prefs.getFloat("scale", 1F));
                                    PlayActivity.BOARD.setHighlightLetter(RENDERER.findBox(e));
                                    render();
                                } else {
                                    int w = boardView.getWidth();
                                    int h = boardView.getHeight();
                                    PlayActivity.RENDERER.fitTo((w < h) ? w : h);
                                    render();
                                    boardView.scrollTo(0, 0);
                                }

                                fitToScreen = !fitToScreen;
                            } else {
                                Position p = PlayActivity.RENDERER.findBox(e);
                                Word old = PlayActivity.BOARD.setHighlightLetter(p);
                                PlayActivity.this.render(old);
                            }

                            lastTap = System.currentTimeMillis();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        } catch (IOException e) {
            System.err.println(this.getIntent().getData());
            e.printStackTrace();

            String filename = null;

            try {
                filename = this.baseFile.getName();
            } catch (Exception ee) {
                e.printStackTrace();
            }

            Toast t = Toast.makeText(this, (("Unable to read file" + filename) != null) ? (" \n" + filename) : "",
                    Toast.LENGTH_SHORT);
            t.show();
            this.finish();

            return;
        }

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

        revealPuzzleDialog = new AlertDialog.Builder(this).create();
        revealPuzzleDialog.setTitle("Reveal Entire Puzzle");
        revealPuzzleDialog.setMessage("Are you sure?");

        revealPuzzleDialog.setButton("OK",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PlayActivity.BOARD.revealPuzzle();
                    render();
                }
            });
        revealPuzzleDialog.setButton2("Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

        this.boardView.setScaleListener(new ScaleListener() {
                TimerTask t;
                Timer renderTimer = new Timer();

                public void onScale(float newScale, final Point center) {
                    fitToScreen = false;

                    if (t != null) {
                        t.cancel();
                    }

                    renderTimer.purge();
                    t = new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                            public void run() {
                                                int w = boardView.getImageView()
                                                                 .getWidth();
                                                int h = boardView.getImageView()
                                                                 .getHeight();
                                                prefs.edit()
                                                     .putFloat("scale", RENDERER.fitTo((w < h) ? w : h))
                                                     .commit();
                                                BOARD.setHighlightLetter(RENDERER.findBox(center));

                                                render(false);
                                            }
                                        });
                                }
                            };
                    renderTimer.schedule(t, 500);
                }
            });

        if (puz.isUpdatable()) {
            this.showErrors = false;
        }

        if (BOARD.isShowErrors() != this.showErrors) {
            BOARD.toggleShowErrors();
        }

        this.showCount = prefs.getBoolean("showCount", false);
        this.render();

        this.across = (AdapterView) this.findViewById(R.id.acrossList);
        this.down = (AdapterView) this.findViewById(R.id.downList);

        if ((this.across == null) && (this.down == null)) {
            this.across = (AdapterView) this.findViewById(R.id.acrossListGal);
            this.down = (AdapterView) this.findViewById(R.id.downListGal);
        }

        if ((across != null) && (down != null)) {
            across.setAdapter(this.acrossAdapter = new ClueListAdapter(this, PlayActivity.BOARD.getAcrossClues(), true));
            across.setFocusableInTouchMode(true);
            down.setAdapter(this.downAdapter = new ClueListAdapter(this, PlayActivity.BOARD.getDownClues(), false));
            across.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        arg0.setSelected(true);
                        PlayActivity.BOARD.jumpTo(arg2, true);
                        render();
                    }
                });
            across.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (!PlayActivity.BOARD.isAcross() || (PlayActivity.BOARD.getCurrentClueIndex() != arg2)) {
                            PlayActivity.BOARD.jumpTo(arg2, true);
                            render();
                        }
                    }

                    public void onNothingSelected(AdapterView<?> view) {
                    }
                });
            down.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                        PlayActivity.BOARD.jumpTo(arg2, false);
                        render();
                    }
                });

            down.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (PlayActivity.BOARD.isAcross() || (PlayActivity.BOARD.getCurrentClueIndex() != arg2)) {
                            PlayActivity.BOARD.jumpTo(arg2, false);
                            render();
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            down.scrollTo(0, 0);
            across.scrollTo(0, 0);
        }

        this.setClueSize(prefs.getInt("clueSize", 12));
        setTitle("Shortyz - " + puz.getTitle() + " - " + puz.getAuthor() + " - 	" + puz.getCopyright());
        utils.finishOnHomeButton(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        // System.out.println("CCM " + view);
        if (view == boardView) {
            Menu clueSize = menu.addSubMenu("Clue Text Size");
            clueSize.add("Small");
            clueSize.add("Medium");
            clueSize.add("Large");

            menu.add("Zoom In");
            menu.add("Zoom Out");
            menu.add("Fit to Screen");
            menu.add("Zoom Reset");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!puz.isUpdatable()) {
            MenuItem showItem = menu.add(this.showErrors ? "Hide Errors" : "Show Errors")
                                    .setIcon(android.R.drawable.ic_menu_view)
                                    .setCheckable(true);
            utils.onActionBarWithText(showItem);

            SubMenu reveal = menu.addSubMenu("Reveal")
                                 .setIcon(android.R.drawable.ic_menu_view);
            reveal.add("Letter");
            reveal.add("Word");
            reveal.add("Puzzle");
            utils.onActionBarWithText(reveal);
        } else {
            menu.add("Show Errors")
                .setEnabled(false)
                .setIcon(android.R.drawable.ic_menu_view);
            menu.add("Reveal")
                .setIcon(android.R.drawable.ic_menu_view)
                .setEnabled(false);
        }

        menu.add("Clues")
            .setIcon(android.R.drawable.ic_menu_agenda);
        menu.add("Info")
            .setIcon(android.R.drawable.ic_menu_info_details);
        menu.add("Help")
            .setIcon(android.R.drawable.ic_menu_help);
        menu.add("Settings")
            .setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Word previous;

        if ((System.currentTimeMillis() - this.resumedOn) < 500) {
            return true;
        }

        switch (keyCode) {
        case KeyEvent.KEYCODE_SEARCH:
            System.out.println("Next clue.");
            PlayActivity.BOARD.setMovementStrategy(MovementStrategy.MOVE_NEXT_CLUE);
            previous = PlayActivity.BOARD.nextWord();
            PlayActivity.BOARD.setMovementStrategy(this.getMovementStrategy());
            this.render(previous);

            return true;

        case KeyEvent.KEYCODE_BACK:
            this.finish();

            return true;

        case KeyEvent.KEYCODE_MENU:
            return false;

        case KeyEvent.KEYCODE_DPAD_DOWN:

            if ((System.currentTimeMillis() - lastKey) > 50) {
                previous = PlayActivity.BOARD.moveDown();
                this.render(previous);
            }

            lastKey = System.currentTimeMillis();

            return true;

        case KeyEvent.KEYCODE_DPAD_UP:

            if ((System.currentTimeMillis() - lastKey) > 50) {
                previous = PlayActivity.BOARD.moveUp();
                this.render(previous);
            }

            lastKey = System.currentTimeMillis();

            return true;

        case KeyEvent.KEYCODE_DPAD_LEFT:

            if ((System.currentTimeMillis() - lastKey) > 50) {
                previous = PlayActivity.BOARD.moveLeft();
                this.render(previous);
            }

            lastKey = System.currentTimeMillis();

            return true;

        case KeyEvent.KEYCODE_DPAD_RIGHT:

            if ((System.currentTimeMillis() - lastKey) > 50) {
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

            if (prefs.getBoolean("spaceChangesDirection", true)) {
                previous = PlayActivity.BOARD.toggleDirection();
                this.render(previous);
            } else {
                previous = PlayActivity.BOARD.playLetter(' ');
                this.render(previous);
            }

            return true;

        case KeyEvent.KEYCODE_ENTER:

            if (prefs.getBoolean("enterChangesDirection", true)) {
                previous = PlayActivity.BOARD.toggleDirection();
                this.render(previous);

                return true;
            } else {
                previous = PlayActivity.BOARD.nextWord();
                this.render(previous);

                return true;
            }

        case KeyEvent.KEYCODE_DEL:

            if ((System.currentTimeMillis() - lastKey) > 50) {
                previous = PlayActivity.BOARD.deleteLetter();
                this.render(previous);
            }

            lastKey = System.currentTimeMillis();

            return true;
        }

        char c = Character.toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) ||
                this.useNativeKeyboard) ? event.getDisplayLabel() : ((char) keyCode));

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

        if (item.getTitle()
                    .equals("Letter")) {
            PlayActivity.BOARD.revealLetter();
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Word")) {
            PlayActivity.BOARD.revealWord();
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Puzzle")) {
            this.showDialog(REVEAL_PUZZLE_DIALOG);

            return true;
        } else if (item.getTitle()
                           .equals("Show Errors") || item.getTitle()
                                                             .equals("Hide Errors")) {
            PlayActivity.BOARD.toggleShowErrors();
            item.setTitle(PlayActivity.BOARD.isShowErrors() ? "Hide Errors" : "Show Errors");
            this.prefs.edit()
                      .putBoolean("showErrors", BOARD.isShowErrors())
                      .commit();
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Settings")) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        } else if (item.getTitle()
                           .equals("Zoom In")) {
            this.boardView.scrollTo(0, 0);

            float newScale = RENDERER.zoomIn();
            this.prefs.edit()
                      .putFloat("scale", newScale)
                      .commit();
            this.fitToScreen = false;
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Zoom Out")) {
            this.boardView.scrollTo(0, 0);

            float newScale = RENDERER.zoomOut();
            this.prefs.edit()
                      .putFloat("scale", newScale)
                      .commit();
            this.fitToScreen = false;
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Fit to Screen")) {
            this.boardView.scrollTo(0, 0);

            int v = (this.boardView.getWidth() < this.boardView.getHeight()) ? this.boardView.getWidth()
                                                                             : this.boardView.getHeight();
            float newScale = RENDERER.fitTo(v);
            this.prefs.edit()
                      .putFloat("scale", newScale)
                      .commit();
            this.render();

            return true;
        } else if (item.getTitle()
                           .equals("Zoom Reset")) {
            float newScale = RENDERER.zoomReset();
            this.prefs.edit()
                      .putFloat("scale", newScale)
                      .commit();
            this.render();
            this.boardView.scrollTo(0, 0);

            return true;
        } else if (item.getTitle()
                           .equals("Info")) {
            if (dialog != null) {
                TextView view = (TextView) dialog.findViewById(R.id.puzzle_info_time);

                if (timer != null) {
                    this.timer.stop();
                    view.setText("Elapsed Time: " + this.timer.time());
                    this.timer.start();
                } else {
                    view.setText("Elapsed Time: " + new ImaginaryTimer(puz.getTime()).time());
                }
            }

            this.showDialog(INFO_DIALOG);

            return true;
        } else if (item.getTitle()
                           .equals("Clues")) {
            Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
            i.setData(Uri.fromFile(baseFile));
            PlayActivity.this.startActivityForResult(i, 0);

            return true;
        } else if (item.getTitle()
                           .equals("Help")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///android_asset/playscreen.html"), this,
                    HTMLActivity.class);
            this.startActivity(i);
        } else if (item.getTitle()
                           .equals("Small")) {
            this.setClueSize(14);
        } else if (item.getTitle()
                           .equals("Medium")) {
            this.setClueSize(18);
        } else if (item.getTitle()
                           .equals("Large")) {
            this.setClueSize(20);
        }

        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.render();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case INFO_DIALOG:

            // This is weird. I don't know why a rotate resets the dialog.
            // Whatevs.
            return createInfoDialog();

        case COMPLETE_DIALOG:
            return completeDialog;

        case REVEAL_PUZZLE_DIALOG:
            return revealPuzzleDialog;

        default:
            return null;
        }
    }

    @Override
    protected void onPause() {
        try {
            if ((puz != null) && (baseFile != null)) {
                if ((puz.getPercentComplete() != 100) && (this.timer != null)) {
                    this.timer.stop();
                    puz.setTime(timer.getElapsed());
                    this.timer = null;
                }

                IO.save(puz, baseFile);
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
    protected void onRestart() {
        super.onRestart();

        if (this.timer != null) {
            this.timer.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.resumedOn = System.currentTimeMillis();
        BOARD.setSkipCompletedLetters(this.prefs.getBoolean("skipFilled", false));
        BOARD.setMovementStrategy(this.getMovementStrategy());

        int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString("keyboardType", "")) ? R.xml.keyboard_dpad
                                                                                          : R.xml.keyboard;
        Keyboard keyboard = new Keyboard(this, keyboardType);
        keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
        keyboardView.setKeyboard(keyboard);
        this.useNativeKeyboard = "NATIVE".equals(prefs.getString("keyboardType", ""));

        if (this.useNativeKeyboard) {
            keyboardView.setVisibility(View.GONE);
        }

        this.showCount = prefs.getBoolean("showCount", false);
        this.onConfigurationChanged(this.configuration);

        if (puz.getPercentComplete() != 100) {
            timer = new ImaginaryTimer(this.puz.getTime());
            timer.start();
        }

        this.runTimer = prefs.getBoolean("showTimer", false);

        if (runTimer) {
            this.handler.post(this.updateTimeTask);
        }

        render();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.timer != null) {
            this.timer.stop();
        }
    }

    private void setClueSize(int dps) {
        this.clue.setTextSize(TypedValue.COMPLEX_UNIT_SP, dps);

        if ((acrossAdapter != null) && (downAdapter != null)) {
            acrossAdapter.textSize = dps;
            acrossAdapter.notifyDataSetInvalidated();
            downAdapter.textSize = dps;
            downAdapter.notifyDataSetInvalidated();
        }

        if (prefs.getInt("clueSize", 14) != dps) {
            this.prefs.edit()
                      .putInt("clueSize", dps)
                      .commit();
        }
    }

    private MovementStrategy getMovementStrategy() {
        if (movement != null) {
            return movement;
        } else {
            String stratName = this.prefs.getString("movementStrategy", "MOVE_NEXT_ON_AXIS");
            System.out.println("Movement Strategy:" + stratName);

            if (stratName.equals("MOVE_NEXT_ON_AXIS")) {
                movement = MovementStrategy.MOVE_NEXT_ON_AXIS;
            } else if (stratName.equals("STOP_ON_END")) {
                movement = MovementStrategy.STOP_ON_END;
            } else if (stratName.equals("MOVE_NEXT_CLUE")) {
                movement = MovementStrategy.MOVE_NEXT_CLUE;
            } else if (stratName.equals("MOVE_PARALLEL_WORD")) {
                movement = MovementStrategy.MOVE_PARALLEL_WORD;
            }

            return movement;
        }
    }

    private Dialog createInfoDialog() {
        if (dialog == null) {
            dialog = new Dialog(this);
        }

        dialog.setTitle("Puzzle Info");
        dialog.setContentView(R.layout.puzzle_info_dialog);

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
            view.setText("Elapsed Time: " + new ImaginaryTimer(puz.getTime()).time());
        }

        ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.puzzle_info_progress);
        progress.setProgress(this.puz.getPercentComplete());

        return dialog;
    }

    private void render() {
        render(null);
    }

    private void render(boolean rescale) {
        this.render(null, rescale);
    }

    private void render(Word previous) {
        this.render(previous, true);
    }

    private void render(Word previous, boolean rescale) {
        if (this.prefs.getBoolean("forceKeyboard", false) ||
        		android.os.Build.VERSION.SDK_INT >= 11 ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) ||
                (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
            if (this.useNativeKeyboard) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                this.keyboardView.setVisibility(View.VISIBLE);
            }
        } else {
            this.keyboardView.setVisibility(View.GONE);
        }

        Clue c = PlayActivity.BOARD.getClue();

        if (c.hint == null) {
            PlayActivity.BOARD.toggleDirection();
            c = PlayActivity.BOARD.getClue();
        }

        this.boardView.setBitmap(RENDERER.draw(previous), rescale);

        /*
         * If we jumped to a new word, ensure the first letter is visible.
         * Otherwise, insure that the current letter is visible. Only necessary
         * if the cursor is currently off screen.
         */
        if (rescale && this.prefs.getBoolean("ensureVisible", true)) {
            Point topLeft;
            Point bottomRight;
            Point cursorTopLeft;
            Point cursorBottomRight;
            cursorTopLeft = RENDERER.findPointTopLeft(PlayActivity.BOARD.getHighlightLetter());
            cursorBottomRight = RENDERER.findPointBottomRight(PlayActivity.BOARD.getHighlightLetter());

            if ((previous != null) && previous.equals(PlayActivity.BOARD.getCurrentWord())) {
                topLeft = cursorTopLeft;
                bottomRight = cursorBottomRight;
            } else {
                topLeft = RENDERER.findPointTopLeft(PlayActivity.BOARD.getCurrentWordStart());
                bottomRight = RENDERER.findPointBottomRight(PlayActivity.BOARD.getCurrentWordStart());
            }

            int tlDistance = cursorTopLeft.distance(topLeft);
            int brDistance = cursorBottomRight.distance(bottomRight);

            if (!this.boardView.isVisible(topLeft) && (tlDistance < brDistance)) {
                this.boardView.ensureVisible(topLeft);
            }

            if (!this.boardView.isVisible(bottomRight) && (brDistance < tlDistance)) {
                this.boardView.ensureVisible(bottomRight);
            }

            // ensure the cursor is always on the screen.
            this.boardView.ensureVisible(cursorBottomRight);
            this.boardView.ensureVisible(cursorTopLeft);
        }

        this.clue.setText("(" + (PlayActivity.BOARD.isAcross() ? "across" : "down") + ") " + c.number + ". " + c.hint +
            (this.showCount ? ("  [" + PlayActivity.BOARD.getCurrentWord().length + "]") : ""));

        if ((puz.getPercentComplete() == 100) && (timer != null)) {
            timer.stop();
            puz.setTime(timer.getElapsed());
            this.completeDialog.setMessage("Completed in " + timer.time());
            this.showDialog(COMPLETE_DIALOG);
            this.timer = null;
        }

        if (this.downAdapter != null) {
            this.downAdapter.setHighlightClue(c);
            this.downAdapter.setActiveDirection(!BOARD.isAcross());
            this.downAdapter.notifyDataSetChanged();

            if (!BOARD.isAcross() && !c.equals(this.down.getSelectedItem())) {
                if (this.down instanceof ListView) {
                    ((ListView) this.down).setSelectionFromTop(this.downAdapter.indexOf(c), (down.getHeight() / 2) -
                        50);
                } else {
                    //Gallery
                    this.down.setSelection(this.downAdapter.indexOf(c));
                }
            }
        }

        if (this.acrossAdapter != null) {
            this.acrossAdapter.setHighlightClue(c);
            this.acrossAdapter.setActiveDirection(BOARD.isAcross());
            this.acrossAdapter.notifyDataSetChanged();

            if (BOARD.isAcross() && !c.equals(this.across.getSelectedItem())) {
                if (across instanceof ListView) {
                    ((ListView) this.across).setSelectionFromTop(this.acrossAdapter.indexOf(c),
                        (across.getHeight() / 2) - 50);
                } else {
                    //Gallery view
                    this.across.setSelection(this.acrossAdapter.indexOf(c));
                }
            }
        }
    }
}
