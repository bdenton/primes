package net.wdenton.primes.util;

import java.time.Duration;
import java.time.Instant;

public interface Timer {
	
	// these are copied from java.time.LocalTime because of visibility
	public static final int HOURS_PER_DAY = 24;
	
	public static final int MINUTES_PER_HOUR = 60;
	public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

	public static final int SECONDS_PER_MINUTE = 60;
	public static final int SECONDS_PER_DAY  = SECONDS_PER_MINUTE * MINUTES_PER_DAY;
	public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	
	public static final long MILLIS_PER_SECOND = 1000L;
	public static final long MILLIS_PER_DAY    = MILLIS_PER_SECOND * SECONDS_PER_DAY;
	public static final long MILLIS_PER_HOUR   = MILLIS_PER_SECOND * SECONDS_PER_HOUR;
	public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
	
	public static final long MICROS_PER_MILLI = 1000L;
	public static final long MICROS_PER_DAY    = MICROS_PER_MILLI * MILLIS_PER_DAY;
	public static final long MICROS_PER_HOUR   = MICROS_PER_MILLI * MILLIS_PER_HOUR;
	public static final long MICROS_PER_MINUTE = MICROS_PER_MILLI * MILLIS_PER_MINUTE;
	public static final long MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;

	public static final long NANOS_PER_MICRO = 1000L;
	public static final long NANOS_PER_DAY    = NANOS_PER_MICRO * MICROS_PER_DAY;
	public static final long NANOS_PER_HOUR   = NANOS_PER_MICRO * MICROS_PER_HOUR;
	public static final long NANOS_PER_MINUTE = NANOS_PER_MICRO * MICROS_PER_MINUTE;
	public static final long NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;
	public static final long NANOS_PER_MILLI  = NANOS_PER_MICRO * MICROS_PER_MILLI;
	
	public void start ();
	public void stop ();
	public void clear ();
	public Instant getStartTime ();
	public Instant getStopTIme ();
	public Duration getElapsedTime ();
	
	public static String tellAvgDuration (Duration duration, long count) {
		return tellDuration(duration.dividedBy(count), true);
	}
	public static String tellDuration (Duration duration) {
		return tellDuration(duration, false);
	}
	public static String tellDuration (Duration duration, boolean showNanos) {
		long seconds = duration.getSeconds();
		int nanos = duration.getNano();
		
        int days = (int)(seconds / SECONDS_PER_DAY);
        long hours = (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        int minutes = (int)((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        int secs = (int)(seconds % SECONDS_PER_MINUTE);
        int millis = (int)(nanos / NANOS_PER_MILLI);
		
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
        	sb.append(days).append("D+");
        }
        String work;
        if (hours > 0 || sb.length() > 0) {
        	work = "00"+hours;
        	sb.append(work.substring(work.length()-2)).append(':');
        }
        if (minutes > 0 || sb.length() > 0) {
        	work = "00"+minutes;
        	sb.append(work.substring(work.length()-2)).append(':');
        }
        work = "00"+secs;
        sb.append(work.substring(work.length()-2));
        if (showNanos) {
        	work = "000000000"+nanos;
        	sb.append('.').append(work.substring(work.length()-9));
        } else {
        	work = "000"+millis;
        	sb.append('.').append(work.substring(work.length()-3));
        }
		return sb.toString();
	}

}
