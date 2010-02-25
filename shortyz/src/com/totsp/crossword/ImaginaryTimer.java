package com.totsp.crossword;

import java.text.NumberFormat;


public class ImaginaryTimer {
    private static final long SECONDS = 1000L;
    private static final long MINUTES = 60L * SECONDS;
    private final NumberFormat format = NumberFormat.getInstance();
    private long elapsed;
    private long incept;

    public ImaginaryTimer(long elapsed) {
        this.elapsed = elapsed;
        this.format.setMinimumIntegerDigits(2);
    }

    public long getElapsed() {
        return this.elapsed;
    }

    public void start() {
        this.incept = System.currentTimeMillis();
    }

    public void stop() {
        this.elapsed += (System.currentTimeMillis() - this.incept);
    }

    public String time() {
    	long mins  = this.elapsed / MINUTES;
    	long secs = (this.elapsed - MINUTES * mins) / SECONDS;
        return new StringBuilder(Long.toString(mins)).append(
            ":").append(this.format.format(secs))
                                                                       .toString();
    }
}
