/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.gwittir.client.ui.AbstractBoundWidget;

/**
 *
 * @author kebernet
 */
public class BoxView extends AbstractBoundWidget<Box> {

    public static String PROP_VALUE = "value";

    private Label number = new Label();
    private Label letter = new Label();
    private Box value;

    @Inject
    public BoxView(Resources resources){
        AbsolutePanel main = new AbsolutePanel();
        main.setStyleName(resources.css().boxPanel());
        number.setStyleName(resources.css().number());
        letter.setStyleName(resources.css().letter());
        main.add(number, 0,0);
        main.add(letter, 0,5);
        super.initWidget(main);
    }

    @Override
    public Box getValue() {
        return this.value;
    }

    @Override
    public void setValue(Box value) {
       if(value.across || value.down ){
           this.number.setText(Integer.toString(value.clueNumber));
       }
       letter.setText(Character.toString(value.solution));
       this.changes.firePropertyChange(PROP_VALUE, this.value, this.value = value);
    }

}
