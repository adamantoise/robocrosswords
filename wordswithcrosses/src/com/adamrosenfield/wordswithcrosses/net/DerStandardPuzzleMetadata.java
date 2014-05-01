package com.adamrosenfield.wordswithcrosses.net;

import java.io.Serializable;
import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

class DerStandardPuzzleMetadata implements Serializable {
	private final String id;
	private Calendar date;
	private String puzzleUrl;
	private String dateUrl;
	
	private transient Puzzle puzzle;
	
	DerStandardPuzzleMetadata(String id) {
		this.id = id;
	}



	public String getPuzzleUrl(String relativeBase) {
		return getUrl(puzzleUrl, relativeBase);
	}

	public void setPuzzleUrl(String puzzleUrl) {
		this.puzzleUrl = puzzleUrl;
	}
	
	

	public String getDateUrl(String relativeBase) {
		return getUrl(dateUrl, relativeBase);
	}



	private String getUrl(String url, String relativeBase) {
		if (url.contains("://")) {
			return url;
		} else {
			return relativeBase + url;
		}
	}



	public void setDateUrl(String dateUrl) {
		this.dateUrl = dateUrl;
	}



	public void setDate(Calendar date) {
		this.date = date;
	}

	public Calendar getDate() {
		return date;
	}

	public String getId() {
		return id;
	}

	public void setPuzzle(Puzzle p) {
		this.puzzle = p;
	}

	public Puzzle getPuzzle() {
		return puzzle;
	}

	@Override
	public String toString() {
		return "DerStandardPuzzleMetadata [id=" + id + ", date=" + date + ", puzzleUrl=" + puzzleUrl + ", puzzle=" + puzzle + "]";
	}

	
	
	
	
}