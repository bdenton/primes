package net.wdenton.primes.math;

public class TestTest {

	public static void main(String[] args) {
		int prime = 30091;
		
		System.out.println("Integer....... "+prime);
		System.out.println("bits.......... "+Integer.toBinaryString(prime));
		int hbit = Integer.highestOneBit(prime);
		System.out.println("hightst bit... "+hbit);
		System.out.println("     binary... "+Integer.toBinaryString(hbit));

	}

}
