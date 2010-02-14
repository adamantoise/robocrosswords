package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

public class ChronDownloader extends AbstractDownloader {
	NumberFormat nf = NumberFormat.getInstance();
	private static final String NAME = "Houston Chronicle";
	
	public ChronDownloader(){
		super("http://www.chron.com/apps/games/xword/puzzles/", DOWNLOAD_DIR, NAME);
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
	}
	
	public File download(Date date) {
		String name =
		"cs"+(date.getYear()-100)+nf.format(date.getMonth() +1)+
					nf.format(date.getDate())+".puz";
		return super.download(date, name);
		
	}

	public String getName() {
		return NAME;
	}
	


}
