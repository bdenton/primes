#ifndef _IncludedWin64LucasCalcs
#define _IncludedWin64LucasCalcs

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
	JNIEXPORT jobject JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64_internalCalcNextResidual(
		JNIEnv* env, jobject self,
		jobject jCurrentS,
		jint currentP,
		jobject jWork1,
		jobject jWork2,
		jboolean debug,
		jboolean getStats,
		jobject jDbSquared,
		jobject jDbMinus2,
		jobject jDbMod2Pm1);

	/**
	 * Convert current M[p] to string
	 *
	 * @param currentP {@code int} current trial Merssene prime ("p" in 2**p-1) ("p" 1 bits)
	 * @param work {@code GmpNumber64} preallocated scratch space
	 * @returns {@code String}
	 */
	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64_internalGetCurrentMString(JNIEnv* env, jobject self, jint currentP, jobject work);
	/**
	 * Convert GmpNumber64 to string
	 *
	 * @param value {@code GmpNumber64} the number to convert
	 * @returns {@code String}
	 */
	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64_internalGetGmpString(JNIEnv* env, jobject self, jobject value);



	struct GmpNumberInfo {
		jobject javaNum;
		mpz_t gmpNum;
		jlongArray javaLimbs;
		jlong* gmpLimbs;
		jboolean limbsAccessed;
		jboolean limbsCopied;
		jboolean limbsModified;
	};

	// Java reflection on the GmpNumber64 class
	struct GmpMethods {
		jclass gmpNumClass;
		jmethodID getLimbs;
		jmethodID getAllocatedLimbCount;
		jmethodID getUsedLimbCount;
		jmethodID setUsedLimbCount;
		jmethodID setJniMadeCopy;
	};

#ifdef __cplusplus
}
#endif

#endif
