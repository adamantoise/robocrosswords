/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Clue;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.client.Renderer.ClickListener;
import com.totsp.crossword.web.client.resources.Css;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

import java.util.Arrays;
import java.util.HashMap;


/**
 *
 * @author kebernet
 */
public class BasicEntryPoint implements EntryPoint {
    static final WASDCodes CODES = GWT.create(WASDCodes.class);
    public static PuzzleServiceAsync SERVICE = Injector.INSTANCE.service();
    public static PuzzleServiceProxy PROXY = new PuzzleServiceProxy(SERVICE, null);
    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Timer autoSaveTimer = null;
    static boolean dirty;
    static Css css = Injector.INSTANCE.resources().css();
    static FocusPanel mainPanel = new FocusPanel();
    static Playboard board;
    static Renderer r = Injector.INSTANCE.renderer();
    static Clue[] acrossClues;
    static Clue[] downClues;
    static final TextArea keyboardIntercept = new TextArea();
    static final Label status = new Label();
    static VerticalPanel verticalPanel = new VerticalPanel();
    static HashMap<Clue, Widget> acrossClueViews = new HashMap<Clue, Widget>();
    static HashMap<Clue, Widget> downClueViews = new HashMap<Clue, Widget>();
    static KeyboardListener l = new KeyboardListener() {
            @Override
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            }

            @Override
            public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                if (board == null) {
                    return;
                }

