/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2015 Adam Rosenfield
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

import java.util.Arrays;

import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;

public class MovementStrategyNextClue extends MovementStrategy {

    @Override
    public String toString() {
        return "MOVE_NEXT_CLUE";
    }

    /**
     * Moves to the word corresponding to the next clue.  If the current word
     * is the last across word, then this moves to the first down word, and if
     * it is the last down word, it moves to the first across word.  Returns
     * true if the first letter of the new clue is blank.
     */
    private boolean moveToNextWord(Playboard board, boolean skipCompletedLetters) {
        Word w = board.getCurrentWord();
        int currentClueNumber = board.getBoxes()[w.start.down][w.start.across].getClueNumber();
        int nextClueIndex;
        boolean nextClueAcross;
        Puzzle puz = board.getPuzzle();
        Integer[] cluesLookup = w.across ? puz.getAcrossCluesLookup() : puz.getDownCluesLookup();
        if (currentClueNumber == cluesLookup[cluesLookup.length - 1]) {
            // At end of clues - move to first clue of other type.
            nextClueIndex = 0;
            nextClueAcross = !w.across;
        } else {
            nextClueIndex = Arrays.binarySearch(cluesLookup, currentClueNumber) + 1;
            nextClueAcross = w.across;
        }
        board.jumpTo(nextClueIndex, nextClueAcross);
        return !board.skipCurrentBox(board.getCurrentBox(), skipCompletedLetters);
    }

    /**
     * Moves to the last letter of the word corresponding to the previous clue.
     * Does nothing if the current word is the first across or first down clue.
     */
    private void moveToPreviousWord(Playboard board) {
        Word w = board.getCurrentWord();
        int currentClueNumber = board.getBoxes()[w.start.down][w.start.across].getClueNumber();
        int previousClueIndex;
        Puzzle puz = board.getPuzzle();
        Integer[] cluesLookup = w.across ? puz.getAcrossCluesLookup() : puz.getDownCluesLookup();
        if (currentClueNumber == cluesLookup[0]) {
            // At beginning of grid - do nothing.
            return;
        } else {
            previousClueIndex = Arrays.binarySearch(cluesLookup, currentClueNumber) - 1;
        }
        board.jumpTo(previousClueIndex, w.across);

        // Move to last letter.
        w = board.getCurrentWord();
        Position newPos;
        if (w.across) {
            newPos = new Position(w.start.across + w.length - 1, w.start.down);
        } else {
            newPos = new Position(w.start.across, w.start.down + w.length - 1);
        }
        board.setHighlightLetter(newPos);
    }

    /**
     * Moves to the next blank letter in this clue, starting at position p.
     * Returns true if such a letter was found; returns false if the clue has
     * already been filled.
     */
    private boolean moveToNextBlank(Playboard board, Position p, boolean skipCompletedLetters) {
        Word w = board.getCurrentWord();
        Box[] wordBoxes = board.getCurrentWordBoxes();

        if (w.across) {
            for (int x = p.across; x < w.start.across + w.length; x++) {
                if (!board.skipCurrentBox(wordBoxes[x - w.start.across], skipCompletedLetters)) {
                    board.setHighlightLetter(new Position(x, p.down));
                    return true;
                }
            }
        } else {
            for (int y = p.down; y < w.start.down + w.length; y++) {
                if (!board.skipCurrentBox(wordBoxes[y - w.start.down], skipCompletedLetters)) {
                    board.setHighlightLetter(new Position(p.across, y));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Word move(Playboard board, boolean skipCompletedLetters) {
        Position p = board.getHighlightLetter();
        Word w = board.getCurrentWord();

        if ((!board.isShowErrors() && board.getPuzzle().getPercentFilled() == 100) ||
                board.getPuzzle().isSolved()) {
            // Puzzle complete - don't move.
            return w;
        }

        Position nextPos;
        if (isWordEnd(p, w)) {
            // At end of a word - move to the next one and continue.
            if (moveToNextWord(board, skipCompletedLetters)) {
                return w;
            }
            nextPos = board.getHighlightLetter();
        } else {
            // In middle of word - move to the next unfilled letter.
            nextPos = w.across ? new Position(p.across + 1, p.down) : new Position(p.across, p.down + 1);
        }
        while (!(moveToNextBlank(board, nextPos, skipCompletedLetters))) {
            if (moveToNextWord(board, skipCompletedLetters)) {
                break;
            }
            nextPos = board.getHighlightLetter();
        }
        return w;
    }

    @Override
    public Word back(Playboard board) {
        Position p = board.getHighlightLetter();
        Word w = board.getCurrentWord();
        if ((w.across && p.across == w.start.across)
                || (!w.across && p.down == w.start.down)) {
            // At beginning of word - move to previous clue.
            moveToPreviousWord(board);
        } else {
            // In middle of word - just move back one character.
            MOVE_NEXT_ON_AXIS.back(board);
        }
        return w;
    }


}

