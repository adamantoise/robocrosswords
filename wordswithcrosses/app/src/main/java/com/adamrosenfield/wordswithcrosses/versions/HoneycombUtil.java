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

package com.adamrosenfield.wordswithcrosses.versions;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager.Request;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;

import com.adamrosenfield.wordswithcrosses.PuzzleFinishedActivity;


@TargetApi(11)
public class HoneycombUtil extends GingerbreadUtil {

    @Override
    public void finishOnHomeButton(final Activity a) {
        ActionBar bar = a.getActionBar();
        if (bar == null) {
            return;
        }
        bar.setDisplayHomeAsUpEnabled(true);
        View home = a.findViewById(android.R.id.home);
        if (home != null) {
            home.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        a.finish();
                    }
                });
        }
    }

    @Override
    public void onActionBarWithText(MenuItem a) {
        a.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT + MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public void onActionBarWithText(SubMenu a) {
        this.onActionBarWithText(a.getItem());
    }

    @Override
    public View onActionBarCustom(Activity a, int id) {
        ActionBar bar = a.getActionBar();
        if (bar == null) {
            return null;
        }
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        bar.setCustomView(id);
        return bar.getCustomView();
    }

    @Override
    protected void setNotificationVisibility(Request request, boolean notification) {
        request.setNotificationVisibility(notification ? Request.VISIBILITY_VISIBLE : Request.VISIBILITY_HIDDEN);
    }
}
