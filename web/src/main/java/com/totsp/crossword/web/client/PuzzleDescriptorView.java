/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.gwittir.client.ui.AbstractBoundWidget;
import com.totsp.gwittir.client.ui.util.BoundWidgetProvider;

/**
 *
 * @author kebernet
 */
public class PuzzleDescriptorView extends AbstractBoundWidget<PuzzleDescriptor>{



    public static final BoundWidgetProvider<PuzzleDescriptorView> PROVIDER = new BoundWidgetProvider<PuzzleDescriptorView>(){

        @Override
        public PuzzleDescriptorView get() {
            return Injector.INSTANCE.puzzleDescriptorView();
        }

    };

    DateTimeFormat format = DateTimeFormat.getFormat("EEEE '<br \\>' MMM dd, yyyy");
    HTML date = new HTML();
    Label source = new Label();
    Label title = new Label();


    @Inject
    public PuzzleDescriptorView(Resources resources){
        FlexTable table = new FlexTable();
        table.insertRow(0);
        table.insertRow(0);

        table.setWidget(0,1, date);
        table.getFlexCellFormatter().setRowSpan(0, 1, 2);
        table.getFlexCellFormatter().setWidth(0, 1, "25%");

        table.setWidget(0,0, source);
        table.setWidget(1,0, title);
        
        date.setStyleName(resources.css().pdDate());
        title.setStyleName(resources.css().pdTitle());
        source.setStyleName(resources.css().pdSource());
        super.initWidget(table);
        this.setStyleName(resources.css().pd());


        table.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                BasicEntryPoint.loadPuzzle(getValue().getId());
            }

        });

    }


    private PuzzleDescriptor value;

    /**
     * Get the value of value
     *
     * @return the value of value
     */
    @Override
    public PuzzleDescriptor getValue() {
        return this.value;
    }

    /**
     * Set the value of value
     *
     * @param newvalue new value of value
     */
    @Override
    public void setValue(PuzzleDescriptor newvalue) {
        this.value = newvalue;
        if(value == null){
            return;
        }
        this.date.setHTML( value.getDate() != null ? format.format(value.getDate()) : "" );
        this.source.setText( value.getSource() );
        this.title.setText(value.getTitle());

    }

    @Override
    public void setModel(Object model){
        this.setValue((PuzzleDescriptor) model);
        super.setModel(model);
    }


}
