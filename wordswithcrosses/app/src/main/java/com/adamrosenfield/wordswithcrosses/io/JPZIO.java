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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.SparseArray;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * Converts a puzzle from the XML format used by JPZ puzzles into the Across
 * Lite .puz format. Strings are HTML formatted, UTF-8. Any unsupported features
 * are either ignored or cause abort. The format is:
 *
 * <crossword-compiler-applet> ... <rectangular-puzzle
 * xmlns="http://crossword.info/xml/rectangular-puzzle"
 * alphabet="ABCDEFGHIJKLMNOPQRSTUVWXYZ"> <metadata> <title>[Title]</title>
 * <creator>[Author]</creator> <copyright>[Copyright]</copyright>
 * <description>[Notes]</description> </metadata> <crossword> <grid
 * width="[Width]" height="[Height]"> <grid-look numbering-scheme="normal" ...
 * /> <cell x="1" y="1" solution="M" number="1"></cell> ... <cell x="1" y="6"
 * type="block"</cell> ... </grid> ... <clues ordering="normal">
 * <title>...Across...</title> <clue ... number="1">...</clue> ... </clues>
 * <clues ordering="normal"> <title>...Down...</title> <clue ...
 * number="1">...</clue> ... </clues> </crossword> </rectangular-puzzle>
 * </crossword-compiler-applet>
 */
public class JPZIO {

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    /**
     * Interface for setting additional puzzle metadata during JPZ-to-PUZ
     * conversion
     */
    public static interface PuzzleMetadataSetter {
        public void setMetadata(Puzzle puzzle);
    }

    /**
     * Default puzzle metadata setter -- does nothing
     */
    public static final PuzzleMetadataSetter NOOP_METADATA_SETTER = new PuzzleMetadataSetter() {
        public void setMetadata(Puzzle puzzle) {
            // No-op
        }
    };

