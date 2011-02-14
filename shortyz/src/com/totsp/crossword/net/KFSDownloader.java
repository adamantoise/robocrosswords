package com.totsp.crossword.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.logging.Level;

import com.totsp.crossword.io.KingFeaturesPlaintextIO;
import com.totsp.crossword.versions.DefaultUtil;

/**
 * King Features Syndicate Puzzles
 * URL: http://[puzzle].king-online.com/clues/YYYYMMDD.txt
 * premier = Sunday
 * joseph = Monday-Saturday
 * sheffer = Monday-Saturday
 */
public class KFSDownloader extends AbstractDownloader {
	private String fullName;
	private String author;
	private int[] days;
	NumberFormat nf = NumberFormat.getInstance();
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	
	public KFSDownloader(String shortName, String fullName, String author, int[] days) {
		super("http://" + shortName + ".king-online.com/clues/", DOWNLOAD_DIR, fullName);
		this.fullName = fullName;
		this.author = author;
		this.days = days;
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
	}
	
	public String getName() {
		return fullName;
	}
	
	public int[] getDownloadDates() {
		return days; 
	}
	
	private File downloadToTempFile(Date date) {
		DefaultUtil util = new DefaultUtil();
		File downloaded = new File(downloadDirectory, this.createFileName(date));
		try{
			URL url = new URL(this.baseUrl + this.createUrlSuffix(date));
			util.downloadFile(url, downloaded, EMPTY_MAP , false, null);
		} catch(Exception e){
			e.printStackTrace();
			downloaded.delete();
			downloaded = null;
		}
		
		if(downloaded == null) {
			LOG.log(Level.SEVERE, "Unable to download plain text KFS file.");
			return null;
		}
		System.out.println("DownloadedKFS: "+downloaded);
		try {
			File tmpFile = File.createTempFile("kfs-temp", "txt");
			downloaded.renameTo(tmpFile);
			return tmpFile;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to move KFS file to temporary location.");
			return null;
		}
	}
	
	public File download(Date date) {
		File downloadTo = new File(this.downloadDirectory,
                this.createFileName(date));

        if (downloadTo.exists()) {
            return null;
        }
		
		File plainText = downloadToTempFile(date);
		if (plainText == null) {
			return null;
		}
		String copyright = "\u00a9 " + (date.getYear() + 1900) + " King Features Syndicate.";
		try {
			InputStream is = new FileInputStream(plainText);
			DataOutputStream os = new DataOutputStream(new FileOutputStream(downloadTo));
			boolean retVal = KingFeaturesPlaintextIO.convertKFPuzzle(is, os, 
					fullName + ", " + df.format(date), author, copyright, date); 
			os.close();
			is.close();
			plainText.delete();
			if(!retVal) {
				LOG.log(Level.SEVERE, "Unable to convert KFS puzzle into Across Lite format.");
				downloadTo.delete();
				downloadTo = null;
			}
		} catch (Exception ioe) {
			LOG.log(Level.SEVERE, "Exception converting KFS puzzle into Across Lite format.", ioe);
			downloadTo.delete();
			downloadTo = null;
		}
		return downloadTo;
	}
	
	@Override
	protected String createUrlSuffix(Date date) {
		return (date.getYear() + 1900) +
		nf.format(date.getMonth() + 1) + nf.format(date.getDate())
		+ ".txt";
	}
}
