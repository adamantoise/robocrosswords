package com.totsp.crossword.puz;

import com.totsp.gwittir.client.beans.annotations.Introspectable;
import java.io.Serializable;


@Introspectable
public class Box implements Serializable {
    
    public String responder;
    public boolean across;
    public boolean cheated;
    public boolean down;
    public char response = ' ';
    public char solution;
    public int clueNumber;

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

        Box other = (Box) obj;

        if (across != other.across) {
        	System.out.println("across");
            return false;
        }

        if (cheated != other.cheated) {
        	System.out.println("cheated");
            return false;
        }

        if (clueNumber != other.clueNumber) {
        	System.out.println("clueNumber");
            return false;
        }

        if (down != other.down) {
        	System.out.println("down");
            return false;
        }

        if (responder == null) {
            if (other.responder != null) {
                return false;
            }
        } else if (!responder.equals(other.responder)) {
            return false;
        }

        if (response != other.response) {
        	System.out.println("response");
            return false;
        }

        if (solution != other.solution) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (across ? 1231 : 1237);
        result = (prime * result) + (cheated ? 1231 : 1237);
        result = (prime * result) + clueNumber;
        result = (prime * result) + (down ? 1231 : 1237);
        result = (prime * result) +
            ((responder == null) ? 0 : responder.hashCode());
        result = (prime * result) + response;
        result = (prime * result) + solution;

        return result;
    }

    @Override
    public String toString() {
        return this.clueNumber + this.solution + " ";
    }
}
