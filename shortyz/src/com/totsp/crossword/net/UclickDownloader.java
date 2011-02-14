package com.totsp.crossword.net;

import static com.totsp.crossword.net.AbstractDownloader.EMPTY_MAP;

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

import com.totsp.crossword.io.UclickXMLIO;
import com.totsp.crossword.versions.DefaultUtil;

/**
 * Uclick XML Puzzles
 * URL: http://picayune.uclick.com/comics/[puzzle]/data/[puzzle]YYMMDD-data.xml
 * crnet (Newsday) = Daily
 * usaon (USA Today) = Monday-Saturday (not holidays)
 * fcx (Universal) = Daily
 * lacal (LA Times Sunday Calendar) = Sunday
 */
public class UclickDownloader extends AbstractDownloader {
	private String shortName;
	private String fullName;
	private String copyright;
	private int[] days;
	NumberFormat nf = NumberFormat.getInstance();
	DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	
	public UclickDownloader(String shortName, String fullName, String copyright, int[] days) {
		super("http://picayune.uclick.com/comics/" + shortName + "/data/", DOWNLOAD_DIR, fullName);
		this.shortName = shortName;
		this.fullName = fullName;
		this.copyright = copyright;
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
		File f = new File(downloadDirectory, this.createFileName(date));
		try{
			URL url = new URL(this.baseUrl + this.createUrlSuffix(date));
			util.downloadFile(url, f, EMPTY_MAP , false, null);
		} catch(Exception e){
			e.printStackTrace();
			f = null;
		}
		if(f == null) {
			LOG.log(Level.SEVERE, "Unable to download uclick XML file.");
			return null;
		}
		try {
			File tmpFile = File.createTempFile("uclick-temp", "xml");
			f.renameTo(tmpFile);
			return tmpFile;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to move uclick XML file to temporary location.");
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
		try {
			InputStream is = new FileInputStream(plainText);
			DataOutputStream os = new DataOutputStream(new FileOutputStream(downloadTo));
			boolean retVal = UclickXMLIO.convertUclickPuzzle(is, os,
					"\u00a9 " + (date.getYear() + 1900) + " " + copyright, date); 
			os.close();
			is.close();
			plainText.delete();
			if(!retVal) {
				LOG.log(Level.SEVERE, "Unable to convert uclick XML puzzle into Across Lite format.");
				downloadTo.delete();
				downloadTo = null;
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, "Exception converting uclick XML puzzle into Across Lite format.", ioe);
			downloadTo.delete();
			downloadTo = null;
		}
		return downloadTo;
	}
	
	@Override
	protected String createUrlSuffix(Date date) {
		return this.shortName + nf.format(date.getYear() - 100) +
		nf.format(date.getMonth() + 1) + nf.format(date.getDate())
		+ "-data.xml";
	}
}
