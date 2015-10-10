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

import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;

public class MovementStrategyNextOnAxis extends MovementStrategy {

    @Override
    public String toString() {
        return "MOVE_NEXT_ON_AXIS";
    }

    @Override
    public Word move(Playboard board, boolean skipCompletedLetters) {
        if (board.isAcross()) {
            return board.moveRight(skipCompletedLetters);
        } else {
            return board.moveDown(skipCompletedLetters);
        }
    }

    @Override
    public Word back(Playboard board) {
        if (board.isAcross()) {
            return board.moveLeft();
        } else {
            return board.moveUp(false);
        }
    }

}


