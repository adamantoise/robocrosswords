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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public abstract class ScaleGestureDetectorProxy
{
    public abstract boolean onTouchEvent(MotionEvent event);

    public static ScaleGestureDetectorProxy create(Context context, TouchImageView view)
    {
        if (android.os.Build.VERSION.SDK_INT >= 8)
        {
            try
            {
                // Construct the listener object
                Class<?> clazz = Class.forName("com.adamrosenfield.wordswithcrosses.view.TouchImageView$ScaleListener");
                Constructor<?> constructor = clazz.getConstructor(TouchImageView.class);
                Object listener = constructor.newInstance(view);

                // Construct the detector proxy
                clazz = Class.forName("com.adamrosenfield.wordswithcrosses.view.ScaleGestureDetectorProxy$ScaleGestureDetectorProxyFroyo");
                constructor = clazz.getConstructor(Context.class, Class.forName("android.view.ScaleGestureDetector$OnScaleGestureListener"));
                return (ScaleGestureDetectorProxy)constructor.newInstance(context, listener);
            }
            catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }

        return new ScaleGestureDetectorProxyDefault();
    }

    public static class ScaleGestureDetectorProxyDefault extends ScaleGestureDetectorProxy
    {
        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            return false;
        }
    }

    @TargetApi(8)
    public static class ScaleGestureDetectorProxyFroyo extends ScaleGestureDetectorProxy
    {
        private ScaleGestureDetector mDetector;

        public ScaleGestureDetectorProxyFroyo(Context context, ScaleGestureDetector.OnScaleGestureListener listener)
        {
            mDetector = new ScaleGestureDetector(context, listener);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            return mDetector.onTouchEvent(event);
        }
    }
}
