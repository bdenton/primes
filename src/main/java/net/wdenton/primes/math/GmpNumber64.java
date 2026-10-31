package net.wdenton.primes.math;

public class GmpNumber64 implements GmpNumber {
	
	private long[] limbs;
	private int allocatedLimbCount;
	private int usedLimbCount;
	private boolean jniMadeCopy;
	
	public GmpNumber64() {
	}
	public GmpNumber64(int bitsize) {
		int fullLimbCount = bitsize >>> BITS_TO_LIMBS_SHIFT;
		int extraBits = bitsize & EXTRA_BITS_MASK;
		int usedLimbCount = fullLimbCount + (0 == extraBits ? 0 : 1);
		int allocLimbCount = usedLimbCount * 2 + 2; // size of squared + extra padding
		
		setAllocatedLimbCount(allocLimbCount);
		setUsedLimbCount(0);
		
		setLimbs(new long[allocLimbCount]);
	}
	
	@Override
	public GmpNumber clear() {
		usedLimbCount = 0;
		if (limbs != null) {
			limbs[0] = 0l;
		}
		return this;
	}

	@Override
	public void release() {
		limbs = null;
		allocatedLimbCount = usedLimbCount = 0;
	}
	
	@Override
	public GmpNumber setValue(long value) {
		if (limbs != null) {
			if (value >= 0) {
				limbs[0] = value;
				usedLimbCount = 1;
			} else {
				limbs[0] = -value;
				usedLimbCount = -1;
			}
		}
		return this;
	}
	
	@Override
	public GmpNumber setValue(long[] value) {
		if (limbs != null) {
			if (value != null && value.length > 0) {
				usedLimbCount = value.length;
				int needed = usedLimbCount + 2;
				if (needed > allocatedLimbCount) {
					allocatedLimbCount = needed;
					setLimbs(new long[needed]);
				}
				System.arraycopy(value, 0, limbs, 0, value.length);
			}
		}
		return this;
	}
	
	@Override
	public long[] getOpaqueValue() {
		long[] result = EMPTY_LIMBS;
		if (usedLimbCount > 0) {
			result = new long[usedLimbCount];
			System.arraycopy(limbs, 0, result, 0, usedLimbCount);
		}
		return result;
	}

	public int getAllocatedLimbCount() {
		return allocatedLimbCount;
	}
	public void setAllocatedLimbCount (int allocatedLimbCount) {
		this.allocatedLimbCount = allocatedLimbCount;
	}
	public int getUsedLimbCount () {
		return usedLimbCount;
	}
	public void setUsedLimbCount (int usedLimbCount) {
		this.usedLimbCount = usedLimbCount;
	}
	public long[] getLimbs () {
		return limbs;
	}
	public void setLimbs (long[] limbs) {
		this.limbs = limbs;
	}
	public boolean isJniMadeCopy() {
		return jniMadeCopy;
	}
	public void setJniMadeCopy(boolean jniMadeCopy) {
		this.jniMadeCopy = jniMadeCopy;
	}
}
