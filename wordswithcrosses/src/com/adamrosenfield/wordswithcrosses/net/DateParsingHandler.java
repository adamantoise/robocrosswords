package com.adamrosenfield.wordswithcrosses.net;

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
				LOG.log(Level.SEVERE, "Unable to parse date \""+dateString+"\".", e);
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