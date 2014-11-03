package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.GregorianCalendar;

import org.xml.sax.InputSource;

import android.test.AndroidTestCase;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class DerStandardParserTest extends AndroidTestCase {

	private static final boolean NOT_START = false;
	private static final boolean START = true;
	private static final int NO_CLUE = 0;
	private static final char NO_SOLUTION = 0;

	public void testPuzzleParsesCorrectly() throws Exception {
		DerStandardParser dsp = new DerStandardParser();

		DerStandardPuzzleMetadata pm = new DerStandardPuzzleMetadata(7613, "http://example.com/7613", new GregorianCalendar());

		InputSource input = new InputSource(new InputStreamReader(getClass().getResourceAsStream("7613-puzzle"), Charset.forName("UTF-8")));
		dsp.parsePuzzle(pm, input);


		input = new InputSource(new InputStreamReader(getClass().getResourceAsStream("7613-solution"), Charset.forName("UTF-8")));
		dsp.parseSolution(pm, input);


		//puzzle-result
		//http://derstandard.at/RaetselApp/Home/GetCrosswordResult formdata:ExternalId=7613



		Puzzle p = pm.getPuzzle();

		assertEquals(13, p.getWidth());
		assertEquals(13, p.getHeight());


		for (int y = 0; y<p.getHeight();y++) {
			System.out.println();

			for (int x = 0; x<p.getWidth();x++) {
				Box b = p.getBoxes()[y][x];
				if (b == null) {
					System.out.print(" ");
				} else {
					System.out.print(b.getSolution());
				}
			}
		}


		assertBox(p,  0,  0, NOT_START, NOT_START, NO_CLUE, NO_SOLUTION);
		assertBox(p,  1,  0, NOT_START, START, 		 1,       'E');
		assertBox(p,  7,  8, NOT_START, START, 		 17,      'T');
		assertBox(p,  7, 11, START,	    NOT_START, 22,      'F');
		assertBox(p,  7, 12, NOT_START, NOT_START, NO_CLUE, 'E');
		assertBox(p,  8, 11, NOT_START, NOT_START, NO_CLUE, 'O');
		assertBox(p, 12, 12, NOT_START, NOT_START, NO_CLUE, NO_SOLUTION);

		assertAcrossClue(p,  0,  6, "Wenn ich rosinngem\u00e4\u00df die Trockenbeeren auslese, zittere ich zuletzt vor Zorn (Ez)");
		assertAcrossClue(p,  5, 15, "<i>EU-Navis</i>\u00a0sind f\u00fcr alle H\u00f6hen geeignet");

		assertDownClue(  p,  5,  9, "Wird der Teil von 7 waagrecht ausf\u00e4llig, ists v\u00f6llig verkehrt");


	}




	private void assertAcrossClue(Puzzle p, int index, Integer number, String text) {
		String[] clues = p.getAcrossClues();
		Integer[] lookup = p.getAcrossCluesLookup();

		assertClue(index, lookup, clues, number, text);
	}

	private void assertDownClue(Puzzle p, int index, Integer number, String text) {
		String[] clues = p.getDownClues();
		Integer[] lookup = p.getDownCluesLookup();

		assertClue(index, lookup, clues, number, text);
	}

	private void assertClue(int index, Integer[] lookup, String[] clues, Integer number, String text) {
		assertEquals(number, lookup[index]);
		assertEquals(text, clues[index]);
	}

	private void assertBox(Puzzle p, int x, int y, boolean editableAcross, boolean editableDown, int clueNumber, char solution) {
		//boxes are addressed as [row][colum] = [y][x], but the call to assertBox takes x/y.
		Box b = p.getBoxes()[y][x];

		String label = "Box ("+x+"/"+y+") ";

		if (b == null) {
			assertEquals(editableAcross, false);
			assertEquals(editableDown, false);
			assertEquals(clueNumber, 0);
			assertEquals(solution, 0);
		} else {
			assertEquals(label+"edit across", editableAcross, b.isAcross());
			assertEquals(label+"edit down",   editableDown,   b.isDown());
			assertEquals(label+"clue", 				clueNumber, 		b.getClueNumber());
			assertEquals(label+"solution",    solution,       b.getSolution());
		}
	}
}

