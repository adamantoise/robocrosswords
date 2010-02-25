package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

public interface Downloader {
	
	public String getName();
	
	public File download(Date date);
	
	public String createFileName(Date date);

}
