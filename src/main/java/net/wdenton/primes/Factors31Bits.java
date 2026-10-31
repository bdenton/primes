/**
 * 
 */
package net.wdenton.primes;

import java.io.IOException;

import net.wdenton.primes.Factors.Factor;
import net.wdenton.primes.util.AccumulatingTimer;
import net.wdenton.primes.util.ShortCalcTimer;
import net.wdenton.primes.util.Timer;

/**
 * @author WilliamDenton
 *
 */
public class Factors31Bits {

	public static final boolean DETAIL = false;
	public static final boolean FACTORS = true;
	public static final int INTERVAL = 50000; 
	
	enum RunType {NEWSTART, CONTINUE, ANALYZE};
	RunType runType = RunType.CONTINUE;
	boolean detail = DETAIL;
	boolean doFactors = FACTORS;
	
	int lowLimit = 2;
	int highLimit = 10000000;
	int runCount = -1;
	int runLimit = -1;

	PrimeFactorFiles files = new PrimeFactorFiles();
	

	// ////////////////////////////////////
	// ////  M A I N  processing
	// ////////////////////////////////////
	
	Factors getFactors (int value) {
		Factors result = new Factors();
		result.setNumber(value); // assumes prime
		
		int limit = (int)(Math.floor(Math.sqrt(value))) + 1;

		int[] primes = files.getPrimes();
		primeLoop:
		for (int i = 0; i < files.getPrimeCount() ; i++) {
			int prime = primes[i];
			if (prime <= limit) {
				int pow = 0;
				while (value%prime == 0) {
					result.setIsPrime(false);
					if (!this.doFactors) {
						break primeLoop;
					}
					pow++;
					value = value / prime;
				}
				if (pow > 0) {
					result.addFactor(prime, pow);
				}
			} else {
				break;
			}
		}
		return result;
	}
	
	void process () throws IOException {
		ShortCalcTimer processTimer = new ShortCalcTimer();
		ShortCalcTimer perPrimeTimer = new ShortCalcTimer();
		AccumulatingTimer totalSieveTime = new AccumulatingTimer();
		long genCount = 0; // number generated this interval
		long totalCount = 0; // total primes generated
		int currentPrime = -1;
		boolean running = true;
		
		processTimer.start();
		
		for (int value = lowLimit; running; value++) {
			if (runCount > 0) {
				highLimit = currentPrime;
				if (totalCount >= runCount) {
					running = false;
				}
			} else
			if (value >= highLimit) {
				running = false;
			}
			if (running) {
				perPrimeTimer.start();
				Factors factors = getFactors(value);
				perPrimeTimer.stop();
				totalSieveTime.addTime(perPrimeTimer.getElapsedTime());
				
				if (detail) System.out.print(""+value+" ["+Timer.tellDuration(perPrimeTimer.getElapsedTime())+"]");
				if (factors.isPrime()) {
					if (detail) System.out.println(" PRIME");
					files.addPrime(value);
					currentPrime = value;
					genCount++;
				} else if (doFactors && detail) {
					StringBuilder sb = new StringBuilder();
					for (Factor factor : factors.getFactors()) {
						if (sb.length() > 0) {
							sb.append(" *");
						}
						sb.append(' ').append(factor.prime);
						if (factor.power > 1) {
							sb.append("^").append(factor.power);
						}
					}
					System.out.println(sb.toString());
				}
				
				if (doFactors) {
					files.writeFactors(factors);
				}
			}
			
			if (!detail || !running) {
				if (genCount >= INTERVAL || !running) {
					String runningTime = Timer.tellDuration(processTimer.getElapsedTime());
					String sieveTime = Timer.tellDuration(totalSieveTime.getAccumulatedTime());
					String avgSieveTime = Timer.tellAvgDuration(totalSieveTime.getAccumulatedTime(), totalSieveTime.getTimerTicks());
					totalSieveTime.reset();
					totalCount += genCount;
					genCount = 0;
					
					System.out.println(runningTime+"... "+totalCount+" : "+currentPrime+" ("+sieveTime+"/"+avgSieveTime+")");
				}
			}
		}

		if (doFactors) {
			files.closeFactors();
		}
		files.updatePrimes();
		
		System.out.println("\n===> There are ["+files.getPrimeCount()+
				           "] primes between ["+lowLimit+"] and ["+highLimit+
				           "] found in "+Timer.tellDuration(processTimer.getElapsedTime()));
		if (doFactors) {
			System.out.println("===> Largest number of factors ["+files.getMaxFactors()+"]; largest exponent ["+files.getMaxPower()+"]");
		}
	}
	
