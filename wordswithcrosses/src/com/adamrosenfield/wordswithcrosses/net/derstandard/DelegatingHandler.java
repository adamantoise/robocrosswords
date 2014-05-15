package com.adamrosenfield.wordswithcrosses.net.derstandard;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
/**
 * This file is part of Words With Crosses.
 * 
 * Copyright (this file) 2014 Wolfgang Groiss
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *
 **/
public class DelegatingHandler implements ContentHandler {
	private final ContentHandler[] handlers;
	
	public DelegatingHandler(ContentHandler... handlers) {
		this.handlers = handlers;
	}

	public void setDocumentLocator(Locator locator) {
		for (ContentHandler ch : handlers) {
			ch.setDocumentLocator(locator);
		}
	}

	public void startDocument() throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.startDocument();
		}
	}

	public void endDocument() throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.endDocument();
		}
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.startPrefixMapping(prefix, uri);
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.endPrefixMapping(prefix);
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.startElement(uri, localName, qName, atts);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.endElement(uri, localName, qName);
		}
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.characters(chars, start, length);
		}
	}

	public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.ignorableWhitespace(chars, start, length);
		}
	}

	public void processingInstruction(String target, String data) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.processingInstruction(target, data);
		}
	}

	public void skippedEntity(String name) throws SAXException {
		for (ContentHandler ch : handlers) {
			ch.skippedEntity(name);
		}
	}

}
