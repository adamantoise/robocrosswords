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

    private final SparseArray<Position> acrossWordStarts = new SparseArray<>();
    private final SparseArray<Position> downWordStarts = new SparseArray<>();
    private MovementStrategy movementStrategy = MovementStrategy.MOVE_NEXT_ON_AXIS;
    private Position cursorPosition = new Position(0, 0);
    private final Puzzle puzzle;
    private final long puzzleId;
    private final Box[][] boxes;
    private boolean isCursorAcross = true;
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

    public void setIsCursorAcross(boolean isCursorAcross) {
        this.isCursorAcross = isCursorAcross;
    }

    public boolean isCursorAcross() {
        return isCursorAcross;
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

    public Box getBox(int row, int column) {
        return boxes[row][column];
    }

    public Box getBox(Position p) {
        return boxes[p.down][p.across];
    }

    public Clue getClue() {
        Clue c = new Clue();

        Position start = getCurrentWordStart();
        if (boxes[start.down][start.across] == null) {
            return c;
        }

        c.number = boxes[start.down][start.across].getClueNumber();
        c.hint = this.isCursorAcross ? puzzle.findAcrossClue(c.number) : puzzle.findDownClue(c.number);

        return c;
    }

    private int getClueNumberForWordAtPos(int row, int col, boolean isAcross) {
        Position wordStart = getWordStart(row, col, isAcross);
        return boxes[wordStart.down][wordStart.across].getClueNumber();
    }

    public Box getCurrentBox() {
        return boxes[cursorPosition.down][cursorPosition.across];
    }

    /** Returns the 0 based index of the current clue based on the current across or down state
     *
     * @return index of the across or down clue based on the current state
     */
    public int getCurrentClueIndex() {
        Clue c = this.getClue();

        if (isCursorAcross) {
            return Arrays.binarySearch(this.puzzle.getAcrossCluesLookup(), c.number);
        } else {
            return Arrays.binarySearch(this.puzzle.getDownCluesLookup(), c.number);
        }
    }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = getCurrentWordStart();
        w.across = isCursorAcross;
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
        return getWordStart(cursorPosition, isCursorAcross);
    }

    private Position getWordStart(Position pos, boolean isAcross) {
        return getWordStart(pos.down, pos.across, isAcross);
    }

    private Position getWordStart(int row, int col, boolean isAcross) {
        if (isAcross) {
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

    private Position getWordEnd(Position pos, boolean isAcross) {
        return getWordEnd(pos.down, pos.across, isAcross);
    }

    private Position getWordEnd(int row, int col, boolean isAcross) {
        if (isAcross) {
            int width = getWidth();
            while (col < width - 1 && boxes[row][col] != null && boxes[row][col + 1] != null) {
                col++;
            }

            return new Position(col, row);
        } else {
            int height = getHeight();
            while (row < height - 1 && boxes[row][col] != null && boxes[row + 1][col] != null) {
                row++;
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
        solveState.position = new Position(cursorPosition);
        solveState.isOrientationAcross = isCursorAcross;

        return solveState;
    }

    public void setSolveState(SolveState solveState) {
        setCursorPosition(solveState.position);
        setIsCursorAcross(solveState.isOrientationAcross);
    }

    public void setCursorPosition(Position pos) {
        if (pos.down >= 0 &&
            pos.down < getHeight() &&
            pos.across >= 0 &&
            pos.across < getWidth() &&
            boxes[pos.down][pos.across] != null)
        {
            cursorPosition = pos;
        }
    }

    public void setCursorPosition(PositionAndOrientation pos) {
        setCursorPosition(pos.pos);
        setIsCursorAcross(pos.isAcross);
    }

    public Position getCursorPosition() {
        return cursorPosition;
    }

    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    public void setMovementStrategy(MovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public long getPuzzleID() {
        return puzzleId;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public boolean isShowErrors() {
        return showErrors;
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
            int width = getWidth();
            while (col < width && boxes[start.down][col] != null) {
                col++;
            }

            return col - start.across;
        } else {
            int row = start.down;
            int height = getHeight();
            while (row < height && boxes[row][start.across] != null) {
                row++;
            }

            return row - start.down;
        }
    }

    public int getWordRange() {
        return getWordRange(getCurrentWordStart(), isCursorAcross());
    }

    /**
     * Handler for the backspace key.  Uses the following algorithm:
     * -If current box is empty, move back one character.  If not, stay still.
     * -Delete the letter in the current box.
     */
    public Word deleteLetter() {
        Box currentBox = boxes[cursorPosition.down][cursorPosition.across];
        Word wordToReturn = getCurrentWord();

        if (currentBox.getResponse() == ' ') {
            wordToReturn = moveToPreviousLetterStopAtEndOfWord();
            currentBox = boxes[cursorPosition.down][cursorPosition.across];
        }

        currentBox.setResponse(' ');
        onBoardChanged();

        return wordToReturn;
    }

    public void jumpTo(int clueIndex, boolean across) {

        if (across) {
            if (clueIndex >= 0 && clueIndex < puzzle.getAcrossCluesLookup().length) {
                cursorPosition = acrossWordStarts.get(puzzle.getAcrossCluesLookup()[clueIndex]);
                isCursorAcross = across;
            }
        } else {
            if (clueIndex >= 0 && clueIndex < puzzle.getDownCluesLookup().length) {
                cursorPosition = downWordStarts.get(puzzle.getDownCluesLookup()[clueIndex]);
                isCursorAcross = across;
            }
        }
    }

    public Word moveLeft() {
        return moveLeft(false);
    }

    public Word moveLeft(boolean skipCompleted) {
        return moveCursor(0, -1, skipCompleted, false);
    }

    public Word moveLeftStopAtEndOfWord(boolean skipCompleted) {
        return moveCursor(0, -1, skipCompleted, true);
    }

    public Word moveRight() {
        return moveRight(false);
    }

    public Word moveRight(boolean skipCompleted) {
        return moveCursor(0, 1, skipCompleted, false);
    }

    public Word moveRightStopAtEndOfWord(boolean skipCompleted) {
        return moveCursor(0, 1, skipCompleted, true);
    }

    public Word moveUp() {
        return moveUp(false);
    }

    public Word moveUp(boolean skipCompleted) {
        return moveCursor(-1, 0, skipCompleted, false);
    }

    public Word moveUpStopAtEndOfWord(boolean skipCompleted) {
        return moveCursor(-1, 0, skipCompleted, true);
    }

    public Word moveDown() {
        return moveDown(false);
    }

    public Word moveDown(boolean skipCompleted) {
        return moveCursor(1, 0, skipCompleted, false);
    }

    public Word moveDownStopAtEndOfWord(boolean skipCompleted) {
        return moveCursor(1, 0, skipCompleted, true);
    }

    public Word moveToPreviousLetterStopAtEndOfWord() {
        if (isCursorAcross) {
            return moveLeftStopAtEndOfWord(false);
        } else {
            return moveUpStopAtEndOfWord(false);
        }
    }

    private Word moveCursor(int dirRows, int dirCols, boolean skipCompleted, boolean stopAtEndOfWord) {
        Word w = getCurrentWord();

        Position newPos = getNextLetterInDirection(cursorPosition, dirRows, dirCols, skipCompleted, stopAtEndOfWord);
        setCursorPosition(newPos);

        return w;
    }

    /**
     * Returns the coordinates of the next cell in the given direction from the
     * given cell, optionally skipping over completed cells.  If we hit an edge
     * of the puzzle, the last non-empty cell we hit before then will be
     * returned (which might be completed, even if skipCompleted is true).
     */
    private Position getNextLetterInDirection(Position pos, int dirRows, int dirCols, boolean skipCompleted, boolean stopAtEndOfWord) {
        int lastValidRow = pos.down;
        int lastValidCol = pos.across;
        int row = lastValidRow;
        int col = lastValidCol;
        int width = getWidth();
        int height = getHeight();

        while (true) {
            row += dirRows;
            col += dirCols;
            if (row < 0 || row >= height || col < 0 || col >= width) {
                break;
            }

            Box box = boxes[row][col];
            if (box != null) {
                if (!shouldSkipLetter(box, skipCompleted)) {
                    return new Position(col, row);
                }

                lastValidRow = row;
                lastValidCol = col;
            } else if (stopAtEndOfWord) {
                break;
            }
        }

        return new Position(lastValidCol, lastValidRow);
    }

    public Word moveToNextLetter() {
        return moveToNextLetter(skipCompletedLetters);
    }

    public Word moveToNextLetter(boolean skipCompletedLetters) {
        return moveToNextLetter(skipCompletedLetters, movementStrategy);
    }

    public Word moveToNextLetterStopAtEndOfWord() {
        return moveToNextLetter(skipCompletedLetters, MovementStrategy.STOP_ON_END);
    }

    private Word moveToNextLetter(boolean skipCompletedLetters, MovementStrategy strategy) {
        Word previous = getCurrentWord();

        PositionAndOrientation newCursor = getNextLetterFromStrategy(cursorPosition, skipCompletedLetters, strategy);
        setCursorPosition(newCursor);

        return previous;
    }

    private PositionAndOrientation getNextLetterFromStrategy(Position pos, boolean skipCompletedLetters, MovementStrategy strategy) {
        switch (strategy) {
        case MOVE_NEXT_ON_AXIS:
            return getNextLetterMoveOnAxis(pos, skipCompletedLetters);

        case STOP_ON_END:
            return getNextLetterStopOnEnd(pos, skipCompletedLetters);

        case MOVE_NEXT_CLUE:
            return getNextLetterMoveNextClue(pos, skipCompletedLetters);

        case MOVE_PARALLEL_WORD:
            return getNextLetterMoveParallelWord(pos, skipCompletedLetters);
        }

        throw new RuntimeException("Unhandled MovementStrategy!");
    }

    private PositionAndOrientation getNextLetterMoveOnAxis(Position pos, boolean skipCompletedLetters) {
        Position newPos;

        if (isCursorAcross) {
            newPos = getNextLetterInDirection(pos, 0, 1, skipCompletedLetters, false);
            if (shouldSkipLetter(newPos, skipCompletedLetters)) {
                newPos = getNextLetterInDirection(pos, 0, 1, false, false);
            }
        } else {
            newPos = getNextLetterInDirection(pos, 1, 0, skipCompletedLetters, false);
            if (shouldSkipLetter(newPos, skipCompletedLetters)) {
                newPos = getNextLetterInDirection(pos, 1, 0, false, false);
            }
        }

        return new PositionAndOrientation(newPos, isCursorAcross);
    }

    private PositionAndOrientation getNextLetterStopOnEnd(Position pos, boolean skipCompletedLetters) {
        Position newPos;

        if (isCursorAcross) {
            newPos = getNextLetterInDirection(pos, 0, 1, skipCompletedLetters, true);
            if (shouldSkipLetter(newPos, skipCompletedLetters)) {
                newPos = getNextLetterInDirection(pos, 0, 1, false, true);
            }
        } else {
            newPos = getNextLetterInDirection(pos, 1, 0, skipCompletedLetters, true);
            if (shouldSkipLetter(newPos, skipCompletedLetters)) {
                newPos = getNextLetterInDirection(pos, 1, 0, false, true);
            }
        }

        return new PositionAndOrientation(newPos, isCursorAcross);
    }

    private PositionAndOrientation getNextLetterMoveNextClue(Position pos, boolean skipCompletedLetters) {
        int row = pos.down;
        int col = pos.across;
        int width = getWidth();
        int height = getHeight();
        boolean isAcross = isCursorAcross;

        while (true) {
            // Try advancing to the next letter within the current clue, if
            // any.  If we're at the end of the word, find the next clue.  If
            // we're at the end of the puzzle, switch directions.
            if (isAcross) {
                if (col + 1 < width && boxes[row][col + 1] != null) {
                    col++;
                } else {
                    int clue = getClueNumberForWordAtPos(row, col, isAcross);
                    int clueIndex = acrossWordStarts.indexOfKey(clue);
                    if (clueIndex < acrossWordStarts.size() - 1) {
                        Position nextClue = acrossWordStarts.valueAt(clueIndex + 1);
                        row = nextClue.down;
                        col = nextClue.across;
                    } else {
                        Position nextClue = downWordStarts.valueAt(0);
                        row = nextClue.down;
                        col = nextClue.across;
                        isAcross = !isAcross;
                    }
                }
            } else {
                if (row + 1 < height && boxes[row + 1][col] != null) {
                    row++;
                } else {
                    int clue = getClueNumberForWordAtPos(row, col, isAcross);
                    int clueIndex = downWordStarts.indexOfKey(clue);
                    if (clueIndex < downWordStarts.size() - 1) {
                        Position nextClue = downWordStarts.valueAt(clueIndex + 1);
                        row = nextClue.down;
                        col = nextClue.across;
                    } else {
                        Position nextClue = acrossWordStarts.valueAt(0);
                        row = nextClue.down;
                        col = nextClue.across;
                        isAcross = !isAcross;
                    }
                }
            }

            if (!shouldSkipLetter(boxes[row][col], skipCompletedLetters)) {
                return new PositionAndOrientation(row, col, isAcross);
            }

            // If we wrapped all the way around without finding an empty cell
            // (which can only happen if the puzzle has been filled and
            // skipCompletedLetters is true), just advance to the next letter
            // without skipping completed letters.
            if (row == pos.down && col == pos.across && isAcross == isCursorAcross) {
                return getNextLetterMoveNextClue(pos, false);
            }
        }
    }

    private PositionAndOrientation getNextLetterMoveParallelWord(Position pos, boolean skipCompletedLetters) {
        int row = pos.down;
        int col = pos.across;
        int width = getWidth();
        int height = getHeight();

        // First, try to move to the next letter within the current word
        while (true) {
            if (isCursorAcross) {
                if (col + 1 < width && boxes[row][col + 1] != null) {
                    col++;
                } else {
                    break;
                }
            } else {
                if (row + 1 < height && boxes[row + 1][col] != null) {
                    row++;
                } else {
                    break;
                }
            }

            if (!shouldSkipLetter(boxes[row][col], skipCompletedLetters)) {
                return new PositionAndOrientation(row, col, isCursorAcross);
            }
        }

        // Now, advance one row or column at a time, find the closest-matching
        // clue, and see if that clue has any candidate letters.
        Position initialWordStart = getWordStart(pos, isCursorAcross);
        Position initialWordEnd = getWordEnd(pos, isCursorAcross);
        int wordLength = (isCursorAcross ? initialWordEnd.across - initialWordStart.across + 1 : initialWordEnd.down - initialWordStart.down + 1);
        row = initialWordStart.down;
        col = initialWordStart.across;

        while (true) {
            // Advance to the next row or column.  If we're back to where we
            // started, then all of the candidate words we examined were full.
            // We could try finding a different, less-proper word, but it makes
            // more sense just to advance instead without skipping over filled
            // in letters.
            if (isCursorAcross) {
                row = (row + 1) % height;
                if (row == initialWordStart.down) {
                    return getNextLetterMoveParallelWord(pos, false);
                }
            } else {
                col = (col + 1) % width;
                if (col == initialWordStart.across) {
                    return getNextLetterMoveParallelWord(pos, false);
                }
            }

            // Find the best-fitting word in this row or column, where "best"
            // is defined as the one that has the most letters in common;
            // ties are broken by whichever word is closer to the start.
            int bestRowStart = -1;
            int bestColStart = -1;
            int bestRowEnd = -1;
            int bestColEnd = -1;
            int bestInCommon = -99999;
            if (isCursorAcross) {
                for (col = 0; col < width; col++) {
                    if (boxes[row][col] != null && (col == 0 || boxes[row][col - 1] == null)) {
                        Position wordEnd = getWordEnd(row, col, isCursorAcross);
                        int inCommon = Math.min(initialWordEnd.across - col, wordEnd.across - initialWordStart.across);
                        if (inCommon > bestInCommon ||
                            inCommon == bestInCommon && Math.abs(col - initialWordStart.across) < Math.abs(bestColStart - initialWordStart.across)) {
                            bestRowStart = row;
                            bestColStart = col;
                            bestRowEnd = row;
                            bestColEnd = wordEnd.across;
                            bestInCommon = inCommon;
                        }
                    }
                }
            } else {
                for (row = 0; row < height; row++) {
                    if (boxes[row][col] != null && (row == 0 || boxes[row - 1][col] == null)) {
                        Position wordEnd = getWordEnd(row, col, isCursorAcross);
                        int inCommon = Math.min(initialWordEnd.down - row, wordEnd.down - initialWordStart.down);
                        if (inCommon > bestInCommon ||
                            inCommon == bestInCommon && Math.abs(row - initialWordStart.down) < Math.abs(bestRowStart - initialWordStart.down)) {
                            bestRowStart = row;
                            bestColStart = col;
                            bestRowEnd = wordEnd.down;;
                            bestColEnd = col;
                            bestInCommon = inCommon;
                        }
                    }
                }
            }

            // Check the candidate word for a letter we can go to
            if (isCursorAcross) {
                for (col = bestColStart; col <= bestColEnd; col++) {
                    if (!shouldSkipLetter(boxes[row][col], skipCompletedLetters)) {
                        return new PositionAndOrientation(row, col, isCursorAcross);
                    }
                }
            } else {
                for (row = bestRowStart; row <= bestRowEnd; row++) {
                    if (!shouldSkipLetter(boxes[row][col], skipCompletedLetters)) {
                        return new PositionAndOrientation(row, col, isCursorAcross);
                    }
                }
            }
        }
    }

    public Word moveToNextWord(MovementStrategy strategy) {
        Word previous = getCurrentWord();

        int endOfWordRow = cursorPosition.down;
        int endOfWordCol = cursorPosition.across;

        if (previous.across) {
            endOfWordCol = previous.start.across + previous.length - 1;
        } else {
            endOfWordRow = previous.start.down + previous.length - 1;
        }

        Position endOfWord = new Position(endOfWordCol, endOfWordRow);
        PositionAndOrientation nextWordPos = getNextLetterFromStrategy(endOfWord, skipCompletedLetters, strategy);

        setCursorPosition(nextWordPos);

        return previous;
    }

    public Word playLetter(char letter) {
        Box b = boxes[cursorPosition.down][cursorPosition.across];

        if (b == null) {
            return null;
        }

        b.setResponse(letter);
        onBoardChanged();

        boolean skipCompleted = (skipCompletedLetters && letter != ' ');
        return moveToNextLetter(skipCompleted);
    }

    public Position revealLetter() {
        Box b = boxes[cursorPosition.down][cursorPosition.across];

        if ((b != null) && (b.getSolution() != b.getResponse())) {
            b.setCheated(true);
            b.setResponse(b.getSolution());
            onBoardChanged();

            return cursorPosition;
        }

        return null;
    }

    public List<Position> revealPuzzle() {
        ArrayList<Position> changes = new ArrayList<>();

        int width = getWidth();
        int height = getHeight();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
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
        Position oldCursor = cursorPosition;
        Word w = getCurrentWord();
        cursorPosition = w.start;

        for (int i = 0; i < w.length; i++) {
            Position p = revealLetter();

            if (p != null) {
                changes.add(p);
            }

            moveToNextLetter(false);
        }

        cursorPosition = oldCursor;

        onBoardChanged();

        return changes;
    }

    private boolean shouldSkipLetter(Position p, boolean skipCompleted) {
        Box box = boxes[p.down][p.across];
        return (box == null || shouldSkipLetter(box, skipCompleted));
    }

    private boolean shouldSkipLetter(Box b, boolean skipCompleted) {
        return skipCompleted &&
               (b.getResponse() != ' ') &&
               (!isShowErrors() || b.getResponse() == b.getSolution());
    }

    public void toggleDirection() {
        isCursorAcross = !isCursorAcross;
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
            return (across * 65537 + down);
        }

        @Override
        public String toString() {
            return "[" + across + " x " + down + "]";
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

    private static class PositionAndOrientation {
        public final Position pos;
        public final boolean isAcross;

        public PositionAndOrientation(Position pos, boolean isAcross) {
            this.pos = pos;
            this.isAcross = isAcross;
        }

        public PositionAndOrientation(int row, int col, boolean isAcross) {
            this.pos = new Position(col, row);
            this.isAcross = isAcross;
        }
    }

    public interface OnBoardChangedListener {
        public void onBoardChanged();
    }
}
