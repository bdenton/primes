package net.wdenton.primes.util;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class ShortCalcTimer implements Timer, Serializable {
	private static final long serialVersionUID = -7812881532878054550L;
	protected Instant startTime;
	protected Instant stopTime;
	protected Duration elapsedTime = Duration.ZERO;
	protected boolean started = false;
	
	public ShortCalcTimer() {
		super();
	}
	
	public ShortCalcTimer(ShortCalcTimer copy) {
		this.startTime = copy.startTime;
		this.stopTime = copy.stopTime;
		this.started = copy.started;
		if (copy.elapsedTime != null) {
			this.elapsedTime = Duration.from(copy.elapsedTime);
		}
	}

	@Override
	public void start() {  // multiple starts just resets the start time
		startTime = Instant.now();
		elapsedTime = Duration.ZERO;
		started = true;
	}

	@Override
	public void stop() {
		if (started) { // only process the 1st stop
			stopTime = Instant.now();
			elapsedTime = Duration.between(startTime, stopTime);
			started = false; // no longer started
		}
	}

	@Override
	public void clear() {
		startTime = null;
		stopTime = null;
		started = false;
		elapsedTime = Duration.ZERO;
	}

	@Override
	public Instant getStartTime() {
		return startTime;
	}

	@Override
	public Instant getStopTIme() {
		return stopTime;
	}

	@Override
	public Duration getElapsedTime() {
		if (started) {
			return  Duration.between(startTime, Instant.now()); // duration of a running timer
		}
		return elapsedTime; // else just the elapsed time it was running
	}
}
