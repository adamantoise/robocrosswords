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
        Position p = board.getHighlightLetter();
        Word w = board.getCurrentWord();

        if (!isWordEnd(p, w)) {
            MOVE_NEXT_ON_AXIS.move(board, skipCompletedLetters);

            Word newWord = board.getCurrentWord();
            if (!newWord.equals(w)) {
                // If moving along the same axis put the cursor into a
                // different word, revert back to our previous position.
                board.setHighlightLetter(p);
            } else if (skipCompletedLetters) {
                // If we we ended up in a square which ought to have been
                // skipped, it means we're in the last word in the current row
                // or column, and we're now in the last cell of that word.
                // To ensure the behavior is consistent in both of the cases
                // where we're in the last word vs. not, revert back to our
                // previous position.
                Position current = board.getHighlightLetter();
                if (board.skipCurrentBox(current, skipCompletedLetters)) {
                    board.setHighlightLetter(p);
                }
            }
        }

        return w;
    }

    @Override
    public Word back(Playboard board) {
        Word w = board.getCurrentWord();
        Position p = board.getHighlightLetter();
        if (!p.equals(w.start)) {
            MOVE_NEXT_ON_AXIS.back(board);
        }
        return w;
    }

}
