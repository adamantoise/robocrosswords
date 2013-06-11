package com.adamrosenfield.wordswithcrosses;

import static com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication.BOARD;
import static com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication.RENDERER;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");
	private static final int INFO_DIALOG = 0;
	private static final int REVEAL_PUZZLE_DIALOG = 2;

	/** Clue font sizes, in sp */
	private static final int CLUE_SIZES[] = {12, 14, 16};

	@SuppressWarnings("rawtypes")
	private AdapterView across;
	@SuppressWarnings("rawtypes")
	private AdapterView down;
	private AlertDialog revealPuzzleDialog;
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
	private boolean showTimer = false;
	private boolean showingProgressBar = false;

	private UpdateTimeTask updateTimeTask = new UpdateTimeTask();

	private boolean showCount = false;
	private boolean showErrors = false;
	private boolean useNativeKeyboard = false;
	private long lastKey;
	private long resumedOn;

    private DisplayMetrics metrics;

    // Saved scale from before we fit to screen
    private float lastBoardScale = 1.0f;
    private boolean fitToScreen = false;

    private boolean hasSetInitialZoom = false;

    private static final int MENU_ID_SHOW_ERRORS = 1;
    private static final int MENU_ID_REVEAL = 2;
    private static final int MENU_ID_REVEAL_LETTER = 3;
    private static final int MENU_ID_REVEAL_WORD = 4;
    private static final int MENU_ID_REVEAL_PUZZLE = 5;
    private static final int MENU_ID_CLUES = 6;
    private static final int MENU_ID_INFO = 7;
    private static final int MENU_ID_HELP = 8;
    private static final int MENU_ID_SETTINGS = 9;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		this.configuration = newConfig;

		if (this.prefs.getBoolean("forceKeyboard", false)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
			if (this.useNativeKeyboard) {
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

		showTimer = prefs.getBoolean("showTimer", false);
		updateTimeTask.updateTime();
	}

	/** Called when the activity is first created. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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

		// Must happen after all calls to requestWindowFeature()
        setContentView(R.layout.play);

		utils.holographic(this);
		utils.finishOnHomeButton(this);

		this.showErrors = this.prefs.getBoolean("showErrors", false);
		setDefaultKeyMode(Activity.DEFAULT_KEYS_DISABLE);

		if (prefs.getBoolean("fullScreen", false)) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		String filename = null;
		try {
		    puzzleId = getIntent().getLongExtra(EXTRA_PUZZLE_ID,  -1);
		    if (puzzleId == -1) {
		        throw new IOException(EXTRA_PUZZLE_ID + " extra must be specified");
		    }

		    PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
		    filename = dbHelper.getFilename(puzzleId);
		    if (filename == null) {
		        throw new IOException("Invalid puzzle ID: " + puzzleId);
		    }

		    baseFile = new File(filename);
		    puz = IO.load(baseFile);

			BOARD = new Playboard(puz, puzzleId, getMovementStrategy());
			RENDERER = new PlayboardRenderer(BOARD, !prefs.getBoolean("suppressHints", false));

            SolveState solveState = dbHelper.getPuzzleSolveState(puzzleId);
            if (solveState != null) {
                BOARD.setSolveState(solveState);
            }

			BOARD.setSkipCompletedLetters(this.prefs.getBoolean("skipFilled", false));

			BOARD.setOnBoardChangedListener(new OnBoardChangedListener() {
			    public void onBoardChanged() {
			        updateProgressBar();
			    }
			});

			int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString(
					"keyboardType", "")) ? R.xml.keyboard_dpad : R.xml.keyboard;
			Keyboard keyboard = new Keyboard(this, keyboardType);
			keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
			keyboardView.setKeyboard(keyboard);
			this.useNativeKeyboard = "NATIVE".equals(prefs.getString(
					"keyboardType", ""));

			if (this.useNativeKeyboard) {
				keyboardView.setVisibility(View.GONE);
			}

			keyboardView
					.setOnKeyboardActionListener(new OnKeyboardActionListener() {
						private long lastSwipe = 0;

						public void onKey(int primaryCode, int[] keyCodes) {
							long eventTime = System.currentTimeMillis();

							if ((eventTime - lastSwipe) < 500) {
								return;
							}

							KeyEvent event = new KeyEvent(eventTime, eventTime,
									KeyEvent.ACTION_DOWN, primaryCode, 0, 0, 0,
									0, KeyEvent.FLAG_SOFT_KEYBOARD
											| KeyEvent.FLAG_KEEP_TOUCH_MODE);
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

							KeyEvent event = new KeyEvent(eventTime, eventTime,
									KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DPAD_DOWN, 0, 0, 0, 0,
									KeyEvent.FLAG_SOFT_KEYBOARD
											| KeyEvent.FLAG_KEEP_TOUCH_MODE);
							PlayActivity.this.onKeyUp(
									KeyEvent.KEYCODE_DPAD_DOWN, event);
						}

						public void swipeLeft() {
							long eventTime = System.currentTimeMillis();
							lastSwipe = eventTime;

							KeyEvent event = new KeyEvent(eventTime, eventTime,
									KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
									KeyEvent.FLAG_SOFT_KEYBOARD
											| KeyEvent.FLAG_KEEP_TOUCH_MODE);
							PlayActivity.this.onKeyUp(
									KeyEvent.KEYCODE_DPAD_LEFT, event);
						}

						public void swipeRight() {
							long eventTime = System.currentTimeMillis();
							lastSwipe = eventTime;

							KeyEvent event = new KeyEvent(eventTime, eventTime,
									KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0, 0, 0,
									KeyEvent.FLAG_SOFT_KEYBOARD
											| KeyEvent.FLAG_KEEP_TOUCH_MODE);
							PlayActivity.this.onKeyUp(
									KeyEvent.KEYCODE_DPAD_RIGHT, event);
						}

						public void swipeUp() {
							long eventTime = System.currentTimeMillis();
							lastSwipe = eventTime;

							KeyEvent event = new KeyEvent(eventTime, eventTime,
									KeyEvent.ACTION_DOWN,
									KeyEvent.KEYCODE_DPAD_UP, 0, 0, 0, 0,
									KeyEvent.FLAG_SOFT_KEYBOARD
											| KeyEvent.FLAG_KEEP_TOUCH_MODE);
							PlayActivity.this.onKeyUp(KeyEvent.KEYCODE_DPAD_UP,
									event);
						}
					});

			clue = (TextView)this.findViewById(R.id.clueLine);
			if (clue.getVisibility() != View.GONE && android.os.Build.VERSION.SDK_INT >= 14) {
				clue.setVisibility(View.GONE);
				View clueLine = utils.onActionBarCustom(this, R.layout.clue_line_only);
				if (clueLine != null) {
				    clue = (TextView)clueLine.findViewById(R.id.clueLine);
				    timerText = (TextView)clueLine.findViewById(R.id.timerText);
				}
			}
			clue.setClickable(true);
			clue.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					Intent i = new Intent(PlayActivity.this, ClueListActivity.class);
					i.setData(Uri.fromFile(baseFile));
					PlayActivity.this.startActivityForResult(i, 0);
				}
			});

			boardView = (CrosswordImageView)findViewById(R.id.board);
			boardView.setBoard(BOARD, metrics);

			this.registerForContextMenu(boardView);

			boardView.setClickListener(new ClickListener() {
			    public void onClick(Position pos) {
			        Word prevWord = null;
                    if (pos != null) {
                        prevWord = BOARD.setHighlightLetter(pos);
                    }
                    render(prevWord);
			    }

			    public void onDoubleClick(Position pos) {
			        if (prefs.getBoolean("doubleTap",  false)) {
			            if (fitToScreen) {
			                boardView.setRenderScale(lastBoardScale);

			                Word prevWord = null;
                            if (pos != null) {
                                prevWord = BOARD.setHighlightLetter(pos);
                            }
                            render(prevWord);
			            } else {
			                lastBoardScale = boardView.getRenderScale();
			                boardView.fitToScreen();
			                render();
			            }

			            fitToScreen = !fitToScreen;
			        } else {
			            onClick(pos);
			        }
			    }

			    public void onLongClick(Position pos) {
			        Word prevWord = null;
			        if (pos != null) {
			            prevWord = BOARD.setHighlightLetter(pos);
			        }
			        boardView.render(prevWord);
			        openContextMenu(boardView);
			    }
			});

			boardView.setRenderScaleListener(new RenderScaleListener() {
			    public void onRenderScaleChanged(float renderScale) {
			        fitToScreen = false;
			    }
			});

		} catch (IOException e) {
			e.printStackTrace();

			String text = getResources().getString(R.string.load_puzzle_failed);
			if (filename != null) {
			    text += filename;
			}

			Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
			toast.show();
			this.finish();

			return;
		}

		revealPuzzleDialog = new AlertDialog.Builder(this).create();
		revealPuzzleDialog.setTitle(getResources().getString(R.string.reveal_puzzle_title));
		revealPuzzleDialog.setMessage(getResources().getString(R.string.reveal_puzzle_body));

		revealPuzzleDialog.setButton(
				DialogInterface.BUTTON_POSITIVE,
				getResources().getString(R.string.reveal_puzzle_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						BOARD.revealPuzzle();
						render();
					}
				});
		revealPuzzleDialog.setButton(
				DialogInterface.BUTTON_NEGATIVE,
				getResources().getString(R.string.reveal_puzzle_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		if (BOARD.isShowErrors() != this.showErrors) {
			BOARD.toggleShowErrors();
		}

		this.render();

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
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					arg0.setSelected(true);
					BOARD.jumpTo(arg2, true);
					render();
				}
			});
			across.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if (!BOARD.isAcross()
							|| (BOARD.getCurrentClueIndex() != arg2)) {
						BOARD.jumpTo(arg2, true);
						render();
					}
				}

				public void onNothingSelected(AdapterView<?> view) {
				}
			});
			down.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						final int arg2, long arg3) {
					BOARD.jumpTo(arg2, false);
					render();
				}
			});

			down.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					if (BOARD.isAcross()
							|| (BOARD.getCurrentClueIndex() != arg2)) {
						BOARD.jumpTo(arg2, false);
						render();
					}
				}

				public void onNothingSelected(AdapterView<?> arg0) {
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
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int clickIndex, long arg3) {
					boolean across = clickIndex <= BOARD.getAcrossClues().length +1;
					int index = clickIndex -1;
					if(index > BOARD.getAcrossClues().length ){
						index = index - BOARD.getAcrossClues().length - 1;
					}
					arg0.setSelected(true);
					BOARD.jumpTo(index, across);
					render();
				}
			});
			allClues.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int clickIndex, long arg3) {
						boolean across = clickIndex <= BOARD.getAcrossClues().length +1;
						int index = clickIndex -1;
						if(index > BOARD.getAcrossClues().length ){
							index = index - BOARD.getAcrossClues().length - 1;
						}
							if(!BOARD.isAcross() == across && BOARD.getCurrentClueIndex() != index){
							arg0.setSelected(true);
							BOARD.jumpTo(index, across);
							render();
						}
				}

				public void onNothingSelected(AdapterView<?> view) {
				}
			});

		}

		this.setClueSize(prefs.getInt("clueSize", CLUE_SIZES[0]));
		setTitle("Words With Crosses - " + puz.getTitle() + " - " + puz.getAuthor()
				+ " - 	" + puz.getCopyright());
		this.showCount = prefs.getBoolean("showCount", false);
	}

	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
	    if (!hasSetInitialZoom) {
	        boardView.fitToScreen();
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

		    int clueSize = prefs.getInt("clueSize", CLUE_SIZES[0]);
		    int clueSizeIndex = Arrays.binarySearch(CLUE_SIZES, clueSize);
		    if (clueSizeIndex >= 0) {
		        menu.getItem(0).getSubMenu().getItem(clueSizeIndex).setChecked(true);
		    }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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

		if (WordsWithCrossesApplication.isTabletish(metrics)) {
            utils.onActionBarWithText(showItem);
            utils.onActionBarWithText(reveal);
        }

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
			BOARD.setMovementStrategy(MovementStrategy.MOVE_NEXT_CLUE);
			previous = BOARD.nextWord();
			BOARD.setMovementStrategy(this.getMovementStrategy());
			this.render(previous);

			return true;

		case KeyEvent.KEYCODE_BACK:
			this.finish();

			return true;

		case KeyEvent.KEYCODE_MENU:
			return false;

		case KeyEvent.KEYCODE_DPAD_DOWN:

			if ((System.currentTimeMillis() - lastKey) > 50) {
				previous = BOARD.moveDown();
				this.render(previous);
			}

			lastKey = System.currentTimeMillis();

			return true;

		case KeyEvent.KEYCODE_DPAD_UP:

			if ((System.currentTimeMillis() - lastKey) > 50) {
				previous = BOARD.moveUp();
				this.render(previous);
			}

			lastKey = System.currentTimeMillis();

			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT:

			if ((System.currentTimeMillis() - lastKey) > 50) {
				previous = BOARD.moveLeft();
				this.render(previous);
			}

			lastKey = System.currentTimeMillis();

			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:

			if ((System.currentTimeMillis() - lastKey) > 50) {
				previous = BOARD.moveRight();
				this.render(previous);
			}

			lastKey = System.currentTimeMillis();

			return true;

		case KeyEvent.KEYCODE_DPAD_CENTER:
			previous = BOARD.toggleDirection();
			this.render(previous);

			return true;

		case KeyEvent.KEYCODE_SPACE:

			if ((System.currentTimeMillis() - lastKey) > 150) {
				if (prefs.getBoolean("spaceChangesDirection", true)) {
					previous = BOARD.toggleDirection();
					this.render(previous);
				} else {
					previous = BOARD.playLetter(' ');
					this.render(previous);
				}
			}

			lastKey = System.currentTimeMillis();

			return true;

		case KeyEvent.KEYCODE_ENTER:

			if ((System.currentTimeMillis() - lastKey) > 150) {
				if (prefs.getBoolean("enterChangesDirection", true)) {
					previous = BOARD.toggleDirection();
					this.render(previous);
				} else {
					previous = BOARD.nextWord();
					this.render(previous);
				}

				lastKey = System.currentTimeMillis();

				return true;
			}

		case KeyEvent.KEYCODE_DEL:

			if ((System.currentTimeMillis() - lastKey) > 150) {
				previous = BOARD.deleteLetter();
				this.render(previous);
			}

			lastKey = System.currentTimeMillis();

			return true;
		}

		char c = Character
				.toUpperCase(((this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) || this.useNativeKeyboard) ? event
						.getDisplayLabel() : ((char) keyCode));

		if (ALPHA.indexOf(c) != -1) {
			previous = BOARD.playLetter(c);
			this.render(previous);

			return true;
		}

		return super.onKeyUp(keyCode, event);
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
			BOARD.toggleShowErrors();
			int showErrorsStr = (BOARD.isShowErrors() ? R.string.menu_hide_errors : R.string.menu_show_errors);
			item.setTitle(showErrorsStr);
			prefs.edit().putBoolean("showErrors", BOARD.isShowErrors()).commit();
			render();
			return true;

	    case MENU_ID_SETTINGS:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;

	    case MENU_ID_INFO:
			if (dialog != null) {
				TextView view = (TextView)dialog.findViewById(R.id.puzzle_info_time);
				updateElapsedTime(view);
			}

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

	    case R.id.context_clue_text_size_small:
			setClueSize(CLUE_SIZES[0]);
			return true;

	    case R.id.context_clue_text_size_medium:
			setClueSize(CLUE_SIZES[1]);
			return true;

	    case R.id.context_clue_text_size_large:
			setClueSize(CLUE_SIZES[2]);
			return true;

	    default:
	        return false;
		}
	}

	@SuppressWarnings("deprecation")
    private void deprecatedShowDialog(int dialog) {
	    showDialog(dialog);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		this.render();
	}

	@Override
    protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case INFO_DIALOG:
			// This is weird. I don't know why a rotate resets the dialog.
			// Whatevs.
			return createInfoDialog();

		case REVEAL_PUZZLE_DIALOG:
			return revealPuzzleDialog;

		default:
			return null;
		}
	}

	@Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
	    if (id == INFO_DIALOG) {
	        TextView timeText = (TextView)dialog.findViewById(R.id.puzzle_info_time);
	        updateElapsedTime(timeText);

	        ProgressBar progress = (ProgressBar)dialog.findViewById(R.id.puzzle_info_progress);
	        progress.setProgress((int)(puz.getFractionComplete() * 10000));
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
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(clue.getWindowToken(), 0);
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
		this.resumedOn = System.currentTimeMillis();
		BOARD.setSkipCompletedLetters(this.prefs
				.getBoolean("skipFilled", false));
		BOARD.setMovementStrategy(getMovementStrategy());

		int keyboardType = "CONDENSED_ARROWS".equals(prefs.getString(
				"keyboardType", "")) ? R.xml.keyboard_dpad : R.xml.keyboard;
		Keyboard keyboard = new Keyboard(this, keyboardType);
		keyboardView = (KeyboardView) this.findViewById(R.id.playKeyboard);
		keyboardView.setKeyboard(keyboard);
		this.useNativeKeyboard = "NATIVE".equals(prefs.getString(
				"keyboardType", ""));

		if (this.useNativeKeyboard) {
			keyboardView.setVisibility(View.GONE);
		}

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

	private void setClueSize(int dps) {
		this.clue.setTextSize(TypedValue.COMPLEX_UNIT_SP, dps);

		if ((acrossAdapter != null) && (downAdapter != null)) {
			acrossAdapter.textSize = dps;
			acrossAdapter.notifyDataSetInvalidated();
			downAdapter.textSize = dps;
			downAdapter.notifyDataSetInvalidated();
		}

		if (prefs.getInt("clueSize", CLUE_SIZES[0]) != dps) {
			this.prefs.edit().putInt("clueSize", dps).commit();
		}
	}

	private MovementStrategy getMovementStrategy() {
		String stratName = prefs.getString("movementStrategy", "MOVE_NEXT_ON_AXIS");

		if (stratName.equals("MOVE_NEXT_ON_AXIS")) {
			return MovementStrategy.MOVE_NEXT_ON_AXIS;
		} else if (stratName.equals("STOP_ON_END")) {
			return MovementStrategy.STOP_ON_END;
		} else if (stratName.equals("MOVE_NEXT_CLUE")) {
			return MovementStrategy.MOVE_NEXT_CLUE;
		} else if (stratName.equals("MOVE_PARALLEL_WORD")) {
			return MovementStrategy.MOVE_PARALLEL_WORD;
		} else {
		    LOG.warning("Invalid movement strategy: " + stratName);
		    return MovementStrategy.MOVE_NEXT_ON_AXIS;
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
		if (this.prefs.getBoolean("forceKeyboard", false)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
				|| (this.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED)) {
			if (this.useNativeKeyboard) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
						InputMethodManager.HIDE_IMPLICIT_ONLY);
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
			    boardView.ensureVisible(BOARD.getHighlightLetter());
			} else {
			    boardView.ensureVisible(BOARD.getCurrentWordStart());
			}
		}

		String dirStr = getResources().getString(BOARD.isAcross() ? R.string.across : R.string.down);
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
			if (BOARD.isAcross()) {
				ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
						.get(0);
				cla.setActiveDirection(BOARD.isAcross());
				cla.setHighlightClue(c);
				this.allCluesAdapter.notifyDataSetChanged();
				this.allClues.setSelectionFromTop(cla.indexOf(c) + 1,
						(this.allClues.getHeight() / 2) - 50);
			} else {
				ClueListAdapter cla = (ClueListAdapter) this.allCluesAdapter.sections
						.get(1);
				cla.setActiveDirection(!BOARD.isAcross());
				cla.setHighlightClue(c);
				this.allCluesAdapter.notifyDataSetChanged();
				this.allClues.setSelectionFromTop(
						cla.indexOf(c) + BOARD.getAcrossClues().length + 2,
						(this.allClues.getHeight() / 2) - 50);
			}
		}

		if (this.down != null) {
			this.downAdapter.setHighlightClue(c);
			this.downAdapter.setActiveDirection(!BOARD.isAcross());
			this.downAdapter.notifyDataSetChanged();

			if (!BOARD.isAcross() && !c.equals(this.down.getSelectedItem())) {
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
			this.acrossAdapter.setActiveDirection(BOARD.isAcross());
			this.acrossAdapter.notifyDataSetChanged();

			if (BOARD.isAcross() && !c.equals(this.across.getSelectedItem())) {
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
		this.clue.requestFocus();
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
            String timeElapsed;
            if (timer != null) {
                timeElapsed = timer.time();
            } else {
                timeElapsed = new ImaginaryTimer(puz.getTime()).time();
            }

            if (timerText != null) {
                timerText.setText(timeElapsed);
            } else {
                setTitle(timeElapsed);
            }

            if (showTimer && !isScheduled) {
                isScheduled = true;
                handler.postDelayed(this, 1000);
            }
        }
	}
}
