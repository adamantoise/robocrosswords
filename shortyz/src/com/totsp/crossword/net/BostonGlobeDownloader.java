package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

public class BostonGlobeDownloader extends AbstractDownloader {
	NumberFormat nf = NumberFormat.getInstance();
	private static final String NAME = "Boston Globe";
	
	public BostonGlobeDownloader(){
		super("http://standalone.com/dl/puz/boston/", DOWNLOAD_DIR, NAME);
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
	}
	
	public File download(Date date) {
		String name = "boston_"+nf.format(date.getMonth() +1)+
					nf.format(date.getDate())+
					(date.getYear()+ 1900)
					+".puz";
		return super.download(date, name);
		
	}

	public String getName() {
		return NAME;
	}
	


}
