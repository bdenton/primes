#ifndef _IncludedWin64LucasCalcs3
#define _IncludedWin64LucasCalcs3

#include <jni.h>
#include <gmp.h>

#ifdef __cplusplus
extern "C" {
#endif

	/**
	 * Check if 'prime' is a factor of 2**P-1
	 *
	 * @param currentP {@code int} current Mersenne exponent
	 * @param prime {@code int} prime factor candidate
	 *
	 * @returns true if 'prime' is a factor of 2**P-1
	 */
	JNIEXPORT jint JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v3_internalTrialFactor(
		JNIEnv* env, jobject self,
		jint currentP,
		jint prime);

#ifdef __cplusplus
}
#endif

#endif
