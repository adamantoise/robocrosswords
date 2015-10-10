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

import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;

public class MovementStrategyParallelWord extends MovementStrategy {

    @Override
    public String toString() {
        return "MOVE_PARALLEL_WORD";
    }

    @Override
    public Word move(Playboard board, boolean skipCompletedLetters) {
        Word w = board.getCurrentWord();
        Position p = board.getHighlightLetter();

        if (isWordEnd(p, w)) {
            //board.setHighlightLetter(w.start);
            Word newWord;
            if (w.across) {
                board.moveDown();
                while (board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes().length) {
                    board.moveDown();
                }
                if (board.getClue().hint == null) {
                    board.toggleDirection();
                }
                newWord = board.getCurrentWord();
            } else {
                board.moveRight();
                while (board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes()[0].length) {
                    board.moveRight();
                }
                if (board.getClue().hint == null) {
                    board.toggleDirection();
                }
                newWord = board.getCurrentWord();

            }
            if (!newWord.equals(w)) {
                board.setHighlightLetter(newWord.start);
                board.setAcross(w.across);
            }

        } else {
            MOVE_NEXT_ON_AXIS.move(board,
                    skipCompletedLetters);
            Word newWord = board.getCurrentWord();
            if (!newWord.equals(w)) {
                Position end = new Position(w.start.across + (w.across ? w.length - 1: 0),
                        w.start.down + (w.across? 0 : w.length - 1));
                board.setHighlightLetter(end);
                this.move(board, skipCompletedLetters);
            }
        }

        return w;
    }

    @Override
    public Word back(Playboard board) {
        Word w = board.getCurrentWord();
        Position p = board.getHighlightLetter();
        if ((w.across && p.across == w.start.across)
                || (!w.across && p.down == w.start.down)) {
            //board.setHighlightLetter(w.start);
            Word newWord;
            Position lastPos = null;
            if (w.across) {
                board.moveUp();
                while (!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes().length) {
                    lastPos = board.getHighlightLetter();
                    board.moveUp();

                }
                if (board.getClue().hint == null) {
                    board.toggleDirection();
                }
                newWord = board.getCurrentWord();
            } else {
                board.moveLeft();
                while (!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes()[0].length) {
                    lastPos = board.getHighlightLetter();
                    board.moveLeft();
                }
                if (board.getClue().hint == null) {
                    board.toggleDirection();
                }
                newWord = board.getCurrentWord();

            }
            if (!newWord.equals(w)) {

                Position newPos = new Position(newWord.start.across + (newWord.across ? newWord.length - 1 : 0), newWord.start.down + (newWord.across ? 0 : newWord.length - 1));

                board.setHighlightLetter(newPos);
                board.setAcross(w.across);
            }

        } else {
            Word newWord = MOVE_NEXT_ON_AXIS.back(board);
            if (!newWord.equals(w)) {
                board.setHighlightLetter(newWord.start);
            }
        }

        return w;
    }

}
