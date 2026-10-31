package net.wdenton.primes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Factors {

	private boolean isPrime;
	private int number;
	private List<Factor> factors;

	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
		this.isPrime = true;
	}

	public List<Factor> getFactors() {
		return factors != null
				? factors
				: Collections.emptyList();
	}
	public void setFactors(List<Factor> factors) {
		this.isPrime = null == factors || factors.size() == 0;
		this.factors = factors;
	}
	public void addFactor(Factor factor) {
		if (null == factors) {
			factors = new ArrayList<Factor>();
		}
		factors.add(factor);
		isPrime = false;
	}
	public void addFactor(int prime, int power) {
		addFactor(new Factor(prime, power));
	}
	
	public void setIsPrime(boolean isPrime) {
		this.isPrime = isPrime;
	}
	
	public boolean isPrime () {
		return isPrime;
	}

	public static class Factor {
		int prime;
		int power;
		public Factor(int prime, int power) {
			this.prime = prime;
			this.power = power;
		}
		public int getPrime() {
			return prime;
		}
		public void setPrime(int prime) {
			this.prime = prime;
		}
		public int getPower() {
			return power;
		}
		public void setPower(int power) {
			this.power = power;
		}
	}
}
