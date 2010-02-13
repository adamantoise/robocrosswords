package com.totsp.crossword.puz;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;


import java.util.ArrayList;


public class IO {
	
	public static void saveNative(Puzzle puz, OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeShort(puz.fileChecksum);
	}
	
	
    public static Puzzle loadNative(InputStream is) throws IOException {
        DataInputStream input = new DataInputStream(is);
        Puzzle puz = new Puzzle();
        puz.fileChecksum = input.readShort();

        byte[] fileMagic = new byte[0xB];

        for (int i = 0; i < fileMagic.length; i++) {
            fileMagic[i] = input.readByte();
        }

        input.skipBytes(1);
        puz.fileMagic = new String(fileMagic);
        assert puz.fileMagic.equals("ACROSS&DOWN");
        puz.cibChecksum = input.readShort();
        puz.maskedLowChecksums = new byte[4];

        for (int i = 0; i < puz.maskedLowChecksums.length; i++) {
            puz.maskedLowChecksums[i] = input.readByte();
        }

        puz.maskedHighChecksums = new byte[4];

        for (int i = 0; i < puz.maskedHighChecksums.length; i++) {
            puz.maskedHighChecksums[i] = input.readByte();
        }

        byte[] versionString = new byte[3];

        for (int i = 0; i < versionString.length; i++) {
            versionString[i] = input.readByte();
        }

        input.skip(1);
        puz.versionString = new String(versionString);
        puz.reserved1C = input.readShort();
        puz.unknown = input.readShort();
        puz.reserved20 = new byte[0xC];

        for (int i = 0; i < puz.reserved20.length; i++) {
            puz.reserved20[i] = input.readByte();
        }

        puz.setWidth(0xFFFF & input.readByte());
        puz.setHeight(0xFFFF & input.readByte());
        puz.numberOfClues = (int) input.readByte() +
            ((int) input.readByte() >> 8);
        puz.unknown30 = input.readShort();
        puz.unknown32 = input.readShort();

        Box[][] boxes = new Box[puz.getWidth()][puz.getHeight()];

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                byte solution = input.readByte();

                if (solution != '.') {
                    boxes[x][y] = new Box();
                    boxes[x][y].solution = (char) solution;
                }
            }
        }

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                char answer = (char) input.readByte();

                if (answer == '.') {
                    continue;
                } else if (answer == '-') {
                    boxes[x][y].response = ' ';
                } else {
                    boxes[x][y].response = answer;
                }
            }
        }

        puz.setBoxes(boxes);

        StringBuffer sb = new StringBuffer();

        for (byte nextByte = input.readByte(); nextByte != 0x0;
                nextByte = input.readByte()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }

        puz.title = sb.toString();

        sb = new StringBuffer();

        for (byte nextByte = input.readByte(); nextByte != 0x0;
                nextByte = input.readByte()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }

        puz.author = sb.toString();

        sb = new StringBuffer();

        for (byte nextByte = input.readByte(); nextByte != 0x0;
                nextByte = input.readByte()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }

        puz.copyright = sb.toString();

        ArrayList<String> acrossClues = new ArrayList<String>();
        ArrayList<Integer> acrossCluesLookup = new ArrayList<Integer>();
        ArrayList<Integer> downCluesLookup = new ArrayList<Integer>();
        ArrayList<String> downClues = new ArrayList<String>();

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] == null) {
                    continue;
                }

                if (boxes[x][y].across && (boxes[x][y].clueNumber != 0)) {
                    StringBuilder acrossClue = new StringBuilder();

                    for (byte nextChar = input.readByte(); nextChar != 0x0;
                            nextChar = input.readByte()) {
                        if (nextChar != 0x0) {
                            acrossClue.append((char) nextChar);
                        }
                    }

                    acrossCluesLookup.add(boxes[x][y].clueNumber);
                    acrossClues.add(acrossClue.toString());
                }

                if (boxes[x][y].down && (boxes[x][y].clueNumber != 0)) {
                    StringBuilder downClue = new StringBuilder();

                    for (byte nextChar = input.readByte(); nextChar != 0x0;
                            nextChar = input.readByte()) {
                        if (nextChar != 0x0) {
                            downClue.append((char) nextChar);
                        }
                    }

                    downCluesLookup.add(boxes[x][y].clueNumber);
                    downClues.add(downClue.toString());
                }
            }
        }

        puz.downClues = downClues.toArray(new String[downClues.size()]);
        puz.downCluesLookup = downCluesLookup.toArray(new Integer[downCluesLookup.size()]);
        puz.acrossClues = acrossClues.toArray(new String[acrossClues.size()]);
        puz.acrossCluesLookup = acrossCluesLookup.toArray(new Integer[acrossCluesLookup.size()]);

        StringBuilder notes = new StringBuilder();

        for (byte nextChar = input.readByte(); nextChar != 0x0;
                nextChar = input.readByte()) {
            if (nextChar != 0x0) {
                notes.append((char) nextChar);
            }
        }

        boolean eof = false;

        try {
            input.readByte();
        } catch (EOFException e) {
            eof = true;
        }
        assert eof : "Should have been the end of file.";

        return puz;
    }
}
