/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.puz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import com.totsp.crossword.io.IO;

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
    
    public void testSave() throws Exception {
    	Puzzle puz = IO.loadNative(IOTest.class.getResourceAsStream("/test.puz"));
        System.out.println("Loaded.");
        File tmp = File.createTempFile("test", ".puz");
        tmp.deleteOnExit();
        IO.saveNative(puz, new FileOutputStream(tmp));
        
        Puzzle puz2 = IO.loadNative(new FileInputStream(tmp));
//        System.out.println(puz.acrossClues[puz2.acrossClues.length -1 ]+" \n"+puz2.acrossClues[puz2.acrossClues.length -1 ]);
//        System.out.println(puz.acrossClues.length +" == "+puz2.acrossClues.length);
//        System.out.println(Arrays.equals(puz.acrossClues, puz2.acrossClues));
        Box[][] b1 = puz.getBoxes();
        Box[][] b2 = puz2.getBoxes();
        
        for(int x=0; x < b1.length; x++ ){
        	for(int y=0; y<b1[x].length; y++){
        		System.out.println(b1[x][y] +" == "+ b2[x][y] );
        	}
        }
        
        assertEquals( puz, puz2);
        
        Puzzle p = IO.load(tmp);
        p.setDate(new Date());
        p.setSource("Unit Test");
        
        IO.save(p, tmp);
        
        PuzzleMeta m = IO.meta(tmp);
        
        System.out.println(m.title +"\n"+m.source+"\n"+m.percentComplete);
        
        
    }

    
}
