package net.wdenton.primes.math;

public class GmpLucasCalcs64v3 extends GmpLucasCalcs64v2 {
	
	static {
		System.loadLibrary("LucasCalcs64");
	}

	public boolean trialFactor (int currentP, int prime) {
		return internalTrialFactor(currentP, prime); 
	}
	
	private native boolean internalTrialFactor (int currentP, int prime);
	
}
