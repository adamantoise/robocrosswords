package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class DerStandardParser {
	private static final String CANCEL = "Cancel";
	private static final Pattern P_CHARSET_IN_TYPE = Pattern.compile("[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)");
	private static final String P_HINT= "([0-9]+)([^_]+)___";
	private static final Pattern P_HREF_PUZZLE = Pattern.compile(".*/Kreuzwortraetsel-Nr-([0-9]+)(\\?.*)?");
	
	private static final Logger LOG = Logger.getLogger("DerStandardParser");

	final static boolean DEBUG_IMAGE_PARSING = false;
	
	
	public void parseIndex(InputSource inputSource, final DerStandardPuzzleCache puzzles) throws SAXException, ParserConfigurationException, IOException {
		ContentHandler handler = new DefaultHandler() {
		  //<a href="/1319184020935/Kreuzwortraetsel-Nr-6941?_lexikaGroup=1">Kreuzworträtsel Nr. 6941</a>

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("a".equals(localName)) {
				String href = attributes.getValue("href");
				if (href != null) {
					Matcher m = P_HREF_PUZZLE.matcher(href);
					if (m.matches()) {
						String id = m.group(1);
						if (id != null) {
							//we already have this id. assuming inverse-chronological order, we can stop now.
							if (puzzles.contains(id)) {
								throw new SAXException(CANCEL);
							}
							
							DerStandardPuzzleMetadata pm = puzzles.createOrGet(id);
							pm.setDateUrl(href);
						}
					}
				}
			}
		}
	  };
	  
	  
	  try {
	  	parse(inputSource, handler);
	  } catch (SAXException se) {
	  	//if we didn't actively cancel, it's a genuine error => rethrow. otherwise swallow and stop parsing.
	  	if (CANCEL != se.getMessage()) {
	  		throw se;
	  	}
	  }
	}
	
	private void parse(InputSource input, ContentHandler handler) throws SAXException, IOException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
		
	  xmlReader.setContentHandler(handler);
	  xmlReader.parse(input);
	}
	
	public void parsePuzzle(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, ParserConfigurationException, IOException {
		PuzzleParsingHandler pph = new PuzzleParsingHandler(pm);
		
		parse(input, pph);
	}
	
	public void parseDate(DerStandardPuzzleMetadata pm, DerStandardPuzzleCache puzzles, InputSource input) throws SAXException, ParserConfigurationException, IOException {
		DateParsingHandler dph = new DateParsingHandler(pm, puzzles);
		
		parse(input, dph);
	}
	
	
	public void parseSolution(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, IOException, JSONException {
		SolutionParser sp = new SolutionParser(pm);
		sp.parse(input);
	}
	
	

}
