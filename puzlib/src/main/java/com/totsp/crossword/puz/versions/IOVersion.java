package com.totsp.crossword.puz.versions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;

public interface IOVersion {
	public void write(Puzzle puz, OutputStream os) throws IOException;
	public void read(Puzzle puz, InputStream is) throws IOException;
	public PuzzleMeta readMeta(InputStream is) throws IOException;
}
