package com.totsp.crossword.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.NumberFormat;

import java.util.Date;

/**
 * Los Angeles Times
 * URL: http://www.cruciverb.com/puzzles/lat/latYYMMDD.puz
 * Date = Daily
 */
public class LATDownloader extends AbstractDownloader {
    public static final String NAME = "Los Angeles Times";
    NumberFormat nf = NumberFormat.getInstance();

    protected LATDownloader() {
        super("http://www.cruciverb.com/puzzles/lat/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public String getName() {
        return LATDownloader.NAME;
    }
    
    public int[] getDownloadDates() {
    	return DATE_DAILY;
    }

    public File download(Date date) {
        return this.download(date,this.createUrlSuffix(date));
    }

    @Override
    protected File download(Date date, String urlSuffix) {
        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            System.out.println(url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Referer", "http://www.cruciverb.com/puzzles.php?op=showarch&pub=lat");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.0 Safari/534.13");
            System.out.println("Response : "+connection.getResponseCode());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                File f = new File(downloadDirectory, this.createFileName(date));
                FileOutputStream fos = new FileOutputStream(f);
                AbstractDownloader.copyStream(connection.getInputStream(), fos);
                fos.close();

                return f;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

	@Override
	protected String createUrlSuffix(Date date) {
		return "lat" + this.nf.format(date.getYear() - 100) +
        this.nf.format(date.getMonth() + 1) +
        this.nf.format(date.getDate()) + ".puz";
	}
}
