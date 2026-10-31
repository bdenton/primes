package net.wdenton.primes.math;

public interface GmpNumber {
	public static final int BITS_PER_LIMB = 64;
	public static final int BITS_TO_LIMBS_SHIFT = 6;
	public static final int EXTRA_BITS_MASK = 0x0000003F;
	public static final long[] EMPTY_LIMBS = new long[0];
	
	public long[] getOpaqueValue();
	public GmpNumber setValue (long value);
	public GmpNumber setValue (long[] value);
	public GmpNumber clear (); // might return null if deallocated
	public void release ();
}
