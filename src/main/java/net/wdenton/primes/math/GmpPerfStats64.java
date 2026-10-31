package net.wdenton.primes.math;

public class GmpPerfStats64 {
	
	// Basic calibrations - set during JNI library initialization
	protected long ticksPerSec = -1;
	protected long nanosPerTick = -1;
	protected double clockSpeedMHz = -1.0;

	// timestamps - set at various points during GmpLucasCalc processing
	protected long startTime = -1;
	protected long startSpow2 = -1;
	protected long endSpow2 = -1;
	protected long startMinus2 = -1;
	protected long endMinus2 = -1;
	protected long startMod2Pm1 = -1;
	protected long endMod2Pm1 = -1;
	protected long endTime = -1;
	
	protected GmpPerfStats64 () {
		super();
		initStats();
	}
	protected void initStats () {
		internalInitStats();
	}
	
	protected void resetStats () {
		startTime = -1;
		startSpow2 = -1;
		endSpow2 = -1;
		startMinus2 = -1;
		endMinus2 = -1;
		startMod2Pm1 = -1;
		endMod2Pm1 = -1;
		endTime = -1;
	}

	public long elapsedTotal () {
		return this.elapsedNanos(endTime - startTime);
	}
	
	public long elapsedSpow2 () {
		return this.elapsedNanos(endSpow2 - startSpow2);
	}
	
	public long elapsedMinus2 () {
		return this.elapsedNanos(endMinus2 - startMinus2);
	}
	
	public long elapsedMod2Pm1 () {
		return this.elapsedNanos(endMod2Pm1 - startMod2Pm1);
	}
	
	public long elapsedNanos (long tick) {
		return tick * nanosPerTick;
	}

//	public long elapsedMicrosecs () {
//		long tick = this.internal_elapsed();
//		return this.elapsedMicrosecs(tick);
//	}
//	public long elapsedMicrosecs (long tick) {
//		double ticksPerUSec = (0.0+ticksPerSec) / 1000000.0;
//		double fltresult = (0.0+tick) / ticksPerUSec;
//		long result = new Double(fltresult).longValue();
//		return result;
//	}
	
	private native void internalInitStats ();


}
