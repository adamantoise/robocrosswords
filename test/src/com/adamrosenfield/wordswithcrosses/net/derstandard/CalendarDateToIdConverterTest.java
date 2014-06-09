package com.adamrosenfield.wordswithcrosses.net.derstandard;

import java.util.Calendar;


import org.junit.Test;
import static org.junit.Assert.*;
import static com.adamrosenfield.wordswithcrosses.net.derstandard.CalendarDateToIdConverter.NONE;

public class CalendarDateToIdConverterTest {

	@Test
	public void testCalculationsAgainstKnownPoints() {
		CalendarDateToIdConverter testee = new CalendarDateToIdConverter();
		
		assertId("2014-03-05", 7616, testee);
		assertId("2014-06-04", 7691, testee);
		assertId("2014-06-07", 7694, testee);
		assertId("2013-06-18", 7400, testee);
		assertId("2013-04-16", 7350, testee); 
		assertId("2013-02-15", 7300, testee);
		assertId("2012-01-13", 6970, testee); //had to google the lower numbers to find old mapping. don't forget that the online publication is at 18:00 on the day before print publication (whose date we care about)
	}
	
	@Test
	public void testNoResultOutsideOfLimits() {
		CalendarDateToIdConverter testee = new CalendarDateToIdConverter();

		assertId("2012-01-02", 6961, testee);
		assertId("2012-01-01", NONE, testee);
		assertId("2011-12-31", NONE, testee);
		assertId("2011-05-05", NONE, testee);
		
		assertId("2016-12-31", 8468, testee);
		assertId("2017-01-01", NONE, testee);
		assertId("2017-01-02", NONE, testee);
		assertId("2017-05-05", NONE, testee);
	}

	private void assertId(String date, int expected, CalendarDateToIdConverter testee) {
		Calendar c = CalendarDateToIdConverter.parse(date);
		
		int actual = testee.getId(c);
		assertEquals("For "+date, expected, actual);
		
	}

}
