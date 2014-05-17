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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DateParsingHandler extends DefaultHandler {
    private static final Logger LOG = Logger.getLogger("DateParsingHandler");
    private static final DateFormat DF_DERSTANDARD = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

    private final DerStandardPuzzleMetadata pm;
    private final DerStandardPuzzleCache puzzles;

    private boolean running = true;
    private boolean receivingDateString = false;
    private StringBuilder sbDate;

    public DateParsingHandler(DerStandardPuzzleMetadata pm, DerStandardPuzzleCache puzzles) {
        this.pm = pm;
        this.puzzles = puzzles;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (running && "span".equals(localName)) {
            String clazz = attributes.getValue("class");
            if ("date".equals(clazz)) {
                receivingDateString = true;
                sbDate = new StringBuilder();
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (receivingDateString) {
            receivingDateString = false;
            running = false;

            String dateString = sbDate.toString();
            try {
                Date d = DF_DERSTANDARD.parse(dateString);
                Calendar c = new GregorianCalendar();
                c.setTime(d);

                puzzles.setDate(pm, c);
            } catch (ParseException e) {
                LOG.log(Level.SEVERE, "Unable to parse date \"" + dateString + "\".", e);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (receivingDateString) {
            sbDate.append(ch, start, length);
        }
    }
}