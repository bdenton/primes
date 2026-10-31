package net.wdenton.primes.math;

import java.math.BigInteger;

import net.wdenton.primes.mersenne.MersenneFiles;

class MersenneFactorCheck {

	private static MersenneFiles files = new MersenneFiles();
	private static 	GmpLucasCalcs64v3 lucasCalcs = new GmpLucasCalcs64v3();

	private static int primeCnt;
	private static int primeIx;
	private static int primeHigh;
	private static int[] primes;

	private static boolean verbose = false;
	private static boolean details = true;
	
	private final static BigInteger TWO = BigInteger.valueOf(2);

	public static boolean isPrime (long n) {
		if (n == 2)
			return true;
		if ((n < 2) || ((n & 1) == 0))
			return false;
		long maxFactor = (long) Math.sqrt((double) n);
		for (long possibleFactor = 3; possibleFactor <= maxFactor; possibleFactor += 2)
			if ((n % possibleFactor) == 0)
				return false;
		return true;
	}

	public static BigInteger findFactorMersenneNumber (int primeP) {
		if (primeP <= 0) {
			throw new IllegalArgumentException();
		}
		BigInteger bigP = BigInteger.valueOf(primeP);
		BigInteger m = BigInteger.ONE.shiftLeft(primeP).subtract(BigInteger.ONE);
		// There are more complicated ways of getting closer to sqrt(), but not that
		// important here, so go with simple
		BigInteger maxFactor = BigInteger.ONE.shiftLeft((primeP + 1) >>> 1);
		BigInteger twoP = BigInteger.valueOf(primeP << 1);
		BigInteger possibleFactor = BigInteger.ONE;
		int possibleFactorBits12 = 0;
		int twoPBits12 = primeP & 3;

		while ((possibleFactor = possibleFactor.add(twoP)).compareTo(maxFactor) <= 0) {
			possibleFactorBits12 = (possibleFactorBits12 + twoPBits12) & 3;
			// "Furthermore, q must be 1 or 7 mod 8". We know it's odd due to the +1 done
			// above, so bit 0 is set. Therefore, we only care about bits 1 and 2 equaling
			// 00 or 11
			if ((possibleFactorBits12 == 0) || (possibleFactorBits12 == 3))
				if (TWO.modPow(bigP, possibleFactor).equals(BigInteger.ONE))
					return possibleFactor;
		}
		return null;
	}
	

	public static void checkMersenneNumber(int p) {
		if (!isPrime(p)) {
			System.out.println("M" + p + " is not prime");
			return;
		}
		BigInteger factor = findFactorMersenneNumber(p);
		if (factor == null)
			System.out.println("M" + p + " is prime");
		else
			System.out.println("M" + p + " is not prime, has factor " + factor);
		return;
	}

	
	public static int[] checkMersenneFactors (int prime) {
		int[] result = new int[2];
		result[0] = 0; // assume prime
		
		if (verbose) System.out.println("\nPrime: "+prime);
		
		int twoP = prime * 2;
		
		// poor man's sqrt
//		int test = (int)(Math.ceil(Math.sqrt((double)prime)));
		BigInteger maxFactor = BigInteger.ONE.shiftLeft((prime + 1) >>> 1);
		// limit for 2kP+1
		int limit = Integer.MAX_VALUE;
		try {
			limit = maxFactor.intValueExact();
		} catch (ArithmeticException e) {
			;
		}
		
		if (verbose) System.out.println("   max factor: "+limit);
		
		int kLimit = (int)Math.ceil(((double)(limit - 1)) / (double)twoP);
		
		if (verbose) System.out.println("   max K: "+kLimit);
		
		int ix = primeIx + 1;
		int candidate = 1;
		
		kLoop:
		for (int k = 1; ; k++) {
			candidate += twoP;
			if (verbose) System.out.print("   K: "+k+" candidate: "+candidate);
			if (candidate > limit) {
				if (verbose) System.out.println(" exceeds sqrt(P)");
				break kLoop;
			}
			
			int factor = 0;
			for (; ix < primeCnt; ix++) {
				if (primes[ix] >= candidate) {
					if (primes[ix] == candidate) {
						factor = candidate;
					}
					break;
				}
			}
			if (ix >= primeCnt) {
				if (verbose) System.out.println(" is greater than known primes");
				break kLoop;
			}
			if (0 == factor) {
				if (verbose) System.out.println(" is not prime");
				continue kLoop;
			}
			
//			boolean isFactor = trialFactor(prime, factor);
			boolean isFactor = lucasCalcs.trialFactor(prime, factor);
			
			if (isFactor) {
				if (verbose) System.out.println(" is prime factor of M[p]");
				result[0] = factor;
				result[1] = k;
				break kLoop;
			} else {
				result[1] = k;
				if (verbose) System.out.println(" is prime");
			}
		}
		return result;
	}
	
	public static boolean trialFactor (int currentP, int factor) {
		boolean result = false;

		if (factor > 2) {
			int testBit = Integer.highestOneBit(currentP);
			int square = 1;
			while (testBit > 0) {
				square = (int)(((long)square * square) % factor);
				if ((currentP & testBit) != 0) {
					square <<= 1;
					if (square > factor) {
						square -= factor;
					}
				}
				testBit >>= 1;
			}
			if (1 == square) {
				result = true;
			}
		}

		return result;
	}
	
	
	public static void main (String[] args) {
		files.setRootPath("/primes");
		files.loadPrimes();
		primeCnt = files.getPrimeCount();
		primes = files.getPrimes();
		primeHigh = files.getPrimeHigh();
		int maxFactor = (primeHigh - 1) / 2; // highest supported P in 2kP+1 (k=1) 
		
		int low = 1;
		int interval = 10000;
		int cntPrimes = 0;
		int cntFactors = 0;
		int cntK = 0;
		
		for (primeIx = 1; primeIx < primeCnt; primeIx++) {
			int prime = primes[primeIx];
			if (prime <= maxFactor) {
				cntPrimes++;
				// result[0] = possible factor or 0
				// result[1] = factors 'k' in 2kP+1
				int[] result = checkMersenneFactors(prime);
				if (0 == result[0]) {
					cntFactors++;
				}
				cntK += result[1];
				if (details) System.out.println("M["+prime+"] "+(0==result[0] ? "has no small factors" : "has factor of "+result[0]+" (k="+result[1]+")"));
				if (prime >= interval) {
					int pct = (cntFactors * 100) / cntPrimes;
					if (details) System.out.print("\n");
					System.out.println(">>>> "+low+"-"+interval+"\tprimes: "+cntPrimes+"\twithFactor: "+cntFactors+"\tpct: "+pct+"\tmodpow: "+cntK);
					if (details) System.out.print("\n");
					low = interval;
					interval += 10000;
					cntPrimes = 0;
					cntFactors = 0;
					cntK = 0;
				}
			} else {
				System.out.println("M["+prime+"] too large for file of known primes");
				break;
			}
		}
		
		
//		for (int p = 1; p <= 50; p++)
//			checkMersenneNumber(p);
//		checkMersenneNumber(929);
//		return;
	}

}
