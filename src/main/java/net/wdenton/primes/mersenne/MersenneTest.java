package net.wdenton.primes.mersenne;

import java.io.IOException;
import java.math.BigInteger;

import net.wdenton.primes.math.GmpLucasCalcs64v2;
import net.wdenton.primes.math.GmpNumber;

public class MersenneTest {

	boolean debug = false;
	
	static final int PROGRESS_INTERVAL_MS = 1000;

	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
//	private static final long[] EMPTY_LIMBS = new long[0];

	private MersenneReporter reporter = new MersenneReporter();
	GmpLucasCalcs64v2 lucasCalcs = new GmpLucasCalcs64v2();
	
	int sqrMethod = 0;
	
	BigInteger dbCurrentM;
	BigInteger dbCurrentS;
	
	Mersenne2StateV2 state = new Mersenne2StateV2();
	GmpNumber currentS;
	
	void process (int prime) throws IOException {
		state.currentP = prime;

		state.clearThisPrimeStats();
		state.thisPrimeTimer.start(); // start processing the new prime
		
		lucasCalcs.reset();
		lucasCalcs.setCurrentPrime(state.currentP);

		if (sqrMethod >= 0) {
			lucasCalcs.setSqrMethod(sqrMethod);
		}
		currentS = lucasCalcs.initLucasLoop(debug, true);
		if (debug) {
			dbCurrentS =  BIG4;
			dbCurrentM = BIG2.pow(state.currentP).subtract(BIG1);
		}
		
		state.llLoopCtr = 0; // LL loop starting counter

		reporter.progressPstats(state);
		
		// phase = LL_LOOP
		
		if (state.currentP > 2) {
			long[] totalStats = {0l, 0l, 0l, 0l};
			
			long nextProgress = System.currentTimeMillis() + PROGRESS_INTERVAL_MS;;
			int progressCtr = 0;
			long[] progressStats = {0l, 0l, 0l, 0l};
			boolean keepGoing = true;

			// this is really the equivalent of:
			// for ( ; state.calcCnt < state.currentP-2; state.calcCnt++) {
			// and is done this way so as to serialize changes to "state"
			while (keepGoing && state.llLoopCtr < state.currentP-2) {
		//		System.out.println("\n+++ "+state.llLoopCtr+" +++++++++++++++++++++++++++++");
						
				currentS = lucasCalcs.calcNextResidual((int)state.llLoopCtr+1);
				long[] stats = lucasCalcs.getStats();
				
				if (debug) {
					keepGoing = debugCppResults();
				}

				state.llLoopCtr++;
				progressCtr++;
				for (int i = 0; i < 4; i++) {
					progressStats[i] += stats[i];
					totalStats[i] += stats[i];
				}
				
				long now = System.currentTimeMillis();
				if (now >= nextProgress) {
					reporter.progressLLstats(state, totalStats, progressCtr, progressStats);
					nextProgress = now + PROGRESS_INTERVAL_MS;
					for (int i = 0; i < 4; i++) {
						progressStats[i] = 0;
					}
					progressCtr = 0;
				}
			}

			if (progressStats[0] > 0) {
				for (int i = 0; i < 4; i++) {
					totalStats[i] += progressStats[i];
				}
				reporter.reportStats(state, totalStats);
			}	
		}
	}

	enum ST {MATCH, NOMATCH, NOTTESTED};
	
	boolean debugCppResults () {
		boolean success = true;
		dbCurrentS = dbCurrentS.pow(2);
		String dbSquared = dbCurrentS.toString();
		ST squaredSt = ST.NOTTESTED;
		dbCurrentS = dbCurrentS.subtract(BIG2);
		String dbMinus2 = dbCurrentS.toString();
		ST minus2St = ST.NOTTESTED;
		dbCurrentS = dbCurrentS.mod(dbCurrentM);
		String dbMod2Pm1 = dbCurrentS.toString();
		ST mod2Pm1St = ST.NOTTESTED;
		
		String[] cppValues= lucasCalcs.getDebugValues();
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
		}
		
		if (!success) {
			System.out.println("\n\n######################### FAIL == FAIL == FAIL #########################");
			System.out.println("##");
			System.out.println("##  Lucas-Lehmer calc: JNI/GMP method does NOT MATCH Java BigInteger");
			System.out.println("##");
			System.out.println("##  Current prime: "+state.currentP);
			System.out.println("##  LL loop count: "+state.llLoopCtr);
			System.out.println("##");
			System.out.println("##  -- S[n-1]**2 ........ "+squaredSt.toString());
			if (squaredSt == ST.NOMATCH) {
				System.out.println("##               GMP .... "+cppValues[0] );
				System.out.println("##               Java ... "+dbSquared );
			}
			System.out.println("##  -- ... - 2 .......... "+minus2St.toString());
			if (minus2St == ST.NOMATCH) {
				System.out.println("##               GMP .... "+cppValues[1] );
				System.out.println("##               Java ... "+dbMinus2 );
			}
			System.out.println("##  -- ... mod 2**P-1 ... "+mod2Pm1St.toString());
			if (mod2Pm1St == ST.NOMATCH) {
				System.out.println("##               GMP .... "+cppValues[2] );
				System.out.println("##               Java ... "+dbMod2Pm1 );
			}
			System.out.println("##");
			System.out.println("########################################################################");
			//System.exit(4);
		}
		return success;
	}
	
	public static void main(String[] args) {
		try {
			MersenneTest engine = new MersenneTest();
			int prime = -1;
			
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String arg = args[i]; 
					if (arg.length() > 0) {
						if (arg.charAt(0) == '-') {
							switch (arg.substring(1).toLowerCase()) {
							case "d":
							case "debug":
								engine.debug = true;
								break;
							case "s":
							case "sqr":
								if (i < args.length-1) {
									String value = args[++i];
									try {
										engine.sqrMethod = Integer.parseInt(value);
									} catch (NumberFormatException e) {
										System.out.println("!! option ["+arg+"] invalid value ["+value+"]");
										System.exit(4);
									}
								} else {
									System.out.println("!! option ["+arg+"] needs a numeric value");
									System.exit(4);
								}
								break;
							default:
								System.out.println("!! unknown command line option ["+arg+"]");
							}
						} else {
							try {
								prime = Integer.parseInt(arg);
							} catch (NumberFormatException e) {
								System.out.println("!! test prime invalid value ["+arg+"]");
								System.exit(4);
							}
						}
					}
				}
			}
			
			engine.process(prime);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
