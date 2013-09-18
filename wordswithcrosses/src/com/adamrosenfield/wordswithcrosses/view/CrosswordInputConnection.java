/**
 * This file is part of Words With Crosses.
 *
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

package com.adamrosenfield.wordswithcrosses.view;

import android.view.inputmethod.BaseInputConnection;
import android.view.KeyEvent;
import android.view.View;

/**
 * Helper class for ensuring that the delete key functions properly on certain
 * native soft keyboards, see http://stackoverflow.com/q/4886858/9530
 */
public class CrosswordInputConnection extends BaseInputConnection
{
    public CrosswordInputConnection(View targetView)
    {
        super(targetView, false);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength)
    {
        // Magic: in latest Android, deleteSurroundingText(1, 0) will be
        // called for backspace
        if (beforeLength == 1 && afterLength == 0)
        {
            // Backspace
            sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }

        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}
