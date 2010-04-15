/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.web.client.resources.Resources;

/**
 *
 * @author kebernet
 */
@Singleton
public class Renderer {
    private Resources resources;
    private Provider<BoxView> boxViewProvider;
    private ClickListener listener;
    private Playboard board;
    private FlexTable table;
    @Inject
    public Renderer(Resources resources, Provider<BoxView> boxViewProvider){
        this.resources = resources;
        this.boxViewProvider = boxViewProvider;
    }


    public FlexTable initialize(Playboard playboard){
        this.board = playboard;
        table = new FlexTable();
        table.setCellSpacing(0);
        table.setCellPadding(0);

        for(int across=0; across < this.board.getBoxes().length; across++){
            for(int down=0; down < this.board.getBoxes()[across].length; down++){

                 Box b = board.getBoxes()[across][down];

                 if(b == null){
                    table.getCellFormatter().setStyleName(down, across, this.resources.css().black());
                    table.setWidget(down, across, new HTML("&nbsp;"));
                } else {
                     table.getCellFormatter().setStyleName(down, across, this.resources.css().square());
                     BoxView view = this.boxViewProvider.get();
                     view.setValue(b);
                     table.setWidget(down, across, view);
                }
            }


        }

        table.addTableListener(new TableListener(){

            @Override
            public void onCellClicked(SourcesTableEvents sender, int down, int across) {
                if(Renderer.this.listener != null){
                    Renderer.this.listener.onClick(across, down);
                }
            }

        });

        this.render();
        return table;


    }

    public void setClickListener(ClickListener l){
        this.listener = l;
    }
    
    public void render(){
        this.render(null);
    }


    public void render(Word w){
        Word currentWord = this.board.getCurrentWord();
        Position currentHighlightLetter = this.board.getHighlightLetter();
        for(int across = 0; across < this.board.getBoxes().length; across++){
            for(int down=0; down < this.board.getBoxes()[across].length; down++){
               CellFormatter formatter = this.table.getCellFormatter();
               Box box = this.board.getBoxes()[across][down];
               if(!currentWord.checkInWord(across, down) && w != null &&  !w.checkInWord(across, down)){
                    continue;
                }

                if(box == null){
                    continue;
                }

                BoxView view = (BoxView) table.getWidget(down, across);
                view.setValue(box);

                if(box.isCheated()){
                    formatter.addStyleName(down, across, resources.css().cheated());
                }

                if(currentWord.checkInWord(across, down)){
                    formatter.addStyleName(down, across, resources.css().currentHighlightWord());
                } else {
                    formatter.removeStyleName(down, across, resources.css().currentHighlightWord());
                }

                if(currentHighlightLetter.across == across && currentHighlightLetter.down == down){
                    formatter.addStyleName(down, across, resources.css().currentLetterHighlight());
                } else {
                    formatter.removeStyleName(down, across, resources.css().currentLetterHighlight());
                }
                if(board.isShowErrors() && box.getResponse() != ' ' && box.getResponse() != box.getSolution() ){
                    formatter.addStyleName(down, across, resources.css().error());
                } else {
                    formatter.removeStyleName(down, across, resources.css().error());
                }

            }
        }
    }


    public static  interface  ClickListener {
        public void onClick(int acoss, int down);
    }
}
