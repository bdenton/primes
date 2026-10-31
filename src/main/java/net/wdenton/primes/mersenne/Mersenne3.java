package net.wdenton.primes.mersenne;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import net.wdenton.primes.math.GmpLucasCalcs;
import net.wdenton.primes.math.GmpLucasCalcs64v3;
import net.wdenton.primes.math.GmpNumber;
import net.wdenton.primes.mersenne.Mersenne2State.Phase;
import net.wdenton.primes.util.ShortCalcTimer;
import net.wdenton.primes.util.Timer;

public class Mersenne3 {

	enum RunType {CONTINUE, FRESH_START};
	RunType runType = RunType.FRESH_START;
	boolean initialized = false;
	
	boolean debug = false;
	boolean verbose = false;
	int tfLow = 1023;
	int tfHigh = -1; // will default to highest supported by primes file
	
	static final int PROGRESS_INTERVAL_MS = 1000;
	static final long CKPT_INTERVAL_MINS = 10; // checkpoint every 10 minutes for big numbers
	static final Duration CKPT_INTERVAL_DURATION = Duration.of(CKPT_INTERVAL_MINS, ChronoUnit.MINUTES);

	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
	private static final long[] EMPTY_LIMBS = new long[0];

	private MersenneFiles files = new MersenneFiles();
	private MersenneReporter reporter = new MersenneReporter();
	GmpLucasCalcs64v3 lucasCalcs = new GmpLucasCalcs64v3();
	
	BigInteger dbCurrentM;
	BigInteger dbCurrentS;
	int primeCnt;
	int primeHigh;
	int maxFactor; // highest 2kP+1 supported by primes file
	int[] primes;
	
	Mersenne2StateV2 state = null;
	GmpNumber currentS;
	
	void process () throws IOException {
		
		primeLoop:
 		while ((state = getNextState(state)) != null) {
			Timer ckptTimer = new ShortCalcTimer(); 
			
			// 1. get next prime candidate for P
			
			if (state.phase == Phase.NEXT_PRIME) {
//				state.currentP = files.nextPrime();
//				state.primeIx++;
				state.currentP = primes[++state.primeIx];
				state.findNextMpPrimesTested++;

				state.clearThisPrimeStats();
				state.thisPrimeTimer.start(); // start processing the new prime
				
				reporter.progressP(state);
				state.phase = Phase.TF;
			}
			
			// 2. Trial Factor by a number of small primes
			
			if (state.phase == Phase.TF) {
				if (state.currentP >= tfLow && state.currentP <= tfHigh && state.currentP <= maxFactor) {
					
					int twoP = state.currentP * 2;
					
					// poor man's sqrt
					int limit = maxFactor;
					if (state.currentP < 64) {
						limit = 1 << ((state.currentP + 1) >>> 1);
					}
					int kLimit = (int)Math.ceil(((double)(limit - 1)) / (double)twoP);
					
					long nextProgress = System.currentTimeMillis();

					int primeIx = state.primeIx + 1; // start scanning after currentP
					int candidate = 1;
					
					// this loop identifies candidate primes of the form 2kP+1
					kLoop:
					while (primeIx < primeCnt) {
						candidate += twoP;
						if (candidate > limit) {
							break kLoop;
						}

						scanLoop:
						for (; primeIx < primeCnt; primeIx++) {
							if (primes[primeIx] >= candidate) {
								if (primes[primeIx] == candidate) {
									long beg = System.nanoTime();
									boolean isFactor = lucasCalcs.trialFactor(state.currentP, candidate);
									state.thisPrimeTFcalcTime += System.nanoTime() - beg;
									state.thisPrimeTFcalcCnt++;
									
									if (isFactor) {
										state.thisPrimeTimer.stop(); // finished processing the current prime
										// TODO - report each tested prime stats
										
										if (verbose) {
											reporter.progressTFfound(state, candidate);
										}
										state.findNextMpTfTests++;
										state.findNextMpTFbypassLL += state.currentP - 2;
										continue primeLoop;
									}
								}
								break scanLoop;
							}
						}

						long now = System.currentTimeMillis();
						if (now >= nextProgress) {
							reporter.progressTF(state, candidate);
							nextProgress = now + PROGRESS_INTERVAL_MS;
						}
					}
				}
				state.phase = Phase.LL_INIT;
			}
			
			// 3. Initialize Lucas-Lehmer loop
			
			if (state.phase == Phase.LL_INIT) {
				state.findNextMpLlTests++;
				ckptTimer.start();
				
				lucasCalcs.reset();
				lucasCalcs.setCurrentPrime(state.currentP);
				currentS = lucasCalcs.initLucasLoop(debug);
				if (debug) {
					dbCurrentS =  BIG4;
					dbCurrentM = BIG2.pow(state.currentP).subtract(BIG1);
				}
				
				state.phase = Phase.LL_LOOP;
				state.llLoopCtr = 0; // LL loop starting counter

			}
			
			// 4. The actual Lucas-Lehmer calc loop
			
			if (state.currentP > 2) {
				long nextProgress = System.currentTimeMillis();

				// this is really the equivalent of:
				// for ( ; state.calcCnt < state.currentP-2; state.calcCnt++) {
				// and is done this way so as to serialize changes to "state"
				while (state.llLoopCtr < state.currentP-2) {
					long beg = System.nanoTime();
					
					currentS = lucasCalcs.calcNextResidual((int)state.llLoopCtr);
					if (debug) {
						debugCppResults();
					}
					
					state.llLoopCtr++;
					state.thisPrimeLLcalcTime += System.nanoTime()-beg;
					state.thisPrimeLLcalcCnt++;

					long now = System.currentTimeMillis();
					if (now >= nextProgress) {
						reporter.progressLL(state);
						nextProgress = now + PROGRESS_INTERVAL_MS;
					}
					if (ckptTimer.getElapsedTime().compareTo(CKPT_INTERVAL_DURATION) >= 0) {
						// get persisted value of currentS
						state.saveCurrentS(currentS);
						checkpointState(state, true);
						ckptTimer.start();
					}
				}
			}

			// LL_LOOP is done.. examine results

			state.thisPrimeTimer.stop(); // finished processing the current prime
			// TODO - report each tested prime stats
			
			// add thisPrime stats to findNextMp stats
			state.findNextMpLLcalcTime += state.thisPrimeLLcalcTime;
			state.findNextMpLLcalcCnt += state.thisPrimeLLcalcCnt;
			state.findNextMpTFcalcTime += state.thisPrimeTFcalcTime;
			state.findNextMpTFcalcCnt += state.thisPrimeTFcalcCnt;

			if (2 == state.currentP || lucasCalcs.isResidualZero()) {
				
				state.mNo++;
				state.findNextMpTimer.stop(); // end of total time to find this M[p]

				String currentM = lucasCalcs.getCurrentMString();
				String reportLine = reporter.generateReportLine(state, currentM);
				String[] dataRecord = reporter.generateDataRecord(state, currentM);
				System.out.println("\r"+reportLine);
				
				files.writeReport(reportLine);
				files.writeData(dataRecord);
				
				reporter.clearProgress();
				state.clearFindMpStats();
				state.findNextMpTimer.start(); // start timer looking for next M[p]
			}
			
		}
	}
	
