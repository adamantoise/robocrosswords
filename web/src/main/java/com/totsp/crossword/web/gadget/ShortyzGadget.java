/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.gadget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.IntrinsicFeature;
import com.google.gwt.gadgets.client.NeedsIntrinsics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.totsp.crossword.web.client.BasicEntryPoint;
import com.totsp.crossword.web.client.Module.PuzzleServiceProvider;
import com.totsp.crossword.web.shared.PuzzleService;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
@com.google.gwt.gadgets.client.Gadget.ModulePrefs(
            title = "Shortyz",
            author = "Robert Cooper",
            author_email = "kebernet@gmail.com"
)
public class ShortyzGadget extends Gadget<UserPreferences> implements NeedsIntrinsics{

    UserPreferences prefs;
    PuzzleServiceAsync service;
    @Override
    protected void init(UserPreferences preferences) {
        this.prefs = preferences;
        BasicEntryPoint entry = new BasicEntryPoint();
        BasicEntryPoint.SERVICE = service;
        entry.onModuleLoad();
    }

    @Override
    public void initializeFeature(IntrinsicFeature feature) {
        service = GWT.create(PuzzleService.class);
        ServiceDefTarget serviceDef = (ServiceDefTarget) service;
        String rpcUrl = serviceDef.getServiceEntryPoint();
        rpcUrl = feature.getCachedUrl(rpcUrl);
        Window.alert("Setting RPC URL: "+rpcUrl);
        serviceDef.setServiceEntryPoint(rpcUrl);
    }

}
