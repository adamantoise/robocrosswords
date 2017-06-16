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

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

public class CrosswordRelativeLayout extends RelativeLayout
{
    private Activity mActivity;

    public CrosswordRelativeLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if (context instanceof Activity)
        {
            mActivity = (Activity)context;
        }
    }

    /**
     * Intercepts the Back button so that we can close the activity instead of
     * closing the native keyboard, if that's open
     */
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event)
    {
        if (android.os.Build.VERSION.SDK_INT >= 5 && isBackButtonUpEvent(event))
        {
            return true;
        }

        return super.dispatchKeyEventPreIme(event);
    }

    private boolean isBackButtonUpEvent(KeyEvent event)
    {
        // Based on http://stackoverflow.com/a/5811630/9530
        if (mActivity != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled())
            {
                mActivity.onBackPressed();
            }

            return true;
        }

        return false;
    }
}
