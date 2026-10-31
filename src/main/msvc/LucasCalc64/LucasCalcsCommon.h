#ifndef _IncludedWin64LucasCalcsCommon
#define _IncludedWin64LucasCalcsCommon

#include <jni.h>
#include <gmp.h>

#ifdef __cplusplus
extern "C" {
#endif

	// Lucas-Lehmer calc methods
	void calcSpow2(mpz_ptr result, mpz_srcptr currentS, int sqrMethod, BOOL debug);
	void calcMinus2(mpz_ptr result, mpz_srcptr Spow2, BOOL debug);
	void calcMod2Pm1(mpz_ptr result, int currentP, mpz_ptr work, mpz_ptr work2, BOOL debug);

	// debug/logging methods
	void copyNumber(mpz_ptr target, mpz_srcptr source, BOOL debug);
	void calcTwoPm1(mpz_ptr result, int currentP, BOOL debug);
	jstring num2Jstr(JNIEnv* env, mpz_srcptr value, BOOL debug);

#ifdef __cplusplus
}
#endif

#endif