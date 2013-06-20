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

import java.text.NumberFormat;

public class ImaginaryTimer {
    private static final long SECONDS = 1000L;
    private static final long MINUTES = 60L * SECONDS;
    private static final long HOURS = 60L * MINUTES;
    private final NumberFormat numberFormat = NumberFormat.getInstance();
    private boolean running = false;
    private long elapsed;
    private long start;

    public ImaginaryTimer(long elapsed) {
        this.elapsed = elapsed;
        numberFormat.setMinimumIntegerDigits(2);
    }

    public long getElapsed() {
        return running ? ((System.currentTimeMillis() - start) + elapsed) : elapsed;
    }

    public void start() {
        start = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        elapsed += (System.currentTimeMillis() - start);
        running = false;
    }

    public String time() {
        long elapsed = getElapsed();
        long hours = elapsed / HOURS;
        long minutes = (elapsed / MINUTES) % 60;
        long seconds = (elapsed / SECONDS) % 60;

        if (hours > 0) {
            return new StringBuilder()
                .append(hours)
                .append(":")
                .append(numberFormat.format(minutes))
                .append(":")
                .append(numberFormat.format(seconds))
                .toString();
        } else {
            return new StringBuilder()
                .append(minutes)
                .append(":")
                .append(numberFormat.format(seconds))
                .toString();
        }
    }
}
