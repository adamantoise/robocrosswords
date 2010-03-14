/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.shared.PuzzleService;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kebernet
 */
public class PuzzleServlet extends RemoteServiceServlet implements PuzzleService {

    @Override
    public Puzzle findPuzzle(Long puzzleId) {
        try {
            return IO.loadNative(new URL("http://herbach.dnsalias.com/Tausig/av100310.puz").openStream());
        } catch (IOException ex) {
            Logger.getLogger(PuzzleServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

    }

}
