/**
 * 
 */
package net.wdenton.primes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.wdenton.primes.Factors31Bits.RunType;

/**
 * @author WilliamDenton
 *
 */
public class PrimeDistribution {

	public static final int INTERVAL = 1000;
	public static final String DLM = "|";
	PrimeFile file = new PrimeFile();

	private Path distroPath;
	private OutputStream distroStream = null;
	private PrintWriter distroOut = null;

	public static final String DISTRO_FILE = "primes-dist.txt";
	public static final String PAIRS_FILE = "prime-pairs.txt";
	
	enum RunType {DISTRO, PAIRS};
	public RunType runType = RunType.DISTRO;
	public String outName = DISTRO_FILE;

	
	public void writeDistro (int interval, int count) {
		if (null == distroOut) {
			openDistro();
		}
		distroOut.println(""+interval+DLM+count);
		distroOut.flush();
	}

	public void writePairs (int pair1, int pair2, int distance, String msg) {
		if (null == distroOut) {
			openDistro();
		}
		distroOut.println(""+pair1+DLM+pair2+DLM+distance+msg);
		distroOut.flush();
	}
	
	public void openDistro () {
		try {
			if (null == distroOut) {
				distroPath = Paths.get(file.getRootPath(), outName);
				distroStream = Files.newOutputStream(distroPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				distroOut = new PrintWriter(distroStream);
			}
		} catch (IOException e) {
			System.out.println("#### error opening prime distribution data file ["+distroPath+"]");
			e.printStackTrace();
		}
	}
	
	public void closeDistro () {
		if (distroOut != null) {
			try {
				distroOut.flush();
				distroOut.close();
			} catch (Throwable e) {
				;
			}
		}
		if (distroStream != null) {
			try {
				distroStream.flush();
				distroStream.close();
			} catch (IOException e) {
				;
			}
		}
	}
	
	
	
	// ////////////////////////////////////
	// ////  M A I N  processing
	// ////////////////////////////////////
	
	void process () throws IOException {
		file.openPrimes();
		int primeCnt = file.getPrimeCount();

		// for DISTRO
		int count = 0;
		int nextInterval = INTERVAL;
		
		// for PAIRS
		int pCount = 0;
		int pTotal = 0;
		int pNextInt = INTERVAL;
		int lastPrime = 0;
		int lastPair = 0;
		int widestSep = 0;
		String pMsg = "";

		for (int i = 0; i < primeCnt ; i++) {
			int prime = file.nextPrime();
			
			switch (runType) {
				case DISTRO: {
					if (prime > nextInterval) {
						writeDistro(nextInterval, count);
						nextInterval += INTERVAL;
						count = 0;
					}
					count++;
					break;
				}
				case PAIRS: {
					int primeDist = prime - lastPrime;
					if (lastPrime > 0 && primeDist <= 2) {
						// have a pair
						if (prime > pNextInt) {
							pMsg = DLM + pNextInt + DLM + pCount;
							pNextInt += INTERVAL;
							pCount = 0;
						}
						pCount++;
						pTotal++;
						
						int pairDist = lastPair > 0 ? prime - lastPair : -1;
						writePairs(lastPrime, prime, pairDist, pMsg);
						lastPair = prime;
						if (pairDist > widestSep) {
							widestSep = pairDist;
						}
						pMsg = "";
					}
					lastPrime = prime;
					break;
				}
				default: {}
			}
		}
		switch (runType) {
			case DISTRO: {
				if (count > 0) {
					writeDistro(nextInterval, count);
				}
				System.out.println("<<< Prime counts per "+INTERVAL+" saved to "+distroPath);
				break;
			}
			case PAIRS: {
				if (pTotal > 0) {
					System.out.println("<<< "+pTotal+" Prime pairs saved to "+distroPath);
					System.out.println("<<< largest gap beween pairs: "+widestSep);
				}
				break;
			}
			default: {}
		}
		closeDistro();
	}
	
	boolean canProcess (RunType fromArgs) throws IOException {
		boolean result = true;
		String action = "";
		
		switch (fromArgs) {
			case DISTRO: {
				action = "calculate distribution of primes per "+INTERVAL; 
				runType = RunType.DISTRO;
				outName = DISTRO_FILE;
				break;
			}
			case PAIRS: {
				action = "analyze distribution of prime pairs";
				runType = RunType.PAIRS;
				outName = PAIRS_FILE;
				break;
			}
			default: {}
		}
		
		System.out.println("<<<<<<<<<<  PrimeDistribution: " + action + " >>>>>>>>>>");
		System.out.println("<<<<  File directory..... ["+file.getRootPath()+"]");
		System.out.println("<<<<  Output file name... ["+outName+"]");
		System.out.println("");
		
		return result;
	}

	public static void main(String[] args) {
		PrimeDistribution engine = new PrimeDistribution();
		RunType fromArgs = engine.runType;
		boolean canRun = true;
		
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i]; 
				if (arg.length() > 0) {
					if (arg.charAt(0) == '-') {
						switch (arg.substring(1).toLowerCase()) {
							case "d":
							case "dist":
							case "distro" :
								fromArgs = RunType.DISTRO;
								break;
							case "p":
							case "pair":
							case "pairs" :
								fromArgs = RunType.PAIRS;
								break;
							default:
								System.out.println("!! unknown command line option ["+arg+"]");
								canRun = false;
						}
					} else {
						engine.file.setRootPath(arg);
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
