// LucasCalcs64.cpp : Defines the exported functions for the DLL.
//

#include "framework.h"

#include <stdlib.h>
#include <string.h>
#include "LucasCalcs64.h"

#ifdef __cplusplus
extern "C" {
#endif

	// reflection helpers
	static void reflectGmpNumber(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo, jobject jGmpNumber);
	static void reflectGmpNumberClass(JNIEnv* env, GmpMethods* javaMethods, jobject jGmpNumber, BOOL extended);
	static void returnJavaArrayLink(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo, const char* debug);
	static void resetJavaArrayLink(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo);



	/**
	 * Calculates next Lucas-Lehmer residual value:
	 *    S[n] = (S[n-1]**2 - 2) mod M
	 *
	 * @param currentS {@code GmpNumber64} current LL residual
	 * @param currentP {@code int} current trial Merssene prime
	 * @param work1 {@code GmpNumber64} preallocated intermediate working field
	 * @param work2 {@code GmpNumber64} preallocated intermediate working field
	 * @param debug {@code boolean} process the following debug values?
	 * @param dbSquared {@code GmpNumber64} (optional) debug results of s[n-1]**2 operation
	 * @param dbMinus2 {@code GmpNumber64} (optional) debug results of s[n-1]**2-2 operation
	 * @param dbMod2Pm1 {@code GmpNumber64} (optional) debug results of mod 2**P-1 operation
	 *
	 * currentS updated with the result
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
		jobject jDbMod2Pm1)
	{
		GmpMethods javaMethods = {};
		GmpNumberInfo currentSinfo = {};
		GmpNumberInfo work1info = {};
		GmpNumberInfo work2info = {};
		// debug info
		GmpNumberInfo dbSquared = {};
		GmpNumberInfo dbMinus2 = {};
		GmpNumberInfo dbMod2Pm1 = {};

		reflectGmpNumberClass(env, &javaMethods, jCurrentS, TRUE);
		reflectGmpNumber(env, &javaMethods, &currentSinfo, jCurrentS);
		reflectGmpNumber(env, &javaMethods, &work1info, jWork1);
		reflectGmpNumber(env, &javaMethods, &work2info, jWork2);
		if (debug) {
			reflectGmpNumber(env, &javaMethods, &dbSquared, jDbSquared);
			reflectGmpNumber(env, &javaMethods, &dbMinus2, jDbMinus2);
			reflectGmpNumber(env, &javaMethods, &dbMod2Pm1, jDbMod2Pm1);
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    work1 = currentS ** 2
		// /////////////////////////////////////////////////////////////////////

		GmpNumberInfo* source = &currentSinfo;
		GmpNumberInfo* result = &work1info;

		calcSpow2(result->gmpNum, source->gmpNum, 0, false);  // no operand overlap allowed
		result->limbsModified = JNI_TRUE;
		if (debug) {
			copyNumber(dbSquared.gmpNum, result->gmpNum, false);
			dbSquared.limbsModified = JNI_TRUE;
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    currentS = scratch - 2
		// /////////////////////////////////////////////////////////////////////

		source = result;
		result = &currentSinfo;

		calcMinus2(result->gmpNum, source->gmpNum, false);
		result->limbsModified = JNI_TRUE;
		if (debug) {
			copyNumber(dbMinus2.gmpNum, result->gmpNum, false);
			dbMinus2.limbsModified = JNI_TRUE;
		}

		// /////////////////////////////////////////////////////////////////////
		// ////    currentS = scratch mod 2Pm1
		// /////////////////////////////////////////////////////////////////////

		calcMod2Pm1(result->gmpNum, currentP, work1info.gmpNum, work2info.gmpNum, false);
		result->limbsModified = JNI_TRUE;
		if (debug) {
			copyNumber(dbMod2Pm1.gmpNum, result->gmpNum, false);
			dbMod2Pm1.limbsModified = JNI_TRUE;
		}

		returnJavaArrayLink(env, &javaMethods, &currentSinfo, "currentS");	// pass back changes
		resetJavaArrayLink(env, &javaMethods, &work1info);		// ignore any changes
		resetJavaArrayLink(env, &javaMethods, &work2info);		// ignore any changes
		if (debug) {
			returnJavaArrayLink(env, &javaMethods, &dbSquared, NULL);
			returnJavaArrayLink(env, &javaMethods, &dbMinus2, NULL);
			returnJavaArrayLink(env, &javaMethods, &dbMod2Pm1, NULL);
		}
		return jCurrentS;
	}


	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64_internalGetCurrentMString(
		JNIEnv* env, jobject self,
		jint currentP,
		jobject jWork1) {

		GmpMethods javaMethods = {};
		GmpNumberInfo workInfo = {};

		reflectGmpNumberClass(env, &javaMethods, jWork1, FALSE);
		reflectGmpNumber(env, &javaMethods, &workInfo, jWork1);

		calcTwoPm1(workInfo.gmpNum, currentP, false);
		jstring result = num2Jstr(env, workInfo.gmpNum, false);

		resetJavaArrayLink(env, &javaMethods, &workInfo);
		return result;
	}

	JNIEXPORT jstring JNICALL Java_net_wdenton_primes_math_GmpLucasCalcs64_internalGetGmpString(
		JNIEnv* env, jobject self,
		jobject value) {

		jstring result = NULL;

		if (value != NULL) {
			GmpMethods javaMethods = {};
			GmpNumberInfo javaValueInfo = {};

			reflectGmpNumberClass(env, &javaMethods, value, FALSE);
			reflectGmpNumber(env, &javaMethods, &javaValueInfo, value);
			result = num2Jstr(env, javaValueInfo.gmpNum, false);

			resetJavaArrayLink(env, &javaMethods, &javaValueInfo);
		}

		return result;
	}

	static void reflectGmpNumberClass(JNIEnv* env, GmpMethods* javaMethods, jobject jGmpNumber, BOOL enhanced) {
		if (jGmpNumber != NULL) {
			jclass gmpNumClass = env->GetObjectClass(jGmpNumber);
			javaMethods->gmpNumClass = gmpNumClass;
			javaMethods->getLimbs = env->GetMethodID(gmpNumClass, "getLimbs", "()[J");
			javaMethods->getAllocatedLimbCount = env->GetMethodID(gmpNumClass, "getAllocatedLimbCount", "()I");
			javaMethods->getUsedLimbCount = env->GetMethodID(gmpNumClass, "getUsedLimbCount", "()I");
			if (enhanced) {
				javaMethods->setUsedLimbCount = env->GetMethodID(gmpNumClass, "setUsedLimbCount", "(I)V");
				javaMethods->setJniMadeCopy = env->GetMethodID(gmpNumClass, "setJniMadeCopy", "(Z)V");
			}
		}
	}

	static void reflectGmpNumber(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo, jobject jGmpNumber) {
		numInfo->javaNum = jGmpNumber;

		if (jGmpNumber != NULL) {
			mpz_init(numInfo->gmpNum);
			numInfo->gmpNum->_mp_alloc = env->CallIntMethod(jGmpNumber, javaMethods->getAllocatedLimbCount);
			numInfo->gmpNum->_mp_size = env->CallIntMethod(jGmpNumber, javaMethods->getUsedLimbCount);
			numInfo->javaLimbs = (jlongArray)env->CallObjectMethod(jGmpNumber, javaMethods->getLimbs);
			numInfo->gmpLimbs = env->GetLongArrayElements(numInfo->javaLimbs, &numInfo->limbsCopied);
			numInfo->gmpNum->_mp_d = (mp_limb_t*)numInfo->gmpLimbs;
			numInfo->limbsAccessed = JNI_TRUE;
		}
	}

	static void returnJavaArrayLink(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo, const char* debug) {
		/*******************
		if (debug != NULL) {
			printf("Returning %s\n", debug);
			printf("   mpz_t: %p\n", &numInfo->gmpNum);
			printf("   jniMadeCopy: %u\n", (int)numInfo->limbsCopied);
			printf("   used limbs: %u\n", (int)numInfo->gmpNum->_mp_size);
			printf("   mpz_ptr: %p\n", PTR(numInfo->gmpNum));
			printf("   gmpLimbs: %p\n", numInfo->gmpLimbs);
		}
		********************/

		env->CallVoidMethod(numInfo->javaNum, javaMethods->setJniMadeCopy, numInfo->limbsCopied);
		env->ReleaseLongArrayElements(numInfo->javaLimbs, numInfo->gmpLimbs, 0);
		env->CallVoidMethod(numInfo->javaNum, javaMethods->setUsedLimbCount, numInfo->gmpNum->_mp_size);
	}
	static void resetJavaArrayLink(JNIEnv* env, GmpMethods* javaMethods, GmpNumberInfo* numInfo) {
		env->ReleaseLongArrayElements(numInfo->javaLimbs, numInfo->gmpLimbs, JNI_ABORT);
	}

#ifdef __cplusplus
}
#endif