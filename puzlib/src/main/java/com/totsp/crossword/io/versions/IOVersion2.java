package com.totsp.crossword.io.versions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;

public class IOVersion2 extends IOVersion1 {

	@Override 
	protected void applyMeta(Puzzle puz, PuzzleMeta meta){
		super.applyMeta(puz, meta);
		//System.out.println("Applying V2 Meta");
		puz.setUpdatable(meta.updateable);
		puz.setSourceUrl(meta.sourceUrl);
	}
	
	@Override
	public PuzzleMeta readMeta(InputStream is) throws IOException{
		//System.out.println("Read V2");
		PuzzleMeta meta = super.readMeta(is);
		meta.updateable = is.read() == 1;
		meta.sourceUrl = IO.readNullTerminatedString(is);
		//System.out.println(meta);
		return meta;
	}
	
	@Override 
	protected void writeMeta(Puzzle puz, DataOutputStream dos) throws IOException{
		super.writeMeta(puz, dos);
		//System.out.println("Writing V2 meta");
		dos.write(puz.isUpdatable() ? 1 : -1); 
		IO.writeNullTerminatedString(dos, puz.getSourceUrl());
	}
	
}
