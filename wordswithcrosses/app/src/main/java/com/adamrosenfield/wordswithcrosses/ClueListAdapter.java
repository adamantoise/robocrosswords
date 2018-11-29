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

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.graphics.Color;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Clue;

public class ClueListAdapter extends BaseAdapter {
    private static final int TRANSPARENT = Color.TRANSPARENT;
    private static final int HIGHLIGHT = Color.argb(100, 200, 191, 231);
    private static final int DEFAULT_TEXT_SIZE = 14;

    private int textSize = DEFAULT_TEXT_SIZE;

    private Clue highlightClue;
    private final Activity activity;
    private final SparseArray<Box[]> cache = new SparseArray<>();
    private final Clue[] clues;
    private final boolean across;
    private boolean isActive = false;

    public ClueListAdapter(Activity activity, Clue[] clues, boolean across) {
        this.activity = activity;
        this.clues = clues;
        this.across = across;
    }

    public void setActiveDirection(boolean isActive) {
        this.isActive = isActive;
    }

    public int getCount() {
        return clues.length;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setHighlightClue(Clue c) {
        this.highlightClue = c;
    }

    public Object getItem(int i) {
        return clues[i];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(R.layout.clue_detail_item, null);
        }

        TextView line = (TextView) view.findViewById(R.id.clueLine);
        line.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.textSize);

        TextView word = (TextView) view.findViewById(R.id.clueWord);
        word.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (this.textSize - 1.75));

        Clue c = this.clues[position];
        line.setText(c.number + ". " + c.hint);

        Box[] boxes = this.cache.get(c.number);

        if (boxes == null) {
            boxes = WordsWithCrossesApplication.BOARD.getWordBoxes(c.number, across);
            cache.put(c.number, boxes);
        }

        StringBuilder sb = new StringBuilder();

        for (Box b : boxes) {
            if (b == null) {
                continue;
            }

            sb.append((b.getResponse() == ' ') ? '_' : b.getResponse())
              .append(' ');
        }

        sb.append(" [").append(boxes.length).append("]");
        word.setText(sb);

        if (this.isActive && c.equals(this.highlightClue)) {
            view.setBackgroundColor(HIGHLIGHT);
        } else {
            view.setBackgroundColor(TRANSPARENT);
        }

        return view;
    }

    public int indexOf(Clue clue) {
        return Arrays.binarySearch(clues, clue,
            new Comparator<Clue>() {
                public int compare(Clue clue1, Clue clue2) {
                    return Integer.valueOf(clue1.number).compareTo(clue2.number);
                }
            });
    }
}
