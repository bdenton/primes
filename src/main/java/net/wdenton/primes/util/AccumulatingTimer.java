package net.wdenton.primes.util;

import java.io.Serializable;
import java.time.Duration;

public class AccumulatingTimer extends ShortCalcTimer implements Timer, Serializable {
	private static final long serialVersionUID = 1L;

	private Duration accumulatedTime = Duration.ZERO;
	private long ticksAdded = 0;

	@Override
	public void stop() {
		super.stop();
		accumulatedTime = accumulatedTime.plus(elapsedTime);
	}

	@Override
	public void clear() {
		reset();
	}
	
	public void addTime (AccumulatingTimer added) {
		accumulatedTime = accumulatedTime.plus(added.getElapsedTime());
		ticksAdded++;
	}
	public void addTime (Duration added) {
		accumulatedTime = accumulatedTime.plus(added);
		ticksAdded++;
	}
	public void reset () {
		accumulatedTime = Duration.ZERO;
		ticksAdded = 0;
	}

	public Duration getAccumulatedTime() {
		return accumulatedTime;
	}
	
	public long getTimerTicks () {
		return ticksAdded;
	}
	
}