	// ////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////
	// ////
	// ////     State Management and Checkpointing
	// ////
	// ////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////
	
	Mersenne2StateV2 getNextState (Mersenne2StateV2 lastState) throws IOException {
		Mersenne2StateV2 result = null;
		if (initialized) {
			result = lastState;
			result.phase = Phase.NEXT_PRIME;
			result.opaqueCurrentS = EMPTY_LIMBS;
			result.llLoopCtr = -1;

			checkpointState(result, true);
			
		} else {
			initialized = true;
			if (runType == RunType.FRESH_START) {
				result = new Mersenne2StateV2();
				result.phase = Phase.NEXT_PRIME;
				files.clearResults();
				System.out.println("\n");

				result.findNextMpTimer.start(); // start timer on finding first M[p]
				
			} else { // continue from checkpoint
				result = files.readState();
				
				System.out.println("<<<< Resuming processing from last checkpoint:");
				System.out.println("<<<<   Processing phase... ["+result.phase+"]");
				System.out.println("<<<<   Current prime...... ["+result.currentP+"]");
				System.out.println("<<<<   Current LL step.... ["+result.llLoopCtr+"]\n");

//				int p = files.positionPrimes(result.primeIx);
				int p = primes[result.primeIx];
				if (p !=  result.currentP) {
					System.out.println("#### primes are misaligned");
				}
				
				if (result.phase == Phase.LL_LOOP) {
					lucasCalcs.setCurrentPrime(result.currentP);
					currentS = lucasCalcs.makeGmpNumber(currentS, result.opaqueCurrentS);
					lucasCalcs.resumeLucasLoop(result.currentP, currentS, debug);
				}
				
				result.findNextMpTimer.resume();	// this is from the -c startup
				result.thisPrimeTimer.resume();
				reporter.progressP(result);
			}
		}
		return result;
	}

	void checkpointState (Mersenne2StateV2 toSave, boolean keepGoing) {
		// freeze timers so can resume from checkpoint
		toSave.findNextMpTimer.suspend();
		if (toSave.phase != Phase.NEXT_PRIME) {
			toSave.thisPrimeTimer.suspend();
		}
		
		files.writeState(toSave);
		
		// resume timers to continue running
		if (keepGoing) {
			toSave.findNextMpTimer.resume();
			if (toSave.phase != Phase.NEXT_PRIME) {
				toSave.thisPrimeTimer.resume();
			}
		}
	}
	