    private static InputStream unzipOrPassthrough(InputStream is)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IO.copyStream(is, baos);
        try {
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                is = new ByteArrayInputStream(baos.toByteArray());
            } else {
                while (entry != null && entry.isDirectory()) {
                    entry = zis.getNextEntry();
                }
                baos = new ByteArrayOutputStream();
                IO.copyStream(zis, baos);
                is = new ByteArrayInputStream(baos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            is = new ByteArrayInputStream(baos.toByteArray());
        }

        // replace &nbsp; with space

        Scanner in = new Scanner(is);
        ByteArrayOutputStream replaced = new ByteArrayOutputStream();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(replaced), 8192);
        while (in.hasNextLine()) {
            String line = in.nextLine();
            line = line.replaceAll("&nbsp;", " ");
            out.write(line + "\n");
        }
        out.close();
        return new ByteArrayInputStream(replaced.toByteArray());
    }

    public static Puzzle readPuzzle(InputStream is) {
        Puzzle puz = new Puzzle();
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser parser = factory.newSAXParser();

//           parser.setProperty("http://xml.org/sax/features/validation",
//           false);
            XMLReader xr = parser.getXMLReader();
            xr.setContentHandler(new JPZXMLParser(puz));
            xr.parse(new InputSource(unzipOrPassthrough(is)));

            puz.setVersion(IO.VERSION_STRING);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("Unable to parse XML file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return puz;
    }

    public static boolean convertJPZPuzzle(InputStream is, DataOutputStream os) {
        return convertJPZPuzzle(is, os, NOOP_METADATA_SETTER);
    }

    public static boolean convertJPZPuzzle(InputStream is, DataOutputStream os,
            PuzzleMetadataSetter metadataSetter) {

        try {
            Puzzle puz = readPuzzle(is);
            puz.setVersion(IO.VERSION_STRING);
            metadataSetter.setMetadata(puz);

            IO.save(puz, os);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.warning("Unable to parse XML file: " + e.getMessage());

            return false;
        }
    }

    public static void convertJPZPuzzle(File jpzFile, File destFile)
            throws IOException {
        convertJPZPuzzle(jpzFile, destFile, NOOP_METADATA_SETTER);
    }

    public static void convertJPZPuzzle(File jpzFile, File destFile,
            PuzzleMetadataSetter metadataSetter) throws IOException {
        FileInputStream fis = new FileInputStream(jpzFile);
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(destFile));
            try {
                if (!convertJPZPuzzle(fis, dos, metadataSetter)) {
                    destFile.delete();
                    throw new IOException("Failed to convert JPZ file: " + jpzFile);
                }
            } finally {
                dos.close();
            }
        } finally {
            fis.close();
        }
    }

    private static class JPZXMLParser extends DefaultHandler {
        private SparseArray<String> acrossNumToClueMap = new SparseArray<>();
        private SparseArray<String> downNumToClueMap = new SparseArray<>();
        private Puzzle puz;
        private StringBuilder curBuffer;
        private Box[][] boxes;
        private int[][] clueNums;
        private boolean inAcross = false;
        //private boolean inAuthor = false;
        //private boolean inClue = false;
        //private boolean inClueTitle = false;
        private boolean inClues = false;
        //private boolean inCopyright = false;
        //private boolean inDescription = false;
        private boolean inDown = false;
        private boolean inMetadata = false;
        //private boolean inTitle = false;
        private int clueNumber = 0;
        private int height;
        private int maxClueNum = -1;
        private int width;

        public JPZXMLParser(Puzzle puz) {
            this.puz = puz;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (curBuffer != null) {
                curBuffer.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String nsURI, String strippedName, String tagName)
                throws SAXException {
            strippedName = strippedName.trim();

            String name = (strippedName.length() == 0) ? tagName.trim() : strippedName;

            if (name.equalsIgnoreCase("metadata")) {
                inMetadata = false;
            } else if (inMetadata) {
                if (name.equalsIgnoreCase("title")) {
                    puz.setTitle(curBuffer.toString());
                    //inTitle = false;
                    curBuffer = null;
                } else if (name.equalsIgnoreCase("creator")) {
                    puz.setAuthor(curBuffer.toString());
                    //inAuthor = false;
                    curBuffer = null;
                } else if (name.equalsIgnoreCase("copyright")) {
                    puz.setCopyright(curBuffer.toString());
                    //inCopyright = false;
                    curBuffer = null;
                } else if (name.equalsIgnoreCase("description")) {
                    puz.setNotes(curBuffer.toString());
                    //inDescription = false;
                    curBuffer = null;
                }
            } else if (name.equalsIgnoreCase("grid")) {
                puz.setBoxes(boxes);
            } else if (name.equalsIgnoreCase("clues")) {
                inClues = false;
                inAcross = false;
                inDown = false;
            } else if (inClues) {
                if (name.equalsIgnoreCase("title")) {
                    String title = curBuffer.toString().toLowerCase(Locale.US);

                    if (title.contains("across")) {
                        inAcross = true;
                    } else if (title.contains("down")) {
                        inDown = true;
                    } else {
                        throw new SAXException("Clue list is neither across nor down.");
                    }

                    //inClueTitle = false;
                    curBuffer = null;
                } else if (name.equalsIgnoreCase("clue")) {
                    if (inAcross) {
                        acrossNumToClueMap.put(clueNumber, curBuffer.toString());
                    } else if (inDown) {
                        downNumToClueMap.put(clueNumber, curBuffer.toString());
                    } else {
                        throw new SAXException("Unexpected end of clue tag.");
                    }
                }
            } else if (name.equalsIgnoreCase("crossword")) {
                int numberOfClues = acrossNumToClueMap.size() + downNumToClueMap.size();
                puz.setNumberOfClues(numberOfClues);

                String[] rawClues = new String[numberOfClues];
                int i = 0;

                for (int clueNum = 1; clueNum <= maxClueNum; clueNum++) {
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

                // verify clue numbers
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (clueNums[y][x] != 0) {
                            if (puz.getBoxes()[y][x].getClueNumber() != clueNums[y][x]) {
                                throw new SAXException("Irregular numbering scheme.");
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void startElement(String nsURI, String strippedName,
                String tagName, Attributes attributes) throws SAXException {
            strippedName = strippedName.trim();

            String name = (strippedName.length() == 0) ? tagName.trim() : strippedName;

            if (name.equalsIgnoreCase("metadata")) {
                inMetadata = true;
            } else if (inMetadata) {
                if (name.equalsIgnoreCase("title")) {
                    //inTitle = true;
                    curBuffer = new StringBuilder();
                } else if (name.equalsIgnoreCase("creator")) {
                    //inAuthor = true;
                    curBuffer = new StringBuilder();
                } else if (name.equalsIgnoreCase("copyright")) {
                    //inCopyright = true;
                    curBuffer = new StringBuilder();
                } else if (name.equalsIgnoreCase("description")) {
                    //inDescription = true;
                    curBuffer = new StringBuilder();
                }
            } else if (name.equalsIgnoreCase("grid")) {
                width = Integer.parseInt(attributes.getValue("width"));
                height = Integer.parseInt(attributes.getValue("height"));
                puz.setWidth(width);
                puz.setHeight(height);
                boxes = new Box[height][width];
                clueNums = new int[height][width];
            } else if (name.equalsIgnoreCase("cell")) {
                int x = Integer.parseInt(attributes.getValue("x")) - 1;
                int y = Integer.parseInt(attributes.getValue("y")) - 1;

                String type = attributes.getValue("type");
                if (type == null || !type.equals("block")) {
                    boxes[y][x] = new Box();

                    String sol = attributes.getValue("solution");
                    if (sol != null) {
                        boxes[y][x].setSolution(sol.charAt(0));
                    } else {
                        // If the file doesn't have a solution, just put in filler
                        // data
                        boxes[y][x].setSolution('Z');
                    }

                    if ("circle".equalsIgnoreCase(attributes.getValue("background-shape"))) {
                        boxes[y][x].setCircled(true);
                    }

                    String number = attributes.getValue("number");

                    if (number != null) {
                        clueNums[y][x] = Integer.parseInt(number);
                    }
                }
            } else if (name.equalsIgnoreCase("clues")) {
                inClues = true;
            } else if (inClues) {
                if (name.equalsIgnoreCase("title")) {
                    //inClueTitle = true;
                    curBuffer = new StringBuilder();
                } else if (name.equalsIgnoreCase("clue")) {
                    //inClue = true;
                    clueNumber = Integer
                            .parseInt(attributes.getValue("number"));

                    if (clueNumber > maxClueNum) {
                        maxClueNum = clueNumber;
                    }

                    curBuffer = new StringBuilder();
                }
            }
        }
    }
}
