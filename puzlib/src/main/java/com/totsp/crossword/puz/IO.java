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
		for(char c : puz.fileMagic.toCharArray()){
			dos.writeByte(c);
		}
		dos.writeByte(0);
		dos.writeShort(puz.cibChecksum);
		dos.write(puz.maskedLowChecksums);
		dos.write(puz.maskedHighChecksums);
		dos.writeBytes(puz.versionString);
		dos.writeByte(0);
		
		dos.writeShort(puz.reserved1C);
		dos.writeShort(puz.unknown);
		
		dos.write(puz.reserved20);
		dos.writeByte(puz.getWidth());
		
		dos.writeByte(puz.getHeight());
		
		dos.writeByte(puz.numberOfClues);
		dos.writeByte(puz.numberOfClues << 8 );
		dos.writeShort(puz.unknown30);
		dos.writeShort(puz.unknown32);
		
		Box[][] boxes = puz.getBoxes();
		for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if(boxes[x][y] == null ){
                	dos.writeByte('.');
                } else {
                	dos.writeByte(boxes[x][y].solution);
                }
            }
        }

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
            	if(boxes[x][y] == null ){
                	dos.writeByte('.');
                } else {
                	dos.writeByte(boxes[x][y].response == ' ' ? '-' : boxes[x][y].response);
                }
            }
        }
        writeNullTerminatedString(dos, puz.title);
        writeNullTerminatedString(dos, puz.author);
        writeNullTerminatedString(dos, puz.copyright);
        
        for(String clue: puz.rawClues){
        	writeNullTerminatedString(dos, clue);
        }
        
        writeNullTerminatedString(dos, puz.notes);
		
		
	}
	
	public static void writeNullTerminatedString(OutputStream os , String value) throws IOException {
		value = value == null ? "" : value;
		for(char c : value.toCharArray() ){
			os.write(c);
		}
		os.write(0);
	}
	
	public static String readNullTerminatedString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();

        for (byte nextByte = (byte) is.read(); nextByte != 0x0;
                nextByte = (byte) is.read()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }
        
        return sb.length() ==0 ? null : sb.toString();

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
        ArrayList<String> rawClues = new ArrayList<String>();

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
                    String value = acrossClue.toString();
                    acrossClues.add(value);
                    rawClues.add(value);
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
                    String value = downClue.toString();
                    downClues.add(value);
                    rawClues.add(value);
                }
            }
        }

        puz.downClues = downClues.toArray(new String[downClues.size()]);
        puz.downCluesLookup = downCluesLookup.toArray(new Integer[downCluesLookup.size()]);
        puz.acrossClues = acrossClues.toArray(new String[acrossClues.size()]);
        puz.acrossCluesLookup = acrossCluesLookup.toArray(new Integer[acrossCluesLookup.size()]);
        puz.rawClues = rawClues.toArray(new String[rawClues.size()]);
        StringBuilder notes = new StringBuilder();

        for (byte nextChar = input.readByte(); nextChar != 0x0;
                nextChar = input.readByte()) {
            if (nextChar != 0x0) {
                notes.append((char) nextChar);
            }
        }
        puz.notes = notes.toString();

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
