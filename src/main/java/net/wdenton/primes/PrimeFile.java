package net.wdenton.primes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PrimeFile {

	public static final String DEFAULT_ROOT_PATH = "/primes";
	
	protected String rootPath = DEFAULT_ROOT_PATH;
	protected boolean doPairs = false;
	
	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	
	public boolean doPairs () {
		return this.doPairs;
	}

	public void setDoPairs (boolean yn) {
		this.doPairs = yn;
	}
	
	
	// ////////////////////////////////////
	// ////  PRIMES file management
	// ////////////////////////////////////

	private Path primesPath = null;
	private InputStream primesInStream = null;
	private DataInputStream primesIn = null;
	private OutputStream primesOutStream = null;
	private DataOutputStream primesOut = null;

	public static final String PRIME_FILE = "primes.dat";
	
	private int primes[];
	private int primeCnt = -1;
	private volatile int primeIx = 0;
	private int primeLim;
	private int primeHigh = -1;
	private int lastPrime = 0;
	private int thisPrime = 0;

	private static final int PRIME_BLOCK_SIZE = 25000;
	
	public boolean openPrimes () {
		boolean result = false;
		primesPath = Paths.get(rootPath, PRIME_FILE);
		if (Files.exists(primesPath)) {
			try {
				primesInStream = Files.newInputStream(primesPath, StandardOpenOption.READ);
				primesIn = new DataInputStream(primesInStream);
				primeCnt = primesIn.readInt();
				primeIx = -1;
				lastPrime = thisPrime = 0;
				primeHigh = -1;
				result = true;
			} catch (IOException e) {
				System.out.println("!!!!ERROR!!!!  Cannot open prime list file ["+primesPath.toString()+"]");
				e.printStackTrace();
			}
		} else {
			System.out.println("!!!!ERROR!!!!  Prime list file ["+primesPath.toString()+"] not  found");
		}
		return result;
	}
	
	public void closePrimesIn () {
		if (primesIn != null) {
			try {
				primesIn.close();
			} catch (Exception e) {}
		}
		if (primesInStream != null) {
			try {
				primesInStream.close();
			} catch (Exception e) {}
		}
	}

	public int positionPrimes (int primeIx) throws IOException {
		int result = -1;
		while (this.primeIx < primeIx) {
			result = nextPrime();
		}
		return result;
	}
	
	public int nextPrime () throws IOException {
		primeIx++;
//		primeDelta = primesIn.readInt();
		lastPrime = thisPrime;
		thisPrime = primesIn.readInt();
		return thisPrime;
	}
	
	public int getDelta () {
		return primeIx > 0 ? thisPrime - lastPrime
				           : 0;
	}
	
	public boolean loadPrimes () {
		boolean result = false;
		primesPath = Paths.get(rootPath, PRIME_FILE);
		if (openPrimes()) {
			try {
				int reportPt = 1000000;
				primeLim = (primeCnt/PRIME_BLOCK_SIZE + 1) * PRIME_BLOCK_SIZE;
				primes = new int[primeLim];
				System.out.println("--> allocate prime list to ["+primeLim+"] entries");
				for (int i = 0; i < primeCnt; i++) {
					primes[i] = primeHigh = primesIn.readInt();
					if (i == reportPt) {
						System.out.println("    ... "+i+" ("+primeHigh+")");
						reportPt += 1000000;
					}
				}
				System.out.println("--> read prime list with ["+primeCnt+"] entries. Highest prime ["+primeHigh+"]");
				result = true;
			} catch (IOException e) {
				System.out.println("!!!!ERROR!!!!  Cannot open prime list file ["+primesPath.toString()+"]");
				e.printStackTrace();
			} finally {
				closePrimesIn();
			}
		} 
		return result;
	}
	
	public boolean initPrimes () {
		primeCnt = 0;
		primeIx = -1;
		lastPrime = thisPrime = 0;
		primeHigh = -1;
		primeLim = PRIME_BLOCK_SIZE;
		primes = new int[PRIME_BLOCK_SIZE];
		System.out.println("--> allocate prime list with ["+primeLim+"] entries");
		return true;
	}
	
	
	public void addPrime (int value) {
		if (primeCnt >= primeLim) {
			int newLim = primeLim + PRIME_BLOCK_SIZE;
//			System.out.println("\n--> reallocate prime list with ["+newLim+"] entries\n");
			int[] newPrimes = new int[newLim];
			System.arraycopy(primes, 0, newPrimes, 0, primeLim);
			primes = newPrimes;
			primeLim = newLim;
		}
		primes[primeCnt++] = value;
		lastPrime = thisPrime;
		primeHigh = thisPrime = value;
		if (doPairs) {
			if (lastPrime > 0 && (value - lastPrime) <= 2) {
				System.out.println("==> paired primes: "+lastPrime+" :: "+value);
			}
 		}
	}

	public void updatePrimes () {
		primesPath = Paths.get(rootPath, PRIME_FILE);
		try {
			closePrimesIn(); // ensure no open primes file
			if (Files.exists(primesPath)) {
				Files.delete(primesPath);
				System.out.println("\n--> replacing existing prime list file ["+primesPath.toString()+"]");
			} else {
				System.out.println("\n--> creating prime list file ["+primesPath.toString()+"]");
			}
			createDirectory();
			primesOutStream = Files.newOutputStream(primesPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			primesOut = new DataOutputStream(primesOutStream);
			
			primesOut.writeInt(primeCnt);
//			lastPrime = 0;
		
			for (int i = 0; i < primeCnt; i++) {
				int value = primes[i];
//				primesOut.writeInt(value - lastPrime);
				primesOut.writeInt(value);
//				lastPrime = value;
			}
			System.out.println("--> prime list file written with ["+primeCnt+"] entries");
		} catch (IOException e) {
			System.out.println("!!!!ERROR!!!!  Error writing prime list file ["+primesPath.toString()+"]");
			e.printStackTrace();
		} finally {
			if (primesOut != null) {
				try {
					primesOut.flush();
					primesOut.close();
				} catch (Exception e) {}
			}
			if (primesOutStream != null) {
				try {
					primesOutStream.flush();
					primesOutStream.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public int getPrimeCount () {
		return primeCnt;
	}
	
	public int[] getPrimes () {
		return primes;
	}
	
	public int getPrimeHigh() {
		return primeHigh;
	}

	public int getCurrentPrimeIndex () {
		return primeIx;
	}
	
	public int getAvailablePrimes () {
		return primeCnt - primeIx;
	}

	
	
	// /////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////
	
	protected void createDirectory () {
		Path dir = Paths.get(rootPath);
		if (Files.notExists(dir)) {
			try {
				Files.createDirectories(dir);
				System.out.println("--> created output directory ["+dir+"]");
			} catch (IOException e) {
				System.out.println("!!!!ERROR!!!!  Error creating output directory ["+dir+"]");
			}
		}
	}
}
