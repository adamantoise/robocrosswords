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

public class MovementStrategyStopOnEnd extends MovementStrategy {

    @Override
    public String toString() {
        return "STOP_ON_END";
    }

    @Override
    public Word move(Playboard board, boolean skipCompletedLetters) {
        // This is overly complex, but I am trying to save calls to heavy
        // methods on the board.

        Position p = board.getHighlightLetter();
        Word w = board.getCurrentWord();
        if (isWordEnd(p, w)) {
            return w;
        } else {
            MOVE_NEXT_ON_AXIS.move(board,skipCompletedLetters);
            Word newWord = board.getCurrentWord();
            if (newWord.equals(w)) {
                return w;
            } else {
                board.setHighlightLetter(p);
                return w;
            }
        }
    }

    @Override
    public Word back(Playboard board) {
        Word w = board.getCurrentWord();
        Position p = board.getHighlightLetter();
        if(!p.equals(w.start)){
            MOVE_NEXT_ON_AXIS.back(board);
        }
        return w;
    }

}
