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

package com.adamrosenfield.wordswithcrosses.puz;

public class Box {

    private boolean across;
    private boolean cheated;
    private boolean down;
    private boolean circled;
    private char response = ' ';
    private char solution;
    private int clueNumber;

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

        if (isAcross() != other.isAcross()) {
            return false;
        }

        if (isCheated() != other.isCheated()) {
            return false;
        }

        if (getClueNumber() != other.getClueNumber()) {
            return false;
        }

        if (isDown() != other.isDown()) {
            return false;
        }

        if (isCircled() != other.isCircled()) {
            return false;
        }

        if (getResponse() != other.getResponse()) {
            return false;
        }

        if (getSolution() != other.getSolution()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (isAcross() ? 1231 : 1237);
        result = (prime * result) + (isCheated() ? 1231 : 1237);
        result = (prime * result) + getClueNumber();
        result = (prime * result) + (isDown() ? 1231 : 1237);
        result = (prime * result) + (isCircled() ? 1231 : 1237);
        result = (prime * result) + getResponse();
        result = (prime * result) + getSolution();

        return result;
    }

    @Override
    public String toString() {
        return this.getClueNumber() + this.getSolution() + " ";
    }

    /**
     * @return the across
     */
    public boolean isAcross() {
        return across;
    }

    /**
     * @param across the across to set
     */
    public void setAcross(boolean across) {
        this.across = across;
    }

    /**
     * @return the cheated
     */
    public boolean isCheated() {
        return cheated;
    }

    /**
     * @param cheated the cheated to set
     */
    public void setCheated(boolean cheated) {
        this.cheated = cheated;
    }

    /**
     * @return the down
     */
    public boolean isDown() {
        return down;
    }

    /**
     * @param down the down to set
     */
    public void setDown(boolean down) {
        this.down = down;
    }

    /**
     * @return if the box is circled
     */
    public boolean isCircled() {
        return circled;
    }

    /**
     * @param circled the circled to set
     */
    public void setCircled(boolean circled) {
        this.circled = circled;
    }

    /**
     * @return the response
     */
    public char getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(char response) {
        this.response = response;
    }

    /**
     * @return the solution
     */
    public char getSolution() {
        return solution;
    }

    /**
     * @param solution the solution to set
     */
    public void setSolution(char solution) {
        this.solution = solution;
    }

    /**
     * @return the clueNumber
     */
    public int getClueNumber() {
        return clueNumber;
    }

    /**
     * @param clueNumber the clueNumber to set
     */
    public void setClueNumber(int clueNumber) {
        this.clueNumber = clueNumber;
    }
}
