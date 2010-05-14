package com.totsp.crossword.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;


public class NYTDownloader extends AbstractDownloader {
    private static final String[] MONTHS = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec"
        };
    public static final String NAME = "New York Times";
    private static final String LOGIN_URL = "http://www.nytimes.com/auth/login";
    NumberFormat nf = NumberFormat.getInstance();
    private HashMap<String, String> params = new HashMap<String, String>();

    protected NYTDownloader(String username, String password) {
        super("http://select.nytimes.com/premium/xword/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        params.put("is_continue", "true");
        params.put("SAVEOPTION", "YES");
        params.put("URI", "http://www.nytimes.com");
        params.put("OQ", "");
        params.put("OP", "");
        params.put("USERID", username);
        params.put("PASSWORD", password);
    }

    public String getName() {
        return NYTDownloader.NAME;
    }

    public File download(Date date) {
        //Feb2310.puz
        return this.download(date,
            MONTHS[date.getMonth()] + this.nf.format(date.getDate()) +
            this.nf.format(date.getYear() - 100) + ".puz");
    }

    @Override
    protected File download(Date date, String urlSuffix) {
        try {
            URL url = new URL(this.baseUrl + urlSuffix);
            HttpClient client = this.login();

            HttpGet get = new HttpGet(url.toString());
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                File f = new File(downloadDirectory, this.createFileName(date));
                FileOutputStream fos = new FileOutputStream(f);
                AbstractDownloader.copyStream(response.getEntity().getContent(),
                    fos);
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

    private HttpClient login() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams()
                  .setParameter("User-Agent",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6");

        HttpGet httpget = new HttpGet(LOGIN_URL);

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());

        if (entity != null) {
            entity.consumeContent();
        }

        System.out.println("Initial set of cookies:");

        List<Cookie> cookies = httpclient.getCookieStore().getCookies();

        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        HttpPost httpost = new HttpPost(LOGIN_URL);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        for (Entry<String, String> e : this.params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }

        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpclient.execute(httpost);
        entity = response.getEntity();

        System.out.println("Login form get: " + response.getStatusLine());

        if (entity != null) {
            entity.consumeContent();
        }

        System.out.println("Post logon cookies:");
        cookies = httpclient.getCookieStore().getCookies();

        if (cookies.isEmpty()) {
            System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
            }
        }

        return httpclient;
    }
    
    public File update(Date date){
    	try {
    		String urlSuffix = MONTHS[date.getMonth()] + this.nf.format(date.getDate()) +
            this.nf.format(date.getYear() - 100) + ".puz";
    		
            URL url = new URL(this.baseUrl + urlSuffix);
            HttpClient client = this.login();

            HttpGet get = new HttpGet(url.toString());
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                File f = new File(downloadDirectory, this.createFileName(date)+".tmp");
                FileOutputStream fos = new FileOutputStream(f);
                AbstractDownloader.copyStream(response.getEntity().getContent(),
                    fos);
                fos.close();

                File original = new File(downloadDirectory, this.createFileName(date));
                
                Puzzle oPuz = IO.load(original);
                Puzzle nPuz = IO.load(f);
                
                boolean updated = false;
                for(int x=0; x < oPuz.getBoxes().length; x++){
                	for(int y=0; y < oPuz.getBoxes()[x].length; y++){
                		Box oBox = oPuz.getBoxes()[x][y];
                		Box nBox = nPuz.getBoxes()[x][y];
                		if( oBox != null && nBox != null && oBox.getSolution() != nBox.getSolution()){
                			oBox.setSolution(nBox.getSolution());
                			updated = true;
                		}
                	}
                }
                if(updated){
                	IO.save(oPuz, original);
                	f.delete();
                } else {
                	return null;
                }
                
                
                return original;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
        	e.printStackTrace();
        }

        return null;
    }
}
