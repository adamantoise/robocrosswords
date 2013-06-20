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

package com.adamrosenfield.wordswithcrosses.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;


public class HighlightTextView extends TextView {

    private Paint blackPaint = new Paint();
    private Paint highlight = new Paint();
    private Path path = new Path();
    private DisplayMetrics metrics;

    {
        blackPaint.setColor(Color.BLACK);
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Style.FILL_AND_STROKE);
        highlight.setColor(Color.argb(100, 200, 191, 231));
        highlight.setStyle(Style.FILL_AND_STROKE);
    }

    public HighlightTextView(Context context, AttributeSet as) {
        super(context, as);
        metrics = new DisplayMetrics();

        WindowManager manager = (WindowManager) this.getContext()
                                                    .getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay()
               .getMetrics(metrics);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = this.getHeight();
        int width = this.getWidth();
        int center = height / 2;
        int topBreak = center - (int) (metrics.density * 10F);
        int bottomBreak = center + (int) (metrics.density * 10F);
        path.moveTo(width, 0F);
        path.lineTo(width, topBreak);
        path.lineTo(width - (metrics.density * 10), center);
        path.lineTo(width, bottomBreak);
        path.lineTo(width, height);
        path.lineTo(width + 1, height);
        path.lineTo(width + 1, -1);
        path.lineTo(width, 0);

        canvas.drawRect(0, 0, width, height, highlight);
        canvas.drawPath(path, blackPaint);
        super.onDraw(canvas);
    }
}