	boolean canProcess (RunType fromArgs) throws IOException {
		boolean result = true;
		
		switch (fromArgs) {
			case NEWSTART: {
				lowLimit = 2;
				result &= files.initPrimes();
				break;
			}
			case CONTINUE: {
				result &= files.loadPrimes();
				if (doFactors) {
					result &= files.findLastFactor();
					lowLimit = files.getLastFactor() + 1;
				} else {
					lowLimit = files.getPrimes()[files.getPrimeCount()-1] + 1;
				}
				break;
			}
			default: {}
		}
		
		if (runLimit > 2) {
			highLimit = runLimit;
		}
		

		System.out.println("<<<<<<<<<<  Factors31Bits: find prime factors of 31-bit numbers >>>>>>>>>>");
		System.out.println("<<<<  File directory..... ["+files.getRootPath()+"]");
		System.out.println("<<<<  Include factors?... ["+this.doFactors+"]");
		System.out.println("<<<<  Starting number.... ["+lowLimit+"]");
		if (runCount > 0) {
			System.out.println("<<<<  Prime count........ ["+runCount+"]");
		} else {
			System.out.println("<<<<  Ending number...... ["+highLimit+"]");
		}
		System.out.println("");
		
		if (lowLimit > 2) {
			if (files.getPrimeCount() <= 0) {
				System.out.println("!!!!ERROR!!!!  No primes file found. Required when starting number > 2");
				result = false;
			}
		} 
		if (lowLimit < 2) {
			System.out.println("!!!!ERROR!!!!  Invalid starting number. Must be >= 2");
			result = false;
		}
		if (runCount <= 0 && highLimit < lowLimit) {
			System.out.println("!!!!ERROR!!!!  Invalid ending number. Must be >= starting number");
			result = false;
		}
		if (doFactors) {
			result &= files.initFactorsOutput(fromArgs == RunType.NEWSTART);
		}
		
		runType = fromArgs;
		
		return result;
	}

	public static void main(String[] args) {
		Factors31Bits engine = new Factors31Bits();
		RunType fromArgs = engine.runType;
		boolean canRun = true;
		
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
								fromArgs = RunType.NEWSTART;
								break;
							case "d":
							case "detail":
								engine.detail = true;
								break;
							case "#":
							case "count":
								if (i < args.length-1) {
									String value = args[++i];
									try {
										engine.runCount = Integer.parseInt(value);
									} catch (NumberFormatException e) {
										System.out.println("!! option ["+arg+"] invalid value ["+value+"]");
										canRun = false;
									}
								} else {
									System.out.println("!! option ["+arg+"] needs a numeric value");
									canRun = false;
								}
								break;
							case "l":
							case "limit":
								if (i < args.length-1) {
									String value = args[++i];
									try {
										engine.runLimit = Integer.parseInt(value);
									} catch (NumberFormatException e) {
										System.out.println("!! option ["+arg+"] invalid value ["+value+"]");
										canRun = false;
									}
								} else {
									System.out.println("!! option ["+arg+"] needs a numeric value");
									canRun = false;
								}
								break;
							case "nofactors":
								engine.doFactors = false;
								break;
							default:
								System.out.println("!! unknown command line option ["+arg+"]");
								canRun = false;
						}
					} else {
						engine.files.setRootPath(arg);
					}
				}
			}
		}
		
		try {
			if (canRun && engine.canProcess(fromArgs)) {
				engine.process();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
}
