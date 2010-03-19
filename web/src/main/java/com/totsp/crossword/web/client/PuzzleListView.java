/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.inject.Inject;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.gwittir.client.ui.BoundVerticalPanel;
import com.totsp.gwittir.client.ui.util.BoundWidgetTypeFactory;

/**
 *
 * @author kebernet
 */
public class PuzzleListView extends BoundVerticalPanel<PuzzleDescriptor>{

    private static BoundWidgetTypeFactory FACTORY = new BoundWidgetTypeFactory(false);
    static {
        FACTORY.add(PuzzleDescriptor.class, PuzzleDescriptorView.PROVIDER);
    }


    @Inject
    public PuzzleListView(Resources resources){
        super(FACTORY, null);
    }


}
