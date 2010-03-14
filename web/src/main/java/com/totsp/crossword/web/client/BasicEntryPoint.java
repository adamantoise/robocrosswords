/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public class BasicEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {

        StyleInjector.inject(Injector.INSTANCE.resources().css().getText());



        final Renderer r = Injector.INSTANCE.renderer();

        PuzzleServiceAsync s = Injector.INSTANCE.service();
        s.findPuzzle(1L, new AsyncCallback<Puzzle>(){

            @Override
            public void onFailure(Throwable caught) {
                Window.alert(caught.toString());
            }

            @Override
            public void onSuccess(Puzzle result) {
               Playboard board = new Playboard(result);
               RootPanel.get().add( r.initialize(board));
            }

        });
    }


}
