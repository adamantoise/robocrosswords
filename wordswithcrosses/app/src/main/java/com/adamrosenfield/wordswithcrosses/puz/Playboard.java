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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.SparseArray;

import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper.SolveState;

public class Playboard {

    private SparseArray<Position> acrossWordStarts = new SparseArray<>();
    private SparseArray<Position> downWordStarts = new SparseArray<>();
    private MovementStrategy movementStrategy = MovementStrategy.MOVE_NEXT_ON_AXIS;
    private Position highlightLetter = new Position(0, 0);
    private Puzzle puzzle;
    private long puzzleId = -1;
    private Box[][] boxes;
    private boolean across = true;
    private boolean showErrors;
    private boolean skipCompletedLetters;
    private OnBoardChangedListener onBoardChangedListener;

    public Playboard(Puzzle puzzle, long puzzleId, MovementStrategy movementStrategy) {
        this(puzzle, puzzleId);
        this.movementStrategy = movementStrategy;
    }

    public Playboard(Puzzle puzzle, long puzzleId) {
        this.puzzle = puzzle;
        this.puzzleId = puzzleId;
        this.highlightLetter = new Position(0, 0);
        this.boxes = new Box[puzzle.getBoxes().length][puzzle.getBoxes()[0].length];

        for (int r = 0; r < puzzle.getBoxes().length; r++) {
            for (int c = 0; c < puzzle.getBoxes()[r].length; c++) {
                boxes[r][c] = puzzle.getBoxes()[r][c];

                if (boxes[r][c] != null &&
                    boxes[r][c].isAcross() &&
                    boxes[r][c].getClueNumber() != 0)
                {
                    acrossWordStarts.put(boxes[r][c].getClueNumber(), new Position(c, r));
                }

                if (boxes[r][c] != null &&
                    boxes[r][c].isDown() &&
                    boxes[r][c].getClueNumber() != 0)
                {
                    downWordStarts.put(boxes[r][c].getClueNumber(), new Position(c, r));
                }
            }
        }

        if (boxes[0][0] == null) {
            moveRight(false);
        }
    }

    public int getWidth() {
        return puzzle.getWidth();
    }

    public int getHeight() {
        return puzzle.getHeight();
    }

    public void setAcross(boolean across) {
        this.across = across;
    }

    public boolean isAcross() {
        return across;
    }

    public Clue[] getAcrossClues() {
        Clue[] clues = new Clue[puzzle.getAcrossClues().length];

        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getAcrossClues()[i];
            clues[i].number = puzzle.getAcrossCluesLookup()[i];
        }

