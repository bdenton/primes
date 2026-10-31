package net.wdenton.primes.mersenne;

import java.io.Serializable;

import net.wdenton.primes.math.GmpNumber;
import net.wdenton.primes.mersenne.Mersenne2State.Phase;
import net.wdenton.primes.util.LongCalcTimer;

public class Mersenne2StateV2 implements Serializable {
	private static final long serialVersionUID = -7083292792067885605L;
	Phase phase;
	int primeIx = -1; // entry# in primes file
	int mNo; // ordinal Mersenne number last found
	int currentP; // current prime exponent
	long llLoopCtr; // LL loop counter: 0->p-2
	
	// stats & timers
	//                           	// >>> Stats to find next Mersenne prime
	LongCalcTimer findNextMpTimer;	// elapsed time
	long findNextMpLLcalcTime;		// time in actual LL calc
	long findNextMpLLcalcCnt; 		// # of LL calcs done
	long findNextMpTFcalcTime;		// time in actual TF calc
	long findNextMpTFcalcCnt; 		// # of TF calcs done
	long findNextMpTFbypassLL;		// # of LL calcs bypassed by TF tests
	int findNextMpPrimesTested; 	// # of primes tested
	int findNextMpTfTests;			// # of trial factor rejects
	int findNextMpLlTests;			// # of LL test rejects
	
	LongCalcTimer thisPrimeTimer; 	// elapsed time processing current prime
	long thisPrimeLLcalcTime;		// time in actual LL calc
	long thisPrimeLLcalcCnt; 		// # of LL calcs done
	long thisPrimeTFcalcTime;		// time in actual TF calc
	long thisPrimeTFcalcCnt; 		// # of TF calcs done

	long[] opaqueCurrentS; // current residual in LL loop
	
	public Mersenne2StateV2() {
		findNextMpTimer = new LongCalcTimer();
		thisPrimeTimer = new LongCalcTimer();
		clearFindMpStats();
		clearThisPrimeStats();
	}
	
	public Mersenne2StateV2 (Mersenne2State old) {
		this.phase = old.phase;
		this.primeIx = old.primeIx;
		this.mNo = old.mNo;
		this.currentP = old.currentP;
		this.llLoopCtr = old.llLoopCtr;
		
		this.findNextMpTimer = new LongCalcTimer(old.findNextMpTimer);
		this.findNextMpLLcalcTime = old.findNextMpLLcalcTime;
		this.findNextMpLLcalcCnt = old.findNextMpLLcalcCnt;
		this.findNextMpPrimesTested = old.findNextMpPrimesTested;
		
		this.thisPrimeTimer = new LongCalcTimer(old.thisPrimeTimer);
		this.thisPrimeLLcalcTime = old.thisPrimeLLcalcTime;
		this.thisPrimeLLcalcCnt = old.thisPrimeLLcalcCnt;

		if (old.opaqueCurrentS != null) {
			int len = old.opaqueCurrentS.length; 
			this.opaqueCurrentS = new long[len];
			if (len > 0) {
				System.arraycopy(old.opaqueCurrentS, 0, this.opaqueCurrentS, 0, len);
			}
		}
	}

	public void clearFindMpStats () {
		findNextMpTimer.clear();
		findNextMpLLcalcTime= 0;
		findNextMpLLcalcCnt = 0;
		findNextMpTFcalcTime= 0;
		findNextMpTFcalcCnt = 0;
		findNextMpTFbypassLL = 0;
		findNextMpPrimesTested = 0;
		findNextMpTfTests = 0;
		findNextMpLlTests = 0;
	}
	
	public void clearThisPrimeStats () {
		thisPrimeTimer.clear();
		thisPrimeLLcalcTime = 0;;
		thisPrimeLLcalcCnt = 0;
		thisPrimeTFcalcTime = 0;;
		thisPrimeTFcalcCnt = 0;
	}
	
	public void saveCurrentS (GmpNumber currentS) {
		opaqueCurrentS = currentS != null
						? currentS.getOpaqueValue()
						: null;
	}
}
