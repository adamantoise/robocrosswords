/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
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
 */

package com.adamrosenfield.wordswithcrosses.io;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.SparseArray;

import com.adamrosenfield.wordswithcrosses.io.charset.StandardCharsets;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * Converts a puzzle from the XML format used by uclick syndicated puzzles
 * to the Across Lite .puz format.  The format is:
 *
 * <crossword>
 *   <Title v="[Title]" />
 *   <Author v="[Author]" />
 *   <Width v="[Width]" />
 *   <Height v="[Height]" />
 *   <AllAnswer v="[Grid]" />
 *   <across>
 *      <a[i] a="[Answer]" c="[Clue]" n="[GridIndex]" cn="[ClueNumber]" />
 *   </across>
 *   <down>
 *      <d[j] ... />
 *   </down>
 * </crossword>
 *
 * [Grid] contains all of the letters in the solution, reading left-to-right,
 * top-to-bottom, with - for black squares. [i] is an incrementing number for
 * each across clue, starting at 1. [GridIndex] is the offset into [Grid] at
 * which the clue starts.  [Clue] text is HTML escaped.
 */
public class UclickXMLIO {

    private static class UclickXMLParser extends DefaultHandler {
        private Puzzle puz;
        private SparseArray<String> acrossNumToClueMap = new SparseArray<>();
        private SparseArray<String> downNumToClueMap = new SparseArray<>();
        private boolean inAcross = false;
        private boolean inDown = false;
        private int maxClueNum = -1;

        public UclickXMLParser(Puzzle puz) {
            this.puz = puz;
        }

        @Override
        public void startElement(String nsURI, String strippedName,
                String tagName, Attributes attributes) throws SAXException {
            strippedName = strippedName.trim();
            String name = strippedName.length() == 0 ? tagName.trim() : strippedName;
            if (inAcross) {
                int clueNum = Integer.parseInt(attributes.getValue("cn"));
                if (clueNum > maxClueNum) {
                    maxClueNum = clueNum;
                }

                acrossNumToClueMap.put(clueNum, urlDecode(attributes.getValue("c")));
            } else if (inDown) {
                int clueNum = Integer.parseInt(attributes.getValue("cn"));
                if (clueNum > maxClueNum) {
                    maxClueNum = clueNum;
                }

                downNumToClueMap.put(clueNum, urlDecode(attributes.getValue("c")));
            } else if (name.equalsIgnoreCase("title")) {
                puz.setTitle(urlDecode(attributes.getValue("v")));
            } else if (name.equalsIgnoreCase("author")) {
                puz.setAuthor(urlDecode(attributes.getValue("v")));
            } else if (name.equalsIgnoreCase("width")) {
                puz.setWidth(Integer.parseInt(attributes.getValue("v")));
            } else if (name.equalsIgnoreCase("height")) {
                puz.setHeight(Integer.parseInt(attributes.getValue("v")));
            } else if (name.equalsIgnoreCase("allanswer")) {
                String rawGrid = attributes.getValue("v");
                Box[] boxesList = new Box[puz.getHeight()*puz.getWidth()];
                for (int i = 0; i < rawGrid.length(); i++) {
                    char sol = rawGrid.charAt(i);
                    if (sol != '-') {
                        boxesList[i] = new Box();
                        boxesList[i].setSolution(sol);
                        boxesList[i].setResponse(' ');
                    }
                }
                puz.setBoxesList(boxesList);
                puz.setBoxes(puz.buildBoxes());
            } else if (name.equalsIgnoreCase("across")) {
                inAcross = true;
            } else if (name.equalsIgnoreCase("down")) {
                inDown = true;
            }
        }

        @Override
        public void endElement(String nsURI, String strippedName,
                String tagName) throws SAXException {
            strippedName = strippedName.trim();
            String name = strippedName.length() == 0 ? tagName.trim() : strippedName;

            if (name.equalsIgnoreCase("across")) {
                inAcross = false;
            } else if (name.equalsIgnoreCase("down")) {
                inDown = false;
            } else if (name.equalsIgnoreCase("crossword")) {
                int numberOfClues = acrossNumToClueMap.size() + downNumToClueMap.size();
                puz.setNumberOfClues(numberOfClues);
                String[] rawClues = new String[numberOfClues];
                int i = 0;
                for(int clueNum = 1; clueNum <= maxClueNum; clueNum++) {
                    String clue = acrossNumToClueMap.get(clueNum);
                    if (clue != null) {
                        rawClues[i] = clue;
                        i++;
                    }

                    clue = downNumToClueMap.get(clueNum);
                    if (clue != null) {
                        rawClues[i] = clue;
                        i++;
                    }
                }
                puz.setRawClues(rawClues);
            }
        }
    }

    public static boolean convertUclickPuzzle(InputStream is, DataOutputStream os,
            String copyright, Calendar date) {
        Puzzle puz = new Puzzle();
        puz.setDate(date);
        puz.setCopyright(copyright);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            //parser.setProperty("http://xml.org/sax/features/validation", false);
            XMLReader xr = parser.getXMLReader();
            xr.setContentHandler(new UclickXMLParser(puz));
            xr.parse(new InputSource(is));

            puz.setVersion(IO.VERSION_STRING);
            puz.setNotes("");

            IO.save(puz, os);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch(UnsupportedEncodingException e) {
            // Should never happen
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
