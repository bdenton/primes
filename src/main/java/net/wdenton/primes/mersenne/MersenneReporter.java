package net.wdenton.primes.mersenne;

import java.time.Duration;

public class MersenneReporter {

	// these are copied from java.time.LocalTime because of visibility
	public static final int HOURS_PER_DAY = 24;
	
	public static final int MINUTES_PER_HOUR = 60;
	public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;

	public static final int SECONDS_PER_MINUTE = 60;
	public static final int SECONDS_PER_DAY  = SECONDS_PER_MINUTE * MINUTES_PER_DAY;
	public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	
	public static final long MILLIS_PER_SECOND = 1000L;
	public static final long MILLIS_PER_DAY    = MILLIS_PER_SECOND * SECONDS_PER_DAY;
	public static final long MILLIS_PER_HOUR   = MILLIS_PER_SECOND * SECONDS_PER_HOUR;
	public static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
	
	public static final long MICROS_PER_MILLI = 1000L;
	public static final long MICROS_PER_DAY    = MICROS_PER_MILLI * MILLIS_PER_DAY;
	public static final long MICROS_PER_HOUR   = MICROS_PER_MILLI * MILLIS_PER_HOUR;
	public static final long MICROS_PER_MINUTE = MICROS_PER_MILLI * MILLIS_PER_MINUTE;
	public static final long MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;

	public static final long NANOS_PER_MICRO = 1000L;
	public static final long NANOS_PER_DAY    = NANOS_PER_MICRO * MICROS_PER_DAY;
	public static final long NANOS_PER_HOUR   = NANOS_PER_MICRO * MICROS_PER_HOUR;
	public static final long NANOS_PER_MINUTE = NANOS_PER_MICRO * MICROS_PER_MINUTE;
	public static final long NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;
	public static final long NANOS_PER_MILLI  = NANOS_PER_MICRO * MICROS_PER_MILLI;
	
	private static final String BS = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b"
			                       + "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b"
                                   + "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b"
    		                       + "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
	private static final String SP = "                                                                        "
                                   + "                                                                        "
                                   + "                                                                        "
                                   + "                                                                        ";
	
	
	public String generateReportLine (Mersenne2StateV2 state, String currentM) {
		int pLen = Integer.toString(state.currentP).length();

		int digits = currentM.length();
		int digLen = Integer.toString(digits).length();
		StringBuilder num = new StringBuilder(); 
		if (digits < 26) {
			num.append(SP.substring(0, 27-digits))
				.append(currentM);
		} else {
			num.append(currentM.substring(0, 12))
			   .append("...")
			   .append(currentM.substring(digits-12));
		}
		
		int nTotLen = Integer.toString(state.findNextMpPrimesTested).length();
		int nTfLen = Integer.toString(state.findNextMpTfTests).length();
		int tfPct = (state.findNextMpTfTests * 100) / state.findNextMpPrimesTested;
		int nTfPct = Integer.toString(tfPct).length();

		StringBuilder sb = new StringBuilder();
		sb.append("M[").append(state.mNo).append("]::").append((state.mNo < 10) ? "  " : " ")
		   .append(" p: ").append(SP.substring(0, 8 - pLen)).append(state.currentP) // 99,999,999 limit
		   .append(" M(p): ").append(num.toString())
		   .append(" digits: ").append(SP.substring(0, 10 - digLen)).append(digits)
		   .append(" elapsed: ").append(tellDuration(state.findNextMpTimer.getRunningTime()))
		   .append(" tested: ").append(SP.substring(0, 5 - nTotLen)).append(state.findNextMpPrimesTested)
		   .append("; TF: ").append(SP.substring(0, 5 - nTfLen)).append(state.findNextMpTfTests)
		   .append("(").append(SP.substring(0, 2 - nTfPct)).append(tfPct).append("%)")
		   .append(" calcs [LL: ").append(state.findNextMpLLcalcCnt)
		   .append(" ("+state.findNextMpTFbypassLL+" saved)")
		   .append(state.findNextMpLLcalcCnt > 0 
					? (" avg: "+(state.findNextMpLLcalcTime / state.findNextMpLLcalcCnt)+"ns; ")
					: "; ")
		   .append("TF: ").append(state.findNextMpTFcalcCnt)
		   .append(state.findNextMpTFcalcCnt > 0 
					? (" avg: "+(state.findNextMpTFcalcTime / state.findNextMpTFcalcCnt)+"ns]")
					: "]")
		   ;
		String report = sb.toString();
		return report;
	}

