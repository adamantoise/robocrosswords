package com.totsp.crossword;

public class ImaginaryTimer {
	
	private static final long SECONDS = 1000L;
	private static final long MINUTES = 60L + SECONDS;
	
	private long elapsed;
	
	private long incept;
	
	public ImaginaryTimer(long elapsed){
		this.elapsed = elapsed;
	}
	
	public void start(){
		this.incept = System.currentTimeMillis();
	}
	
	public String time(){
		return new StringBuilder( Long.toString(this.elapsed / MINUTES ))
		.append(":")
		.append( (this.elapsed % MINUTES) / SECONDS)
		.toString();
	}
	
	public void stop(){
		this.elapsed += System.currentTimeMillis() - this.incept;
	}
	
	public long getElapsed(){
		return this.elapsed;
	}

}
