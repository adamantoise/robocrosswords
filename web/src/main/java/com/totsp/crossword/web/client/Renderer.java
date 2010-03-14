/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.web.client.resources.Resources;

/**
 *
 * @author kebernet
 */
@Singleton
public class Renderer {
    private Resources resources;
    private Provider<BoxView> boxViewProvider;
    @Inject
    public Renderer(Resources resources, Provider<BoxView> boxViewProvider){
        this.resources = resources;
        this.boxViewProvider = boxViewProvider;
    }


    public FlexTable initialize( Playboard playboard){

        FlexTable table = new FlexTable();
        table.setCellSpacing(0);
        table.setCellPadding(0);
        for(Box[] row : playboard.getBoxes() ){
             int rowIndex =  table.getRowCount();
             table.insertRow( table.getRowCount());
             int cellIndex = 0;
             for(Box b : row){
                
                table.addCell(rowIndex);
                if(b == null){
                    table.getCellFormatter().setStyleName(rowIndex, cellIndex, this.resources.css().black());
                    table.setWidget(rowIndex, cellIndex, new HTML("&nbsp;"));
                } else {
                     table.getCellFormatter().setStyleName(rowIndex, cellIndex, this.resources.css().square());
                     BoxView view = this.boxViewProvider.get();
                     view.setValue(b);
                     table.setWidget(rowIndex, cellIndex, view);
                }
                cellIndex++;
             }
             rowIndex++;
             cellIndex = 0;
        }



        return table;


    }

}
