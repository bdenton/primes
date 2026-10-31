/**
 * 
 */
package net.wdenton.primes;

import java.io.IOException;
import java.util.List;

import net.wdenton.primes.Factors.Factor;

/**
 * @author WilliamDenton
 *
 */
public class FactorPrintMeasure {
	
	public static final int PCT_SUPER = 65;
	public static final int PCT_DOT = 60;

	public static final int INTERVAL = 1000000;
	public static final String DLM = "|";
	PrimeFactorFiles files = new PrimeFactorFiles();

	
	
	// ////////////////////////////////////
	// ////  M A I N  processing
	// ////////////////////////////////////
	
	void process () throws IOException {
		files.initFactorsInput();
		
		int maxSpace = 0;
		int intervalMaxSpace = 0;
		int intNumber = 0;
		int intFactors = 0;
		int intMaxExp = 0;
		int intDigits = 0;
		int intSupers = 0;
		int intDots = 0;
		int lastInterval = 0;
		int nextInterval = INTERVAL;

		for (Factors entry : files.asIterable()) {
			int number = entry.getNumber();
			
			if (!entry.isPrime()) {
				List<Factor> factors = entry.getFactors();
				int nFactors = factors.size();

				int digits = 0;
				int supers = 0;
				int dots = nFactors - 1;
				int maxExp = 1;
				
				for (Factor factor : factors) {
					digits += Integer.toString(factor.getPrime()).length();
					int exp = factor.getPower();
					if (exp > maxExp) {
						maxExp = exp;
					}
					if (exp > 1) {
						supers += Integer.toString(exp).length();
					}
				}
				int space = ( (digits * 100)
						    + (supers * PCT_SUPER)
						    + (dots * PCT_DOT)
						    + 49
						    ) / 100;
				if (space > maxSpace) {
					maxSpace = space;
					reportMax("new max space", number, nFactors, maxExp, digits, supers, dots, space);
				}
				if (space > intervalMaxSpace) {
					intervalMaxSpace = space;
					intNumber = number;
					intFactors = nFactors;
					intMaxExp = maxExp;
					intDigits = digits;
					intSupers = supers;
					intDots = dots;
				}
			}
			if (number >= nextInterval) {
				reportMax("["+lastInterval+"]-["+nextInterval+"] max", intNumber, intFactors, intMaxExp, intDigits, intSupers, intDots, intervalMaxSpace);
				lastInterval = nextInterval;
				nextInterval += INTERVAL;
				intervalMaxSpace = 0;
			}
		}
		if (intervalMaxSpace > 0) {
			reportMax("["+lastInterval+"]-["+nextInterval+"] max", intNumber, intFactors, intMaxExp, intDigits, intSupers, intDots, intervalMaxSpace);
		}
		files.closeFactors();
	}

	void reportMax(String msg, int number, int factors, int maxExp, int digits, int supers, int dots, int space) {
		System.out.println(msg+"... number ["+number+"] factors ["+factors+"] highest exponeent ["+maxExp+"] (digits ["+digits+"] supers ["+supers+"] dots ["+dots+"]) =  space ["+space+"]");
	}

	public static void main(String[] args) {
		FactorPrintMeasure engine = new FactorPrintMeasure();
		
		if (args != null && args.length > 0) {
			engine.files.setRootPath(args[0]);
		}
		
		try {
			engine.process();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
}
