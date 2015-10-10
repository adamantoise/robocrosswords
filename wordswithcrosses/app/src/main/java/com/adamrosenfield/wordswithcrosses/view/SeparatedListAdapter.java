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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.adamrosenfield.wordswithcrosses.R;

public class SeparatedListAdapter extends BaseAdapter implements SectionIndexer {
    private static final int TYPE_SECTION_HEADER = 0;
    private final ArrayAdapter<String> headers;
    private final ArrayList<String> headerLabels = new ArrayList<>();
    public final ArrayList<Adapter> sections = new ArrayList<>();

    public SeparatedListAdapter(Context context) {
        headers = new ArrayAdapter<>(context, R.layout.puzzle_list_header);
    }

    public int getCount() {
        // total together all sections, plus one for each section header
        int total = 0;

        for (Adapter adapter : this.sections)
            total += (adapter.getCount() + 1);

        return total;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    public Object getItem(int position) {
        int section = 0;

        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                return headers.getItem(section);
            }

            if (position < size) {
                return adapter.getItem(position - 1);
            }

            // otherwise jump into next section
            position -= size;
            section++;
        }

        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (position == 0) {
                return TYPE_SECTION_HEADER;
            }

            if (position < size) {
                return 1;
            }

            // otherwise jump into next section
            position -= size;

            //type += adapter.getViewTypeCount();
        }

        return -1;
    }

    public View getView(int i, View view, ViewGroup group) {
        int sectionnum = 0;

        for (Adapter adapter : this.sections) {
            int size = adapter.getCount() + 1;

            // check if position inside this section
            if (i == 0) {
                return headers.getView(sectionnum, view, group);
            }

            if (i < size) {
                return adapter.getView(i - 1, view, group);
            }

            // otherwise jump into next section
            i -= size;
            sectionnum++;
        }

        return null;
    }

    @Override
    public int getViewTypeCount() {
        //      // assume that headers count as one, then total all sections
        //      int total = 1;
        //      for(Adapter adapter : this.sections)
        //          total += adapter.getViewTypeCount();
        //      return total;
        return 2;
    }

    public void addSection(String section, Adapter adapter) {
        headers.add(section);
        headerLabels.add(section);
        sections.add(adapter);
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    public int getPositionForSection(int section) {
        if (section <= 0) {
            return 0;
        } else if (section >= sections.size()) {
            return (getCount() - 1);
        }

        int itemCount = 0;
        for (int i = 0; i < section; i++) {
            itemCount += (sections.get(i).getCount() + 1);
        }

        return itemCount;
    }

    public int getSectionForPosition(int position) {
        if (position <= 0) {
            return 0;
        }

        int itemCount = 0;
        for (int i = 0; i < sections.size(); i++) {
            itemCount += (sections.get(i).getCount() + 1);
            if (itemCount >= position) {
                return i;
            }
        }

        return (sections.size() - 1);
    }

    public Object[] getSections() {
        return headerLabels.toArray();
    }
}
