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

import android.util.SparseArray;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class PuzzleParsingHandler extends DefaultHandler {

    private final DerStandardPuzzleMetadata pm;

    private ContentHandler currentDelegate;

    private PuzzleHandler cwtableHandler = new PuzzleHandler();
    private HintsHandler questHandler = new HintsHandler();

    public PuzzleParsingHandler(DerStandardPuzzleMetadata pm) {
        this.pm = pm;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("table") && hasClass(attributes, "cwtable")) {
            currentDelegate = cwtableHandler;
        } else if (localName.equals("div") && hasClass(attributes, "quest")) {
            currentDelegate = questHandler;
        } else if (currentDelegate != null) {
            currentDelegate.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentDelegate != null) {
            currentDelegate.characters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentDelegate != null) {
            currentDelegate.endElement(uri, localName, qName);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (cwtableHandler.wasSane() &&
            questHandler.wasSane()) {
            Puzzle p = new Puzzle();

            p.setAuthor("derStandard.at");
            p.setCopyright("derStandard.at");
            p.setDate(pm.getDate());

            cwtableHandler.saveTo(p);
            questHandler.saveTo(p);

            pm.setPuzzle(p);
        }
    }

    private boolean hasClass(Attributes attributes, String clazz) {
        String aClass = attributes.getValue("class");
        if (aClass != null) {
            if (aClass.equals(clazz)) {
                return true;
            } else {
                String[] split = aClass.split("\\s");
                for (String s : split) {
                    if (s.equals(clazz)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private final class HintsHandler extends DefaultHandler {
        SparseArray<String> horizontal = new SparseArray<>();
        SparseArray<String> vertical = new SparseArray<>();
        SparseArray<String> current;
        boolean inQuestItem = false;
        boolean inQuestItemText = false;
        StringBuilder questItemText = new StringBuilder();
        StringBuilder questItemNumber = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (inQuestItem && inQuestItemText) {
                questItemText.append("<").append(localName).append(">");
            } else if (localName.equals("div")) {
                if (hasClass(attributes, "hquest")) {
                    current = horizontal;
                } else if (hasClass(attributes, "vquest")) {
                    current = vertical;
                } else if (hasClass(attributes, "questitem")) {
                    inQuestItem = true;
                }
            }

        }

        public boolean wasSane() {
            return horizontal.size() > 0 && vertical.size() > 0;
        }

        public void saveTo(Puzzle p) {
            String[] acrossClues = new String[horizontal.size()];
            Integer[] acrossCluesLookup = new Integer[horizontal.size()];
            copyOver(horizontal, acrossClues, acrossCluesLookup);

            String[] downClues = new String[vertical.size()];
            Integer[] downCluesLookup = new Integer[vertical.size()];
            copyOver(vertical, downClues, downCluesLookup);

            p.setAcrossClues(acrossClues);
            p.setAcrossCluesLookup(acrossCluesLookup);
            p.setDownClues(downClues);
            p.setDownCluesLookup(downCluesLookup);

            p.setNumberOfClues(horizontal.size() + vertical.size());

            p.setRawClues(makeRawClues(p.getBoxes(), horizontal, vertical));
        }

        private String[] makeRawClues(Box[][] boxes, SparseArray<String> horizontal, SparseArray<String> vertical) {
            String[] ret = new String[horizontal.size() + vertical.size()];
            int i = 0;

            for (int r = 0; r < boxes.length; r++) {
                for (int c = 0; c < boxes[r].length; c++) {
                    if (boxes[r][c] == null) {
                        continue;
                    }

                    Box box = boxes[r][c];
                    int clueNumber = box.getClueNumber();

                    if (clueNumber != 0) {
                        if (box.isAcross()) {
                            ret[i++] = horizontal.get(clueNumber);
                        }

                        if (box.isDown()) {
                            ret[i++] = vertical.get(clueNumber);
                        }
                    }
                }
            }

            return ret;
        }

        private void copyOver(SparseArray<String> map, String[] clues, Integer[] cluesLookup) {
            for (int i = 0; i < map.size(); i++) {
                int key = map.keyAt(i);
                String value = map.valueAt(i);
                clues[i] = value.trim();
                cluesLookup[i] = key;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("div")) {
                if (inQuestItem) {
                    int number = Integer.parseInt(questItemNumber.toString().trim());
                    String text = questItemText.toString();

                    current.put(number, text);

                    inQuestItem = false;
                    inQuestItemText = false;
                    questItemText.setLength(0);
                    questItemNumber.setLength(0);
                }
            } else if (inQuestItem && !inQuestItemText && "em".equals(localName)) {
                inQuestItemText = true;
            } else if (inQuestItem && inQuestItemText) {
                questItemText.append("</").append(localName).append(">");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inQuestItem) {
                if (inQuestItemText) {
                    questItemText.append(ch, start, length);
                } else {
                    questItemNumber.append(ch, start, length);
                }
            }
        }
    }

    private final class PuzzleHandler extends DefaultHandler {
        int width = 0;
        int height = 0;

        boolean[][] editable = new boolean[50][50];
        int[][] number = new int[50][50];

        int row = -1;
        int col = -1;

        boolean inWordNumber = false;
        StringBuilder wordNumber = new StringBuilder();

        public void saveTo(Puzzle p) {
            Box[] boxes = new Box[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    if (editable[x][y]) {
                        Box b = new Box();
                        boxes[y * width + x] = b;

                        int nr = number[x][y];
                        if (nr != 0) {
                            b.setClueNumber(nr);

                            boolean across = !isEditable(x - 1, y) && isEditable(x + 1, y);
                            boolean down = !isEditable(x, y - 1) && isEditable(x, y + 1);

                            b.setAcross(across);
                            b.setDown(down);
                        }
                    }
                }
            }

            p.setBoxesList(boxes);

            p.setWidth(width);
            p.setHeight(height);
        }

        public boolean wasSane() {
            return width > 0 && height > 0;
        }

        private boolean isEditable(int x, int y) {
            if (x < 0 || y < 0)
                return false;

            if (x >= width) {
                return false;
            }

            if (y >= height) {
                return false;
            }

            return editable[x][y];
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("tr".equals(localName) && hasClass(attributes, "cwrow")) {
                row += 1;
                col = -1;

                if (height <= row) {
                    height = row + 1;
                }

            } else if ("div".equals(localName) && hasClass(attributes, "cwwordcontainer")) {
                col += 1;

                if (width <= col) {
                    width = col + 1;
                }
            }

            if ("div".equals(localName) && hasClass(attributes, "cweditable")) {
                editable[col][row] = true;
            } else if ("div".equals(localName) && hasClass(attributes, "cwwordnumber")) {
                inWordNumber = true;
                wordNumber.setLength(0);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("div".equals(localName) && inWordNumber) {
                int nr = Integer.parseInt(wordNumber.toString().trim());
                number[col][row] = nr;
            }
            inWordNumber = false;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inWordNumber) {
                wordNumber.append(ch, start, length);
            }
        }

    }
}