                if ((keyCode == KeyCodes.KEY_DOWN) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.s()))) {
                    Word w = board.moveDown();
                    render(w);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_UP) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.w()))) {
                    Word w = board.movieUp();
                    render(w);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_LEFT) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.a()))) {
                    Word w = board.moveLeft();
                    render(w);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_RIGHT) ||
                        (((modifiers & KeyboardListener.MODIFIER_CTRL) > 0) &&
                        (keyCode == CODES.d()))) {
                    Word w = board.moveRight();
                    render(w);

                    return;
                }

                //                if (keyCode == ' ') {
                //                    Position p = board.getHighlightLetter();
                //                    Word w = board.playLetter(' ');
                //                    board.setHighlightLetter(p);
                //                    render(w);
                //
                //                    return;
                //                }
                if ((keyCode == ' ') || (keyCode == KeyCodes.KEY_ENTER)) {
                    Word w = board.setHighlightLetter(board.getHighlightLetter());
                    render(w);

                    return;
                }

                if ((keyCode == KeyCodes.KEY_BACKSPACE) ||
                        (keyCode == KeyCodes.KEY_DELETE)) {
                    Word w = board.deleteLetter();
                    render(w);
                    dirty = true;

                    return;
                }

                if (((modifiers & KeyboardListener.MODIFIER_CTRL) == 0) &&
                        (ALPHA.indexOf(Character.toUpperCase(keyCode)) != -1)) {
                    Word w = board.playLetter(Character.toUpperCase(keyCode));
                    render(w);
                    dirty = true;

                    return;
                }
            }

            @Override
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {
            }
        };

    static ScrollPanel acrossScroll = new ScrollPanel();
    static ScrollPanel downScroll = new ScrollPanel();
    static PuzzleListView plv = Injector.INSTANCE.puzzleListView();
    private static Request request = null;
    private static HandlerRegistration closingRegistration = null;
    private static Widget lastClueWidget;

    public static void loadPuzzle(final Long id) {
        status.setText("Loading puzzle...");
        status.setStyleName(css.statusInfo());

        if (request == null) {
            request = PROXY.findPuzzle(id,
                    new AsyncCallback<Puzzle>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.toString());
                        }

                        @Override
                        public void onSuccess(Puzzle result) {
                            History.newItem("play=" + id, false);
                            startPuzzle(id, result);
                            keyboardIntercept.addKeyboardListener(l);
                            request = null;
                            status.setStyleName(css.statusHidden());
                            status.setText(" ");
                        }
                    });

            r.setClickListener(new ClickListener() {
                    @Override
                    public void onClick(int across, int down) {
                        Word w = board.setHighlightLetter(new Position(across,
                                    down));
                        render(w);
                        keyboardIntercept.setFocus(true);
                    }
                });
        } else {
            request.cancel();
            request = null;
            loadPuzzle(id);
        }
    }

    @Override
    public void onModuleLoad() {
        Element e = DOM.getElementById("loadingIndicator");

        if (e != null) {
            e.removeFromParent();
        }

        History.newItem("list", false);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    if (event.getValue().equals("list")) {
                        if (closingRegistration != null) {
                            closingRegistration.removeHandler();
                            closingRegistration = null;
                            
                        }
                        if (autoSaveTimer != null){
                            autoSaveTimer.cancel();
                            autoSaveTimer.run();
                            autoSaveTimer = null;
                        }
                        mainPanel.setWidget(plv);
                        keyboardIntercept.removeKeyboardListener(l);

                        displayChangeListener.onDisplayChange();
                    } else if(event.getValue().startsWith("play=")){
                        Long id = Long.parseLong(event.getValue().split("=")[1]);
                        loadPuzzle(id);
                    }
                }
            });
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setWidth("100%");
        StyleInjector.inject(Injector.INSTANCE.resources().css().getText());
        keyboardIntercept.setWidth("1px");
        keyboardIntercept.setHeight("1px");
        keyboardIntercept.setStyleName(css.keyboardIntercept());
        RootPanel.get().add(keyboardIntercept);
        RootPanel.get().add(verticalPanel);
        verticalPanel.add(status);
        verticalPanel.setCellHorizontalAlignment(status,
            HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.add(mainPanel);
        verticalPanel.setCellHorizontalAlignment(mainPanel,
            HasHorizontalAlignment.ALIGN_CENTER);

        status.setText("Loading puzzles...");
        status.setStyleName(css.statusInfo());
        PROXY.listPuzzles(new AsyncCallback<PuzzleDescriptor[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed to load puzzles. \n"+caught.toString());

                }

                @Override
                public void onSuccess(PuzzleDescriptor[] result) {
                    Arrays.sort(result);
                    mainPanel.setWidget(plv);
                    plv.setValue(Arrays.asList(result));
                    setFBSize();
                    status.setStyleName(css.statusHidden());
                    status.setText(" ");
                }
            });
    }

    private static native void setFBSize() /*-{
    if($wnd.FB && $wnd.FB.CanvasClient.setSizeToContent){
    $wnd.FB.CanvasClient.setSizeToContent();
    }
    }-*/;

    private static void render(Word w) {
        r.render(w);

        if (lastClueWidget != null) {
            lastClueWidget.removeStyleName(css.highlightClue());
        }

        if (board.isAcross() && (board.getClue() != null)) {
            lastClueWidget = acrossClueViews.get(board.getClue());
            acrossScroll.ensureVisible(lastClueWidget);
        } else if (board.getClue() != null) {
            lastClueWidget = downClueViews.get(board.getClue());
            downScroll.ensureVisible(lastClueWidget);
        }

        lastClueWidget.addStyleName(css.highlightClue());
        keyboardIntercept.setFocus(true);
    }

    private static void render() {
        render(null);
    }

    private static void startPuzzle(final long listingId, final Puzzle puzzle) {
        VerticalPanel outer = new VerticalPanel();
        board = new Playboard(puzzle);
        board.setHighlightLetter(new Position(0, 0));

        FlexTable t = r.initialize(board);

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(t);

        acrossScroll.setWidth("155px");
        downScroll.setWidth("155px");

        VerticalPanel list = new VerticalPanel();

        acrossScroll.setWidget(list);

        int index = 0;

        for (Clue c : acrossClues = board.getAcrossClues()) {
            final int cIndex = index;
            Grid g = new Grid(1, 2);
            g.setWidget(0, 0, new Label(c.number + ""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter()
             .setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0, 1, new Label(c.hint));
            g.setStyleName(css.clueBox());

            list.add(g);
            acrossClueViews.put(c, g);
            g.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        board.jumpTo(cIndex, true);
                        mainPanel.setFocus(true);
                        render();
                    }
                });
            index++;
        }

        acrossScroll.setWidget(list);

        list = new VerticalPanel();

        downScroll.setWidget(list);
        index = 0;

        for (Clue c : downClues = board.getDownClues()) {
            final int cIndex = index;
            Grid g = new Grid(1, 2);
            g.setWidget(0, 0, new Label(c.number + ""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter()
             .setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0, 1, new Label(c.hint));
            g.setStyleName(css.clueBox());

            list.add(g);
            downClueViews.put(c, g);
            g.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        board.jumpTo(cIndex, false);
                        mainPanel.setFocus(true);
                        render();
                    }
                });
            index++;
        }

        downScroll.setWidget(list);
        hp.add(acrossScroll);
        hp.add(downScroll);

        render(board.getCurrentWord());
        outer.add(hp);
        
        HorizontalPanel controls = new HorizontalPanel();
        Button back = new Button("Return to List", new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
               History.newItem("list");
            }

        });
        back.getElement().getStyle().setMarginRight(30, Unit.PX);

        controls.add(back);


        controls.add(new Button("Show Errors", new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                board.toggleShowErrors();
                ((Button)event.getSource()).setText( board.isShowErrors() ? "Hide Errors": "Show Errors");
                render();
            }

        }));
        controls.add( new Button("Reveal Letter", new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                board.revealLetter();
                dirty = true;
                render();
            }

        }));
        controls.add( new Button("Reveal Word", new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                board.revealWord();
                dirty = true;
                render();
            }

        }));
        controls.add( new Button("Reveal Puzzle", new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                board.revealPuzzle();
                dirty = true;
                render();
            }

        }));
        controls.getElement().getStyle().setMarginTop(10, Unit.PX);
        outer.add(controls);

        mainPanel.setWidget(outer);

        keyboardIntercept.setFocus(true);

        acrossScroll.setHeight(t.getOffsetHeight() + "px ");
        downScroll.setHeight(t.getOffsetHeight() + "px ");
        setFBSize();

        autoSaveTimer = new Timer() {
                    @Override
                    public void run() {
                        if (!dirty) {
                            return;
                        }

                        dirty = false;
                        status.setStyleName(css.statusInfo());
                        status.setText("Autosaving...");
                        PROXY.savePuzzle(listingId, puzzle,
                            new AsyncCallback() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    GWT.log("Save failed", caught);
                                    status.setStyleName(css.statusError());
                                    status.setText("Failed to save puzzle.");
                                }

                                @Override
                                public void onSuccess(Object result) {
                                    status.setStyleName(css.statusHidden());
                                    status.setText(" ");
                                }
                            });
                    }
                };
        autoSaveTimer.scheduleRepeating(2 * 60 * 1000);
        closingRegistration = Window.addWindowClosingHandler(new ClosingHandler() {
                    @Override
                    public void onWindowClosing(ClosingEvent event) {
                        if (dirty) {
                            event.setMessage("Abandon unsaved changes?");
                        }
                    }
                });
       displayChangeListener.onDisplayChange();
    }


    public static DisplayChangeListener displayChangeListener = new DisplayChangeListener(){

        @Override
        public void onDisplayChange() {
            ; //noop
        }

    };

    public static interface DisplayChangeListener {

        public void onDisplayChange();

    }
}
