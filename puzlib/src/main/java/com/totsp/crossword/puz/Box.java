package com.totsp.crossword.puz;

import java.text.NumberFormat;

public class Box {

    private static final NumberFormat FORMAT = NumberFormat.getIntegerInstance();

    static {
        FORMAT.setMinimumIntegerDigits(3);
    }
    public int clueNumber;
    public boolean cheated;
    public char solution;
    public char response = ' ';
    public boolean down;
    public boolean across;
    public String responder;
    
    @Override
    public String toString() {
        return FORMAT.format(this.clueNumber) + this.solution+" ";
    }
}
