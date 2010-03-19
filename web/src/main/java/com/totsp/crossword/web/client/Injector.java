/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.Ginjector;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public interface Injector extends Ginjector {

    public static final Injector INSTANCE = GWT.create(Injector.class);


    Resources resources();
    Renderer renderer();
    PuzzleServiceAsync service();
    BoxView boxView();
    PuzzleDescriptorView puzzleDescriptorView();
    PuzzleListView puzzleListView();
}