        return clues;
    }

    public Box[][] getBoxes() {
        return boxes;
    }

    public Clue getClue() {
        Clue c = new Clue();

        Position start = getCurrentWordStart();
        if (boxes[start.down][start.across] == null) {
            return c;
        }

        c.number = boxes[start.down][start.across].getClueNumber();
        c.hint = this.across ? puzzle.findAcrossClue(c.number) : puzzle.findDownClue(c.number);

        return c;
    }

    public Box getCurrentBox() {
        return boxes[highlightLetter.down][highlightLetter.across];
    }

    /** Returns the 0 based index of the current clue based on the current across or down state
     *
     * @return index of the across or down clue based on the current state
     */
    public int getCurrentClueIndex() {
        Clue c = this.getClue();

        if (across) {
            return Arrays.binarySearch(this.puzzle.getAcrossCluesLookup(), c.number);
        } else {
            return Arrays.binarySearch(this.puzzle.getDownCluesLookup(), c.number);
        }
    }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = getCurrentWordStart();
        w.across = across;
        w.length = getWordRange();

        return w;
    }

    public Box[] getCurrentWordBoxes() {
        Word currentWord = getCurrentWord();
        Box[] result = new Box[currentWord.length];

        int across = currentWord.start.across;
        int down = currentWord.start.down;

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (currentWord.across) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = this.boxes[newDown][newAcross];
        }

        return result;
    }

    public Position[] getCurrentWordPositions() {
        Word currentWord = this.getCurrentWord();
        Position[] result = new Position[currentWord.length];
        int across = currentWord.start.across;
        int down = currentWord.start.down;

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (currentWord.across) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = new Position(newAcross, newDown);
        }

        return result;
    }

    public Position getCurrentWordStart() {
        int row = highlightLetter.down;
        int col = highlightLetter.across;
        if (isAcross()) {
            while (col > 0 && boxes[row][col] != null && boxes[row][col - 1] != null) {
                col--;
            }

            return new Position(col, row);
        } else {
            while (row > 0 && boxes[row][col] != null && boxes[row - 1][col] != null) {
                row--;
            }

            return new Position(col, row);
        }
    }

    public Clue[] getDownClues() {
        Clue[] clues = new Clue[puzzle.getDownClues().length];

        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getDownClues()[i];
            clues[i].number = puzzle.getDownCluesLookup()[i];
        }

        return clues;
    }

    public SolveState getSolveState() {
        SolveState solveState = new SolveState();
        solveState.position = new Position(getHighlightLetter());
        solveState.isOrientationAcross = isAcross();

        return solveState;
    }

    public void setSolveState(SolveState solveState) {
        setHighlightLetter(solveState.position);
        setAcross(solveState.isOrientationAcross);
    }

    public Word setHighlightLetter(Position highlightLetter) {
        Word w = getCurrentWord();

        if (highlightLetter.equals(this.highlightLetter)) {
            this.toggleDirection();
        } else {
            if ((highlightLetter.down >= 0) &&
                (highlightLetter.down < boxes.length) &&
                (highlightLetter.across >= 0) &&
                (highlightLetter.across < boxes[highlightLetter.down].length) &&
                (boxes[highlightLetter.down][highlightLetter.across] != null))
            {
                this.highlightLetter = highlightLetter;
            }
        }

        return w;
    }

    public Position getHighlightLetter() {
        return highlightLetter;
    }

    public void setMovementStrategy(MovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }

    public Puzzle getPuzzle() {
        return this.puzzle;
    }

    public long getPuzzleID() {
        return puzzleId;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public boolean isShowErrors() {
        return this.showErrors;
    }

    public void toggleShowErrors() {
        showErrors = !showErrors;
    }

    public void setSkipCompletedLetters(boolean skipCompletedLetters) {
        this.skipCompletedLetters = skipCompletedLetters;
    }

    public boolean isSkipCompletedLetters() {
        return skipCompletedLetters;
    }

    public void setOnBoardChangedListener(OnBoardChangedListener listener) {
        onBoardChangedListener = listener;
    }

    public Box[] getWordBoxes(int number, boolean isAcross) {
        Position start = isAcross ? this.acrossWordStarts.get(number) : this.downWordStarts.get(number);
        int range = this.getWordRange(start, isAcross);
        int across = start.across;
        int down = start.down;
        Box[] result = new Box[range];

        for (int i = 0; i < result.length; i++) {
            int newAcross = across;
            int newDown = down;

            if (isAcross) {
                newAcross += i;
            } else {
                newDown += i;
            }

            result[i] = boxes[newDown][newAcross];
        }

        return result;
    }

    public int getWordRange(Position start, boolean across) {
        if (across) {
            int col = start.across;
            while (col < boxes[start.down].length && boxes[start.down][col] != null) {
                col++;
            }

            return col - start.across;
        } else {
            int row = start.down;
            while (row < boxes.length && boxes[row][start.across] != null) {
                row++;
            }

            return row - start.down;
        }
    }

    public int getWordRange() {
        return getWordRange(getCurrentWordStart(), isAcross());
    }

    /**
     * Handler for the backspace key.  Uses the following algorithm:
     * -If current box is empty, move back one character.  If not, stay still.
     * -Delete the letter in the current box.
     */
    public Word deleteLetter() {
        Box currentBox = boxes[highlightLetter.down][highlightLetter.across];
        Word wordToReturn = getCurrentWord();

        if (currentBox.getResponse() == ' ') {
            wordToReturn = previousLetter();
            currentBox = boxes[highlightLetter.down][highlightLetter.across];
        }

        currentBox.setResponse(' ');
        onBoardChanged();

        return wordToReturn;
    }

    public void jumpTo(int clueIndex, boolean across) {

        if (across) {
            if (clueIndex >= 0 && clueIndex < puzzle.getAcrossCluesLookup().length) {
                highlightLetter = acrossWordStarts.get(puzzle.getAcrossCluesLookup()[clueIndex]);
                this.across = across;
            }
        } else {
            if (clueIndex >= 0 && clueIndex < puzzle.getDownCluesLookup().length) {
                highlightLetter = downWordStarts.get(puzzle.getDownCluesLookup()[clueIndex]);
                this.across = across;
            }
        }
    }

    public Word moveLeft() {
        return moveLeft(false);
    }

    public Word moveLeft(boolean skipCompleted) {
        Word w = getCurrentWord();

        Position newPos = moveLeft(getHighlightLetter(), skipCompleted);
        this.setHighlightLetter(newPos);

        return w;
    }

    private Position moveLeft(Position original, boolean skipCompleted) {
        Position lastValid = original;
        Position current = original;
        while (true) {
            if (current.across <= 0) {
                return lastValid;
            }

            current = new Position(current.across - 1, current.down);
            Box box = boxes[current.down][current.across];

            if (box != null) {
                if (!skipCurrentBox(box, skipCompleted)) {
                    return current;
                }

                lastValid = current;
            }
        }
    }

    public Word moveRight() {
        return moveRight(false);
    }

    public Word moveRight(boolean skipCompleted) {
        Word w = getCurrentWord();

        Position newPos = moveRight(getHighlightLetter(), skipCompleted);
        setHighlightLetter(newPos);

        return w;
    }

    private Position moveRight(Position original, boolean skipCompleted) {
        Position lastValid = original;
        Position current = original;
        while (true) {
            if (current.across >= boxes[current.down].length - 1) {
                return lastValid;
            }

            current = new Position(current.across + 1, current.down);
            Box box = boxes[current.down][current.across];

            if (box != null) {
                if (!skipCurrentBox(box, skipCompleted)) {
                    return current;
                }

                lastValid = current;
            }
        }
    }

    public Word moveUp() {
        return moveUp(false);
    }

    public Word moveUp(boolean skipCompleted) {
        Word w = getCurrentWord();

        Position newPos = moveUp(getHighlightLetter(), skipCompleted);
        setHighlightLetter(newPos);

        return w;
    }

    private Position moveUp(Position original, boolean skipCompleted) {
        Position lastValid = original;
        Position current = original;
        while (true) {
            if (current.down <= 0) {
                return lastValid;
            }

            current = new Position(current.across, current.down - 1);
            Box box = boxes[current.down][current.across];

            if (box != null) {
                if (!skipCurrentBox(box, skipCompleted)) {
                    return current;
                }

                lastValid = current;
            }
        }
    }

    public Word moveDown() {
        return moveDown(false);
    }

    public Word moveDown(boolean skipCompleted) {
        Word w = getCurrentWord();

        Position newPos = moveDown(getHighlightLetter(), skipCompleted);
        setHighlightLetter(newPos);

        return w;
    }

    private Position moveDown(Position original, boolean skipCompleted) {
        Position lastValid = original;
        Position current = original;
        while (true) {
            if (current.down >= boxes.length - 1) {
                return lastValid;
            }

            current = new Position(current.across, current.down + 1);
            Box box = boxes[current.down][current.across];

            if (box != null) {
                if (!skipCurrentBox(box, skipCompleted)) {
                    return current;
                }

                lastValid = current;
            }
        }
    }

    public Word nextLetter(boolean skipCompletedLetters) {
        return movementStrategy.move(this, skipCompletedLetters);
    }

    public Word nextLetter() {
        return nextLetter(skipCompletedLetters);
    }

    public Word nextWord() {
        Word previous = getCurrentWord();

        Position p = getHighlightLetter();

        int newAcross = p.across;
        int newDown = p.down;

        if (previous.across) {
            newAcross = (previous.start.across + previous.length) - 1;
        } else {
            newDown = (previous.start.down + previous.length) - 1;
        }

        Position newPos = new Position(newAcross, newDown);

        if (!newPos.equals(p)) {
            setHighlightLetter(newPos);
        }

        nextLetter();

        return previous;
    }

    public Word playLetter(char letter) {
        Box b = boxes[highlightLetter.down][highlightLetter.across];

        if (b == null) {
            return null;
        }

        b.setResponse(letter);
        onBoardChanged();

        return nextLetter();
    }

    public Word previousLetter() {
        return movementStrategy.back(this);
    }

    public Word previousWord() {
        Word previous = getCurrentWord();

        Position p = getHighlightLetter();

        int newAcross = p.across;
        int newDown = p.down;

        if (previous.across) {
            newAcross = previous.start.across - 1;
        } else {
            newDown = previous.start.down - 1;
        }

        setHighlightLetter(new Position(newAcross, newDown));
        previousLetter();

        Word current = getCurrentWord();
        setHighlightLetter(new Position(current.start.across, current.start.down));

        return previous;
    }

    public Position revealLetter() {
        Box b = boxes[highlightLetter.down][highlightLetter.across];

        if ((b != null) && (b.getSolution() != b.getResponse())) {
            b.setCheated(true);
            b.setResponse(b.getSolution());
            onBoardChanged();

            return highlightLetter;
        }

        return null;
    }

    public List<Position> revealPuzzle() {
        ArrayList<Position> changes = new ArrayList<>();

        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                Box b = boxes[r][c];

                if ((b != null) && (b.getSolution() != b.getResponse())) {
                    b.setCheated(true);
                    b.setResponse(b.getSolution());
                    changes.add(new Position(c, r));
                }
            }
        }

        onBoardChanged();

        return changes;
    }

    public List<Position> revealWord() {
        ArrayList<Position> changes = new ArrayList<>();
        Position oldHighlight = this.highlightLetter;
        Word w = getCurrentWord();
        this.highlightLetter = w.start;

        for (int i = 0; i < w.length; i++) {
            Position p = revealLetter();

            if (p != null) {
                changes.add(p);
            }

            nextLetter(false);
        }

        this.highlightLetter = oldHighlight;

        onBoardChanged();

        return changes;
    }

    public boolean skipCurrentBox(Box b, boolean skipCompleted) {
        return skipCompleted &&
               (b.getResponse() != ' ') &&
               (!isShowErrors() || b.getResponse() == b.getSolution());
    }

    public Word toggleDirection() {
        Word w = getCurrentWord();
        across = !across;

        return w;
    }

    private void onBoardChanged() {
        if (onBoardChangedListener != null) {
            onBoardChangedListener.onBoardChanged();
        }
    }

    public static class Clue {

        public String hint;
        public int number;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final Clue other = (Clue) obj;

            if ((this.hint == null) ? (other.hint != null) : (!this.hint.equals(other.hint))) {
                return false;
            }

            if (this.number != other.number) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return this.number;
        }

        @Override
        public String toString() {
            return number + ". " + hint;
        }
    }

    public static class Position {

        public int across;
        public int down;

        protected Position(){

        }

        public Position(int across, int down) {
            this.down = down;
            this.across = across;
        }

        public Position(Position p) {
            across = p.across;
            down = p.down;
        }

        @Override
        public boolean equals(Object o) {
            if ((o == null) || (o.getClass() != this.getClass())) {
                return false;
            }

            Position p = (Position) o;

            return ((p.down == this.down) && (p.across == this.across));
        }

        @Override
        public int hashCode() {
            return this.across ^ this.down;
        }

        @Override
        public String toString() {
            return "[" + this.across + " x " + this.down + "]";
        }
    }

    public static class Word {

        public Position start;
        public boolean across;
        public int length;

        public boolean checkInWord(int across, int down) {
            int ranging = this.across ? across : down;
            boolean offRanging = this.across ? (down == start.down) : (across == start.across);

            int startPos = this.across ? start.across : start.down;

            return (offRanging && (startPos <= ranging) && ((startPos + length) > ranging));
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != Word.class) {
                return false;
            }

            Word check = (Word) o;

            return check.start.equals(this.start) && (check.across == this.across) && (check.length == this.length);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = (29 * hash) + ((this.start != null) ? this.start.hashCode() : 0);
            hash = (29 * hash) + (this.across ? 1 : 0);
            hash = (29 * hash) + this.length;

            return hash;
        }
    }

    public interface OnBoardChangedListener {
        public void onBoardChanged();
    }
}
