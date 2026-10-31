// LucasCalcs64v3.cpp : Defines the exported functions for the DLL.
//

#include "framework.h"
#include <stdlib.h>
#include <string.h>
#include "GmpNumber64v2.h"
#include "LucasCalcsCommon.h"
#include "LucasCalcs64v3.h"

#ifdef __cplusplus
extern "C" {
#endif

	static jint highestOneBit(jint value);


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
		jint prime) {

		jboolean result = JNI_FALSE;

		if (prime > 2) {
			jint testBit = highestOneBit(currentP);
			jint square = 1;
			while (testBit > 0) {
				square = ((long long)square * square) % prime;
				if (currentP & testBit) {
					square <<= 1;
					if (square > prime) {
						square -= prime;
					}
				}
				testBit >>= 1;
			}
			if (1 == square) {
				result = JNI_TRUE;
			}
		}

		return result;
	}

	static jint highestOneBit(jint i) {
		// HD, Figure 3-1
		i |= (i >> 1);
		i |= (i >> 2);
		i |= (i >> 4);
		i |= (i >> 8);
		i |= (i >> 16);
		return i - (i >> 1);
	}

#ifdef __cplusplus
}
#endif