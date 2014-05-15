package com.adamrosenfield.wordswithcrosses.net.derstandard;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

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
