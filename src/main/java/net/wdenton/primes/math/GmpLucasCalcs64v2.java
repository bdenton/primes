package net.wdenton.primes.math;

public class GmpLucasCalcs64v2 extends GmpLucasCalcs {
	
	static {
		System.loadLibrary("LucasCalcs64");
	}

	private int sqrMethod = 0; // 0=mpn_sqr 1=mpn_mul_n 2=mpn_mul
	private GmpNumber work1;
	private GmpNumber work2;
	

	@Override
	public void reset () {
		super.reset();
		if (work1 != null) {
			work1 = work1.clear();
		}
		if (work2 != null) {
			work2 = work2.clear();
		}
	}

	@Override
	public GmpNumber allocateNumber(int bitsize) {
		return new GmpNumber64v2(bitsize);
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
	
	public int getSqrMethod() {
		return sqrMethod;
	}

	public void setSqrMethod(int sqrMethod) {
		this.sqrMethod = sqrMethod;
	}

	@Override
	public GmpNumber calcNextResidual (int loopCnt) {
		// S**2 - 2 mod M
		internalCalcNextResidual(
				loopCnt,
				(GmpNumber64v2)currentS, currentP, 
				sqrMethod,
				(GmpNumber64v2)work1, (GmpNumber64v2)work2, 
				debug, getStats,
				(GmpNumber64v2)dbSquared, (GmpNumber64v2)dbMinus2, (GmpNumber64v2)dbMod2Pm1);
		return currentS;
	}
	
	@Override
	public boolean isResidualZero () {
		return internalIsZero((GmpNumber64v2)currentS);
	}

	@Override
	public String getCurrentMString () {
		String result = internalGetCurrentMString(currentP, (GmpNumber64v2)work1);
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
				result[0] = internalGetGmpString((GmpNumber64v2)dbSquared);
			}
			if (dbMinus2 != null) {
				result[1] = internalGetGmpString((GmpNumber64v2)dbMinus2);
			}
			if (dbMod2Pm1 != null) {
				result[2] = internalGetGmpString((GmpNumber64v2)dbMod2Pm1);
			}
		}
		return result;
	}
	
	public GmpNumber getWork1() {
		return work1;
	}

	public GmpNumber getWork2() {
		return work2;
	}

	private native void internalCalcNextResidual (
			int loopCnt,
			GmpNumber64v2 currentS, int currentP,
			int sqrMethod,
			GmpNumber64v2 work1, GmpNumber64v2 work2,
			boolean debug, boolean getStats,
			GmpNumber64v2 dbSquared, GmpNumber64v2 dbMinus2, GmpNumber64v2 dbMod2Pm1);
	
	private native String internalGetCurrentMString (int currentP, GmpNumber64v2 work1);
	
	private native String internalGetGmpString (GmpNumber64v2 value);
	
	private native boolean internalIsZero (GmpNumber64v2 value);
	
}
