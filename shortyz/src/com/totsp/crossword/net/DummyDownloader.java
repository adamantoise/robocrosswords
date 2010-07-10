package com.totsp.crossword.net;

import java.io.File;
import java.util.Date;

/**
 * Does not actually download any puzzles; just adds an "All Available" option to the dropdown.
 */
public class DummyDownloader implements Downloader {
	public String createFileName(Date date) {
		return null;
	}

	public File download(Date date) {
		return null;
	}

	public int[] getDownloadDates() {
		return null;
	}

	public String getName() {
		return null;
	}

	public String sourceUrl(Date date) {
		return null;
	}

	public String toString() {
		return "All available puzzles";
	}
}
