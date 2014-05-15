package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public interface DerStandardPuzzleCache {

	boolean contains(String id);
	DerStandardPuzzleMetadata createOrGet(String id);

	void setDate(DerStandardPuzzleMetadata pm, Calendar c);

}