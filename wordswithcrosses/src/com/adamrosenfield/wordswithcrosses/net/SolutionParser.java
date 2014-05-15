package com.adamrosenfield.wordswithcrosses.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.ccil.cowan.tagsoup.AutoDetector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class SolutionParser {
	

	private static final Logger LOG = Logger.getLogger("SolutionParser");

	private final DerStandardPuzzleMetadata pm;
	
	public SolutionParser(DerStandardPuzzleMetadata pm) {
		this.pm = pm;
	}

	public void parse(InputSource input) throws SAXException, IOException, JSONException {
		Reader reader = getReader(input);
		String json = read(reader);
		
		Puzzle p = pm.getPuzzle();
		
		JSONObject response = new JSONObject(json);
		JSONArray results = response.getJSONArray("Results");
		for (int i=0; i<results.length(); i++) {
			JSONObject result = results.getJSONObject(i);
			int wordNumber = result.getInt("WordNumber");
			String answer = result.getString("Answer");
			boolean horizontal = result.getBoolean("Horizontal");
			
			writeAnswer(p, wordNumber, answer, horizontal);
		}

		p.setScrambled(false);
	}

	
	
	
	private void writeAnswer(Puzzle p, int wordNumber, String answer, boolean horizontal) {
		int[] coordinates = findFieldByClueNumber(p, wordNumber);
		int x = coordinates[0];
		int y = coordinates[1];
		
		for (char c : answer.toCharArray()) {
			Box b = p.getBoxes()[y][x];
			if (b != null) {
				b.setSolution(c);
			}

			if (horizontal) {
				++x;
			} else {
				++y;
			}
		}
	}

	private int[] findFieldByClueNumber(Puzzle p, int wordNumber) {
		for (int y = 0; y < p.getHeight(); y++) {
			for (int x = 0; x < p.getWidth(); x++) {
				Box b = p.getBoxes()[y][x];
				if (b != null && b.getClueNumber() == wordNumber) {
					return new int[]{x,y};
				}
			}
		}

		return null;
	}

	private String read(Reader reader) throws IOException {
		BufferedReader br = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    StringBuilder sb = new StringBuilder();

    String line = null;
    while ((line = br.readLine()) != null)
    {
        sb.append(line + "\n");
    }
		return sb.toString();
	}




	//stolen from tagsoup
	private final AutoDetector theAutoDetector = new AutoDetector() {
		public Reader autoDetectingReader(InputStream i) {
			return new InputStreamReader(i);
			}
		};
	
	
	//stolen from tagsoup
	private Reader getReader(InputSource s) throws SAXException, IOException {
		Reader r = s.getCharacterStream();
		InputStream i = s.getByteStream();
		String encoding = s.getEncoding();
		String publicid = s.getPublicId();
		String systemid = s.getSystemId();
		if (r == null) {
			if (i == null) i = getInputStream(publicid, systemid);
//			i = new BufferedInputStream(i);
			if (encoding == null) {
				r = theAutoDetector.autoDetectingReader(i);
				}
			else {
				try {
					r = new InputStreamReader(i, encoding);
					}
				catch (UnsupportedEncodingException e) {
					r = new InputStreamReader(i);
					}
				}
			}
//		r = new BufferedReader(r);
		return r;
		}

	//stolen from tagsoup
	// Get an InputStream based on a publicid and a systemid
	private InputStream getInputStream(String publicid, String systemid) throws IOException, SAXException {
		URL basis = new URL("file", "", System.getProperty("user.dir") + "/.");
		URL url = new URL(basis, systemid);
		URLConnection c = url.openConnection();
		return c.getInputStream();
		}
}