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

import java.text.DateFormat;
import java.text.NumberFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class PuzzleFinishedActivity extends WordsWithCrossesActivity{
    private static final long SECONDS = 1000;
    private static final long MINUTES = SECONDS * 60;
    private static final long HOURS = MINUTES * 60;
    private final NumberFormat two_int = NumberFormat.getIntegerInstance();
    private final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.completed);
        getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        long puzzleId = WordsWithCrossesApplication.BOARD.getPuzzleID();
        Puzzle puz = WordsWithCrossesApplication.BOARD.getPuzzle();

        two_int.setMinimumIntegerDigits(2);

        long elapsed = puz.getTime();

        long hours = elapsed / HOURS;
        elapsed = elapsed % HOURS;

        long minutes = elapsed / MINUTES;
        elapsed = elapsed % MINUTES;

        long seconds = elapsed / SECONDS;

        String elapsedString = (hours > 0 ? two_int.format(hours) + ":" : "") +
                two_int.format(minutes) + ":"+
                two_int.format(seconds);

        int totalClues = puz.getAcrossClues().length + puz.getDownClues().length;
        int totalBoxes = 0;
        int cheatedBoxes = 0;
        for (Box b : puz.getBoxesList()) {
            if (b == null) {
                continue;
            }
            if (b.isCheated()) {
                cheatedBoxes++;
            }
            totalBoxes++;
        }

        String cheatedStr = (int)Math.ceil((double)cheatedBoxes * 100D / (double)totalBoxes) + "%";
        String fullCheatedStr = cheatedBoxes + " (" + cheatedStr + ")";

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        String source = dbHelper.getPuzzleSource(puzzleId);
        String dateStr = df.format(puz.getDate().getTime());

        final String shareMessage;
        if (cheatedBoxes == 0) {
            shareMessage = getResources().getString(R.string.share_text, source, dateStr, elapsedString);
        } else {
            shareMessage = getResources().getQuantityString(
                R.plurals.share_text_hinted, cheatedBoxes, source, dateStr, elapsedString, cheatedBoxes, cheatedStr);
        }

        TextView elapsedTime = (TextView)findViewById(R.id.elapsed);
        elapsedTime.setText(elapsedString);

        TextView totalCluesView = (TextView)findViewById(R.id.totalClues);
        totalCluesView.setText(Integer.toString(totalClues));

        TextView totalBoxesView = (TextView)findViewById(R.id.totalBoxes);
        totalBoxesView.setText(Integer.toString(totalBoxes));

        TextView cheatedBoxesView = (TextView)findViewById(R.id.cheatedBoxes);
        cheatedBoxesView.setText(fullCheatedStr);

        Button share = (Button) findViewById(R.id.share);
        share.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
//              sendIntent.putExtra(Intent.EXTRA_SUBJECT,
//                      "I finished a puzzle in Robo Crosswords!");
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share your time"));
            }
        });

        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }
}
