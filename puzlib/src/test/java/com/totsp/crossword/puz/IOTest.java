/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.puz;

import junit.framework.TestCase;

/**
 *
 * @author kebernet
 */
public class IOTest extends TestCase {
    
    public IOTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of load method, of class IO.
     */
    public void testLoad() throws Exception {
        Puzzle puz = IO.loadNative(IOTest.class.getResourceAsStream("/test.puz"));
        System.out.println("Loaded.");
        Box[][] boxes = puz.getBoxes();
        for(int x=0; x<boxes.length; x++){
            for(int y=0; y<boxes[x].length; y++){
                System.out.print( boxes[x][y]  == null ? "null " : boxes[x][y]);
            }
            System.out.println();
        }
        System.out.println("One across: "+ puz.findAcrossClue(1));
        System.out.println("14  across: "+ puz.findAcrossClue(14));
        System.out.println("18  down  : "+ puz.findDownClue(18));
        System.out.println("2 down: "+puz.findDownClue(2));
    }

    
}
