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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import android.content.res.Resources;
import android.text.TextUtils;

import com.adamrosenfield.wordswithcrosses.R;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class IO {
    public static final byte[] FILE_MAGIC = new byte[] {
        (byte)'A', (byte)'C', (byte)'R', (byte)'O', (byte)'S', (byte)'S',
        (byte)'&', (byte)'D', (byte)'O', (byte)'W', (byte)'N', (byte)0
    };

    public static final String VERSION_STRING = "1.2";
    private static final Charset CHARSET = Charset.forName("Cp1252");

    // Extra Section IDs
    private enum ExtraSection
    {
        GEXT,
        LTIM,
        Unknown,
    }
    // TODO: Support GRBS, RTBL, and RUSR sections for rebus puzzles

    // GEXT section bitmasks
    private static final byte GEXT_WAS_INCORRECT  = (byte)0x10;
    private static final byte GEXT_IS_INCORRECT   = (byte)0x20;
    private static final byte GEXT_SQUARE_CIRCLED = (byte)0x80;

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    /**
     * Copies the data from an InputStream object to an OutputStream object.
     *
     * @param sourceStream
     *            The input stream to be read.
     * @param destinationStream
     *            The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException
     *                from java.io calls.
     */
    public static int copyStream(InputStream sourceStream, OutputStream destinationStream)
        throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[4096];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }

        destinationStream.flush();

        return totalBytes;
    }

    public static Puzzle load(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return load(new DataInputStream(fis));
        } finally {
            fis.close();
        }
    }

    public static Puzzle load(DataInputStream input) throws IOException {
        long startTime = System.currentTimeMillis();

        Puzzle puz = new Puzzle();

        input.skipBytes(2);

        byte[] magic = new byte[12];
        input.readFully(magic);
        if (!Arrays.equals(magic, FILE_MAGIC)) {
            throw new IOException("Invalid/missing magic");
        }

        input.skipBytes(10);

        byte[] versionString = new byte[3];

        for (int i = 0; i < versionString.length; i++) {
            versionString[i] = input.readByte();
        }

        input.skip(1);
        puz.setVersion(new String(versionString));

        input.skipBytes(2);
        puz.setSolutionChecksum(Short.reverseBytes(input.readShort()));

        input.skipBytes(12);

        puz.setWidth(0xFFFF & input.readByte());
        puz.setHeight(0xFFFF & input.readByte());
        puz.setNumberOfClues(Short.reverseBytes(input.readShort()));

        input.skipBytes(2);
        puz.setScrambled(input.readShort() != 0);

        Box[][] boxes = new Box[puz.getHeight()][puz.getWidth()];
        byte[] answerByte = new byte[1];

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                answerByte[0] = input.readByte();

                char solution = new String(answerByte, CHARSET.name()).charAt(0);

                if (solution != '.') {
                    boxes[r][c] = new Box();
                    boxes[r][c].setSolution(solution);
                }
            }
        }

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                answerByte[0] = input.readByte();

                char answer = new String(answerByte, CHARSET.name()).charAt(0);

                if (answer == '.') {
                    continue;
                } else if (answer == '-') {
                    boxes[r][c].setResponse(' ');
                } else if (boxes[r][c] != null) {
                    boxes[r][c].setResponse(answer);
                } else {
                    LOG.warning("IO.load(): Unexpected answer: " + r + "," + c + " " + answer);
                }
            }
        }

        puz.setBoxes(boxes);

        puz.setTitle(readNullTerminatedString(input));
        puz.setAuthor(readNullTerminatedString(input));
        puz.setCopyright(readNullTerminatedString(input));

        Resources resources = WordsWithCrossesApplication.getContext().getResources();
        if (TextUtils.isEmpty(puz.getTitle())) {
            puz.setTitle(resources.getString(R.string.puzzle_untitled));
        }
        if (TextUtils.isEmpty(puz.getAuthor())) {
            puz.setAuthor(resources.getString(R.string.author_unknown));
        }

        ArrayList<String> acrossClues = new ArrayList<>();
        ArrayList<Integer> acrossCluesLookup = new ArrayList<>();
        ArrayList<Integer> downCluesLookup = new ArrayList<>();
        ArrayList<String> downClues = new ArrayList<>();
        ArrayList<String> rawClues = new ArrayList<>();

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] == null) {
                    continue;
                }

                if (boxes[r][c].isAcross()
                        && (boxes[r][c].getClueNumber() != 0)) {
                    String value = readNullTerminatedString(input);

                    acrossCluesLookup.add(boxes[r][c].getClueNumber());
                    acrossClues.add(value);
                    rawClues.add(value);
                }

                if (boxes[r][c].isDown() && (boxes[r][c].getClueNumber() != 0)) {
                    String value = readNullTerminatedString(input);
                    downCluesLookup.add(boxes[r][c].getClueNumber());
                    downClues.add(value);
                    rawClues.add(value);
                }
            }
        }

        puz.setDownClues(downClues.toArray(new String[downClues.size()]));
        puz.setDownCluesLookup(downCluesLookup
                .toArray(new Integer[downCluesLookup.size()]));
        puz.setAcrossClues(acrossClues.toArray(new String[acrossClues.size()]));
        puz.setAcrossCluesLookup(acrossCluesLookup
                .toArray(new Integer[acrossCluesLookup.size()]));
        puz.setRawClues(rawClues.toArray(new String[rawClues.size()]));

        puz.setNotes(readNullTerminatedString(input));

        boolean eof = false;

        while (!eof) {
            try {
                switch (readExtraSectionType(input)) {
                case GEXT:
                    readGextSection(input, puz);
                    break;

                case LTIM:
                    readLtimSection(input, puz);
                    break;

                default:
                    skipExtraSection(input);
                }
            } catch (EOFException e) {
                eof = true;
            }
        }

        LOG.info("Load complete in " + (System.currentTimeMillis() - startTime) + " ms");

        return puz;
    }

    private static ExtraSection readExtraSectionType(DataInputStream input)
            throws IOException {
        byte[] title = new byte[4];

        for (int i = 0; i < title.length; i++) {
            title[i] = input.readByte();
        }

        String section = new String(title);

        if ("GEXT".equals(section)) {
            return ExtraSection.GEXT;
        } else if ("LTIM".equals(section)) {
            return ExtraSection.LTIM;
        } else {
            return ExtraSection.Unknown;
        }
    }

    private static void readGextSection(DataInputStream input, Puzzle puz)
            throws IOException {
        input.skipBytes(4);

        Box[][] boxes = puz.getBoxes();

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                byte gextInfo = input.readByte();

                if ((gextInfo & (GEXT_WAS_INCORRECT | GEXT_IS_INCORRECT)) != 0) {
                    if (boxes[r][c] != null) {
                        boxes[r][c].setCheated(true);
                    }
                }
                if ((gextInfo & GEXT_SQUARE_CIRCLED) != 0) {
                    if (boxes[r][c] != null) {
                        boxes[r][c].setCircled(true);
                    }
                }
            }
        }

        input.skipBytes(1);
    }

    private static void readLtimSection(DataInputStream input, Puzzle puz) throws IOException {
        short numBytes = Short.reverseBytes(input.readShort());
        input.skipBytes(2); // checksum
        byte[] ltimBytes = new byte[numBytes + 1];
        input.readFully(ltimBytes);

        String ltimStr = new String(ltimBytes);
        String[] ltimParts = ltimStr.split(",");
        if (ltimParts.length != 2) {
            LOG.warning("Bad LTIM section: " + ltimStr);
            return;
        }

        try {
            int secondsElapsed = Math.max(Integer.parseInt(ltimParts[0]), 0);
            puz.setTime(secondsElapsed * 1000);
        } catch (NumberFormatException e) {
            LOG.warning("Bad LTIM section: " + ltimStr);
        }
    }

   private static void skipExtraSection(DataInputStream input) throws IOException {
        short numBytes = Short.reverseBytes(input.readShort());
        input.skipBytes(2); // checksum
        input.skipBytes(numBytes); // data
        input.skipBytes(1); // null terminator
    }

    private static String readNullTerminatedString(InputStream is)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);

        for (byte nextByte = (byte)is.read(); nextByte != 0x0; nextByte = (byte)is.read()) {
            baos.write(nextByte);

            if (baos.size() > 4096) {
                throw new IOException("Run on string!");
            }
        }

        return (baos.size() == 0) ? null : new String(baos.toByteArray(), CHARSET.name());
    }

    public static void save(Puzzle puzzle, File destFile) throws IOException {
        long incept = System.currentTimeMillis();

        File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, destFile.getName());

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            save(puzzle, fos);
        } finally {
            fos.close();
        }

        if (!tempFile.renameTo(destFile)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destFile);
        }

        LOG.info("Save complete in " + (System.currentTimeMillis() - incept) + " ms");
    }

    public static void save(Puzzle puz, OutputStream os)
            throws IOException {
        /*
         * We write the puzzle to a temporary output stream, with 0 entered for
         * any checksums. Once we have this written out, we can calculate all of
         * the checksums and write the file to the original output stream.
         */
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        DataOutputStream tmpDos = new DataOutputStream(tmp);

        tmpDos.writeShort(0);

        tmpDos.write(FILE_MAGIC);

        tmpDos.write(new byte[10]);

        tmpDos.writeBytes(puz.getVersion());
        tmpDos.writeByte(0);

        tmpDos.write(new byte[2]);

        tmpDos.writeShort(Short.reverseBytes(puz.getSolutionChecksum()));

        tmpDos.write(new byte[12]);

        int width = puz.getWidth();
        int height = puz.getHeight();
        int numberOfBoxes = width * height;

        tmpDos.writeByte(width);
        tmpDos.writeByte(height);

        int numberOfClues = puz.getNumberOfClues();

        tmpDos.writeShort(Short.reverseBytes((short) numberOfClues));
        tmpDos.writeShort(Short.reverseBytes((short) 1));

        short scrambled = puz.isScrambled() ? (short) 4 : (short) 0;
        tmpDos.writeShort(Short.reverseBytes(scrambled));

        Box[][] boxes = puz.getBoxes();
        byte[] gextSection = new byte[numberOfBoxes];

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] == null) {
                    tmpDos.writeByte('.');
                } else {
                    byte val = (byte)boxes[r][c].getSolution(); // Character.toString().getBytes("Cp1252")[0];
                    tmpDos.writeByte(val);

                    byte gextVal = 0;
                    if (boxes[r][c].isCheated()) {
                        gextVal |= GEXT_WAS_INCORRECT;
                    }
                    if (boxes[r][c].isCircled()) {
                        gextVal |= GEXT_SQUARE_CIRCLED;
                    }
                    gextSection[width * r + c] = gextVal;
                }
            }
        }

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] == null) {
                    tmpDos.writeByte('.');
                } else {
                    byte val = (byte) boxes[r][c].getResponse(); // Character.toString().getBytes("Cp1252")[0];
                    tmpDos.writeByte((boxes[r][c].getResponse() == ' ') ? '-'
                            : val);
                }
            }
        }

        writeNullTerminatedString(tmpDos, puz.getTitle());
        writeNullTerminatedString(tmpDos, puz.getAuthor());
        writeNullTerminatedString(tmpDos, puz.getCopyright());

        for (String clue : puz.getRawClues()) {
            writeNullTerminatedString(tmpDos, clue);
        }

        writeNullTerminatedString(tmpDos, puz.getNotes());

        if (puz.getTime() > 0) {
            int secondsElapsed = (int)(puz.getTime() / 1000);
            String ltimStr = secondsElapsed + ",0";
            byte[] ltimBytes = ltimStr.getBytes();
            writeExtraSection(tmpDos, "LTIM", ltimBytes);
        }

        writeExtraSection(tmpDos, "GEXT", gextSection);

        byte[] puzByteArray = tmp.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(puzByteArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // Calculate checksums and write to byte array.
        int c_cib = checksumCIB(puzByteArray, 0);
        bb.putShort(0x0E, (short) c_cib);

        int c_primary = checksumPrimaryBoard(puzByteArray, numberOfBoxes,
                numberOfClues, c_cib);
        bb.putShort(0, (short) c_primary);

        int c_sol = checksumSolution(puzByteArray, numberOfBoxes, 0);
        int c_grid = checksumGrid(puzByteArray, numberOfBoxes, 0);
        int c_part = checksumPartialBoard(puzByteArray, numberOfBoxes,
                numberOfClues, 0);

        bb.position(0x10);
        bb.put((byte) (0x49 ^ (c_cib & 0xFF)));
        bb.put((byte) (0x43 ^ (c_sol & 0xFF)));
        bb.put((byte) (0x48 ^ (c_grid & 0xFF)));
        bb.put((byte) (0x45 ^ (c_part & 0xFF)));
        bb.put((byte) (0x41 ^ ((c_cib & 0xFF00) >> 8)));
        bb.put((byte) (0x54 ^ ((c_sol & 0xFF00) >> 8)));
        bb.put((byte) (0x45 ^ ((c_grid & 0xFF00) >> 8)));
        bb.put((byte) (0x44 ^ ((c_part & 0xFF00) >> 8)));

        // Dump byte array to output stream.
        os.write(puzByteArray);
    }

    private static void writeExtraSection(DataOutputStream dos, String sectionName, byte[] data) throws IOException {
        dos.writeBytes(sectionName);
        dos.writeShort(Short.reverseBytes((short)data.length));

        // Calculate checksum here so we don't need to find this place in
        // the file later.
        int cksum = checksumRegion(data);
        dos.writeShort(Short.reverseBytes((short)cksum));
        dos.write(data);
        dos.writeByte(0);
    }

    /**
     * Attempts to unscramble the solution using the input key. Modifications to
     * the solution array occur in place. If true, the unscrambled solution
     * checksum is valid.
     */
    private static boolean tryUnscramble(Puzzle p, int key_int, byte[] solution) {
        p.unscrambleKey[0] = (key_int / 1000) % 10;
        p.unscrambleKey[1] = (key_int / 100) % 10;
        p.unscrambleKey[2] = (key_int / 10) % 10;
        p.unscrambleKey[3] = (key_int / 1) % 10;

        for (int i = 3; i >= 0; i--) {
            unscrambleString(p, solution);
            System.arraycopy(p.unscrambleBuf, 0, solution, 0,
                    p.unscrambleBuf.length);
            unshiftString(p, solution, p.unscrambleKey[i]);

            for (int j = 0; j < solution.length; j++) {
                int letter = (solution[j] & 0xFF) - p.unscrambleKey[j % 4];

                if (letter < 65) {
                    letter += 26;
                }

                solution[j] = (byte)letter;
            }
        }

        if (p.solutionChecksum == (short)IO.checksumRegion(solution)) {
            int s = 0;
            for (int i = 0; i < p.getBoxesList().length; i++) {
                Box b = p.getBoxesList()[i];
                if (b != null) {
                    b.setSolution((char)solution[s++]);
                }
            }
            return true;
        }
        return false;
    }

    // TODO: Call this somewhere?
    public static boolean crackPuzzle(Puzzle puz) {
        for (int a = 0; a < 10000; a++) {
            if (tryUnscramble(puz, a, puz.initializeUnscrambleData())) {
                return true;
            }
        }
        return false;
    }

    private static void writeNullTerminatedString(OutputStream os, String value)
            throws IOException {
        value = (value == null) ? "" : value;

        byte[] encoded = CHARSET.encode(value).array();
        os.write(encoded);
        os.write(0);
    }

    private static void unscrambleString(Puzzle p, byte[] str) {
        int oddIndex = 0;
        int evenIndex = str.length / 2;

        for (int i = 0; i < str.length; i++) {
            if ((i % 2) == 0) {
                p.unscrambleBuf[evenIndex++] = str[i];
            } else {
                p.unscrambleBuf[oddIndex++] = str[i];
            }
        }
    }

    private static void unshiftString(Puzzle p, byte[] str, int keynum) {
        System.arraycopy(str, str.length - keynum, p.unscrambleTmp, 0, keynum);
        System.arraycopy(str, 0, str, keynum, str.length - keynum);
        System.arraycopy(p.unscrambleTmp, 0, str, 0, keynum);
    }

    private static int checksumCIB(byte[] puzByteArray, int cksum) {
        return checksumRegion(puzByteArray, 0x2C, 8, cksum);
    }

    private static int checksumGrid(byte[] puzByteArray, int numberOfBoxes, int cksum) {
        return checksumRegion(puzByteArray, 0x34 + numberOfBoxes, numberOfBoxes, cksum);
    }

    private static int checksumPartialBoard(byte[] puzByteArray, int numberOfBoxes, int numberOfClues, int cksum) {
        int offset = 0x34 + (2 * numberOfBoxes);

        for (int i = 0; i < (4 + numberOfClues); i++) {
            int startOffset = offset;

            while (puzByteArray[offset] != 0 && offset < puzByteArray.length) {
                offset++;
            }

            int length = offset - startOffset;

            if ((i > 2) && (i < (3 + numberOfClues))) {
                cksum = checksumRegion(puzByteArray, startOffset, length, cksum);
            } else if (length > 0) {
                cksum = checksumRegion(puzByteArray, startOffset, length + 1, cksum);
            }

            offset++;
        }

        return cksum;
    }

    private static int checksumPrimaryBoard(byte[] puzByteArray,
            int numberOfBoxes, int numberOfClues, int cksum) {
        cksum = checksumSolution(puzByteArray, numberOfBoxes, cksum);
        cksum = checksumGrid(puzByteArray, numberOfBoxes, cksum);
        cksum = checksumPartialBoard(puzByteArray, numberOfBoxes, numberOfClues,
                cksum);

        return cksum;
    }

    private static int checksumSolution(byte[] puzByteArray, int numberOfBoxes, int cksum) {
        return checksumRegion(puzByteArray, 0x34, numberOfBoxes, cksum);
    }

    private static int checksumRegion(byte[] data) {
        return checksumRegion(data, 0);
    }

    private static int checksumRegion(byte[] data, int cksum) {
        return checksumRegion(data, 0, data.length, cksum);
    }

    private static int checksumRegion(byte[] data, int offset, int length, int cksum) {
        for (int i = offset; i < (offset + length); i++) {
            if ((cksum & 0x1) != 0) {
                cksum = (cksum >> 1) + 0x8000;
            } else {
                cksum = cksum >> 1;
            }

            cksum += (0xFF & data[i]);
            cksum = cksum & 0xFFFF;
        }

        return cksum;
    }
}
