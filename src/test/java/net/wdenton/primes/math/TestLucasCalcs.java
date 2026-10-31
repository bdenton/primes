package net.wdenton.primes.math;

import java.io.IOException;
import java.math.BigInteger;

public class TestLucasCalcs {

	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
	
	
	// ////////////////////////////////////
	// ////  PRIMES file management
	// ////////////////////////////////////

	int currentP; // current prime exponent
	BigInteger currentM; // current M[p] (2**p)-1
	BigInteger currentS; // current residual in LL loop
	BigInteger squared;
	BigInteger minus2;
	GmpLucasCalcs64v2 gmpLucasCalcs = new GmpLucasCalcs64v2();
	GmpNumber gmpCurrentS;
	BigInteger compCurrentS;
	
	void process (int currentP, int LLtoStop, boolean debug) throws IOException {
		
		currentM = BIG2.pow(currentP).subtract(BIG1);
		System.out.println(">>> Prime ["+currentP+"]; M[p] ["+getMpString(currentM)+"]");

		gmpLucasCalcs.setCurrentPrime(currentP);
		gmpLucasCalcs.setSqrMethod(0);
		
		// phase = LL_LOOP
		
		currentS = compCurrentS =  BIG4;
		gmpCurrentS = gmpLucasCalcs.initLucasLoop(true);


		int calcCnt = -1;
		int startDump = LLtoStop > 1 ? LLtoStop-1 : 1;
		for (calcCnt = 0; calcCnt < currentP-2; calcCnt++) {
			squared = currentS.pow(2);
			minus2 = squared.subtract(BIG2);
			currentS = minus2.mod(currentM);
			compCurrentS = compCurrentS.pow(2).subtract(BIG2).mod(currentM);
			gmpCurrentS = gmpLucasCalcs.calcNextResidual();
			
//			if (calcCnt+1 >= startDump) {
//				dumpResults(calcCnt+1);
//			}
//			if (calcCnt+1 > LLtoStop) {
//				break;
//			}
		}
		System.out.println("prime ["+currentP+"] "+(gmpLucasCalcs.isResidualZero()? "IS":"IS NOT")+" a Mersenne prime");
		
	}

//	System.out.println("   "+calcCnt+": sq: ["+squared+"]["+Long.toString(squared,16)+
//			                         "] m2: ["+minus2+"]["+Long.toString(minus2,16)+
//			                         "] mod: ["+currentS+"]["+Long.toString(currentS,16)+
//			                         "] mymod: ["+myMod+"]["+Long.toString(myMod,16)+"]");

	
	enum ST {MATCH, NOMATCH, NOTTESTED};
	
	void dumpResults(int calcCnt) {
		boolean success = true;
		String dbSquared = squared.toString();
		ST squaredSt = ST.NOTTESTED;
		String dbMinus2 = minus2.toString();
		ST minus2St = ST.NOTTESTED;
		String dbMod2Pm1 = currentS.toString();
		ST mod2Pm1St = ST.NOTTESTED;
		String dbCompCurrentS = compCurrentS.toString();
		ST compMatch = ST.NOTTESTED;
		
		String[] cppValues= gmpLucasCalcs.getDebugValues();
		if (cppValues != null) {
			if (cppValues.length > 0 && cppValues[0] != null) {
				if (dbSquared.equalsIgnoreCase(cppValues[0])) {
					squaredSt = ST.MATCH;
				} else {
					squaredSt = ST.NOMATCH;
					success = false;
				}
			}
			if (cppValues.length > 1 && cppValues[1] != null) {
				if (dbMinus2.equalsIgnoreCase(cppValues[1])) {
					minus2St = ST.MATCH;
				} else {
					minus2St = ST.NOMATCH;
					success = false;
				}
			}
			if (cppValues.length > 2 && cppValues[2] != null) {
				if (dbMod2Pm1.equalsIgnoreCase(cppValues[2])) {
					mod2Pm1St = ST.MATCH;
				} else {
					mod2Pm1St = ST.NOMATCH;
					success = false;
				}
			}
			if (dbMod2Pm1.equalsIgnoreCase(dbCompCurrentS)) {
				compMatch = ST.MATCH;
			} else {
				compMatch = ST.NOMATCH;
				success = false;
			}
		}
		
		if (success) {
			System.out.println("\n["+calcCnt+"] ********************************************************************");
		} else {
			System.out.println("\n["+calcCnt+"] **** FAIL ** FAIL ** FAIL ** FAIL **********************************");
		}
		System.out.println("**");
		System.out.println("**   S[n-1]**2 ........ "+squaredSt.toString()+":");
		showValues(dbSquared, cppValues[0]);
		System.out.println("**   ... - 2 .......... "+minus2St.toString()+":");
		showValues(dbMinus2, cppValues[1]);
		System.out.println("**   ... mod 2**P-1 ... "+mod2Pm1St.toString()+" "+compMatch.toString()+":");
		showValues(dbMod2Pm1, cppValues[2], dbCompCurrentS);
		System.out.println("**");
		System.out.println("************************************************************************");
	}
	
	void showValues (String bi, String gmp) {
		System.out.println("**       BI:  "+getMpString(bi));
		System.out.println("**       gmp: "+getMpString(gmp));
	}
	void showValues (String bi, String gmp, String comp) {
		showValues(bi, gmp);
		System.out.println("**       BI+: "+getMpString(comp));
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
			int currentP = -1;
			int LLtoStop = -1;
			boolean debug = false;
			
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String arg = args[i]; 
					if (arg.length() > 0) {
						if (arg.charAt(0) == '-') {
							switch (arg.substring(1).toLowerCase()) {
								case "p":
								case "prime":
									currentP = getIntegerValue(arg, args, i++);
									break;
								case "l":
								case "llcount":
									LLtoStop = getIntegerValue(arg, args, i++);
									break;
								case "d":
								case "debug":
									debug = true;
									break;
								default:
									System.out.println("!! unknown command line option ["+arg+"]");
							}
						}
					}
				}
			}
			if (currentP > 0 && LLtoStop > 0) {
				TestLucasCalcs engine = new TestLucasCalcs();
				engine.process(currentP, LLtoStop, debug);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
	
	private static int getIntegerValue (String option, String[] args, int ix) {
		int result = -1;
		if (ix+1 < args.length) {
			String valueS = args[ix+1];
			try {
				result = Integer.parseInt(valueS);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Invalid ["+option+"] option value ["+valueS+"]. ("+e.getLocalizedMessage()+")");
			}

		} else {
			throw new ArrayIndexOutOfBoundsException("The option ["+option+"] has no value");
		}
	
		return result;
	}
}
