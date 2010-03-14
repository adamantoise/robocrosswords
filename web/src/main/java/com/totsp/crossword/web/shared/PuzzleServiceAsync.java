/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.shared;

import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.totsp.crossword.puz.Puzzle;

/**
 *
 * @author kebernet
 */
public interface PuzzleServiceAsync {


    public Request findPuzzle(Long puzzleId, AsyncCallback<Puzzle> callback);
}
