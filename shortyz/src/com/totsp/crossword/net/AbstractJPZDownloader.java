package com.totsp.crossword.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.totsp.crossword.io.JPZIO;

public abstract class AbstractJPZDownloader extends AbstractDownloader {

	protected AbstractJPZDownloader(String baseUrl, File downloadDirectory, String downloaderName) {
		super(baseUrl, downloadDirectory, downloaderName);
	}
	
	
	
	protected File download(Date date, String urlSuffix, Map<String, String> headers) {
		System.out.println("In JPZ Download");
		File jpzFile = super.download(date, urlSuffix, headers, false);
		File puzFile = new File(jpzFile.getParentFile(), jpzFile.getName().substring(0, jpzFile.getName().lastIndexOf('.'))+".puz");
		try {
			FileInputStream is = new FileInputStream(jpzFile);
	        DataOutputStream dos = new DataOutputStream(new FileOutputStream(puzFile));
			JPZIO.convertJPZPuzzle( is, dos , date);
			dos.close();
			jpzFile.delete();
			System.out.println("Downloaded "+jpzFile +" to "+puzFile);
			return puzFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String createFileName(Date date) {
        return (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "-" +
        this.getName().replaceAll(" ", "") + ".jpz";
    }
}
