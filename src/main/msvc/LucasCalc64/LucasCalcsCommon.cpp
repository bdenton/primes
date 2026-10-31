// LucasCalcs64.cpp : Defines the exported functions for the DLL.
//

#include "framework.h"

#include <stdlib.h>
#include <string.h>
#include "LucasCalcsCommon.h"
#include "GmpNumber64v2.h"

static void dumpit(const char* name, mpz_srcptr num);
static void dumpit(const char* name, mpz_srcptr num, BOOL showLimbs);

#ifdef __cplusplus
extern "C" {
#endif

	static const mp_limb_t TWO = 2ull;

	void calcSpow2(mpz_ptr result, mpz_srcptr currentS, int sqrMethod, BOOL debug) {
		if (debug) {
			int bitSize = (int)mpn_sizeinbase(PTR(currentS), SIZ(currentS), 2);
			printf("size: bits: %u; limbs: %u\n", bitSize, SIZ(currentS));
			dumpit("S[n-1]", currentS, true);
		}
		int vSize = SIZ(currentS);

		switch (sqrMethod) {
			case 1: {	// sqrMethod = 1 : use mpn_mul_n
				mpn_mul_n(PTR(result), PTR(currentS), PTR(currentS), vSize);
				break;
			}

			case 2: {	// sqrMethod = 2 : use mpn_mul
				mpn_mul(PTR(result), PTR(currentS), vSize, PTR(currentS), vSize);
				break;
			}

			default: {	// otherwise use mpn_sqr
				mpn_sqr(PTR(result), PTR(currentS), vSize);
			}
		}

		// mpn_xxx does not adjust used limb count
		vSize <<= 1;
		MPN_NORMALIZE(PTR(result), vSize);
		result->_mp_size = vSize;
		if (debug) {
			int bitSize = (int)mpn_sizeinbase(PTR(result), SIZ(result), 2);
			printf("size: bits: %u; limbs: %u\n", bitSize, SIZ(result));
			dumpit("S[n-1]**2", result, true);
		}
	}

	void calcMinus2(mpz_ptr result, mpz_srcptr Spow2, BOOL debug) {
		mpn_sub_1(PTR(result), PTR(Spow2), SIZ(Spow2), TWO);
		int vSize = SIZ(Spow2);
		MPN_NORMALIZE(PTR(result), vSize);
		result->_mp_size = vSize;
		if (debug) {
			int bitSize = (int)mpn_sizeinbase(PTR(result), SIZ(result), 2);
			printf("size: bits: %u; limbs: %u\n", bitSize, SIZ(result));
			dumpit(" -2   ", result, true);
		}
	}

	void calcMod2Pm1(mpz_ptr result, int currentP, mpz_ptr work, mpz_ptr work2, BOOL debug) {
		/**
			if result.bits.size > currentP:
				@work = result.bits(currentP:*)
				result = result.bits(0:currentP) // result size always >= @work size
				result += @work
				if result.bits.size > currentP:
					@work = result.bits(currentP:*)
					result = result.bits(0:currentP) // size always >= work size
					result += @work
			// check for 2**n-1 result alias
		*/

		if (debug) dumpit("input", result);

		// Pre-calculated invariants
		int fullLimbs = currentP >> BITS_TO_LIMBS_SHIFT;
		int highBits = currentP & PARTIAL_BITS_MASK;
		mp_size_t expectedLimbs = fullLimbs + (highBits > 0 ? 1 : 0);
		if (debug) printf("full limbs:%u + high bits:%u = expected:%u\n", fullLimbs, highBits, expectedLimbs);
		mp_limb_t topMask = (~0ull) >> (64 - highBits);

		// Loop condition: dynamically check if the structure exceeds P bits
		while ( SIZ(result) > fullLimbs + 1
		    || (SIZ(result) == fullLimbs + 1 && highBits != 0 && (PTR(result)[fullLimbs] >> highBits) > 0) 
			|| (SIZ(result) == fullLimbs + 1 && highBits == 0 && PTR(result)[fullLimbs] > 0) ) {

			int copyCnt = SIZ(result) - fullLimbs;

			// Extract the high bits into 'work'
			if (highBits != 0) {

				mpn_copyi(PTR(work2), &PTR(result)[fullLimbs], copyCnt);
				// mpn_copyi does not update _mp_size
				SIZ(work2) = copyCnt;
				if (debug) dumpit("lo copied ", work2);


				mpn_rshift(PTR(work), PTR(work2), SIZ(work2), highBits);
				// mpn_rshift does not update _mp_size
				SIZ(work) = copyCnt;

				while (SIZ(work) > 0 && PTR(work)[SIZ(work) - 1] == 0) {
					SIZ(work)--;
				}
				if (debug) dumpit("lo shifted ", work);

			} else {

				mpn_copyi(PTR(work), &PTR(result)[fullLimbs], copyCnt);
				// mpn_copyi does not update _mp_size
				SIZ(work) = copyCnt;
				if (debug) dumpit("copy ", work);

			}

			// Mask out the most lower "P" bits of result

			if (highBits != 0) {
				PTR(result)[fullLimbs] &= topMask;
			}

			// Dynamically normalize result size before adding

			SIZ(result) = fullLimbs +  (highBits != 0 && PTR(result)[fullLimbs] != 0 ? 1 : 0);
			while (SIZ(result) > 0 && PTR(result)[SIZ(result) - 1] == 0) {
				SIZ(result)--;
			}
			if (debug) dumpit("normalized ", result);

			// Perform the addition (result = result + work)

			mp_limb_t carry = 0;
			if (SIZ(work) > SIZ(result)) {
				carry = mpn_add(PTR(result), PTR(work), SIZ(work), PTR(result), SIZ(result));
				SIZ(result) = SIZ(work);
			}
			else if (SIZ(result) > 0) {
				carry = mpn_add(PTR(result), PTR(result), SIZ(result), PTR(work), SIZ(work));
			}
			else {
				mpn_copyi(PTR(result), PTR(work), SIZ(work));
				SIZ(result) = SIZ(work);
			}
			if (debug) dumpit("b4 carry ", result);

			// Append carry if generated

			if (carry) {
				PTR(result)[SIZ(result)] = carry;
				SIZ(result)++;
				if (debug) dumpit("w/carry ", result);
			}
		
		} // end of while loop

		// --- FINAL PSEUDO-MODULO ADJUSTMENT ---
		// If the number settled exactly on 2**P - 1 (or slightly above due to a carry),
		// do a direct O(1) subtraction rather than letting it ruin the next loop squaring.
		
		if (SIZ(result) == fullLimbs + 1 && highBits != 0 && PTR(result)[fullLimbs] == topMask) {
			// Check if lower limbs are all 1s
			int isAlias = 1;
			for (int i = 0; i < fullLimbs; i++) {
				if (PTR(result)[i] != GMP_NUMB_MAX) {
					isAlias = 0;
					break;
				}
			}

			if (isAlias) {
				if (debug) printf(">> all 1-bits; setting result to zero\n");
				mpn_zero(PTR(result), SIZ(result));
				SIZ(result) = 0;
			}
		}
	}

	void copyNumber(mpz_ptr target, mpz_srcptr source, BOOL debug) {
		mpn_copyi(PTR(target), PTR(source), SIZ(source));
		// mpn_copyi doesn't change _mp_size
		SIZ(target) = SIZ(source);
	}

	void calcTwoPm1(mpz_ptr result, int currentP, BOOL debug) {
		// build a value that is currentP 1-bits
		int fullLimbs = currentP >> BITS_TO_LIMBS_SHIFT;
		int highBits = currentP & PARTIAL_BITS_MASK;
		for (int i = 0; i < fullLimbs; i++) {
			PTR(result)[i] = ~0ull;
		}
		int newSize = fullLimbs;
		if (highBits != 0) {
			PTR(result)[fullLimbs] = ((~0ull) >> (GMP_LIMB_BITS - highBits));
			newSize++;
		}
		SIZ(result) = newSize;
	}

	jstring num2Jstr(JNIEnv* env, mpz_srcptr value, BOOL debug) {
		size_t strSize = mpn_sizeinbase(PTR(value), SIZ(value), 10) + 2;
		char* cString = (char*)malloc(strSize);
		mpz_get_str(cString, 10, value);
		jstring result = env->NewStringUTF(cString);
		free(cString);
		return result;
	}

#ifdef __cplusplus
}
#endif

static void dumpit(const char* name, mpz_srcptr num) {
	dumpit(name, num, false);
}
static void dumpit(const char* name, mpz_srcptr num, BOOL showLimbs) {
	size_t strSize = mpn_sizeinbase(PTR(num), SIZ(num), 10) + 2;
	char* cString = (char*)malloc(strSize);
	printf("%s => \t%s\n", name, mpz_get_str(cString, 10, num));
	if (SIZ(num) > 4 || showLimbs) {
		for (int i = 0; i < SIZ(num); i++) {
			printf("\t\t[%u] %llx\n", i, PTR(num)[i]);
		}
	} else
	if (SIZ(num) <= 4) {
		strSize = mpn_sizeinbase(PTR(num), SIZ(num), 16) + 2;
		char* xString = (char*)malloc(strSize);
		strSize = mpn_sizeinbase(PTR(num), SIZ(num), 2) + 2;
		char* bString = (char*)malloc(strSize);
		printf("\t\t%s\n\t\t%s", mpz_get_str(xString, 16, num), mpz_get_str(bString, 2, num));
	}
	printf("\n");
}
