/**
 * This file is part of Words With Crosses.
 * 
 * Copyright (this file) 2014 Wolfgang Groiss
 * 
 * This file is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 **/

package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.io.IOException;
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
    private static final Pattern P_HREF_PUZZLE = Pattern.compile(".*/Kreuzwortraetsel-Nr-([0-9]+)(\\?.*)?");

    final static boolean DEBUG_IMAGE_PARSING = false;

    public void parseIndex(InputSource inputSource, final DerStandardPuzzleCache puzzles) throws SAXException, ParserConfigurationException, IOException {
        ContentHandler handler = new DefaultHandler() {
            // search for <a href="/1319184020935/Kreuzwortraetsel-Nr-6941?_lexikaGroup=1">Kreuzwortr�tsel Nr. 6941</a>

            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if ("a".equals(localName)) {
                    String href = attributes.getValue("href");
                    if (href != null) {
                        Matcher m = P_HREF_PUZZLE.matcher(href);
                        if (m.matches()) {
                            String sId = m.group(1);
                            if (sId != null) {
                                int id = Integer.parseInt(sId);
                                // we already have this id. assuming inverse-chronological order, we can stop now.
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
            // if we didn't actively cancel, it's a genuine error => rethrow. otherwise swallow and stop parsing.
            if (CANCEL != se.getMessage()) {
                throw se;
            }
        }
    }

    private void parse(InputSource input, ContentHandler handler) throws SAXException, IOException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

        xmlReader.setContentHandler(handler);
        xmlReader.parse(input);
    }

    public void parsePuzzle(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, ParserConfigurationException, IOException {
        PuzzleParsingHandler pph = new PuzzleParsingHandler(pm);

        parse(input, pph);
    }

    public void parseDate(DerStandardPuzzleMetadata pm, DerStandardPuzzleCache puzzles, InputSource input) throws SAXException, ParserConfigurationException,
            IOException {
        DateParsingHandler dph = new DateParsingHandler(pm, puzzles);

        parse(input, dph);
    }

    public void parseSolution(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, IOException, JSONException {
        SolutionParser sp = new SolutionParser(pm);
        sp.parse(input);
    }

}
