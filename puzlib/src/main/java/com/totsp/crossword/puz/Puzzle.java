package com.totsp.crossword.puz;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Puzzle implements Serializable {
    private String author;
    private String copyright;
    private transient String fileMagic;
    private String notes;
    private String title;
    private transient String versionString; // 4bytes
    private String[] acrossClues;
    private Integer[] acrossCluesLookup;
    private String[] downClues;
    private Integer[] downCluesLookup;
    private transient byte[] maskedHighChecksums; //4bytes
    private transient byte[] maskedLowChecksums; //4bytes
    private transient byte[] reserved20; //0xC bytes;
    private int numberOfClues; //2 bytes;
    private transient short cibChecksum;
    private transient short fileChecksum;
    private transient short reserved1C; //2bytes
    private transient short unknown; //2bytes
    private transient short unknown30; //2bytes
    private transient short unknown32; //2bytes;
    private Date pubdate = new Date();
    private String source;
    private String sourceUrl = "";
    private Box[][] boxes;
    private Box[] boxesList;
    private String[] rawClues;
    private boolean updatable;
    private int height; //on byte
    private int width; //one byte;
    private long playedTime;
    
    // GEXT Section
    private boolean hasGEXT;
    private transient short gextLength;
    private transient short gextChecksum;
    

    public void setAcrossClues(String[] acrossClues) {
        this.acrossClues = acrossClues;
    }

    public String[] getAcrossClues() {
        return acrossClues;
    }

    public void setAcrossCluesLookup(Integer[] acrossCluesLookup) {
        this.acrossCluesLookup = acrossCluesLookup;
    }

    public Integer[] getAcrossCluesLookup() {
        return acrossCluesLookup;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setBoxes(Box[][] boxes) {
        this.boxes = boxes;

        int clueCount = 1;

        for (int x = 0; x < boxes.length; x++) {
            boolean tickedClue = false;

            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] == null) {
                    continue;
                }

                if (((x == 0) || (boxes[x - 1][y] == null)) &&
                        (((x + 1) < boxes.length) && (boxes[x + 1][y] != null))) {
                    boxes[x][y].setDown(true);

                    if ((x == 0) || (boxes[x - 1][y] == null)) {
                        boxes[x][y].setClueNumber(clueCount);
                        tickedClue = true;
                    }
                }

                if (((y == 0) || (boxes[x][y - 1] == null)) &&
                        (((y + 1) < boxes[x].length) &&
                        (boxes[x][y + 1] != null))) {
                    boxes[x][y].setAcross(true);

                    if ((y == 0) || (boxes[x][y - 1] == null)) {
                        boxes[x][y].setClueNumber(clueCount);
                        tickedClue = true;
                    }
                }

                if (tickedClue) {
                    clueCount++;
                    tickedClue = false;
                }
            }
        }
    }

    public Box[][] getBoxes() {
        return (boxes == null) ? this.buildBoxes() : boxes;
    }

    public void setBoxesList(Box[] value) {
        System.out.println("Setting list " + value.length);
        this.boxesList = value;
    }

    public Box[] getBoxesList() {
        Box[] result = new Box[boxes.length * boxes[0].length];
        int i = 0;

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                result[i++] = boxes[x][y];
            }
        }

        return result;
    }

    public void setCibChecksum(short cibChecksum) {
        this.cibChecksum = cibChecksum;
    }

    public short getCibChecksum() {
        return cibChecksum;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setDate(Date date) {
        this.pubdate = date;
    }

    public Date getDate() {
        return pubdate;
    }

    public void setDownClues(String[] downClues) {
        this.downClues = downClues;
    }

    public String[] getDownClues() {
        return downClues;
    }

    public void setDownCluesLookup(Integer[] downCluesLookup) {
        this.downCluesLookup = downCluesLookup;
    }

    public Integer[] getDownCluesLookup() {
        return downCluesLookup;
    }

    public void setFileChecksum(short fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public short getFileChecksum() {
        return fileChecksum;
    }

    public void setFileMagic(String fileMagic) {
        this.fileMagic = fileMagic;
    }

    public String getFileMagic() {
        return fileMagic;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    public void setMaskedHighChecksums(byte[] maskedHighChecksums) {
        this.maskedHighChecksums = maskedHighChecksums;
    }

    public byte[] getMaskedHighChecksums() {
        return maskedHighChecksums;
    }

    public void setMaskedLowChecksums(byte[] maskedLowChecksums) {
        this.maskedLowChecksums = maskedLowChecksums;
    }

    public byte[] getMaskedLowChecksums() {
        return maskedLowChecksums;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNumberOfClues(int numberOfClues) {
        this.numberOfClues = numberOfClues;
    }

    public int getNumberOfClues() {
        return numberOfClues;
    }

    public int getPercentComplete() {
        int total = 0;
        int correct = 0;

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] != null) {
                    total++;

                    if (boxes[x][y].getResponse() == boxes[x][y].getSolution()) {
                        correct++;
                    }
                }
            }
        }

        return (correct * 100) / (total);
    }
    
    public int getPercentFilled() {
    	int total = 0;
    	int filled = 0;

        for (int x = 0; x < boxes.length; x++) {
            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] != null) {
                    total++;

                    if (boxes[x][y].getResponse() != ' ') {
                        filled++;
                    }
                }
            }
        }

        return (filled * 100) / (total);
    }

    public void setRawClues(String[] rawClues) {
        this.rawClues = rawClues;
    }

    public String[] getRawClues() {
        return rawClues;
    }

    public void setReserved1C(short reserved1C) {
        this.reserved1C = reserved1C;
    }

    public short getReserved1C() {
        return reserved1C;
    }

    public void setReserved20(byte[] reserved20) {
        this.reserved20 = reserved20;
    }

    public byte[] getReserved20() {
        return reserved20;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setTime(long time) {
        this.playedTime = time;
    }

    public long getTime() {
        return this.playedTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setUnknown(short unknown) {
        this.unknown = unknown;
    }

    public short getUnknown() {
        return unknown;
    }

    public void setUnknown30(short unknown30) {
        this.unknown30 = unknown30;
    }

    public short getUnknown30() {
        return unknown30;
    }

    public void setUnknown32(short unknown32) {
        this.unknown32 = unknown32;
    }

    public short getUnknown32() {
        return unknown32;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public String getVersionString() {
        return versionString;
    }
    
    public void setGEXT(boolean hasGEXT) {
    	this.hasGEXT = hasGEXT;
    }
    
    public boolean getGEXT() {
    	return hasGEXT;
    }
    
    public void setGextLength(short gextLength) {
    	this.gextLength = gextLength;
    }
    
    public short getGextLength() {
    	return gextLength;
    }
    
    public void setGextChecksum(short gextChecksum) {
    	this.gextChecksum = gextChecksum;
    }
    
    public short getGextChecksum() {
    	return gextChecksum;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    public Box[][] buildBoxes() {
        System.out.println("Building boxes " + this.height + "x" + this.width);

        int i = 0;
        boxes = new Box[this.height][this.width];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                boxes[y][x] = boxesList[i++];
            }
        }

        return boxes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Puzzle other = (Puzzle) obj;

        if (!Arrays.equals(acrossClues, other.acrossClues)) {
            System.out.println("acrossClues");

            //            for(int i=0; i < acrossClues.length; i++)
            //            	System.out.println((acrossClues[i].equals(other.acrossClues[i]))+"["+acrossClues[i]+"]==["+other.acrossClues[i]+"]");
            return false;
        }

        if (!Arrays.equals(acrossCluesLookup, other.acrossCluesLookup)) {
            System.out.println("acrossCluesLookup");

            return false;
        }

        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            System.out.println("author");

            return false;
        }

        Box[][] b1 = boxes;
        Box[][] b2 = other.boxes;
        boolean boxEq = true;

        for (int x = 0; x < b1.length; x++) {
            for (int y = 0; y < b1[x].length; y++) {
                boxEq = boxEq
                    ? ((b1[x][y] == b2[x][y]) || b1[x][y].equals(b2[x][y]))
                    : boxEq;
            }
        }

        if (!boxEq) {
            System.out.println("boxes");

            return false;
        }

        if (getCibChecksum() != other.getCibChecksum()) {
            System.out.println("checksum");

            return false;
        }

        if (copyright == null) {
            if (other.copyright != null) {
                System.out.println("copyright");

                return false;
            }
        } else if (!copyright.equals(other.copyright)) {
            return false;
        }

        if (!Arrays.equals(downClues, other.downClues)) {
            System.out.println("downClues");

            return false;
        }

        if (!Arrays.equals(downCluesLookup, other.downCluesLookup)) {
            System.out.println("downCluesLookup");

            return false;
        }

        if (getFileChecksum() != other.getFileChecksum()) {
            System.out.println("fileChecksum");

            return false;
        }

        if (fileMagic == null) {
            if (other.fileMagic != null) {
                return false;
            }
        } else if (!fileMagic.equals(other.fileMagic)) {
            System.out.println("fileMagic");

            return false;
        }

        if (height != other.height) {
            System.out.println("height");

            return false;
        }

        if (notes == null) {
            if (other.notes != null) {
                return false;
            }
        } else if (!notes.equals(other.notes)) {
            System.out.println("notes");

            return false;
        }

        if (getNumberOfClues() != other.getNumberOfClues()) {
            System.out.println("numberOfClues");

            return false;
        }

        if (reserved1C != other.reserved1C) {
            return false;
        }

        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            System.out.println("title");

            return false;
        }

        if (unknown != other.unknown) {
            return false;
        }

        if (unknown30 != other.unknown30) {
            return false;
        }

        if (unknown32 != other.unknown32) {
            return false;
        }

        if (versionString == null) {
            if (other.versionString != null) {
                return false;
            }
        } else if (!versionString.equals(other.versionString)) {
            return false;
        }

        if (width != other.width) {
            return false;
        }

        return true;
    }

    public String findAcrossClue(int clueNumber) {
        return this.acrossClues[Arrays.binarySearch(this.acrossCluesLookup,
            clueNumber)];
    }

    public String findDownClue(int clueNumber) {
        return this.downClues[Arrays.binarySearch(this.downCluesLookup,
            clueNumber)];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(acrossClues);
        result = (prime * result) + Arrays.hashCode(acrossCluesLookup);
        result = (prime * result) + ((author == null) ? 0 : author.hashCode());
        result = (prime * result) + Arrays.hashCode(boxes);
        result = (prime * result) + getCibChecksum();
        result = (prime * result) +
            ((copyright == null) ? 0 : copyright.hashCode());
        result = (prime * result) + Arrays.hashCode(downClues);
        result = (prime * result) + Arrays.hashCode(downCluesLookup);
        result = (prime * result) + getFileChecksum();
        result = (prime * result) +
            ((fileMagic == null) ? 0 : fileMagic.hashCode());
        result = (prime * result) + height;
        result = (prime * result) + Arrays.hashCode(getMaskedHighChecksums());
        result = (prime * result) + Arrays.hashCode(getMaskedLowChecksums());
        result = (prime * result) + ((notes == null) ? 0 : notes.hashCode());
        result = (prime * result) + getNumberOfClues();
        result = (prime * result) + reserved1C;
        result = (prime * result) + Arrays.hashCode(getReserved20());
        result = (prime * result) + ((title == null) ? 0 : title.hashCode());
        result = (prime * result) + unknown;
        result = (prime * result) + unknown30;
        result = (prime * result) + unknown32;
        result = (prime * result) +
            ((versionString == null) ? 0 : versionString.hashCode());
        result = (prime * result) + width;

        return result;
    }

    @Override
    public String toString() {
        return "Puzzle " + boxes.length + " x " + boxes[0].length + " " +
        this.title;
    }
}
