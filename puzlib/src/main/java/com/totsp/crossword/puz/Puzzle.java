package com.totsp.crossword.puz;

import java.util.Arrays;

public class Puzzle {

    short fileChecksum;
    String fileMagic;
    short cibChecksum;
    byte[] maskedLowChecksums; //4bytes
    byte[] maskedHighChecksums; //4bytes
    String versionString; // 4bytes
    short reserved1C; //2bytes
    short unknown; //2bytes
    byte[] reserved20; //0xC bytes;
    private int width; //one byte;
    private //one byte;
    int height;
    int numberOfClues; //2 bytes;
    short unknown30; //2bytes
    short unknown32; //2bytes;
    private Box[][] boxes;
    String[] acrossClues;
    Integer[] acrossCluesLookup;
    String[] downClues;
    Integer[] downCluesLookup;
    String title;
    String author;
    String copyright;
    

    public Box[][] getBoxes() {
        return boxes;
    }

    public void setBoxes(Box[][] boxes) {
        this.boxes = boxes;
        int clueCount = 1;

        for (int x = 0; x < boxes.length; x++) {
            boolean tickedClue = false;
            for (int y = 0; y < boxes[x].length; y++) {
                if(boxes[x][y] == null ){
                    continue;
                }
                if (x == 0 || boxes[x - 1][y] == null) {
                    boxes[x][y].down = true;
                    if (x == 0 || boxes[x - 1][y] == null) {
                        boxes[x][y].clueNumber = clueCount;
                        tickedClue = true;
                    }
                }
                if (y == 0 || boxes[x][y - 1] == null) {
                    boxes[x][y].across = true;
                    if (y == 0 || boxes[x][y - 1] == null) {
                        boxes[x][y].clueNumber = clueCount;
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

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    public String findAcrossClue(int clueNumber){
        return this.acrossClues[ Arrays.binarySearch(this.acrossCluesLookup, clueNumber)];
    }

    public String findDownClue(int clueNumber){
        return this.downClues[Arrays.binarySearch(this.downCluesLookup, clueNumber)];
    }
}
