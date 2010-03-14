/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 *
 * @author kebernet
 */
public class BasicEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        RootPanel.get().add(new Label("Hello, World!"));
    }


}
