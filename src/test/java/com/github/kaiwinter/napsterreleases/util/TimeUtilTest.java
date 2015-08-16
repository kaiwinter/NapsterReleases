package com.github.kaiwinter.napsterreleases.util;

import org.junit.Assert;
import org.junit.Test;

import com.github.kaiwinter.napsterreleases.util.TimeUtil;

public class TimeUtilTest {

	@Test
	public void testSecondsToStringMinus() {
		String string = TimeUtil.secondsToString(-10);
		Assert.assertEquals("00:10", string);
	}

	@Test
	public void testSecondsToStringZero() {
		String string = TimeUtil.secondsToString(0);
		Assert.assertEquals("00:00", string);
	}

	@Test
	public void testSecondsToStringUnderOneMinute() {
		String string = TimeUtil.secondsToString(30);
		Assert.assertEquals("00:30", string);
	}

	@Test
	public void testSecondsToStringOneMinute() {
		String string = TimeUtil.secondsToString(60);
		Assert.assertEquals("01:00", string);
	}

	@Test
	public void testSecondsToStringOverOneMinute() {
		String string = TimeUtil.secondsToString(90);
		Assert.assertEquals("01:30", string);
	}

	@Test
	public void testSecondsToStringOverHundretMinutes() {
		String string = TimeUtil.secondsToString(6030);
		Assert.assertEquals("100:30", string);
	}
}
