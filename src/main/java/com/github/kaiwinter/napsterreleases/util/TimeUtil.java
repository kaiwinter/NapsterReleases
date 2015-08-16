package com.github.kaiwinter.napsterreleases.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for time related conversions.
 */
public final class TimeUtil {

	private TimeUtil() {
		// should not be instantiated
	}

	/**
	 * Converts a timestamp from Long to it's String representation, format: "yyyy-MM-dd".
	 *
	 * @param timestamp
	 *            the timestamp
	 * @return String representation
	 */
	public static String timestampToString(Long timestamp) {
		if (timestamp == null) {
			return "";
		}
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp));
	}

	/**
	 * Converts seconds from long to a String in the format "mm:ss".
	 *
	 * @param seconds
	 *            positive number, if negative it is multiplicated by -1
	 * @return String representation like "mm:ss"
	 */
	public static String secondsToString(long seconds) {
		if (seconds < 0) {
			seconds = seconds * -1;
		}
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}

}
