/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
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
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public class BasicEntryPoint implements EntryPoint {

    static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    Playboard board;
    FocusPanel mainPanel = new FocusPanel();
    Renderer r = Injector.INSTANCE.renderer();
    ScrollPanel acrossScroll = new ScrollPanel();
    ScrollPanel downScroll = new ScrollPanel();
    Css css = Injector.INSTANCE.resources().css();


    KeyboardListener l = new KeyboardListener(){

            @Override
            public void onKeyDown(Widget sender, char keyCode, int modifiers) {

            }

            @Override
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
               if(board == null){
                   return;
               }
               
               if(keyCode == KeyCodes.KEY_DOWN){
                   Window.alert("Down");
                   Word w = board.moveDown();
                   r.render(w);
                   return;
               }
               if(keyCode == KeyCodes.KEY_UP){
                   Word w = board.movieUp();
                   r.render(w);
                   return;
               }
               if(keyCode == KeyCodes.KEY_LEFT){
                   Word w = board.moveLeft();
                   r.render(w);
                   return;
               }
               if(keyCode == KeyCodes.KEY_RIGHT ){
                   Word w = board.moveRight();
                   r.render(w);
                   return;
               }

               if(ALPHA.indexOf(Character.toUpperCase(keyCode)) != -1){
                   Word w = board.playLetter(Character.toUpperCase(keyCode));
                   r.render(w);
                   return;
               }

            }

            @Override
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {

            }

        };


    @Override
    public void onModuleLoad() {

        StyleInjector.inject(Injector.INSTANCE.resources().css().getText());
        
        PuzzleServiceAsync s = Injector.INSTANCE.service();
        s.findPuzzle(1L, new AsyncCallback<Puzzle>(){

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.toString());
            }

            @Override
            public void onSuccess(Puzzle result) {
                startPuzzle(result);
            }

        });

        mainPanel.getElement().getStyle().setBorderColor("red");
        mainPanel.getElement().getStyle().setBorderWidth(2, Unit.PX);
        mainPanel.addKeyboardListener(l);

        RootPanel.get().add(mainPanel);

        r.setClickListener(new ClickListener(){

            @Override
            public void onClick(int across, int down) {
                Word w = board.setHighlightLetter(new Position(across, down));
                r.render(w);
                
            }

        });

    }


    private void startPuzzle(Puzzle puzzle){
        board = new Playboard(puzzle);
        board.setHighlightLetter(new Position(0,0));
        FlexTable t = r.initialize(board);

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(t);
        
        this.acrossScroll.setWidth("200px");
        this.downScroll.setWidth("200px");

        VerticalPanel list = new VerticalPanel();

        this.acrossScroll.setWidget(list);
        int index = 0;
        for(Clue c : board.getAcrossClues() ){
            Label l = new Label( c.number+" "+c.hint );
            l.setStyleName(css.clueBox());
            list.add(l);
            final int cIndex = index;
            l.addClickHandler(new ClickHandler(){

                @Override
                public void onClick(ClickEvent event) {
                    board.jumpTo(cIndex, true);
                    r.render();
                }

            });
            index++;

        }
        acrossScroll.setWidget(list);
       


        list = new VerticalPanel();

        this.downScroll.setWidget(list);
        index = 0;
        for(Clue c : board.getDownClues() ){
            final int cIndex = index;
            Label l = new Label( c.number+" "+c.hint );
            l.setStyleName(css.clueBox());
            list.add(l);
            l.addClickHandler(new ClickHandler(){

                @Override
                public void onClick(ClickEvent event) {
                    board.jumpTo(cIndex, false);
                    r.render();
                }

            });
            index++;

        }
        downScroll.setWidget(list);
        hp.add(downScroll);
        hp.add(acrossScroll);

        r.render(board.getCurrentWord());
        mainPanel.setWidget(hp);
        mainPanel.setFocus(true);
        
        this.acrossScroll.setHeight(t.getOffsetHeight()+"px ");
        this.downScroll.setHeight(t.getOffsetHeight()+"px ");
    }


}
