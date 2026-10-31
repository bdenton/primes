package net.wdenton.primes.math;

public abstract class GmpLucasCalcs extends GmpPerfStats64 {
	
	public static final int REALLOC_INTERVAL = 100000;
	
	protected int currentP;
	protected int currentAllocSize = 0;
	protected boolean debug = false;
	protected boolean getStats = false;
	protected GmpNumber currentS;
	protected GmpNumber dbSquared;
	protected GmpNumber dbMinus2;
	protected GmpNumber dbMod2Pm1;
	
	private static final long LONG_4 = 4l;

	public void reset () {
		currentP = -1;
		if (currentS != null) {
			currentS = currentS.clear();
		}
		if (dbSquared != null) {
			dbSquared = dbSquared.clear();
		}
		if (dbMinus2 != null) {
			dbMinus2 = dbMinus2.clear();
		}
		if (dbMod2Pm1 != null) {
			dbMod2Pm1 = dbMod2Pm1.clear();
		}
	}
	
	public void setCurrentPrime (int currentP) {
		this.currentP = currentP;
	}

	public int getCurrentPrime() {
		return currentP;
	}

	public GmpNumber getCurrentS() {
		return currentS;
	}
	
	public void allocateNumbers() {
		if (null == currentS) {
			currentS = makeGmpNumber(currentS, LONG_4);
		}
		if (debug) {
			if (null == dbSquared) {
				dbSquared = makeGmpNumber(dbSquared); // capture intermediate results;
			}
			if (null == dbMinus2) {
				dbMinus2 = makeGmpNumber(dbMinus2); // capture intermediate results;
			}
			if (null == dbMod2Pm1) {
				dbMod2Pm1 = makeGmpNumber(dbMod2Pm1); // capture intermediate results;
			}
		}
	}
	
	public GmpNumber initLucasLoop () {
		return initLucasLoop(false);
	}
	public GmpNumber initLucasLoop (boolean debug) {
		return initLucasLoop(debug, false);
	}
	public GmpNumber initLucasLoop (boolean debug, boolean getStats) {
		this.debug = debug;
		this.getStats = getStats;
		currentS = makeGmpNumber(currentS, LONG_4);
		allocateNumbers();
		if (getStats) {
			resetStats();
		}
		return currentS;
	}
	
	public GmpNumber resumeLucasLoop (int currentP, GmpNumber currentS) {
		return resumeLucasLoop(currentP, currentS, false);
	}
	public GmpNumber resumeLucasLoop (int currentP, GmpNumber currentS, boolean debug) {
		return resumeLucasLoop(currentP, currentS, debug, false);
	}
	public GmpNumber resumeLucasLoop (int currentP, GmpNumber currentS, boolean debug, boolean getStats) {
		this.debug = debug;
		this.getStats = getStats;
		setCurrentPrime(currentP);
		if (debug) {
			dbSquared = makeGmpNumber(dbSquared); // capture intermediate results;
			dbMinus2 = makeGmpNumber(dbMinus2); // capture intermediate results;
			dbMod2Pm1 = makeGmpNumber(dbMod2Pm1); // capture intermediate results;
		}
		if (null == currentS) {
			this.currentS = makeGmpNumber(currentS, LONG_4);
		} else {
			this.currentS = currentS; // assume it is the same size
		}
		if (getStats) {
			resetStats();
		}
		return currentS;
	}

	public GmpNumber makeGmpNumber (GmpNumber current, long value) {
		return makeGmpNumber(current).setValue(value);
	}
	
	public GmpNumber makeGmpNumber (GmpNumber current, long[] value) {
		return makeGmpNumber(current).setValue(value);
	}
	
	public GmpNumber makeGmpNumber (GmpNumber current) {
		GmpNumber result = current;
		if (null == current || currentP > currentAllocSize) {
			currentAllocSize = ((currentP+REALLOC_INTERVAL-1) / REALLOC_INTERVAL) * REALLOC_INTERVAL;
			if (current != null) {
				current.release(); // release current storage
			}
			result = allocateNumber(currentAllocSize);
		}
		return result;
	}
	
	private static final long[] EMPTY_STATS = new long[0];
	
	public long[] getStats () {
		long[] result = null;
		if (getStats) {
			result = new long[4];
			result[0] = elapsedTotal();
			result[1] = elapsedSpow2();
			result[2] = elapsedMinus2();
			result[3] = elapsedMod2Pm1();
		} else {
			result = EMPTY_STATS;
		}
		return result;
	}
	
	
	public abstract GmpNumber allocateNumber (int bitsize);
	
	public abstract GmpNumber calcNextResidual (int loopCnt);
	
	public abstract boolean isResidualZero ();

	public abstract String getCurrentMString ();
	
	public abstract String[] getDebugValues ();
	
}
