package com.totsp.crossword.puz;

import com.totsp.gwittir.client.beans.annotations.Introspectable;
import java.io.Serializable;
import java.util.Date;

@Introspectable
public class PuzzleMeta implements Serializable {
	
	public String author;
	public String title;
	public String source;
	public Date date;
	public int percentComplete;
	

}
