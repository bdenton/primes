package net.wdenton.primes.math;

import java.io.IOException;
import java.math.BigInteger;

public class TestMersenne {

	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
	
	
		int currentP; // current prime exponent
		int calcCnt; // LL loop counter: 0->p-2
		BigInteger currentM; // current M[p] (2**p)-1
		BigInteger currentS; // current residual in LL loop
	
	void process () throws IOException {
		
		currentP = 127;
		calcCnt = 0; // LL loop starting counter
		currentS =  BIG4;
		currentM = BIG2.pow(currentP).subtract(BIG1);
				
		while (calcCnt < currentP-2) {
						
			currentS = currentS.pow(2).subtract(BIG2).mod(currentM);
			System.out.println("["+calcCnt+"] "+getMpString(currentS));
						
			calcCnt++;
		}

			// LL_LOOP is done.. examine results

		if (2 == currentP || (currentS.compareTo(BigInteger.ZERO) == 0)) {
			
			String strM = currentM.toString();
			int digits = strM.length();
			String num = getMpString(strM);
		
			System.out.println("\nM[p]: "+num);
			System.out.println("digits: "+digits);
		} else {
			System.out.println(">>> ["+currentP+"] is not a Mersenne Prime");
		}
	}

	String getMpString (BigInteger Mp) {
		return getMpString(Mp.toString());
	}
	
	String getMpString (String result) {
		int digits = result.length();
		if (digits >= 50) {
			result = result.substring(0, 24) + "..." + result.substring(digits-24);
		}
		return result;
	}
	
	
	public static void main(String[] args) {
		try {
			TestMersenne engine = new TestMersenne();
			engine.process();
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
