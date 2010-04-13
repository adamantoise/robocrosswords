/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.web.server.model.PuzzleListing;
import com.totsp.crossword.web.shared.NoUserException;
import com.totsp.crossword.web.shared.PuzzleDescriptor;
import com.totsp.crossword.web.shared.PuzzleService;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kebernet
 */
public class PuzzleServlet extends RemoteServiceServlet implements PuzzleService {

    @Override
    public Puzzle findPuzzle(Long puzzleId) {
       

        DataService service = new DataService();
        try{
            String userUri = (String) this.getThreadLocalRequest().getSession().getAttribute("user.id");
            if(userUri != null){
                System.out.println("User: "+userUri+" Puzzle:"+puzzleId);
                Puzzle p = service.loadSavedPuzzle(userUri, puzzleId);
                if(p != null){
                    System.out.println("Found saved.");
                    return p;
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }



        try {
            PuzzleListing l = service.findListingById(PuzzleListing.class, puzzleId);
           
            Puzzle p = (Puzzle) service.deserialize(l.getPuzzleSerial().getBytes());
            System.out.println("Returning "+p);
            return p;
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(PuzzleServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            service.close();
        }
    }

    @Override
    public PuzzleDescriptor[] listPuzzles() {
        PuzzleDescriptor[] result = new PuzzleDescriptor[0];
        DataService service = new DataService();
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -10);
            
            List<PuzzleListing> listings = service.findAfterDateNoPuzzle(cal.getTime());
            int i = 0;
            result = new PuzzleDescriptor[listings.size()];
            for(PuzzleListing listing : listings){
                PuzzleDescriptor desc = new PuzzleDescriptor();
                desc.setId(listing.getId());
                desc.setSource(listing.getSource());
                desc.setTitle(listing.getTitle());
                desc.setDate(listing.getDate());
                System.out.println("\t"+desc.getTitle()+" "+desc.getDate());
                result[i++]=desc;
            }
        } catch (Exception ex) {
            Logger.getLogger(PuzzleServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } finally {
            service.close();
        }
        System.out.println("Returing "+result.length);
        return result;
    }

    @Override
    public void savePuzzle(Long listingId, Puzzle puzzle) throws NoUserException {
        DataService data = new DataService();
        String userUri = (String) this.getThreadLocalRequest().getSession().getAttribute("user.id");
        if(userUri == null){
            throw new NoUserException();
        }
        data.savePuzzle(userUri, listingId, puzzle);
        System.out.println("Save done.");
    }



}
