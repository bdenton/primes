package net.wdenton.primes.util;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class LongCalcTimer extends ShortCalcTimer implements SuspendableTimer, Serializable {
	private static final long serialVersionUID = -1821186616030345439L;
	private Instant suspendStop;
	private Instant latestStart;
	private boolean running = false;
	private Duration runTime = Duration.ZERO;
	private boolean trace = false;
	
	public LongCalcTimer() {
		super();
	}

	public LongCalcTimer(boolean trace) {
		super();
		this.trace = trace;
	}

	public LongCalcTimer(LongCalcTimer copy) {
		super(copy);
		this.suspendStop = copy.suspendStop;
		this.latestStart = copy.latestStart;
		this.running = copy.running;
		if (copy.runTime != null) {
			this.runTime = Duration.from(copy.runTime);
		}
	}

	@Override
	public void start() {
		if (trace) System.out.println("start: started ["+started+"] running ["+running+"]");
		super.start();
		latestStart = Instant.now();
		running = true;
		runTime = Duration.ZERO; // also resets run time
	}

	@Override
	public void suspend() {
		if (trace) System.out.println("suspend: started ["+started+"] running ["+running+"]");
		if (running) {
			suspendStop = Instant.now();
			runTime = runTime.plus(Duration.between(latestStart, suspendStop));
			running = false;
		}
	}

	@Override
	public void resume() {
		if (trace) System.out.println("resume: started ["+started+"] running ["+running+"]");
		if (!running) {
			latestStart = Instant.now();
			running = true;
		}
	}

//	@Override
//	public void startOrResume() {
//		if (null == startTime) {	// ever started?
//			start();				// no.. then start timer
//		} else {
//			resume();				// else... resume runtime
//		}
//	}

	@Override
	public void stop() {
		if (trace) System.out.println("stop: started ["+started+"] running ["+running+"]");
		suspend();
		super.stop();
	}

	@Override
	public void clear() {
		if (trace) System.out.println("clear: started ["+started+"] running ["+running+"]");
		super.clear();
		suspendStop = null;
		latestStart = null;
		running = false;
		runTime = Duration.ZERO;
	}
	
	@Override
	public Duration getRunningTime() {
		if (trace) System.out.println("get: started ["+started+"] running ["+running+"]");
		if (started) {
			if (running) {
				return runTime.plus(Duration.between(latestStart, Instant.now()));
			} 
			return runTime; // suspended -- return accumulated running time
		}
		return getElapsedTime(); // never suspended: just return elapsed
	}
	
}
