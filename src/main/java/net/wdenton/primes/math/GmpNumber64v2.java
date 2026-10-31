package net.wdenton.primes.math;

public class GmpNumber64v2 implements GmpNumber {
	
	
	private MpztHolder64 mpztHolder;
	private boolean allocated = false;

	static {
		System.loadLibrary("LucasCalcs64");
	}
	
	public GmpNumber64v2() {
	}
	public GmpNumber64v2(int bitsize) {
		nativeAllocate((bitsize+BITS_PER_LIMB)*2);
		allocated = true;
	}
	
	@Override
	public GmpNumber setValue(long value) {
		nativeSetLong(value);
		return this;
	}
	
	@Override
	public GmpNumber setValue(long[] value) {
		if (value != null && value.length > 0) {
			nativeSetLimbs(value);
		}
		return this;
	}
	
	@Override
	public long[] getOpaqueValue() {
		long[] result = EMPTY_LIMBS;
		if (mpztHolder != null) {
			result = nativeCopyUsedLimbs();
		}
		return result;
	}

	@Override
	public GmpNumber clear () {
		if (allocated) {
			nativeSetLong(0L);
		}
		return this;
	}

	public void release () {
		synchronized (this) {
			if (allocated) {
				nativeRelease();
				allocated = false;
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	private native void nativeAllocate(int bitCount);
	private native void nativeRelease();
	private native void nativeSetLong(long value);
	private native void nativeSetLimbs(long[] value);
	private native long[] nativeCopyUsedLimbs();
}
