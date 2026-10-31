#ifndef _IncludedWin64LucasCalcs2
#define _IncludedWin64LucasCalcs2

#include <jni.h>
#include <gmp.h>
#include "LucasCalcsCommon.h"

#ifdef __cplusplus
extern "C" {
#endif

	/**
	 * Calculates next Lucas=Lehmer residual value:
	 *    S[n] = (S[n-1]**2 - 3) mod M
	 *
	 * @param currentS {@code GmpNumber64} current LL residual
	 * @param currentM {@code GmpNumber64} current trial Merssene value (2**p-1 = "p" 1 bits)
	 * @param currentP {@code int} current trial Merssene prime
	 * @param work1 {@code GmpNumber64} preallocated scratch space
	 * @param work2 {@code GmpNumber64} preallocated scratch space
	 * @param debug {@code boolean} process the following debug values?
	 * @param dbSquared {@code GmpNumber64} (optional) result of the S[n-1]**2
	 * @param dbMinus2 {@code GmpNumber64} (optional) result of the - 2
	 * @param dbMod2Pm1 {@code GmpNumber64} (optional) result of the mod 2**P-1
	 */
	JNIEXPORT jobject JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalCalcNextResidual(
		JNIEnv* env, jobject self,
		jint loopCnt,
		jobject jCurrentS,
		jint currentP,
		jint sqrMethod,
		jobject jWork1,
		jobject jWork2,
		jboolean debug,
		jboolean doStats,
		jobject jDbSquared,
		jobject jDbMinus2,
		jobject jDbMod2Pm1);

	/**
	 * Convert current M[p] to string
	 *
	 * @param currentP {@code int} current trial Merssene prime ("p" in 2**p-1) ("p" 1 bits)
	 * @param work1 {@code GmpNumber64} preallocated scratch space
	 * @returns {@code String}
	 */
	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalGetCurrentMString(JNIEnv* env, jobject self, jint currentP, jobject work1);

	/**
	 * Convert GmpNumber64 to string
	 *
	 * @param value {@code GmpNumber64} the number to convert
	 * @returns {@code String}
	 */
	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalGetGmpString(JNIEnv* env, jobject self, jobject value);

	/**
	 * Test a number for zero
	 *
	 * @param value {@code GmpNumber64} the number to test
	 * @returns {@code boolean}
	 */
	JNIEXPORT jboolean JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalIsZero(JNIEnv* env, jobject self, jobject value);

#ifdef __cplusplus
}
#endif

#endif
