// GmpNumber64v2.cpp : Native methods to have native GMP values in Java
//

#include "framework.h"
#include <stdlib.h>
#include <string.h>
#include "GmpNumber64v2.h"


#ifdef __cplusplus
extern "C" {
#endif

	static const char* MPZT_HOLDER64_CLASSNAME = "net/wdenton/primes/math/MpztHolder64";
	static const char* MPZT_HOLDER64_CLASSTYPE = "Lnet/wdenton/primes/math/MpztHolder64;";
	static const char* GMPNUMBER64_CLASSNAME = "net/wdenton/primes/math/GmpNumber64v2";
	static const char* GMPNUMBER64_CLASSTYPE = "Lnet/wdenton/primes/math/GmpNumber64v2;";
	static const char* MPZT_HOLDER64_FIELDNAME = "data";
	static const char* MPZT_HOLDER64_FIELDTYPE = "J";
	static const char* GMPNUMBER64_FIELDNAME = "mpztHolder";
	static const char* GMPNUMBER64_FIELDTYPE = MPZT_HOLDER64_CLASSTYPE;

	static jobject getHolderFromNumber(JNIEnv* env, jobject gmpNumber64);
	static mpz_ptr getMpzPtrFromHolder(JNIEnv* env, jobject mpztHolder);
	static void setMpzPtrHolder(JNIEnv* env, jobject gmpNumber64, jlong mpzptr);
	static void clearMpzPtrHolder(JNIEnv* env, jobject gmpNumber64);

	/***** Cached MpztHolder class info. *****/
	
	static jclass holderClass = NULL; // class reference to MpztHolder64
	static jfieldID holderData_fid = NULL; // field reference to 'data' member
	static jmethodID holderCtor_mid = NULL; // method reference to constructor

	/***** Cached GmpNumber64v2 class info. *****/
	static jclass gmpNumberClass = NULL;// class reference to GmpNumber64v2
	static jfieldID gmpNumberHolder_fid = NULL; // field reference to 'mpztHolder'


	/**
	 * Global initializer for GmpNumber64v2 native methods
	 */
	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
		JNIEnv* env;
		void* envp;

		if (vm->GetEnv(&envp, JNI_VERSION_1_8) == JNI_OK) {
			env = (JNIEnv*)envp;
			/*
			 * Initialize fields, methods, class references
			 */
			holderClass = env->FindClass(MPZT_HOLDER64_CLASSNAME);
			if (holderClass != NULL) {
				holderClass = (jclass)env->NewGlobalRef(holderClass);
			}
			if (holderClass != NULL) {
				holderData_fid = env->GetFieldID(holderClass, MPZT_HOLDER64_FIELDNAME, MPZT_HOLDER64_FIELDTYPE);
				holderCtor_mid = env->GetMethodID(holderClass, "<init>", "(J)V");
			}

			gmpNumberClass = env->FindClass(GMPNUMBER64_CLASSNAME);
			if (gmpNumberClass != NULL) {
				gmpNumberClass = (jclass)env->NewGlobalRef(gmpNumberClass);
			}
			if (gmpNumberClass != NULL) {
				gmpNumberHolder_fid = env->GetFieldID(gmpNumberClass, GMPNUMBER64_FIELDNAME, GMPNUMBER64_FIELDTYPE);
			}
		}
		return JNI_VERSION_1_8;
	}


	/**
	 * Allocate memory for the mpztHolder field to hold the native mpz_t object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeAllocate(JNIEnv* env, jobject self, jint bitCount) {
		mpz_ptr theOne = (mpz_ptr)malloc(sizeof(mpz_t));
		mpz_init2(theOne, bitCount);
		setMpzPtrHolder(env, self, (jlong)theOne);
	}


	/**
	 * Release memory for the mpztHolder field to hold the native mpz_t object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeRelease(JNIEnv* env, jobject self) {
		mpz_ptr theOne = getMpzPtrFromNumber(env, self);
		mpz_clear(theOne);
		free(theOne);
		clearMpzPtrHolder(env, self);
	}


	/**
	 * Set the native mpz_t holderr to a long value
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeSetLong(JNIEnv* env, jobject self, jlong value) {
		mpz_ptr theOne = getMpzPtrFromNumber(env, self);
		mpn_copyi(PTR(theOne), (mp_srcptr)&value, 1);
		// mpn_copyi does not set _mp_size
		SIZ(theOne) = 1;
	}


	/**
	 * Set the mpz_t used limbs to previously saved limbs
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeSetLimbs(JNIEnv* env, jobject self, jlongArray usedLimbs) {
		if (usedLimbs != NULL) {
			jsize usedCount = env->GetArrayLength(usedLimbs);
			mpz_ptr theOne = getMpzPtrFromNumber(env, self);
			if (usedCount > 0) {
				jboolean copied = JNI_FALSE;
				jlong* gmpLimbs = env->GetLongArrayElements(usedLimbs, &copied);
				mpn_copyi(PTR(theOne), (mp_srcptr)gmpLimbs, usedCount);
				// mpn_copyi does not set _mp_size
				SIZ(theOne) = usedCount;
				env->ReleaseLongArrayElements(usedLimbs, gmpLimbs, JNI_ABORT);
			}
			theOne->_mp_size = usedCount;
		}
	}

	/**
	 * Make acopy of the used mpz_t limbs for serialization
	 */
	JNIEXPORT jlongArray JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeCopyUsedLimbs(JNIEnv* env, jobject self) {
		mpz_ptr theOne = getMpzPtrFromNumber(env, self);
		jsize usedCount = SIZ(theOne);
		jlongArray javaLimbs = env->NewLongArray(usedCount);
		jboolean copied = JNI_FALSE;
		jlong* gmpLimbs = env->GetLongArrayElements(javaLimbs, &copied);
		mpn_copyi((mp_ptr)gmpLimbs, PTR(theOne), usedCount);
		env->ReleaseLongArrayElements(javaLimbs, gmpLimbs, 0);
		return javaLimbs;
	}



	/**********************************************/
	/**   the methods the JNI library will use   **/
	/**   to access the wrapped GMP number       **/
	/**********************************************/

	/*
	 * return native mpz_t from GmpNumber
	 */
	mpz_ptr getMpzPtrFromNumber(JNIEnv* env, jobject gmpNumber) {
		return getMpzPtrFromHolder(env, getHolderFromNumber(env, gmpNumber));
	}

	/*
	 * return mpztHolder from GmpNumber
	 */
	static jobject getHolderFromNumber(JNIEnv* env, jobject gmpNumber) {
		return env->GetObjectField(gmpNumber, gmpNumberHolder_fid);
	}

	/*
	 * return mpz_t from MpztHolder
	 */
	static mpz_ptr getMpzPtrFromHolder(JNIEnv* env, jobject mpztHolder) {
		return (mpz_ptr)env->GetLongField(mpztHolder, holderData_fid);
	}

	/*
	 * create a new holder for mpz_t and add to number
	 */
	static void setMpzPtrHolder(JNIEnv* env, jobject gmpNumber, jlong mpzptr) {
		// create new holder object
		jobject holder = env->NewObject(holderClass, holderCtor_mid, (jlong)mpzptr);
	
		// place holder in this number
		env->SetObjectField(gmpNumber, gmpNumberHolder_fid, holder);
	}

	/*
	 * release memory allocated to mpz_t and holder 
	 */
	static void clearMpzPtrHolder(JNIEnv* env, jobject gmpNumber) {
		jobject holder = getHolderFromNumber(env, gmpNumber);
		env->SetLongField(holder, holderData_fid, 0);
		env->SetObjectField(gmpNumber, gmpNumberHolder_fid, NULL);
	}

#ifdef __cplusplus
}
#endif