	static class CkptAtExit implements Runnable {
		Mersenne3 process;
		CkptAtExit () {
		}
		CkptAtExit (Mersenne3 process) {
			this.process = process;
		}
		public void run() {
			if (process.files.haveCheckpoint()) {
				System.out.println("\n--> replacing existing checkpoint file ["+process.files.getCheckpointPath()+"]");
			} else {
				System.out.println("\n--> creating checkpoint file ["+process.files.getCheckpointPath()+"]");
			}
			process.checkpointState(process.state, false);
		}
	}

	static CkptAtExit saveState;
	

	enum ST {MATCH, NOMATCH, NOTTESTED};
	
	void debugCppResults () {
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
			System.err.println("\n\n######################### FAIL == FAIL == FAIL #########################");
			System.err.println("##");
			System.err.println("##  Lucas-Lehmer calc: JNI/GMP method does NOT MATCH Java BigInteger");
			System.err.println("##");
			System.err.println("##  Current prime: "+state.currentP);
			System.err.println("##  LL loop count: "+state.llLoopCtr);
			System.err.println("##");
			System.err.println("##  -- S[n-1]**2 ........ "+squaredSt.toString());
			System.err.println("##  -- ... - 2 .......... "+minus2St.toString());
			System.err.println("##  -- ... mod 2**P-1 ... "+mod2Pm1St.toString());
			System.err.println("##");
			System.err.println("########################################################################");
			System.exit(4);
		}
		
	}


	boolean canProcess (RunType fromArgs) {
		boolean result = true;
		System.out.println("<<<<<<<<<<  Mersenne: find Mersenne Primes >>>>>>>>>>");
		System.out.println("<<<<  File directory........... ["+files.getRootPath()+"]");
		
		files.setReporter(reporter);
		
		boolean haveCkpt = files.haveCheckpoint();
		if (fromArgs != null) {
			runType = fromArgs;
		} else {
			runType = haveCkpt ? RunType.CONTINUE : RunType.FRESH_START;
		}

		// first load the primes
		result &= files.loadPrimes();
		if (result) {
			primeCnt = files.getPrimeCount();
			primes = files.getPrimes();
			primeHigh = files.getPrimeHigh();
//			maxFactor = (primeHigh - 1) / 2; // highest supported P in 2kP+1 (k=1)
			maxFactor = primeHigh; // highest supported 2kP+1
			if (tfHigh < 0 || tfHigh > maxFactor) {
				tfHigh = maxFactor;
			}
		}
		
		System.out.println("<<<<  Run type............. ["+runType.toString()+"]");
		System.out.println("<<<<  Trial Factor range... ["+tfLow+"-"+tfHigh+"]");
		System.out.println("<<<<  Debug mode........... ["+debug+"]");
		System.out.println("<<<<  Verbose logging...... ["+verbose+"]");
		
		if (haveCkpt) {
			if (runType == RunType.FRESH_START) {
				System.out.println("<<<<  ignoring existing checkpoint file");
			}
		} else {
			if (runType == RunType.CONTINUE) {
				System.out.println("!!!!ERROR!!!!  No checkpoint file found. Cannot resume processing.");
				result = false;
			}
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		try {
			Mersenne3 engine = new Mersenne3();
			RunType fromArgs = null;
			
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String arg = args[i]; 
					if (arg.length() > 0) {
						if (arg.charAt(0) == '-') {
							switch (arg.substring(1).toLowerCase()) {
								case "c":
								case "cont":
								case "continue" :
									fromArgs = RunType.CONTINUE;
									break;
								case "n":
								case "new":
								case "reset" :
									fromArgs = RunType.FRESH_START;
									break;
								case "d":
								case "debug":
									engine.debug = true;
									break;
								case "v":
								case "verbose":
									engine.verbose = true;
									break;
								case "l":
								case "low":
								case "tl":
								case "tflow":
									engine.tfLow = getIntegerValue(arg, args, i++);
									break;
								case "h":
								case "high":
								case "th":
								case "tfhigh":
									engine.tfHigh = getIntegerValue(arg, args, i++);
									break;
								default:
									System.out.println("!! unknown command line option ["+arg+"]");
							}
						} else {
							engine.files.setRootPath(arg);
						}
					}
				}
			}
			
			if (engine.canProcess(fromArgs)) {
				saveState = new CkptAtExit(engine);
				Runtime.getRuntime().addShutdownHook(new Thread(saveState));
				engine.process();
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
