/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
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
    static final PuzzleServiceAsync s = Injector.INSTANCE.service();
    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Css css = Injector.INSTANCE.resources().css();
    static FocusPanel mainPanel = new FocusPanel();
    static Playboard board;
    static Renderer r = Injector.INSTANCE.renderer();
    static Clue[] acrossClues;
    static Clue[] downClues;

    static HashMap<Clue, Widget> acrossClueViews = new HashMap<Clue, Widget>();
    static HashMap<Clue, Widget> downClueViews = new HashMap<Clue, Widget>();

    static KeyboardListener l = new KeyboardListener() {
            @Override
            public void onKeyDown(Widget sender, char keyCode, int modifiers) {
            }

            @Override
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                if (board == null) {
                    return;
                }

                
                if (keyCode == KeyCodes.KEY_DOWN || ((modifiers & KeyboardListener.MODIFIER_CTRL) > 0 &&  keyCode == CODES.s())) {
                    Word w = board.moveDown();
                    render(w);

                    return;
                }

                if (keyCode == KeyCodes.KEY_UP || ((modifiers & KeyboardListener.MODIFIER_CTRL) > 0 && keyCode == CODES.w())) {
                    Word w = board.movieUp();
                    render(w);

                    return;
                }

                if (keyCode == KeyCodes.KEY_LEFT || ((modifiers & KeyboardListener.MODIFIER_CTRL) > 0 &&  keyCode == CODES.a() )) {
                    Word w = board.moveLeft();
                    render(w);

                    return;
                }

                if (keyCode == KeyCodes.KEY_RIGHT || ((modifiers & KeyboardListener.MODIFIER_CTRL) > 0 && keyCode == CODES.d())) {
                    Word w = board.moveRight();
                    render(w);

                    return;
                }

                if (keyCode == ' ') {
                    Position p = board.getHighlightLetter();
                    Word w = board.playLetter(' ');
                    board.setHighlightLetter(p);
                    render(w);

                    return;
                }

                if( keyCode == KeyCodes.KEY_ENTER){

                    Word w = board.setHighlightLetter(board.getHighlightLetter());
                    render(w);

                    return;

                }

                if ((keyCode == KeyCodes.KEY_BACKSPACE) ||
                        (keyCode == KeyCodes.KEY_DELETE)) {
                    Word w = board.deleteLetter();
                    render(w);
                    
                    return;
                }

                
                if ((modifiers & KeyboardListener.MODIFIER_CTRL) == 0 && ALPHA.indexOf(Character.toUpperCase(keyCode)) != -1) {
                    Word w = board.playLetter(Character.toUpperCase(keyCode));
                    render(w);

                    return;
                }
            }

            @Override
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {
            }
        };

    static ScrollPanel acrossScroll = new ScrollPanel();
    static ScrollPanel downScroll = new ScrollPanel();

    @Override
    public void onModuleLoad() {
        StyleInjector.inject(Injector.INSTANCE.resources().css().getText());
        RootPanel.get().add(mainPanel);
        s.listPuzzles(new AsyncCallback<PuzzleDescriptor[]>(){

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Fail");
            }

            @Override
            public void onSuccess(PuzzleDescriptor[] result) {
                PuzzleListView plv = Injector.INSTANCE.puzzleListView();
                Arrays.sort(result);
                mainPanel.setWidget(plv);
                plv.setValue(Arrays.asList(result));
                setFBSize();
            }

        });
       
    }


    public static void loadPuzzle(Long id){
        
        s.findPuzzle(id,
            new AsyncCallback<Puzzle>() {
                @Override
                public void onFailure(Throwable caught) {
                    Window.alert(caught.toString());
                }

                @Override
                public void onSuccess(Puzzle result) {
                    startPuzzle(result);
                }
            });

        mainPanel.addKeyboardListener(l);

        

        r.setClickListener(new ClickListener() {
                @Override
                public void onClick(int across, int down) {
                    Word w = board.setHighlightLetter(new Position(across, down));
                    render(w);
                    mainPanel.setFocus(true);
                }
            });
    }

    private static void startPuzzle(Puzzle puzzle) {
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

        for (Clue c :acrossClues = board.getAcrossClues()) {
            final int cIndex = index;
            Grid g = new Grid(1,2);
            g.setWidget(0,0, new Label(c.number+""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0,1, new Label(c.hint));
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

        for (Clue c :downClues = board.getDownClues()) {
            final int cIndex = index;
            Grid g = new Grid(1,2);
            g.setWidget(0,0, new Label(c.number+""));
            g.getCellFormatter().setStyleName(0, 0, css.clueNumber());
            g.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
            g.setWidget(0,1, new Label(c.hint));
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
        mainPanel.setWidget(hp);
        mainPanel.setFocus(true);

       acrossScroll.setHeight(t.getOffsetHeight() + "px ");
       downScroll.setHeight(t.getOffsetHeight() + "px ");
       setFBSize();
    }

    private static Widget lastClueWidget;

    private static void render(Word w){
        r.render(w);
        if(lastClueWidget != null){
            lastClueWidget.removeStyleName(css.highlightClue());
        }
        if(board.isAcross()){
            lastClueWidget =acrossClueViews.get(board.getClue());
           acrossScroll.ensureVisible( lastClueWidget );
        } else {
            lastClueWidget =downClueViews.get(board.getClue());
           downScroll.ensureVisible( lastClueWidget);
        }
        lastClueWidget.addStyleName(css.highlightClue());
        

    }

   
    private static  void render(){
        render(null);
    }

    private static native void setFBSize()/*-{
       if($wnd.FB.CanvasClient.setSizeToContent){
            $wnd.FB.CanvasClient.setSizeToContent();
       }
    }-*/;
}
