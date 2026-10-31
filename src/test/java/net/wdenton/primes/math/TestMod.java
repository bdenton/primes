package net.wdenton.primes.math;

import java.io.IOException;
import java.math.BigInteger;

public class TestMod {

	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
	
	
	// ////////////////////////////////////
	// ////  PRIMES file management
	// ////////////////////////////////////

	int[] trialPs = {11, 13, 17, 19, 23};
	
	int currentP; // current prime exponent
	long currentM; // current M[p] (2**p)-1
	long currentS; // current residual in LL loop
	
	void process () throws IOException {
		
		for (int currentP : trialPs) {
			currentM = BIG2.pow(currentP).subtract(BIG1).longValue();
			System.out.println(">>> Prime ["+currentP+"]; M[p] ["+currentM+"]["+Long.toString(currentM,16)+"]");
				
			// phase = LL_LOOP
			
			currentS =  4;

			for (int calcCnt = 0; calcCnt < currentP-2; calcCnt++) {
				long squared = currentS * currentS;
				long minus2 = squared - 2;
				currentS = minus2 % currentM;
				long myMod = myMod(minus2, currentM, currentP);
				
				System.out.println("   "+calcCnt+": sq: ["+squared+"]["+Long.toString(squared,16)+
						                         "] m2: ["+minus2+"]["+Long.toString(minus2,16)+
						                         "] mod: ["+currentS+"]["+Long.toString(currentS,16)+
						                         "] mymod: ["+myMod+"]["+Long.toString(myMod,16)+"]");
			}
		}
	}
	
	long myMod(long partialResult, long twoPm1, int currentP) {
		long result = -1;
		if (partialResult > twoPm1) {
			result = partialResult & twoPm1;
			partialResult = partialResult >>> currentP;
			partialResult += result;
			if (partialResult > twoPm1) {
				result = partialResult & twoPm1;
				partialResult = partialResult >>> currentP;
				partialResult += result;
			}
		}
		result = partialResult;
		return result;
	}

	
	public static void main(String[] args) {
		try {
			TestMod engine = new TestMod();
			engine.process();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
