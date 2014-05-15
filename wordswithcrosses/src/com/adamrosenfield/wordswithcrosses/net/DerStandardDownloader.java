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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.IO;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardParser;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardPuzzleCache;
import com.adamrosenfield.wordswithcrosses.net.derstandard.DerStandardPuzzleMetadata;

/** Downloader for derStandard.at. 
 * 
 * As puzzles are only available as a web application, there's some weird - 
 * and easily broken if the web app changes - stuff to be done to actually produce PUZ files. 
 * 
 * Copyright (this file) 2014 Wolfgang Groiss
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *
 **/
public class DerStandardDownloader extends AbstractDownloader implements DerStandardPuzzleCache {
	private static final String NAME = "DerStandard.at";
	private static final String BASE_URL = "http://derstandard.at";
	private static final String INDEX_URL = BASE_URL + "/r1256744634465/Kreuzwortraetsel";
	private static final String PUZZLE_URL = BASE_URL +	"/raetselapp/?id=";
	private static final String SOLUTION_URL = BASE_URL + "/RaetselApp/Home/GetCrosswordResult";
	
	private static final Pattern P_CHARSET_IN_TYPE = Pattern.compile("[A-Za-z0-9\\-/]+;\\s*charset=([A-Za-z0-9\\-]+)");
	private static final String P_HINT= "([0-9]+)([^_]+)___";
	private static final Pattern P_HREF_PUZZLE = Pattern.compile(".*/Kreuzwortraetsel-Nr-([0-9]+)(\\?.*)?");
	
	private static final DateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final Logger LOG = Logger.getLogger("DerStandardDownloader");

	final static boolean DEBUG_IMAGE_PARSING = false;
	private static final String SERIALIZED_STATE_FILENAME = "DerStandardDownloader.ser";
	
	private Map<String, DerStandardPuzzleMetadata> puzzlesById = new HashMap<String, DerStandardPuzzleMetadata>();
	private Map<String, DerStandardPuzzleMetadata> puzzlesByCalendar = new HashMap<String, DerStandardPuzzleMetadata>();
	
	private final DerStandardParser parser = new DerStandardParser();
	
	private Date lastIndexUpdate;
	
	public DerStandardDownloader() {
		super(BASE_URL, NAME);
		
		loadSerializedStateIfExists();
	}
	
	private void loadSerializedStateIfExists() {
		try {
			File f = getSerializedStateFile();
			
			if (f.exists()) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				try {
					puzzlesById = (Map<String, DerStandardPuzzleMetadata>) ois.readObject();
					puzzlesByCalendar = (Map<String, DerStandardPuzzleMetadata>) ois.readObject();
					
					try {
						lastIndexUpdate = (Date) ois.readObject();
					} catch (IOException ioe) {
						//lastIndexUpdate wasn't saved, swallow and be backward compatible.
					}
				} finally {
					ois.close();
				}
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Unable to load serialized state.", e);
		}
	}
	
	private void saveSerializedState() {
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

		} catch (Exception e) {
			LOG.log(Level.WARNING, "Unable to save serialized state.", e);
		}
	}

	private File getSerializedStateFile() {
		return new File(WordsWithCrossesApplication.TEMP_DIR, SERIALIZED_STATE_FILENAME);
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
		
		DerStandardPuzzleMetadata pm = getPuzzleByDate(date);
		if (pm == null) {
			try {
				refreshPuzzleMetadata();
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Error fetching/parsing metadata.", e);
			}
		
			pm = getPuzzleByDate(date);
		} 
		
		if (pm == null) {
			throw new IOException("Could not find puzzle for "+date);
		}
		
		File targetFile = getTargetFile(pm);
		if (! targetFile.exists()) {
			try {
				downloadPuzzle(pm);
			} catch (Exception e) {
				LOG.log(Level.SEVERE, "Error fetching/parsing puzzle.", e);
				throw new IOException("Could not download puzzle for "+date);
			}
		}
	}

	private DerStandardPuzzleMetadata getPuzzleByDate(Calendar date) {
		return puzzlesByCalendar.get(DF_DATE.format(date.getTime()));
	}
	
	
	
	private void refreshPuzzleMetadata() throws SAXException, ParserConfigurationException, IOException, JSONException {
		if (shouldUpdateIndex()) {
			updateIndex();
			refreshPuzzles();
			saveSerializedState();
		}
	}

	private boolean shouldUpdateIndex() {
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

	private void downloadPuzzle(DerStandardPuzzleMetadata pm) throws SAXException, ParserConfigurationException, IOException, JSONException {
		refresh(pm, true);
	}


	private void refreshPuzzles() throws SAXException, ParserConfigurationException, IOException, JSONException {
		for (final DerStandardPuzzleMetadata pm : puzzlesById.values()) {
			refresh(pm, false);
		}
	}
	
	
	private void refresh(DerStandardPuzzleMetadata pm, boolean downloadPuzzleData) throws SAXException, ParserConfigurationException, IOException, JSONException {
		String dUrl = pm.getDateUrl(BASE_URL);
		String pUrl = pm.getPuzzleUrl(BASE_URL);
		
		boolean save = false;
		
		if (dUrl != null && pm.getDate() == null) {
			InputSource input = new InputSource(getURLReader(getHttpConnection(dUrl)));
			parser.parseDate(pm, this, input);
			save = true;
		}
		
		if (downloadPuzzleData && pUrl != null && pm.getPuzzle() == null) {
			Reader r = getURLReader(getHttpConnection(pUrl));
			try {
				InputSource input = new InputSource(r);
				parser.parsePuzzle(pm, input);
			} finally {
				r.close();
			}
			
			InputSource isSolution = postForSolution(pm.getId()); 
			if (isSolution != null) {
					parser.parseSolution(pm, isSolution);
			}
			
			save = true;
		}
		
		if (save) {
			try {
				savePuzzle(pm);
			} catch (IOException ioe) {
				LOG.log(Level.SEVERE, "Cannot save puzzle "+pm, ioe);
			}
		}
	}

	private InputSource postForSolution(String id) throws MalformedURLException, IOException {
		HttpURLConnection conn = getHttpPostConnection(SOLUTION_URL, "ExternalId="+id);
		Reader reader = getURLReader(conn);
		return new InputSource(reader);
	}

	private void savePuzzle(DerStandardPuzzleMetadata pm) throws IOException {
//    IO.save(Puzzle, targetFile) ist auch n heisser Kandidat
		if (pm.getPuzzle() != null) {
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
		pm.setDate(c);

		String key = DF_DATE.format(c.getTime());
		puzzlesByCalendar.put(key, pm);
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
		
		//so we don't neccessarily get the mobile version 
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; rv:8.0) Gecko/20100101 Firefox/8.0");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		return conn;
	}
	
	public boolean contains(String id) {
		return puzzlesById.containsKey(id);
	}
	
	public DerStandardPuzzleMetadata createOrGet(String id) {
		DerStandardPuzzleMetadata pm = (DerStandardPuzzleMetadata)puzzlesById.get(id);
		if (pm == null) {
			pm = new DerStandardPuzzleMetadata(id);
			pm.setPuzzleUrl(PUZZLE_URL + id);
			puzzlesById.put(id, pm);
		} 
		return pm;
	}

	
	
	
	
	



}
