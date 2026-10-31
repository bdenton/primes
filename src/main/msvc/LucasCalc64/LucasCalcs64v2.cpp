// LucasCalcs64v2.cpp : Defines the exported functions for the DLL.
//

#include "framework.h"
#include <stdlib.h>
#include <string.h>
#include "GmpNumber64v2.h"
#include "GmpPerfStats64.h"
#include "LucasCalcs64v2.h"

#ifdef __cplusplus
extern "C" {
#endif

	/**
	 * Calculates next Lucas-Lehmer residual value:
	 *    S[n] = (S[n-1]**2 - 2) mod M
	 *
	 * @param currentS {@code GmpNumber64} current LL residual
	 * @param currentP {@code int} current trial Merssene prime
	 * @param work1 {@code GmpNumber64} preallocated intermediate working field
	 * @param work2 {@code GmpNumber64} preallocated intermediate working field
	 * @param dbSquared {@code GmpNumber64} (optional) debug results of s[n-1]**2 operation
	 * @param dbMinus2 {@code GmpNumber64} (optional) debug results of s[n-1]**2-2 operation
	 * @param dbMod2Pm1 {@code GmpNumber64} (optional) debug results of mod 2**P-1 operation
	 *
	 * currentS updated with the result
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
		jboolean getStats,
		jobject jDbSquared,
		jobject jDbMinus2,
		jobject jDbMod2Pm1)
	{
		mpz_ptr currentSptr;
		mpz_ptr work1ptr;
		mpz_ptr work2ptr;
		// debug info
		mpz_ptr dbSquared = NULL;
		mpz_ptr dbMinus2 = NULL;
		mpz_ptr dbMod2Pm1 = NULL;

		if (debug) {
			fflush(stdout);
			printf("\n+++ Loop: %u +++++++++++++++++++++++++++++++++++++++++++++\n", loopCnt);
		}
		struct StatFields* statFields = getFieldIds();

		if (getStats) {
			setStatsCounter(env, self, statFields->startTime_fid);
		}

		currentSptr = getMpzPtrFromNumber(env, jCurrentS);
		work1ptr = getMpzPtrFromNumber(env, jWork1);
		work2ptr = getMpzPtrFromNumber(env, jWork2);
		if (debug) {
			dbSquared = getMpzPtrFromNumber(env, jDbSquared);
			dbMinus2 = getMpzPtrFromNumber(env, jDbMinus2);
			dbMod2Pm1 = getMpzPtrFromNumber(env, jDbMod2Pm1);
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    work1 = currentS ** 2
		// /////////////////////////////////////////////////////////////////////

		mpz_ptr source = currentSptr;
		mpz_ptr result = work1ptr;

		if (getStats) {
			setStatsCounter(env, self, statFields->startSpow2_fid);
		}
		calcSpow2(result, source, sqrMethod, debug);  // no operand overlap allowed
		if (getStats) {
			setStatsCounter(env, self, statFields->endSpow2_fid);
		}
		if (debug) {
			copyNumber(dbSquared, result, false);
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    currentS = scratch - 2
		// /////////////////////////////////////////////////////////////////////

		source = result;
		result = currentSptr;

		if (getStats) {
			setStatsCounter(env, self, statFields->startMinus2_fid);
		}
		calcMinus2(result, source, debug);
		if (getStats) {
			setStatsCounter(env, self, statFields->endMinus2_fid);
		}
		if (debug) {
			copyNumber(dbMinus2, result, false);
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    currentS = scratch mod 2Pm1
		// /////////////////////////////////////////////////////////////////////

		if (getStats) {
			setStatsCounter(env, self, statFields->startMod2Pm1_fid);
		}
		calcMod2Pm1(result, currentP, work1ptr, work2ptr, debug);
		if (getStats) {
			setStatsCounter(env, self, statFields->endMod2Pm1_fid);
		}
		if (debug) {
			copyNumber(dbMod2Pm1, result, false);
		}

		if (getStats) {
			setStatsCounter(env, self, statFields->endTime_fid);
		}

		if (debug) {
			fflush(stdout);
		}

		return jCurrentS;
	}


	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalGetCurrentMString(
		JNIEnv* env, jobject self,
		jint currentP,
		jobject jWork1)
	{
		mpz_ptr work1ptr = getMpzPtrFromNumber(env, jWork1);

		calcTwoPm1(work1ptr, currentP, false);
		return num2Jstr(env, work1ptr, false);
	}

	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalGetGmpString(
		JNIEnv* env, jobject self,
		jobject value)
	{

		jstring result = NULL;

		if (value != NULL) {
			mpz_ptr valuePtr = getMpzPtrFromNumber(env, value);
			result = num2Jstr(env, valuePtr, false);
		}

		return result;
	}

	JNIEXPORT jboolean JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64v2_internalIsZero(
		JNIEnv* env, jobject self,
		jobject value)
	{
		jboolean result = JNI_FALSE;

		if (value != NULL) {
			mpz_ptr valuePtr = getMpzPtrFromNumber(env, value);
			if (SIZ(valuePtr) == 0) {
				result = JNI_TRUE;
			}
		}
		return result;
	}

#ifdef __cplusplus
}
#endif