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

import org.json.JSONException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DerStandardParser {
    private void parse(InputSource input, ContentHandler handler) throws SAXException, IOException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

        xmlReader.setContentHandler(handler);
        xmlReader.parse(input);
    }

    public void parsePuzzle(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, IOException {
        PuzzleParsingHandler pph = new PuzzleParsingHandler(pm);

        parse(input, pph);
    }

    public void parseSolution(DerStandardPuzzleMetadata pm, InputSource input) throws SAXException, IOException, JSONException {
        SolutionParser sp = new SolutionParser(pm);
        sp.parse(input);
    }

}
