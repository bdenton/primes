package net.wdenton.primes.math;

public class GmpLucasCalcs64 extends GmpLucasCalcs {
	
	static {
		System.loadLibrary("LucasCalcs64");
	}

	private GmpNumber work1;
	private GmpNumber work2;
	

	@Override
	public void reset () {
		super.reset();
		if (work1 != null) {
			work1.release();
		}
		work1 = null;
		if (work2 != null) {
			work2.release();
		}
		work2 = null;
	}

	@Override
	public GmpNumber allocateNumber(int bitsize) {
		return new GmpNumber64(bitsize);
	}

	@Override
	public void allocateNumbers() {
		super.allocateNumbers();
		if (null == work1) {
			work1 = makeGmpNumber(work1); // work value to reduce reallocations
		}
		if (null == work2) {
			work2 = makeGmpNumber(work2); // work value to reduce reallocations
		}
	}
	
	@Override
	public GmpNumber resumeLucasLoop (int currentP, GmpNumber currentS, boolean debug, boolean getStats) {
		work1 = makeGmpNumber(work1); // work value to reduce reallocations
		work2 = makeGmpNumber(work2); // work value to reduce reallocations
		return super.resumeLucasLoop(currentP, currentS, debug, getStats);
	}
	
	@Override
	public GmpNumber calcNextResidual () {
		// S**2 - 2 mod M
		internalCalcNextResidual(
				(GmpNumber64)currentS, currentP, 
				(GmpNumber64)work1, (GmpNumber64)work2, 
				debug, getStats,
				(GmpNumber64)dbSquared, (GmpNumber64)dbMinus2, (GmpNumber64)dbMod2Pm1);
		return currentS;
	}
	
	@Override
	public boolean isResidualZero () {
		return ((GmpNumber64)currentS).getUsedLimbCount() == 0;
	}

	@Override
	public String getCurrentMString () {
		String result = internalGetCurrentMString(currentP, (GmpNumber64)work1);
		if (result != null) {
			result = result.trim();
		}
		return result;
	}
	
	@Override
	public String[] getDebugValues () {
		String[] result = null;
		if (debug) {
			result = new String[] {null, null, null};
			if (dbSquared != null) {
				result[0] = internalGetGmpString((GmpNumber64)dbSquared);
			}
			if (dbMinus2 != null) {
				result[1] = internalGetGmpString((GmpNumber64)dbMinus2);
			}
			if (dbMod2Pm1 != null) {
				result[2] = internalGetGmpString((GmpNumber64)dbMod2Pm1);
			}
		}
		return result;
	}
	
	private native void internalCalcNextResidual (
			GmpNumber64 currentS, int currentP,
			GmpNumber64 work1, GmpNumber64 work2,
			boolean debug, boolean getStats,
			GmpNumber64 dbSquared, GmpNumber64 dbMinus2, GmpNumber64 dbMod2Pm1);
	
	private native String internalGetCurrentMString (int currentP, GmpNumber64 work1);
	
	private native String internalGetGmpString (GmpNumber64 value);
	
}
