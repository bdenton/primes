package net.wdenton.primes.util;

import java.time.Duration;

public interface SuspendableTimer extends Timer {
	public void suspend ();
	public void resume ();
//	public void startOrResume ();
	public Duration getRunningTime ();
}
