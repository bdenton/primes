/**
 * 
 */
package net.wdenton.primes;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import net.wdenton.primes.Factors.Factor;

/**
 * @author WilliamDenton
 *
 */
public class DumpPrimeFactors {
	
	public static final int PCT_SUPER = 65;
	public static final int PCT_DOT = 60;

	public static final int INTERVAL = 50;
	public static final String DLM = "|";
	PrimeFactorFiles files = new PrimeFactorFiles();

	
	
	// ////////////////////////////////////
	// ////  M A I N  processing
	// ////////////////////////////////////
	
	void process () throws IOException {
		files.initFactorsInput();
		
		int count = 0;

		for (Factors entry : files.asIterable()) {
			count++;
			System.out.print(""+entry.getNumber()+":\t");
			if (entry.isPrime()) {
				System.out.println("PRIME");
			} else {
				StringBuilder sb = new StringBuilder();
				for (Factor factor : entry.getFactors()) {
					if (sb.length() > 0) {
						sb.append(" * ");
					}
					sb.append(factor.prime);
					if (factor.power > 1) {
						sb.append("^").append(factor.power);
					}
				}
				System.out.println(sb.toString());
			}
			if (count >= INTERVAL) {
				prompt(System.in, "==>");
				count = 0;
			}
		}
		files.closeFactors();
	}

	public static void main(String[] args) {
		DumpPrimeFactors engine = new DumpPrimeFactors();
		
		if (args != null && args.length > 0) {
			engine.files.setRootPath(args[0]);
		}
		
		try {
			engine.process();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	protected static String prompt (InputStream in, String msg) throws IOException {
		String result = null;
		StringBuffer sb = new StringBuffer();
        char[] buf;

		System.out.print(msg);
		System.out.flush();

        buf = new char[128];
        int offset = 0;
        int size = buf.length;
        int c;

loop:   while (true) {
            switch (c = in.read()) {
              
            	case -1: 
            	case '\n': {
            		break loop;
            	}

            	case '\r': {
            		int c2 = in.read();
            		if ((c2 != '\n') && (c2 != -1)) {
            			if (!(in instanceof PushbackInputStream)) {
            				in = new PushbackInputStream(in);
            			}
            			((PushbackInputStream)in).unread(c2);
            		} else {
            			break loop;
            		}
            	}

            	default: {
            		if (offset >= size) {
            			sb.append(buf);
            			offset = 0;
            			buf = new char[128];
            			size = buf.length;
            		}
            		buf[offset++] = (char) c;
            		break;
              	}
            }
        } // end: loop

        if ((sb.length() + offset)== 0) {
            return null;
        }

        char[] part = new char[offset];
        System.arraycopy(buf, 0, part, 0, offset);
        sb.append(part);
        result = sb.toString();
        return result;
	}

}
