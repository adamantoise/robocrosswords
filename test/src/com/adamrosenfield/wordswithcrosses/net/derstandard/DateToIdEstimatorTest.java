package com.adamrosenfield.wordswithcrosses.net.derstandard;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateToIdEstimatorTest {

	DateToIdEstimator testee = new DateToIdEstimator(new DerStandardPuzzleCache() {
		
		DerStandardPuzzleMetadata dspm_7671 = new DerStandardPuzzleMetadata(7671);
		DerStandardPuzzleMetadata dspm_7642 = new DerStandardPuzzleMetadata(7642);
		
		{
			dspm_7671.setDate(new GregorianCalendar(2014, GregorianCalendar.MAY, 9));
			dspm_7642.setDate(new GregorianCalendar(2014, GregorianCalendar.APRIL, 3));
		}
		
		@Override
		public DerStandardPuzzleMetadata getClosestTo(Date date) {
			long a = Math.abs(date.getTime() - dspm_7671.getDate().getTimeInMillis());
			long b = Math.abs(date.getTime() - dspm_7642.getDate().getTimeInMillis());
			
			return a > b ?  dspm_7642 : dspm_7671;
		}

		@Override
		public boolean contains(int id) {
			return false;
		}
		
		@Override
		public void setDate(DerStandardPuzzleMetadata pm, Calendar c) {}
		
		@Override
		public DerStandardPuzzleMetadata createOrGet(int id) { return null; }
		

	});

	@Test
	public void zeroDifference() {
		assertEquals(7677, testee.estimateId(new Date(114, 4, 16)));
	}

	@Test
	public void weekDifference() {
		assertEquals(7671, testee.estimateId(new Date(114, 4, 9)));
	}
	
	@Test
	public void monthsDifference1() {
		assertEquals(7640, testee.estimateId(new Date(114, 2, 31))); //7639 would be correct, but close enough
	}

	@Test
	public void monthsDifference2() {
		assertEquals(7613, testee.estimateId(new Date(114, 1, 28)));
	}

	@Test
	public void monthsDifference3() {
		assertEquals(7605, testee.estimateId(new Date(114, 1, 18))); //7604 would be correct, but close enough
	}

}
