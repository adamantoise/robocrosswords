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

import java.util.Arrays;

import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;

public abstract class MovementStrategy {

    public static final MovementStrategy MOVE_NEXT_ON_AXIS = new MovementStrategyNextOnAxis();
    public static final MovementStrategy STOP_ON_END = new MovementStrategyStopOnEnd();
    public static final MovementStrategy MOVE_NEXT_CLUE = new MovementStrategyNextClue();
    public static final MovementStrategy MOVE_PARALLEL_WORD = new MovementStrategyParallelWord();

    public abstract Word move(Playboard board, boolean skipCompletedLetters);

    public abstract Word back(Playboard board);

    // Common helper methods for MovementStrategy implementations
    protected static boolean isLastWordInDirection(Playboard board, Word w) {
        return isLastWordInDirection(board.getBoxes(), w);
    }

    protected static boolean isLastWordInDirection(Box[][] boxes, Word w) {
        if (w.across) {
            return (w.start.down + 1 >= boxes.length);
        } else {
            return (w.start.across + 1 >= boxes[w.start.down].length);
        }
    }

    protected static boolean isWordEnd(Position p, Word w) {
        if (w.across) {
            return (p.across == w.start.across + w.length - 1);
        } else {
            return (p.down == w.start.down + w.length - 1);
        }
    }
}
