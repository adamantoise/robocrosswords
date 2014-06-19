/**
 * This file is part of Words With Crosses.
 *
 * Copyright (this file) 2014 Wolfgang Groiss
 *
 * This file is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.annotation.TargetApi;
import android.os.Build;

import com.adamrosenfield.wordswithcrosses.CalendarUtil;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.net.derstandard.CalendarDateToIdConverter;
import com.adamrosenfield.wordswithcrosses.net.derstandard.ConfigurableDailyDateToIdConverter;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DateToIdConverter;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardParser;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardPuzzleMetadata;

/**
 * Downloader for derStandard.at.
 *
 * As puzzles are only available as a web application, there's some weird - and
 * easily broken if the web app changes - stuff done to actually produce PUZ files.
 *
 **/
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DerStandardDownloader extends AbstractDownloader {
    private static final String NAME = "DerStandard.at";
    private static final String BASE_URL = "http://derstandard.at";
    private static final String PUZZLE_URL = BASE_URL + "/raetselapp/?id=";
    private static final String SOLUTION_URL = BASE_URL + "/RaetselApp/Home/GetCrosswordResult";

    private static final Pattern P_CHARSET_IN_TYPE = Pattern.compile("[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)");

    private static final Logger LOG = Logger.getLogger("DerStandardDownloader");

    private final DerStandardParser parser = new DerStandardParser();
    private static final DateToIdConverter converter = new CalendarDateToIdConverter();

    public DerStandardDownloader() {
        super(BASE_URL, NAME);
    }
   
    public boolean isPuzzleAvailable(Calendar date) {
        return converter.getId(date) != DateToIdConverter.NONE;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return "";
    }

    @Override
    public void download(Calendar date) throws IOException {
        DerStandardPuzzleMetadata pm = getPuzzleByDate(date);

        if (pm == null) {
            throw new IOException("Could not find puzzle for " + date);
        }

        File targetFile = getTargetFile(pm);
        if (!targetFile.exists()) {
            try {
                downloadPuzzle(pm);
            } catch (RefreshException e) {
                LOG.log(Level.SEVERE, "Error fetching/parsing puzzle.", e);
                throw new IOException("Could not download puzzle for " + date);
            }
        }
    }

    private DerStandardPuzzleMetadata getPuzzleByDate(Calendar date) {
        int id = converter.getId(date);

        DerStandardPuzzleMetadata dspm = new DerStandardPuzzleMetadata(id, PUZZLE_URL + id, date);
        
        return dspm;
    }

   
    private void downloadPuzzle(DerStandardPuzzleMetadata pm) throws RefreshException {
        String pUrl = pm.getPuzzleUrl(BASE_URL);
        int id = pm.getId();

        boolean save = false;

        if (pUrl != null && !pm.isPuzzleAvailable()) {
            try {
                Reader r = getURLReader(getHttpConnection(pUrl));
                try {
                    InputSource input = new InputSource(r);
                    parser.parsePuzzle(pm, input);
                    save = true;
                } finally {
                    r.close();
                }
            } catch (IOException e) {
                throw new RefreshException("Fetching/Parsing puzzle for " + pUrl + ".", e);
            } catch (ParserConfigurationException e) {
                throw new RefreshException("Fetching/Parsing puzzle for " + pUrl + ".", e);
            } catch (SAXException e) {
                throw new RefreshException("Fetching/Parsing puzzle for " + pUrl + ".", e);
            }
        }

        if (pm.isPuzzleAvailable() && !pm.isSolutionAvailable()) {
            try {
                InputSource isSolution = postForSolution(id);
                if (isSolution != null) {
                    parser.parseSolution(pm, isSolution);
                    save = true;
                }
            } catch (IOException e) {
                throw new RefreshException("Fetching/Parsing solution for " + id + ".", e);
            } catch (JSONException e) {
                throw new RefreshException("Fetching/Parsing solution for " + id + ".", e);
            } catch (SAXException e) {
                throw new RefreshException("Fetching/Parsing solution for " + id + ".", e);
            }
        }

        if (save) {
            try {
                savePuzzle(pm);
            } catch (IOException ioe) {
                throw new RefreshException("Saving puzzle " + id + ".", ioe);
            }
        }
    }

    private InputSource postForSolution(int id)
            throws MalformedURLException, IOException {
        HttpURLConnection conn = getHttpPostConnection(SOLUTION_URL, "ExternalId=" + id);
        Reader reader = getURLReader(conn);
        return new InputSource(reader);
    }

    private void savePuzzle(DerStandardPuzzleMetadata pm) throws IOException {
        if (pm.isPuzzleAvailable()) {
            File tempFile = new File(WordsWithCrossesApplication.TEMP_DIR, getFilename(pm.getDate()));
            IO.save(pm.getPuzzle(), tempFile);

            File destFile = getTargetFile(pm);
            if (!tempFile.renameTo(destFile)) {
                throw new IOException("Failed to rename " + tempFile + " to " + destFile);
            }

        }
    }

    private File getTargetFile(DerStandardPuzzleMetadata pm) {
        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, getFilename(pm.getDate()));
        return destFile;
    }

    protected Reader getURLReader(HttpURLConnection conn) throws IOException, MalformedURLException, UnsupportedEncodingException {
        InputStream in = (InputStream) conn.getContent();
        String encoding = conn.getContentEncoding();
        String type = conn.getContentType();

        if (encoding == null) {
            Matcher m = P_CHARSET_IN_TYPE.matcher(type);
            if (m.matches()) {
                encoding = m.group(1);
            } else {
                encoding = "ISO-8859-1";
            }
        }

        Reader r = new InputStreamReader(in, encoding);
        return r;
    }

    private HttpURLConnection getHttpPostConnection(String url, String postData) throws IOException, MalformedURLException {
        HttpURLConnection conn = getHttpConnection(url);

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        try {
            osw.append(postData);
        } finally {
            osw.close();
        }

        return conn;
    }

    private HttpURLConnection getHttpConnection(String url) throws IOException, MalformedURLException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        // so we don't neccessarily get the mobile version
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; rv:8.0) Gecko/20100101 Firefox/8.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        return conn;
    }

    private class RefreshException extends Exception {
        private static final long serialVersionUID = 3521756473491768245L;

        private RefreshException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        private RefreshException(Throwable throwable) {
            super(throwable);
        }
    }



}
