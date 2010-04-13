/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleService;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;

/**
 *
 * @author kebernet
 */
public class Module extends AbstractGinModule {

    public static class ResourcesProvider implements Provider<Resources> {

        Resources instance = null;

        @Override
        public Resources get() {
           return instance == null ? instance = GWT.create(Resources.class) : instance;
        }

    }

    public static class PuzzleServiceProvider implements Provider<PuzzleServiceAsync> {


        public static PuzzleServiceAsync INSTANCE = null;
        @Override
        public PuzzleServiceAsync get() {
            return INSTANCE == null ? INSTANCE = GWT.create(PuzzleService.class) : INSTANCE;
        }

    }

    @Override
    protected void configure() {
        this.bind(Resources.class).toProvider(ResourcesProvider.class);
        this.bind(Renderer.class);
        this.bind(PuzzleServiceAsync.class).toProvider(PuzzleServiceProvider.class);
        this.bind(BoxView.class);
        this.bind(PuzzleDescriptorView.class);
        this.bind(PuzzleListView.class);
    }

}
