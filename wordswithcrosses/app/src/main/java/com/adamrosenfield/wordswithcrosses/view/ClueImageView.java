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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView.ClickListener;

public class ClueImageView extends TouchImageView
{
    private float renderScale = 1.0f;

    private ClickListener clickListener;

    private boolean useNativeKeyboard = false;

    public ClueImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setCanScale(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void fitToHeight()
    {
        renderScale = (float)getHeight() / PlayboardRenderer.BOX_SIZE;
        render();
    }

    public void setClickListener(ClickListener listener)
    {
        clickListener = listener;
    }

    public void setUseNativeKeyboard(boolean useNativeKeyboard)
    {
        this.useNativeKeyboard = useNativeKeyboard;
    }

    @Override
    protected void onClick(PointF pos)
    {
        if (clickListener == null)
        {
            return;
        }

        float boxSize = PlayboardRenderer.BOX_SIZE * renderScale;
        int x = (int)(pos.x / boxSize);
        int y = (int)(pos.y / boxSize);
        Position crosswordPos = new Position(x, y);

        clickListener.onClick(crosswordPos);
    }

    public void render()
    {
        Bitmap bitmap = WordsWithCrossesApplication.RENDERER.drawWord(renderScale);
        setImageBitmap(bitmap);
    }

    // Special sauce for ensuring that the delete key functions properly on
    // certain native soft keyboards
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        outAttrs.actionLabel = null;
        outAttrs.inputType = InputType.TYPE_NULL;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NONE;
        return new CrosswordInputConnection(this);
    }

    @Override
    public boolean onCheckIsTextEditor()
    {
        return useNativeKeyboard;
    }
}
