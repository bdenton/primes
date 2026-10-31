package net.wdenton.primes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.wdenton.primes.Factors.Factor;

public class PrimeFactorFiles extends PrimeFile {

	
	
	// ////////////////////////////////////
	// ////  FACTORS file management
	// ////////////////////////////////////

	private Path factorsPath = null;
	private OutputStream factorsOutStream = null;
	private DataOutputStream factorsOut = null;
	private InputStream factorsInStream = null;
	private DataInputStream factorsIn = null;
	
	private int maxFactors = 0;
	private int maxPower = 0;
	private int lastFactor = -1;

	public static final int FACTOR_SYNC_MARK = 0x000000bd;
	public static final int FACTOR_SYNC_MASK = 0x000000ff;
	public static final String FACTORS_FILE = "factors.dat";
	private static final int LOAD_INTERVAL = 10000;

	
	// //////////////////////////
	// ////   O U T P U T
	// //////////////////////////
	
	public boolean initFactorsOutput (boolean newStart) throws IOException {
		boolean result = false;
		factorsPath = Paths.get(rootPath, FACTORS_FILE);
		try {
			if (!newStart /*lowLimit > 2*/) {
				if (Files.exists(factorsPath)) {
					factorsOutStream = Files.newOutputStream(factorsPath, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
					System.out.println("--> appending to existing factors file ["+factorsPath.toString()+"]");
				} else {
					System.out.println("!!!!ERROR!!!!  Existing factors file ["+factorsPath+"] not found. Cannot resume processing.");
				}
			} else {
				if (Files.exists(factorsPath)) {
					Files.delete(factorsPath);
					System.out.println("--> replacing existing factors file ["+factorsPath.toString()+"]");
				} else {
					System.out.println("--> creating new factors file ["+factorsPath.toString()+"]");
				}
				createDirectory();
				factorsOutStream = Files.newOutputStream(factorsPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			}
			if (factorsOutStream != null) {
				factorsOut = new DataOutputStream(factorsOutStream);
				result = true;
			} else {
				System.out.println("!!!!ERROR!!!!  Could not open  factors file ["+factorsPath.toString()+"]");
			}
		} catch (IOException e) {
			System.out.println("!!!!ERROR!!!!  Error opening factors file ["+factorsPath.toString()+"]");
			e.printStackTrace();
		}
		return result;
	}
	
	public void writeFactors (Factors factors) throws IOException {
		try {
			factorsOut.writeByte(FACTOR_SYNC_MARK);
			factorsOut.writeInt(factors.getNumber());
			
			List<Factor> primes = factors.getFactors();

			int factCnt = primes.size();
			if (factCnt > maxFactors) {
				maxFactors = factCnt;
			}
			factorsOut.writeByte(factCnt);
			
			for (Factor factor : primes) {
				int fact = factor.prime;
				int pow = factor.power;
				if (pow > maxPower) {
					maxPower = pow;
				}
				int encoded = sizeMask(fact, pow);
				int[] decoded = getSizes(encoded);
				factorsOut.writeByte(encoded);
				switch (decoded[0]) {
					case 0:
						factorsOut.writeByte(fact);
						break;
					case 1:
						factorsOut.writeShort(fact);
						break;
					case 2:
						factorsOut.writeInt(fact);
						break;
//					case 3:
//						factorsOut.writeLong(fact);
//						break;
				}
			}
		} catch (IOException e) {
			System.out.println("!!!!ERROR!!!!  Error writing factors file ["+factorsPath.toString()+"]");
			e.printStackTrace();
			System.exit(4);
		} finally {
			try {
				factorsOut.flush();
			} catch (Exception e) {}
		}
	}
	
	
	// //////////////////////////
	// ////   I N P U T
	// //////////////////////////
	
	public boolean initFactorsInput () throws IOException {
		boolean result = false;
		factorsPath = Paths.get(rootPath, FACTORS_FILE);
		try {
			if (Files.exists(factorsPath)) {
				factorsInStream = Files.newInputStream(factorsPath, StandardOpenOption.READ);
				factorsIn = new DataInputStream(factorsInStream);
				result = true;
			} else {
				System.out.println("!!!!ERROR!!!!  Prime Factors file ["+factorsPath.toString()+"] not found");
				throw new IOException("Prime Factors file ["+factorsPath.toString()+"] not found");
			}
		} catch (IOException e) {
			System.out.println("!!!!ERROR!!!!  Error opening factors file ["+factorsPath.toString()+"]");
			throw e;
		}
		return result;
	}
	
	public Iterable<Factors> asIterable () throws IOException {
		if (null == factorsIn) {
			initFactorsInput();
		}
		return new FactorsIterator();
	}
	
	public boolean findLastFactor () {
		boolean result = false;
		
		try {
			System.out.println("\nScanning prime factors data file for starting point.");
			
			int ix = 0;
			int lim = LOAD_INTERVAL;
			for (Factors factors : asIterable()) {
				lastFactor = factors.getNumber();
				if (ix++ >= lim) {
					lim += LOAD_INTERVAL;
					System.out.print("\r... ["+ix+"]");
				}
			}
			System.out.println("\r... Scanned ["+ix+"] prime factors.");
			result = true;
		} catch (IOException e) {
			System.out.println("\r!!!!ERROR!!!!  Cannot read prime factor file ["+factorsPath.toString()+"]");
			e.printStackTrace();
		} finally {
			closeFactors();
		}
		return result;
	} 
	
	public Factors getNextNumber () throws IOException {
		Factors result = null;

		if (null == factorsIn) {
			initFactorsInput();
		}
		try {
			Factors work = null;
			try {
				int sync = factorsIn.readUnsignedByte();
				if (FACTOR_SYNC_MARK != (sync & FACTOR_SYNC_MASK)) {
					throw new IOException("!!! Error: Prime Factor file reading out of sync");
				}

				work = new Factors();

				int value = factorsIn.readInt();
				work.setNumber(value);
				
				int cnt = factorsIn.readUnsignedByte();
				if (cnt > 0) {
					for (int i = 0; i < cnt; i++) {
						int mask = (byte)factorsIn.readUnsignedByte();
						int[] decoded = getSizes(mask);
						int prime = -1;
						int power = decoded[1];
						switch (decoded[0]) {
							case 0:
								prime = factorsIn.readByte();
								break;
							case 1:
								prime = factorsIn.readShort();
								break;
							case 2:
								prime = factorsIn.readInt();
								break;
//							case 3:
//								prime = factorsIn.readLong();
//								break;
							default: 
								throw new IOException("!!! Error unexpexped Factor size mask ["+decoded[0]+"]");
						}
						work.addFactor(prime, power);
					}
				}
				result = work;
			} catch (EOFException e) {
				if (work != null) {
					throw new IOException("!!! Error: premature end-of-file reading prime factors file ["+factorsPath.toString()+"]");
				}
			}
		} catch (IOException e) {
			System.out.println("\r!!!!ERROR!!!!  Cannot read prime factor file ["+factorsPath.toString()+"]");
			throw e;
		}
		return result;
	}
	
	public void closeFactors () {
		if (factorsIn != null) {
			try {
				factorsIn.close();
			} catch (Exception e) {}
			factorsIn = null;
		}
		if (factorsInStream != null) {
			try {
				factorsInStream.close();
			} catch (Exception e) {}
			factorsInStream = null;
		}

		if (factorsOut != null) {
			try {
				factorsOut.flush();
				factorsOut.close();
			} catch (Exception e) {}
			factorsOut = null;
		}
		if (factorsOutStream != null) {
			try {
				factorsOutStream.flush();
				factorsOutStream.close();
			} catch (Exception e) {}
			factorsOutStream = null;
		}
	}

	public int getMaxFactors() {
		return maxFactors;
	}

	public int getMaxPower() {
		return maxPower;
	}

	public int getLastFactor() {
		return lastFactor;
	}

	// size of number is encoded in top two bits:
	// 00 - byte
	// 01 - short
	// 10 - int
	// 11 - long
	// bottom six bits = power - 1.
	//
	// this allows for factors of all numbers <= 63 bits
	//
	protected int sizeMask (int value, int power) {
		if (power < 1 || power > 64) {
			throw new RuntimeException("!!! invalid factor exponent ["+power+"]");
		}
		int result = power - 1;
		
		if (value <= 0x007f) {
			return result;
		}
		if (value <= 0x7fff) {
			return (result | 0x00000040);
		}
		if (value <= 0x7fffffff) {
			return (result | 0x00000080);
		}
		return (result | 0x000000c0);
	}
	
	protected int[] getSizes (int mask) {
		int[] result = {0,0};
		
		result[0] = (mask & 0x000000c0) >> 6;	//factor size
		result[1] = (mask & 0x0000003F) + 1;	//power
		return result;
	}
	
	
	// /////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////////

	public class FactorsIterator implements Iterable<Factors>, Iterator<Factors> {
		private Factors nextFactor = null;
		
		public FactorsIterator() throws IOException {
			super();
			nextFactor = getNextNumber();
		}

		@Override // from Iterable
		public Iterator<Factors> iterator () {
			return this;
		}

		@Override // from Iterator
		public boolean hasNext () {
			return nextFactor != null;
		}

		@Override // from Iterator
		public Factors next () {
			if (this.hasNext()) {
				Factors result = nextFactor;
				try {
					nextFactor = getNextNumber();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return result;
			} else {
				throw new NoSuchElementException();
			}
		}
		
	}

}
