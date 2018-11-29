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

package com.adamrosenfield.wordswithcrosses;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SourceListAdapter extends BaseAdapter {
    static final String ALL_SOURCES = "All Sources";
    public String current = ALL_SOURCES;
    private final Activity activity;
    private List<String> sources;

    public SourceListAdapter(Activity activity, List<String> sources) {
        this.activity = activity;
        this.sources = sources;
    }

    public int getCount() {
        return sources.size() + 1;
    }

    public Object getItem(int i) {
        if (i == 0) {
            return ALL_SOURCES;
        } else {
            return sources.get(i - 1);
        }
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int index, View view, ViewGroup group) {
        String value = (index == 0) ? ALL_SOURCES : sources.get(index - 1);
        LayoutInflater inflater = activity.getLayoutInflater();
        view = inflater.inflate(value.equals(this.current) ? R.layout.source_item_highlight : R.layout.source_item, null);

        TextView text = (TextView) view;

        text.setText(value);
        text.setTag(value);

        return text;
    }
}
