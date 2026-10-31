package net.wdenton.primes.mersenne;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Mersenne {

	public static final String DEFAULT_ROOT_PATH = "/primes";
	public static final String PRIME_FILE = "primes.dat";
	public static final String MERSENNE_FILE = "mersennes.txt";
	public static final String CKPT_FILE = "m.ckpt.dat";
	
	public static final int PRIME_THRESHOLD_1 = 15000; // need to start up the timer-driven ckpt at this prime
	
	enum RunType {CONTINUE, FRESH_START};
	
	static String rootPath = DEFAULT_ROOT_PATH;
	RunType runType = RunType.FRESH_START;
	boolean initialized = false;
	static final long CKPT_INTERVAL = 30*60*1000; // checkpoint every 30 minutes for big numbers

	private static final String BS = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
	private static final String SP = "                                                                        ";
	private static final BigInteger BIG1 = BigInteger.valueOf(1);
	private static final BigInteger BIG2 = BigInteger.valueOf(2);
	private static final BigInteger BIG4 = BigInteger.valueOf(4);
	
	
	// ////////////////////////////////////
	// ////  PRIMES file management
	// ////////////////////////////////////

	Path primePath = null;
	InputStream primeStream = null;
	DataInputStream primesIn = null;
	int primeCnt = -1;

	static Path mPath = null;
	static OutputStream mStream = null;
	static PrintWriter mOut = null;
	
	
	boolean openPrimes () {
		boolean result = false;
		Path primePath = Paths.get(rootPath, PRIME_FILE);
		if (Files.exists(primePath)) {
			try {
				primeStream = Files.newInputStream(primePath, StandardOpenOption.READ);
				primesIn = new DataInputStream(primeStream);
				primeCnt = primesIn.readInt();
				result = true;
			} catch (IOException e) {
				System.out.println("!!!!ERROR!!!!  Cannot open prime list file ["+primePath.toString()+"]");
				e.printStackTrace();
			}
		} else {
			System.out.println("!!!!ERROR!!!!  Prime list file ["+primePath.toString()+"] not  found");
		}
		return result;
	}

	void clearResults () {
		try {
			mPath = Paths.get(rootPath, MERSENNE_FILE);
			if (Files.exists(mPath)) {
				System.out.println("<<<<  removing existing Mersenne results file");
				Files.delete(mPath);
			}
		} catch (IOException e) {
			System.out.println("#### error deleting Mersenne results file ["+mPath+"]");
			e.printStackTrace();
		}
		
	}

	void writeResults (String report) {
		String op = "opening";
		try {
			if (null == mStream) {
				mPath = Paths.get(rootPath, MERSENNE_FILE);
				createDirectory();
				if (Files.exists(mPath)) {
					mStream = Files.newOutputStream(mPath, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
				} else {
					mStream = Files.newOutputStream(mPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}
				mOut = new PrintWriter(mStream);
			}

			op = "writing";
			mOut.println(report);
			mOut.flush();
		} catch (IOException e) {
			System.out.println("#### error "+op+" Mersenne results file ["+mPath+"]");
			e.printStackTrace();
		}
		
	}
	
	static enum Phase {NEXT_PRIME, LL_LOOP}
	
	static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		Phase phase;
		int primeIx; // entry# in primes file
		int mNo; // ordinal Mersenne number last found
		int currentP; // current prime exponent
		int calcCnt; // LL loop counter: 0->p-2
		int nCalcs; // # of LL calcs to find next Mersenne
		long calcNanos; // accumulated nanos in LL calcs
		long elapsedFindMs; // elapsed time (ms ) to find next Mersenne
		long currentFindStart; // time the current effort started
		BigInteger currentM; // current M[p] (2**p)-1
		BigInteger currentS; // current residual in LL loop
	};
	static State state = null;
	
	void process () throws IOException {
		
//		long startTime = System.currentTimeMillis();
		long ckptTime = -1;
		
		while ((state = getNextState(state)) != null) {
			
			state.currentFindStart = System.currentTimeMillis();
			
			if (state.phase == Phase.NEXT_PRIME) {
				state.currentP = primesIn.readInt();
				state.primeIx++;

				state.calcCnt = 0; // LL loop starting counter
				state.currentS =  BIG4;
				state.currentM = BIG2.pow(state.currentP).subtract(BIG1);
				
				state.phase = Phase.LL_LOOP;
				progressP(state.currentP);
			}
			
			// phase = LL_LOOP
			
			if (state.currentP > 2) {

				if (state.currentP > PRIME_THRESHOLD_1) {
					ckptTime = System.currentTimeMillis() + CKPT_INTERVAL;
				}
				
				// this is really the equivalent of:
				// for ( ; state.calcCnt < state.currentP-2; state.calcCnt++) {
				// and is done this way so as to serialize changes to "state"
				while (state.calcCnt < state.currentP-2) {
					int counter = state.calcCnt; // for reporting
					long dur;
					
					synchronized (state) {
						long beg = System.nanoTime();
						
						state.currentS = state.currentS.pow(2).subtract(BIG2).mod(state.currentM);
						
						dur = System.nanoTime() - beg;

						state.calcCnt++;
						state.calcNanos += dur;
						state.nCalcs++;
					}
					
					progressLL(counter, dur);

					if (ckptTime > 0 && ckptTime <= System.currentTimeMillis()) {
						checkpointState(state);
						ckptTime += CKPT_INTERVAL;
					}
				}
			}

			// LL_LOOP is done.. examine results

			long timeToFind = System.currentTimeMillis() - state.currentFindStart;
			state.elapsedFindMs += timeToFind;
			
			if (2 == state.currentP || (state.currentS.compareTo(BigInteger.ZERO) == 0)) {
				
				state.mNo++;

				int pLen = Integer.toString(state.currentP).length();

				String strM = state.currentM.toString();
				int digits = strM.length();
				int digLen = Integer.toString(digits).length();
				StringBuilder num = new StringBuilder(); 
				if (digits < 26) {
					num.append(SP.substring(0, 27-digits))
						.append(strM);
				} else {
					num.append(strM.substring(0, 12))
					   .append("...")
					   .append(strM.substring(digits-12));
				}
		
				StringBuilder sb = new StringBuilder();
				sb.append("M[").append(state.mNo).append("]::  ")
				   .append("\tp: ").append(SP.substring(0, 8 - pLen)).append(state.currentP)
				   .append("\tM(p): ").append(num.toString())
				   .append("\tdigits: ").append(SP.substring(0, 10 - digLen)).append(digits)
				   .append("\t(found in ").append(state.elapsedFindMs).append("ms)")
				   .append(" LL calcs: ").append(state.nCalcs)
				   .append(state.nCalcs > 0 
							? (" (avg: "+(state.calcNanos/state.nCalcs)+" nanos)")
							: "")
				   ;
				String report = sb.toString();
				System.out.println("\r"+report);
				
				writeResults(report);
				
				progress = "";
				state.elapsedFindMs = 0;
				state.calcNanos = 0;
				state.nCalcs = 0;
			}
			
		}
	}
	
	State getNextState (State lastState) throws IOException {
		State result = null;
		if (initialized) {
			result = lastState;
			result.phase = Phase.NEXT_PRIME;
			checkpointState(result);
		} else {
			initialized = true;
			if (runType == RunType.FRESH_START) {
				result = new State();
				result.phase = Phase.NEXT_PRIME;
				clearResults();
			} else {
				result = readState();
				System.out.println("<<<< Resuming processing from last checkpoint:");
				System.out.println("<<<<   Processing phase... ["+result.phase+"]");
				System.out.println("<<<<   Current prime...... ["+result.currentP+"]");
				System.out.println("<<<<   Current LL step.... ["+result.calcCnt+"]\n");
				if (result != null) { // must position the primes file
					int p = -1;
					for (int i = 0; i < result.primeIx; i++) {
						p = primesIn.readInt();
					}
					if (p !=  result.currentP) {
						System.out.println("#### primes are misaligned");
					}
					progressP(result.currentP);
				}
			}
		}
		return result;
	}

	String progress = "";
	
	void progressP (int p) {
		progressErase();
		System.out.print("\r"+p+"... ");
	}
	
	void progressErase () {
		if (progress.length() > 0) {
			int ll = progress.length();
			System.out.print(BS.substring(0, ll) + SP.substring(0, ll) + BS.substring(0, ll));
			progress = "";
		}
	}
	
	void progressLL (int c, long dur) {
		progressErase();
		progress = ""+state.calcCnt+" ["+dur+"ns]";
		System.out.print(progress);
	}

	boolean canProcess (RunType fromArgs) {
		boolean result = true;
		System.out.println("<<<<<<<<<<  Mersenne: find Mersenne Primes >>>>>>>>>>");
		System.out.println("<<<<  File directory..... ["+rootPath+"]");
		
		boolean haveCkpt = haveCheckpoint();
		if (fromArgs != null) {
			runType = fromArgs;
		} else {
			runType = haveCkpt ? RunType.CONTINUE : RunType.FRESH_START;
		}
		System.out.println("<<<<  Run type........... ["+runType.toString()+"]");
		
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
		
		result &= openPrimes();
		
		System.out.println("\n");
		
		return result;
	}

	static Path ckptPath = null;
	
	boolean haveCheckpoint () {
		ckptPath = Paths.get(rootPath, CKPT_FILE);
		return Files.exists(ckptPath);
	}
	
	State readState () {
		State result = null;
		InputStream is = null;
		ObjectInputStream ckptIn = null;
		try {
			if (Files.exists(ckptPath)) {
				System.out.println("--> restoring state from existing checkpoint file ["+ckptPath+"]");
				is = Files.newInputStream(ckptPath, StandardOpenOption.READ);
				ckptIn = new ObjectInputStream(is);
				
				result = (State)ckptIn.readObject();

			} else {
				System.out.println("!!!!ERROR!!!!  Checkpoint file ["+ckptPath+"] not found");
			}
			createDirectory();
		
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("!!!!ERROR!!!!  Error reading checkpoint file ["+ckptPath+"]");
			e.printStackTrace();
		} finally {
			if (ckptIn != null) {
				try {
					ckptIn.close();
				} catch (Exception e) {}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {}
			}
		}
		return result;
	}

	static void checkpointState (State toSave) {
		OutputStream os = null;
		ObjectOutputStream ckptOut = null;
		try {
			if (Files.exists(ckptPath)) {
				Files.delete(ckptPath);
			}
			createDirectory();
			os = Files.newOutputStream(ckptPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			ckptOut = new ObjectOutputStream(os);
			
			ckptOut.writeObject(toSave);
		
		} catch (IOException e) {
			System.out.println("!!!!ERROR!!!!  Error writing checkpoint file ["+ckptPath+"]");
			e.printStackTrace();
		} finally {
			if (ckptOut != null) {
				try {
					ckptOut.flush();
					ckptOut.close();
				} catch (Exception e) {}
			}
			if (os != null) {
				try {
					os.flush();
					os.close();
				} catch (Exception e) {}
			}
		}
	}
	
	static Runnable saveState = new Runnable() {
		public void run() {
			if (Files.exists(ckptPath)) {
				System.out.println("\n--> replacing existing checkpoint file ["+ckptPath+"]");
			} else {
				System.out.println("\n--> creating checkpoint file ["+ckptPath+"]");
			}
			synchronized (state) {
				long timeToFind = System.currentTimeMillis() - state.currentFindStart;
				state.elapsedFindMs += timeToFind;
				checkpointState(state);
			}
			
			if (mOut != null) {
				try {
					mOut.flush();
					mOut.close();
				} catch (Throwable e) {
					;
				}
			}
			if (mStream != null) {
				try {
					mStream.flush();
					mStream.close();
				} catch (IOException e) {
					;
				}
			}
		}
	};
	
	static void createDirectory () {
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
	
	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		Mersenne.rootPath = rootPath;
	}
	
	
	public static void main(String[] args) {
		try {
			Mersenne engine = new Mersenne();
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
								default:
									System.out.println("!! unknown command line option ["+arg+"]");
							}
						} else {
							engine.setRootPath(arg);
						}
					}
				}
			}
			
			if (engine.canProcess(fromArgs)) {
				Runtime.getRuntime().addShutdownHook(new Thread(saveState));
				engine.process();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}
}
