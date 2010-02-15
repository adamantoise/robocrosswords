package com.totsp.crossword.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

public class WSJDownloader extends AbstractDownloader {
	NumberFormat nf = NumberFormat.getInstance();
	private static final String NAME = "Wall Street Journal";
	
	public WSJDownloader(){
		super("http://mazerlm.home.att.net/", DOWNLOAD_DIR, NAME);
		nf.setMinimumIntegerDigits(2);
		nf.setMaximumFractionDigits(0);
	}
	
	public File download(Date date) {
		if(date.getDay() != 5){
			return null;
		}
		String name =
		"wsj"+nf.format(date.getYear()-100)+nf.format(date.getMonth() +1)+
					nf.format(date.getDate())+".puz";
		return super.download(date, name);
		
	}

	public String getName() {
		return NAME;
	}
	


}
