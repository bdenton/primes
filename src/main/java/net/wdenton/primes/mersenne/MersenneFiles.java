package net.wdenton.primes.mersenne;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.wdenton.primes.PrimeFile;

public class MersenneFiles extends PrimeFile {

	public static final String REPORT_FILE = "mersenne2.txt";
	public static final String DATA_FILE = "mersenne2.dat";
	public static final String CKPT_FILE = "m2.ckpt.dat";
	
	public static final String DATA_DLM = "|";
	
	
	// ////////////////////////////////////
	// ////  REPORT files management
	// ////////////////////////////////////
	
	private Path rptPath = null;
	private OutputStream rptStream = null;
	private PrintWriter rptOut = null;
	private String lastReportRecord;
	
	private Path dataPath = null;
	private OutputStream dataStream = null;
	private PrintWriter dataOut = null;
	private String lastDataRecord;
	
	private MersenneReporter reporter;

	public void clearResults () {
		try {
			rptPath = Paths.get(rootPath, REPORT_FILE);
			if (Files.exists(rptPath)) {
				System.out.println("<<<<  removing existing Mersenne report file");
				Files.delete(rptPath);
			}
		} catch (IOException e) {
			System.out.println("#### error deleting Mersenne report file ["+rptPath+"]");
			e.printStackTrace();
		}
		try {
			dataPath = Paths.get(rootPath, DATA_FILE);
			if (Files.exists(dataPath)) {
				System.out.println("<<<<  removing existing Mersenne data file");
				Files.delete(dataPath);
			}
		} catch (IOException e) {
			System.out.println("#### error deleting Mersenne data file ["+rptPath+"]");
			e.printStackTrace();
		}
	}

	public void setReporter(MersenneReporter reporter) {
		this.reporter = reporter;
	}

	public void closeResults () {
		if (rptOut != null) {
			try {
				rptOut.flush();
				rptOut.close();
			} catch (Throwable e) {
				;
			}
		}
		if (rptStream != null) {
			try {
				rptStream.flush();
				rptStream.close();
			} catch (IOException e) {
				;
			}
		}
		if (dataOut != null) {
			try {
				dataOut.flush();
				dataOut.close();
			} catch (Throwable e) {
				;
			}
		}
		if (dataStream != null) {
			try {
				dataStream.flush();
				dataStream.close();
			} catch (IOException e) {
				;
			}
		}
	}
	
	public void writeReport (String report) {
		String op = "opening";
		try {
			if (null == rptStream) {
				rptPath = Paths.get(rootPath, REPORT_FILE);
				createDirectory();
				if (Files.exists(rptPath)) {
					rptStream = Files.newOutputStream(rptPath, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
				} else {
					rptStream = Files.newOutputStream(rptPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				}
				rptOut = new PrintWriter(rptStream);
			}

			op = "writing";
			lastReportRecord = report;
			rptOut.println(report);
			rptOut.flush();
		} catch (IOException e) {
			System.out.println("#### error "+op+" Mersenne report file ["+rptPath+"]");
			e.printStackTrace();
		}
	}
	
	public String getLastReportRecord () {
		return lastReportRecord;
	}

	public void writeData (String[] elements) {
		String op = "opening";
		boolean newFile = false;
		try {
			if (null == dataStream) {
				dataPath = Paths.get(rootPath, DATA_FILE);
				createDirectory();
				if (Files.exists(dataPath)) {
					dataStream = Files.newOutputStream(dataPath, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
				} else {
					dataStream = Files.newOutputStream(dataPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					newFile = true;
				}
				dataOut = new PrintWriter(dataStream);
				if (newFile) {
					String[] header = reporter.generateDataHeaderRecord();
					dataOut.println(String.join(DATA_DLM,  header));
				}
			}

			op = "writing";
			lastDataRecord = String.join(DATA_DLM,  elements);
			dataOut.println(lastDataRecord);
			dataOut.flush();
		} catch (IOException e) {
			System.out.println("#### error "+op+" Mersenne data file ["+dataPath+"]");
			e.printStackTrace();
		}
	}
	
	public String getLastDataRecord () {
		return lastDataRecord;
	}

	
	// ////////////////////////////////////
	// ////  CKPT files management
	// ////////////////////////////////////

	private Path ckptPath = null;
	
	public boolean haveCheckpoint () {
		ckptPath = Paths.get(rootPath, CKPT_FILE);
		return Files.exists(ckptPath);
	}
	
	public Path getCheckpointPath () {
		haveCheckpoint(); // make sure the value is set
		return ckptPath;
	}
	
	public Mersenne2StateV2 readState () {
		Mersenne2StateV2 result = null;
		InputStream is = null;
		ObjectInputStream ckptIn = null;
		try {
			if (haveCheckpoint()) {
				System.out.println("--> restoring state from existing checkpoint file ["+ckptPath+"]");
				is = Files.newInputStream(ckptPath, StandardOpenOption.READ);
				ckptIn = new ObjectInputStream(is);
				
				Object obj = ckptIn.readObject();
				if (obj instanceof Mersenne2State) {
					result = new Mersenne2StateV2((Mersenne2State)obj);
				} else {
					result = (Mersenne2StateV2)obj;
				}

			} else {
				System.out.println("!!!!ERROR!!!!  Checkpoint file ["+ckptPath+"] not found");
			}
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

	public void writeState (Mersenne2StateV2 toSave) {
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
	
	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
}
