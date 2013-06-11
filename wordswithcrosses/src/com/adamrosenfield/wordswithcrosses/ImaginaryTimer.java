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
