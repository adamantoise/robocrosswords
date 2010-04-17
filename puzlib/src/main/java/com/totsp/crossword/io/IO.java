package com.totsp.crossword.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.totsp.crossword.io.versions.IOVersion;
import com.totsp.crossword.io.versions.IOVersion1;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;


public class IO {
    public static Puzzle loadNative(InputStream is) throws IOException {
        DataInputStream input = new DataInputStream(is);
        Puzzle puz = new Puzzle();
        puz.setFileChecksum(input.readShort());

        byte[] fileMagic = new byte[0xB];

        for (int i = 0; i < fileMagic.length; i++) {
            fileMagic[i] = input.readByte();
        }

        input.skipBytes(1);
        puz.setFileMagic(new String(fileMagic));
        assert puz.getFileMagic().equals("ACROSS&DOWN");
        puz.setCibChecksum(input.readShort());
        puz.setMaskedLowChecksums(new byte[4]);

        for (int i = 0; i < puz.getMaskedLowChecksums().length; i++) {
            puz.getMaskedLowChecksums()[i] = input.readByte();
        }

        puz.setMaskedHighChecksums(new byte[4]);

        for (int i = 0; i < puz.getMaskedHighChecksums().length; i++) {
            puz.getMaskedHighChecksums()[i] = input.readByte();
        }

        byte[] versionString = new byte[3];

        for (int i = 0; i < versionString.length; i++) {
            versionString[i] = input.readByte();
        }

        input.skip(1);
        puz.setVersionString(new String(versionString));
        puz.setReserved1C(input.readShort());
        puz.setUnknown(input.readShort());
        puz.setReserved20(new byte[0xC]);

        for (int i = 0; i < puz.getReserved20().length; i++) {
            puz.getReserved20()[i] = input.readByte();
        }

        puz.setWidth(0xFFFF & input.readByte());
        puz.setHeight(0xFFFF & input.readByte());
        puz.setNumberOfClues((int) input.readByte() +
            ((int) input.readByte() >> 8));
        puz.setUnknown30(input.readShort());
        puz.setUnknown32(input.readShort());

        Box[][] boxes = new Box[puz.getWidth()][puz.getHeight()];

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                byte solution = input.readByte();

                if (solution != '.') {
                    boxes[x][y] = new Box();
                    boxes[x][y].setSolution((char) solution);
                }
            }
        }

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                char answer = (char) input.readByte();

                if (answer == '.') {
                    continue;
                } else if (answer == '-') {
                    boxes[x][y].setResponse(' ');
                } else {
                    boxes[x][y].setResponse(answer);
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

        puz.setTitle(sb.toString());

        sb = new StringBuffer();

        for (byte nextByte = input.readByte(); nextByte != 0x0;
                nextByte = input.readByte()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }

        puz.setAuthor(sb.toString());

        sb = new StringBuffer();

        for (byte nextByte = input.readByte(); nextByte != 0x0;
                nextByte = input.readByte()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
        }

        puz.setCopyright(sb.toString());

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

                if (boxes[x][y].isAcross() && (boxes[x][y].getClueNumber() != 0)) {
                    StringBuilder acrossClue = new StringBuilder();

                    for (byte nextChar = input.readByte(); nextChar != 0x0;
                            nextChar = input.readByte()) {
                        if (nextChar != 0x0) {
                            acrossClue.append((char) nextChar);
                        }
                    }

                    acrossCluesLookup.add(boxes[x][y].getClueNumber());

                    String value = acrossClue.toString();
                    acrossClues.add(value);
                    rawClues.add(value);
                }

                if (boxes[x][y].isDown() && (boxes[x][y].getClueNumber() != 0)) {
                    StringBuilder downClue = new StringBuilder();

                    for (byte nextChar = input.readByte(); nextChar != 0x0;
                            nextChar = input.readByte()) {
                        if (nextChar != 0x0) {
                            downClue.append((char) nextChar);
                        }
                    }

                    downCluesLookup.add(boxes[x][y].getClueNumber());

                    String value = downClue.toString();
                    downClues.add(value);
                    rawClues.add(value);
                }
            }
        }

        puz.setDownClues(downClues.toArray(new String[downClues.size()]));
        puz.setDownCluesLookup(downCluesLookup.toArray(new Integer[downCluesLookup.size()]));
        puz.setAcrossClues(acrossClues.toArray(new String[acrossClues.size()]));
        puz.setAcrossCluesLookup(acrossCluesLookup.toArray(new Integer[acrossCluesLookup.size()]));
        puz.setRawClues(rawClues.toArray(new String[rawClues.size()]));

        StringBuilder notes = new StringBuilder();

        for (byte nextChar = input.readByte(); nextChar != 0x0;
                nextChar = input.readByte()) {
            if (nextChar != 0x0) {
                notes.append((char) nextChar);
            }
        }

        puz.setNotes(notes.toString());

        boolean eof = false;

        try {
            input.readByte();
        } catch (EOFException e) {
            eof = true;
        }
        assert eof : "Should have been the end of file.";

        return puz;
    }
    
    public static void writeCustom(Puzzle puz, OutputStream os) throws IOException {
    	os.write(1);
    	IOVersion v = new IOVersion1();
    	v.write(puz, os);
    }
    
    public static void readCustom(Puzzle puz, InputStream is) throws IOException{
        int version = is.read();
        IOVersion v;

        switch (version) {
        case 1:
            v = new IOVersion1();

            break;

        default:
            throw new IOException("UnknownVersion");
        }

        v.read(puz, is);
        
        
    }

    public static PuzzleMeta readMeta(InputStream is) throws IOException {
        int version = is.read();
        IOVersion v;

        switch (version) {
        case 1:
            v = new IOVersion1();

            break;

        default:
            throw new IOException("UnknownVersion");
        }

        PuzzleMeta m = v.readMeta(is);

        return m;
    }

    public static String readNullTerminatedString(InputStream is)
        throws IOException {
        StringBuilder sb = new StringBuilder();

        for (byte nextByte = (byte) is.read(); nextByte != 0x0;
                nextByte = (byte) is.read()) {
            if (nextByte != 0x0) {
                sb.append((char) nextByte);
            }
            if(sb.length() > 4096){
            	throw new IOException("Run on string!");
            }
        }

        return (sb.length() == 0) ? null : sb.toString();
    }

    public static void saveNative(Puzzle puz, OutputStream os)
        throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeShort(puz.getFileChecksum());

        for (char c : puz.getFileMagic().toCharArray()) {
            dos.writeByte(c);
        }

        dos.writeByte(0);
        dos.writeShort(puz.getCibChecksum());
        dos.write(puz.getMaskedLowChecksums());
        dos.write(puz.getMaskedHighChecksums());
        dos.writeBytes(puz.getVersionString());
        dos.writeByte(0);

        dos.writeShort(puz.getReserved1C());
        dos.writeShort(puz.getUnknown());

        dos.write(puz.getReserved20());
        dos.writeByte(puz.getWidth());

        dos.writeByte(puz.getHeight());

        dos.writeByte(puz.getNumberOfClues());
        dos.writeByte(puz.getNumberOfClues() << 8);
        dos.writeShort(puz.getUnknown30());
        dos.writeShort(puz.getUnknown32());

        Box[][] boxes = puz.getBoxes();

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] == null) {
                    dos.writeByte('.');
                } else {
                    dos.writeByte(boxes[x][y].getSolution());
                }
            }
        }

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] == null) {
                    dos.writeByte('.');
                } else {
                    dos.writeByte((boxes[x][y].getResponse() == ' ') ? '-'
                                                                : boxes[x][y].getResponse());
                }
            }
        }

        writeNullTerminatedString(dos, puz.getTitle());
        writeNullTerminatedString(dos, puz.getAuthor());
        writeNullTerminatedString(dos, puz.getCopyright());

        for (String clue : puz.getRawClues()) {
            writeNullTerminatedString(dos, clue);
        }

        writeNullTerminatedString(dos, puz.getNotes());
    }

    public static void writeNullTerminatedString(OutputStream os, String value)
        throws IOException {
        value = (value == null) ? "" : value;

        for (char c : value.toCharArray()) {
            os.write(c);
        }

        os.write(0);
    }

    public static void save(Puzzle puz, OutputStream puzzleOutputStream, OutputStream metaOutputStream) throws IOException{
        IO.saveNative(puz, puzzleOutputStream);
    	puzzleOutputStream.close();
        IO.writeCustom(puz, metaOutputStream);
        metaOutputStream.close();
    }
    
    public static void save(Puzzle puz, File baseFile) throws IOException {
    	File metaFile = new File(baseFile.getParentFile(), baseFile.getName().substring(0, baseFile.getName().lastIndexOf(".")) + ".shortyz");
    	FileOutputStream puzzle= new FileOutputStream(baseFile);
    	FileOutputStream meta = new FileOutputStream(metaFile);
    	IO.save(puz, puzzle, meta);
    }

    public static Puzzle load(InputStream puzzleInput, InputStream metaInput ) throws IOException{
        Puzzle puz = IO.loadNative(puzzleInput);
        puzzleInput.close();
        IO.readCustom(puz, metaInput);
        return puz;
    }
    
    public static Puzzle load(File baseFile) throws IOException {
    	File metaFile = new File(baseFile.getParentFile(), baseFile.getName().substring(0, baseFile.getName().lastIndexOf(".")) + ".shortyz");
    	FileInputStream fis = new FileInputStream(baseFile);
    	Puzzle puz = IO.loadNative(fis);
    	fis.close();
    	if(metaFile.exists()){
	    	fis = new FileInputStream(metaFile);
	    	IO.readCustom(puz, fis);
	    	fis.close();
    	}
    	return puz;
    }


    public static PuzzleMeta meta(File baseFile) throws IOException {
    	File metaFile = new File(baseFile.getParentFile(), baseFile.getName().substring(0, baseFile.getName().lastIndexOf(".")) + ".shortyz");
    	FileInputStream fis = new FileInputStream(metaFile);
    	PuzzleMeta m = IO.readMeta(fis);
    	fis.close();
    	return m;
    }
}
