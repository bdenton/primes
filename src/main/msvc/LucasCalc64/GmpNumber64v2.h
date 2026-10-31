#ifndef _IncludedWin64LucasNum64
#define _IncludedWin64LucasNum64

#include <jni.h>

#include <gmp.h>

#ifdef __cplusplus
extern "C" {
#endif

	/**
	 * Allocate memory for the mpztHolder field to hold the native mpz_t object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeAllocate(JNIEnv* env, jobject self, jint bitCount);

	/**
	 * Release memory for the mpztHolder field to hold the native mpz_t object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeRelease(JNIEnv* env, jobject self);

	/**
	 * Set the native mpz_t holderr to a long value
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeSetLong(JNIEnv* env, jobject self, jlong value);

	/**
	 * Set the mpz_t used limbs to previously saved limbs 
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeSetLimbs(JNIEnv* env, jobject self, jlongArray usedLimbs);

	/**
	 * Make acopy of the used mpz_t limbs for serialization
	 */
	JNIEXPORT jlongArray JNICALL Java_net_wdenton_primes_math_GmpNumber64v2_nativeCopyUsedLimbs(JNIEnv* env, jobject self);


	/**********************************************/
	/**   the methods the JNI library will use   **/
	/**   to access the wrapped GMP number       **/
	/**********************************************/

	mpz_ptr getMpzPtrFromNumber(JNIEnv* env, jobject gmpNumber64);

	// nails not supported
	#define BITS_TO_LIMBS_SHIFT 6
	#define PARTIAL_BITS_MASK 0x3f

	/**********************************************/
	/**   The MPIR library included some helper  **/
	/**   macros to deal with the mpz structure  **/
	/**   and to manipulate its large number     **/
	/**   (this is all from mpir's gmp_impl.h)   **/
	/**********************************************/

#ifndef __GMP_IMPL_H__

	/* __mpz_struct Field access macros.  */
	#define SIZ(x) ((x)->_mp_size)
	#define PTR(x) ((x)->_mp_d)
	#define LIMBS(x) ((x)->_mp_d)
	#define ALLOC(x) ((x)->_mp_alloc)

	/* bit count to limb count, rounding up */
	#define BITS_TO_LIMBS(n)  (((n) + (GMP_NUMB_BITS - 1)) / GMP_NUMB_BITS)

	/* On the x86s repe/scasl doesn't seem useful, since it takes many cycles to
       start up and would need to strip a lot of zeros before it'd be faster
       than a simple cmpl loop.  Here are some times in cycles for
       std/repe/scasl/cld and cld/repe/scasl (the latter would be for stripping
       low zeros).

				std   cld
		   P5    18    16
		   P6    46    38
		   K6    36    13
		   K7    21    20
	*/
	#ifndef MPN_NORMALIZE
	#define MPN_NORMALIZE(DST, NLIMBS)        \
		do {                                  \
			while ((NLIMBS) > 0)              \
			{                                 \
				if ((DST)[(NLIMBS) - 1] != 0) \
					break;                    \
				(NLIMBS)--;                   \
			}                                 \
		} while (0)
	#endif
	#ifndef MPN_NORMALIZE_NOT_ZERO
	#define MPN_NORMALIZE_NOT_ZERO(DST, NLIMBS) \
		do {                                    \
			ASSERT ((NLIMBS) >= 1);             \
			while (1)                           \
			{                                   \
				if ((DST)[(NLIMBS) - 1] != 0)   \
					break;                      \
				(NLIMBS)--;                     \
			}                                   \
		} while (0)
	#endif
#endif


#ifdef __cplusplus
}
#endif

#endif
