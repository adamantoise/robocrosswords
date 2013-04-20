package com.adamrosenfield.wordswithcrosses.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import android.net.Uri;

import com.adamrosenfield.wordswithcrosses.io.JPZIO;
import com.adamrosenfield.wordswithcrosses.puz.PuzzleMeta;
import com.adamrosenfield.wordswithcrosses.versions.DefaultUtil;

public abstract class AbstractJPZDownloader extends AbstractDownloader {

	protected AbstractJPZDownloader(String baseUrl, File downloadDirectory, String downloaderName) {
		super(baseUrl, downloadDirectory, downloaderName);
	}
	
	
	
	protected File download(Calendar date, String urlSuffix, Map<String, String> headers) {
		File jpzFile = download(date, urlSuffix, headers, false);
		File puzFile = new File(downloadDirectory, this.createFileName(date));
		try {
			FileInputStream is = new FileInputStream(jpzFile);
	        DataOutputStream dos = new DataOutputStream(new FileOutputStream(puzFile));
			JPZIO.convertJPZPuzzle(is, dos , date);
			dos.close();
			jpzFile.delete();
			return puzFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String createFileName(Calendar date) {
        return (date.get(Calendar.YEAR) +
                "-" +
                (date.get(Calendar.MONTH) + 1) +
                "-" +
                date.get(Calendar.DAY_OF_MONTH) +
                "-" +
                this.getName().replaceAll(" ", "") +
                ".puz");
    }
	
	protected File download(Calendar date, String urlSuffix, Map<String, String> headers, boolean canDefer) {
        LOG.info("Mkdirs: " + this.downloadDirectory.mkdirs());
        LOG.info("Exist: " + this.downloadDirectory.exists());

        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            System.out.println(url);

            File f = new File(downloadDirectory, this.createFileName(date)+".jpz");
            PuzzleMeta meta = new PuzzleMeta();
            meta.date = date;
            meta.source = getName();
            meta.sourceUrl = url.toString();
            meta.updateable = false;
            
            utils.storeMetas(Uri.fromFile(f), meta);
            if( canDefer ){
	            if (utils.downloadFile(url, f, headers, true, this.getName())) {
	                DownloadReceiver.metas.remove(Uri.fromFile(f));
	
	                return f;
	            } else {
	                return Downloader.DEFERRED_FILE;
	            }
            } else {
            	new DefaultUtil().downloadFile(url, f, headers, true, this.getName());
            	return f;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
