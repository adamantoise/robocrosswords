package com.totsp.crossword.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Environment;

public abstract class AbstractDownloader implements Downloader{
	private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
	public static File DOWNLOAD_DIR = new File(Environment.getExternalStorageDirectory(), "crosswords");
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	private String baseUrl;
	private File downloadDirectory;
	private String downloaderName;
	
	protected AbstractDownloader(String baseUrl, File downloadDirectory, String downloaderName){
		this.baseUrl = baseUrl;
		this.downloadDirectory = downloadDirectory;
		this.downloaderName = downloaderName;
	}
	
	
	protected File download(Date date, String urlSuffix){
		
		LOG.info("Mkdirs: "+this.downloadDirectory.mkdirs());
		LOG.info("Exist: "+this.downloadDirectory.exists());
		File downloadTo = new File(this.downloadDirectory, this.createFileName(date));
		if(downloadTo.exists()){
			return null;
		}
		try{
			downloadTo.createNewFile();
			URL u = new URL(this.baseUrl+urlSuffix);
			LOG.warning("Downloading: "+u);
			FileOutputStream fos = new FileOutputStream(downloadTo);
			AbstractDownloader.copyStream(u.openStream(), fos);
			fos.close();
		} catch(IOException ioe){
			LOG.log(Level.SEVERE, "Exception downloading puzzle", ioe);
			downloadTo.delete();
			return null;
		}
		
		return downloadTo;
	
	}
	
	private String createFileName(Date date){
		return (date.getYear()+1900)+"-"+date.getMonth()+"-"+date.getDate()+"-"+this.downloaderName.replaceAll(" ", "")+".puz";
	}
	
	/**
     * Copies the data from an InputStream object to an OutputStream object.
     * 
     * @param sourceStream
     *            The input stream to be read.
     * @param destinationStream
     *            The output stream to be written to.
     * @return int value of the number of bytes copied.
     * @exception IOException
     *                from java.io calls.
     */
    public static int copyStream(InputStream sourceStream, OutputStream destinationStream) throws IOException {
        int bytesRead = 0;
        int totalBytes = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        while (bytesRead >= 0) {
            bytesRead = sourceStream.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
                destinationStream.write(buffer, 0, bytesRead);
            }

            totalBytes += bytesRead;
        }
        destinationStream.flush();
        destinationStream.close();
        return totalBytes;
    }

}
