package net.wdenton.primes.math;

public final class MpztHolder64 {
   final long data;   // actually mpz_ptr (pointer to __mpz_struct in mpir/gmp
   
   public MpztHolder64 (long data) {
     this.data = data;
   }
}