	public String[] generateDataRecord (Mersenne2StateV2 state, String currentM) {
		String[] result = new String[18];
		int i = 0;
		result[i++] = ""+state.mNo;
		result[i++] = ""+state.currentP;

		int digits = currentM.length();
		result[i++] = (digits < 26)
				    ? currentM
				    : currentM.substring(0, 12) + "..." + currentM.substring(digits-12);
		result[i++] = ""+digits;
		
		result[i++] = ""+state.findNextMpPrimesTested;
		result[i++] = ""+state.findNextMpTfTests;
		result[i++] = tellDuration(state.findNextMpTimer.getElapsedTime()); // total calendar time to find
		result[i++] = tellDuration(state.findNextMpTimer.getRunningTime()); // total run time to find
		if (state.findNextMpPrimesTested > 0) {
			Duration avgPerPrime = state.findNextMpTimer.getRunningTime().dividedBy(state.findNextMpPrimesTested);
			result[i++] = tellDuration(avgPerPrime);
		} else {
			result[i++] = "";
		}
		
		result[i++] = ""+state.findNextMpLLcalcCnt; // total number of LL calcs 
		result[i++] = ""+state.findNextMpLLcalcTime; // total time in LL calcs
		if (state.findNextMpPrimesTested > 0) {
			result[i++] = ""+(state.findNextMpLLcalcTime / state.findNextMpPrimesTested);
		} else {
			result[i++] = "";
		}
		if (state.findNextMpLLcalcCnt > 0) {
			result[i++] = ""+(state.findNextMpLLcalcTime / state.findNextMpLLcalcCnt);
		} else {
			result[i++] = "";
		}
		result[i++] = ""+state.findNextMpTFbypassLL; // total number of LL calcs bypassed because TF passed 
		
		result[i++] = ""+state.findNextMpTFcalcCnt; // total number of TF calcs 
		result[i++] = ""+state.findNextMpTFcalcTime; // total time in TF calcs
		if (state.findNextMpPrimesTested > 0) {
			result[i++] = ""+(state.findNextMpTFcalcTime / state.findNextMpPrimesTested);
		} else {
			result[i++] = "";
		}
		if (state.findNextMpTFcalcCnt > 0) {
			result[i++] = ""+(state.findNextMpTFcalcTime / state.findNextMpTFcalcCnt);
		} else {
			result[i++] = "";
		}
		
		return result;
	}

	public String[] generateDataHeaderRecord () {
		String[] result = new String[18];
		int i = 0;
		result[i++] = "M#";
		result[i++] = "P";
		result[i++] = "M[p]";
		result[i++] = "digits";
		result[i++] = "primesTested";
		result[i++] = "TFrejects";
		result[i++] = "elapsedTimeToFind";
		result[i++] = "processingTimeToFind";
		result[i++] = "avgProcessingPerPrimeTested";
		result[i++] = "LLcalcs"; 
		result[i++] = "LLcalcNanos";
		result[i++] = "avgLLnanosPerPrime";
		result[i++] = "avgLLnanosPerCalc";
		result[i++] = "LLcalcsSavedByTF";
		result[i++] = "TFcalcs"; 
		result[i++] = "TFcalcNanos";
		result[i++] = "avgTFnanosPerPrime";
		result[i++] = "avgTFnanosPerCalc";
		return result;
	}
	
	String progress = "";

	public void clearProgress () {
		progress = "";
	}
	
	public void progressP (Mersenne2StateV2 state) {
		progressErase();
//		System.out.print("\r["+tellDuration(state.findNextMpTimer.getElapsedTime())+"]" +
//                "["+tellDuration(state.findNextMpTimer.getRunningTime())+"] "+state.currentP+": ");
		long tfSavedNanos = -1;
		Duration tfSavedTime = null;
		if (state.findNextMpLLcalcCnt > 0) {
			tfSavedNanos = state.findNextMpTFbypassLL * (state.findNextMpLLcalcTime / state.findNextMpLLcalcCnt);
			tfSavedTime = Duration.ofNanos(tfSavedNanos);
		}
		System.out.print("\r["+tellDuration(state.findNextMpTimer.getRunningTime())+"] "+
					"P:"+state.currentP+": [" +
					"{" +
						"TF: "+state.findNextMpTfTests +
						(tfSavedNanos > 0 
								? ", saved: "+tellDuration(tfSavedTime) 
								: "") + 
					"}, " +
					"{" + 
						"LL: "+state.findNextMpLlTests
					);
		}

	private String preamble = "";
	
	public void progressPstats (Mersenne2StateV2 state) {
//		progressErase();
		preamble = ""+state.currentP+": ";
	}
	
	public void progressTF (Mersenne2StateV2 state, int prime) {
		progressErase();
		// elapsed for this prime
		progress = "["+tellDuration(state.thisPrimeTimer.getElapsedTime())+"]" +
				   "["+tellDuration(state.thisPrimeTimer.getRunningTime())+"] " +
				   state.thisPrimeTFcalcCnt + 
				   " trial factor: "+prime;
		System.out.print(progress);
	}

