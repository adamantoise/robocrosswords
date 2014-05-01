package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

public interface DerStandardPuzzleCache {

	DerStandardPuzzleMetadata createOrGet(String id);

	void setDate(DerStandardPuzzleMetadata pm, Calendar c);

}
