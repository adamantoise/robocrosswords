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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DateToIdEstimator;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardParser;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardPuzzleCache;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardPuzzleMetadata;

/**
 * Downloader for derStandard.at.
 *
 * As puzzles are only available as a web application, there's some weird - and
 * easily broken if the web app changes - stuff done to actually produce PUZ files.
 *
 **/
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DerStandardDownloader extends AbstractDownloader implements
        DerStandardPuzzleCache {
    private static final String NAME = "DerStandard.at";
    private static final String BASE_URL = "http://derstandard.at";
    private static final String INDEX_URL = BASE_URL + "/r1256744634465/Kreuzwortraetsel";
    private static final String PUZZLE_URL = BASE_URL + "/raetselapp/?id=";
    private static final String SOLUTION_URL = BASE_URL + "/RaetselApp/Home/GetCrosswordResult";

    private static final Pattern P_CHARSET_IN_TYPE = Pattern.compile("[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)");

    private static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger LOG = Logger.getLogger("DerStandardDownloader");

    final static boolean DEBUG_IMAGE_PARSING = false;
    private static final String SERIALIZED_STATE_FILENAME = "DerStandardDownloader.ser";

    private SortedMap<Integer, DerStandardPuzzleMetadata> puzzlesById = new TreeMap<Integer, DerStandardPuzzleMetadata>();
    private NavigableMap<String, DerStandardPuzzleMetadata> puzzlesByCalendar = new TreeMap<String, DerStandardPuzzleMetadata>();

    private final DerStandardParser parser = new DerStandardParser();
    private final DateToIdEstimator estimator = new DateToIdEstimator(this);

    private Date lastIndexUpdate;

    private boolean initialized = false;

    public DerStandardDownloader() {
        super(BASE_URL, NAME);
    }

    private void initializeIfNeeded() {
        if (!initialized) {

            loadSerializedStateIfExists();
            initialized = true;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSerializedStateIfExists() {
        try {
            File f = getSerializedStateFile();

            if (f.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                try {
                    puzzlesById = (SortedMap<Integer, DerStandardPuzzleMetadata>) ois.readObject();
                    puzzlesByCalendar = (NavigableMap<String, DerStandardPuzzleMetadata>) ois.readObject();
                    lastIndexUpdate = (Date) ois.readObject();
                } finally {
                    ois.close();
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Unable to load serialized state.", e);
        } catch (ClassNotFoundException e) {
            LOG.log(Level.WARNING, "Unable to load serialized state.", e);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to load serialized state.", e);
        }
    }

    private void saveSerializedState() {
        if (initialized) {
            try {
                File f = makeTempFile();

                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
                try {
                    oos.writeObject(puzzlesById);
                    oos.writeObject(puzzlesByCalendar);
                    oos.writeObject(lastIndexUpdate);
                } finally {
                    oos.close();
                }

                File target = getSerializedStateFile();

                if (target.exists()) {
                    target.delete();
                }

                f.renameTo(target);

            } catch (IOException e) {
                LOG.log(Level.WARNING, "Unable to save serialized state.", e);
            }
        }
    }

    private File getSerializedStateFile() {
        return new File(WordsWithCrossesApplication.NON_CROSSWORD_DATA_DIR, SERIALIZED_STATE_FILENAME);
    }

    private File makeTempFile() throws IOException {
        return File.createTempFile(SERIALIZED_STATE_FILENAME, ".temp", WordsWithCrossesApplication.TEMP_DIR);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return "";
    }

    @Override
    public void download(Calendar date) throws IOException {
        try {
            refreshPuzzleMetadata();
        } catch (RefreshException e) {
            LOG.log(Level.SEVERE, "Error fetching/parsing metadata.", e);
        }

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
        initializeIfNeeded();

        return getPuzzleByDateViaEstimator(date.getTime(), new HashSet<Integer>());
    }

    private DerStandardPuzzleMetadata getPuzzleByDateViaEstimator(Date date, Set<Integer> alreadyTried) {
        DerStandardPuzzleMetadata exact = puzzlesByCalendar.get(DF_DATE.format(date.getTime()));

        if (exact != null) {
            return exact;
        }

        int id = estimator.estimateId(date);
        if (alreadyTried.contains(id)) {
            return null;
        }

        alreadyTried.add(id);

        DerStandardPuzzleMetadata estimate = puzzlesById.get(id);
        if (estimate == null) {
            return null;
        }

        try {
            refresh(estimate, false, false);
        } catch (RefreshException re) {
            return null;
        }

        return getPuzzleByDateViaEstimator(date, alreadyTried);
    }

    private void refreshPuzzleMetadata() throws RefreshException {
        if (shouldUpdateIndex()) {
            try {
                updateIndex();
            } catch (IOException e) {
                throw new RefreshException("Updating Index.", e);
            } catch (SAXException e) {
                throw new RefreshException("Updating Index.", e);
            } catch (ParserConfigurationException e) {
                throw new RefreshException("Updating Index.", e);
            }

            refreshPuzzles();
            saveSerializedState();
        }
    }

    private boolean shouldUpdateIndex() {
        initializeIfNeeded();

        if (puzzlesById.isEmpty()) {
            return true;
        }

        if (lastIndexUpdate == null) {
            return true;
        }

        Date now = new Date();
        Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
        if (today.after(lastIndexUpdate)) {
            return true;
        }

        return false;
    }

    private void downloadPuzzle(DerStandardPuzzleMetadata pm) throws RefreshException {
        refresh(pm, true, true);
    }

    private void refreshPuzzles() throws RefreshException {
        initializeIfNeeded();

        int count = 0;
        for (final DerStandardPuzzleMetadata pm : puzzlesById.values()) {
            if (++count < 10 || (pm.isPuzzleAvailable() && !pm.isSolutionAvailable())) {
                refresh(pm, false, true);
            }
        }
    }

    private void refresh(DerStandardPuzzleMetadata pm, boolean downloadPuzzleData, boolean addPuzzleSolutionIfMissing) throws RefreshException {
        String dUrl = pm.getDateUrl(BASE_URL);
        String pUrl = pm.getPuzzleUrl(BASE_URL);
        int id = pm.getId();

        boolean save = false;

        if (dUrl != null && pm.getDate() == null) {
            try {
                InputSource input = new InputSource(getURLReader(getHttpConnection(dUrl)));
                parser.parseDate(pm, this, input);
                save = true;
            } catch (IOException e) {
                throw new RefreshException("Fetching/Parsing puzzle date for " + dUrl + ".", e);
            } catch (ParserConfigurationException e) {
                throw new RefreshException("Fetching/Parsing puzzle date for " + dUrl + ".", e);
            } catch (SAXException e) {
                throw new RefreshException("Fetching/Parsing puzzle date for " + dUrl + ".", e);
            }
        }

        if (downloadPuzzleData && pUrl != null && !pm.isPuzzleAvailable()) {
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

        if (addPuzzleSolutionIfMissing && pm.isPuzzleAvailable() && !pm.isSolutionAvailable()) {
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

    private void updateIndex() throws SAXException, ParserConfigurationException, IOException {
        initializeIfNeeded();

        Reader r = getURLReader(getHttpConnection(INDEX_URL));
        try {
            InputSource input = new InputSource(r);
            parser.parseIndex(input, this);
            lastIndexUpdate = new Date();
        } finally {
            r.close();
        }
    }

    public void setDate(DerStandardPuzzleMetadata pm, Calendar c) {
        initializeIfNeeded();

        pm.setDate(c);

        String key = DF_DATE.format(c.getTime());
        puzzlesByCalendar.put(key, pm);
    }

    public static boolean equals(Date d1, Date d2) {
        return DF_DATE.format(d1).equals(DF_DATE.format(d2));
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

    public boolean contains(int id) {
        initializeIfNeeded();

        return puzzlesById.containsKey(id);
    }

    public DerStandardPuzzleMetadata createOrGet(int id) {
        initializeIfNeeded();

        DerStandardPuzzleMetadata pm = (DerStandardPuzzleMetadata) puzzlesById.get(id);
        if (pm == null) {
            pm = new DerStandardPuzzleMetadata(id);
            pm.setPuzzleUrl(PUZZLE_URL + id);
            puzzlesById.put(id, pm);
        }
        return pm;
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

    public DerStandardPuzzleMetadata getClosestTo(Date date) {
        String s = DF_DATE.format(date);
        Entry<String, DerStandardPuzzleMetadata> floor   = puzzlesByCalendar.floorEntry(s);
        Entry<String, DerStandardPuzzleMetadata> ceiling = puzzlesByCalendar.ceilingEntry(s);

        if (floor == null && ceiling == null) {
            return null;
        } else if (floor == null) {
            return ceiling.getValue();
        } else if (ceiling == null) {
            return floor.getValue();
        } else {
            long dFloor   = Math.abs(date.getTime() -   floor.getValue().getDate().getTimeInMillis());
            long dCeiling = Math.abs(date.getTime() - ceiling.getValue().getDate().getTimeInMillis());

            return dFloor < dCeiling ? floor.getValue() : ceiling.getValue();
        }
    }

}