	public void progressTFfound (Mersenne2StateV2 state, int prime) {
		progressErase();
		// elapsed for this prime
		progress = "["+tellDuration(state.thisPrimeTimer.getElapsedTime())+"]" +
				   "["+tellDuration(state.thisPrimeTimer.getRunningTime())+"] " +
				   "prime factor found ["+prime+"] (tested "+state.thisPrimeTFcalcCnt+" factors)";
		System.out.println(progress);
		clearProgress();
	}

	public void progressLL (Mersenne2StateV2 state) {
		progressErase();
		// elapsed for this prime
//		progress = "["+tellDuration(state.thisPrimeTimer.getElapsedTime())+"]" +
//				   "["+tellDuration(state.thisPrimeTimer.getRunningTime())+"] "
//		// LL loop counter (1-based) 
//				+state.llLoopCtr+" [";
//		// avg nanos per LL calc
//		if (state.thisPrimeLLcalcCnt > 0) {
//			progress += (state.thisPrimeLLcalcTime / state.thisPrimeLLcalcCnt) + "ns] ";
//		} else {
//			progress += state.thisPrimeLLcalcTime + "ns] ";
//		}
//		progress += ""+state.findNextMpTFbypassLL+" TFsaved";

		progress =
				", t: " + tellDuration(state.thisPrimeTimer.getRunningTime()) +
				", n: " + state.llLoopCtr +
				", avg: " + 
					( (state.thisPrimeLLcalcCnt > 0) 
					? (state.thisPrimeLLcalcTime / state.thisPrimeLLcalcCnt) + "ns}]"
					: state.thisPrimeLLcalcTime + "ns}]"
					) ;
		
		System.out.print(progress);
	}

	// prime: [duration] llctr total [##ns] S++2 [##ns] -2 [##ns] mod2P-1 [##ns]
	
	public void progressLLstats (Mersenne2StateV2 state, long[] totalStats, int progressCtr, long[] progressStats) {
//		progressErase();
		
		
		progress = preamble + " ["+tellDuration(state.thisPrimeTimer.getElapsedTime())+"] "
				+state.llLoopCtr + ": avg times {";
		if (progressCtr > 0) {
			progress += " total ["+tellNanoAvgDuration(progressCtr, progressStats[0])+"ns]"
					   +" S**2 ["+tellNanoAvgDuration(progressCtr, progressStats[1])+"ns]"
					   +" -2 ["+tellNanoAvgDuration(progressCtr, progressStats[2])+"ns] "
					   + "mod2P-1 ["+tellNanoAvgDuration(progressCtr, progressStats[3])+"ns] }";
		}
		System.out.println(progress);
	}

	public void reportStats (Mersenne2StateV2 state, long[] totalStats) {
//		progressErase();
		// elapsed for this prime
		progress = preamble +" ["+tellDuration(state.thisPrimeTimer.getElapsedTime())+"] " + state.llLoopCtr + " done: avg times {";
		if (state.llLoopCtr > 0) {
			progress += " total ["+tellNanoAvgDuration((int)state.llLoopCtr, totalStats[0])+"ns] "
					  +"S**2 ["+tellNanoAvgDuration((int)state.llLoopCtr, totalStats[1])+"ns] "
					  +"-2 ["+tellNanoAvgDuration((int)state.llLoopCtr, totalStats[2])+"ns] "
					  +"mod2P-1 ["+tellNanoAvgDuration((int)state.llLoopCtr, totalStats[3])+"ns] }";
		}
		System.out.println(progress);
	}

	protected void progressErase () {
		if (progress.length() > 0) {
			int ll = progress.length();
			System.out.print(BS.substring(0, ll) + SP.substring(0, ll) + BS.substring(0, ll));
			progress = "";
		}
	}
	
	public String tellDuration (Duration duration) {
		long seconds = duration.getSeconds();
		int nanos = duration.getNano();
		
        int days = (int)(seconds / SECONDS_PER_DAY);
        long hours = (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR;
        int minutes = (int)((seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        int secs = (int)(seconds % SECONDS_PER_MINUTE);
        int millis = (int)(nanos / NANOS_PER_MILLI);
		
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
        	sb.append(days).append("D+");
        }
        String work;
        if (hours > 0 || sb.length() > 0) {
        	work = "00"+hours;
        	sb.append(work.substring(work.length()-2)).append(':');
        }
        if (minutes > 0 || sb.length() > 0) {
        	work = "00"+minutes;
        	sb.append(work.substring(work.length()-2)).append(':');
        }
        work = "00"+secs;
        sb.append(work.substring(work.length()-2));
        work = "000"+millis;
        sb.append('.').append(work.substring(work.length()-3));
		return sb.toString();
	}
	
	public String tellNanoDuration (long nanos) {
		return tellDuration(Duration.ofNanos(nanos));
	}
	
	public String tellNanoAvgDuration (int ctr, long nanos) {
		return "" + (nanos / ctr);
	}
}
