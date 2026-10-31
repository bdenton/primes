package net.wdenton.primes.mersenne;

import java.io.Serializable;

import net.wdenton.primes.math.GmpNumber;
import net.wdenton.primes.util.LongCalcTimer;

public class Mersenne2State implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Phase {NEXT_PRIME, TF, LL_INIT, LL_LOOP}
	
	Phase phase;
	int primeIx; // entry# in primes file
	int mNo; // ordinal Mersenne number last found
	int currentP; // current prime exponent
	int llLoopCtr; // LL loop counter: 0->p-2
	
	// stats & timers
	//                           	// >>> Stats to find next Mersenne prime
	LongCalcTimer findNextMpTimer;	// elapsed time
	long findNextMpLLcalcTime;		// time in actual LL calc
	int findNextMpLLcalcCnt; 		// # of LL calcs done
	int findNextMpPrimesTested; 	// # of primes tested
	
	LongCalcTimer thisPrimeTimer; 	// elapsed time processing current prime
	long thisPrimeLLcalcTime;		// time in actual LL calc
	int thisPrimeLLcalcCnt; 		// # of LL calcs done

	long[] opaqueCurrentS; // current residual in LL loop
	
	public Mersenne2State() {
		findNextMpTimer = new LongCalcTimer();
		findNextMpLLcalcTime = 0;
		thisPrimeTimer = new LongCalcTimer();
		thisPrimeLLcalcTime = 0;
	}

	public void clearFindMpStats () {
		findNextMpTimer.clear();
		findNextMpLLcalcTime= 0;
		findNextMpLLcalcCnt = 0;
		findNextMpPrimesTested = 0;
	}
	
	public void clearThisPrimeStats () {
		thisPrimeTimer.clear();
		thisPrimeLLcalcTime = 0;;
		thisPrimeLLcalcCnt = 0;
	}
	
	public void saveCurrentS (GmpNumber currentS) {
		opaqueCurrentS = currentS != null
						? currentS.getOpaqueValue()
						: null;
	}
}
