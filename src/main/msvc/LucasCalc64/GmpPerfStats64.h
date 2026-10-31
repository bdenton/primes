#ifndef _IncludedWin64PerfStats
#define _IncludedWin64PerfStats

#include "framework.h"
#include <jni.h>
#include <gmp.h>

#ifdef __cplusplus
extern "C" {
#endif

	/**
	 * Initialize the Java PerfStats object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpPerfStats64_internalInitStats(
		JNIEnv* env, jobject self);

	/***** Cached GmpPerfStats64 class info. *****/

	struct StatFields {
		jclass statsClass; // class reference to GmpPerfStats64

		jfieldID ticksPerSec_fid; // field reference to 'ticksPerSec' member
		jfieldID nanosPerTick_fid; // field reference to 'nanosPerTick' member
		jfieldID clockSpeedMHz_fid; // field reference to 'clockSpeedMHz' member

		jfieldID startTime_fid; // field reference to 'startTime' member
		jfieldID startSpow2_fid; // field reference to 'startSpow2' member
		jfieldID endSpow2_fid; // field reference to 'endSpow2' member
		jfieldID startMinus2_fid; // field reference to 'startMinus2' member
		jfieldID endMinus2_fid; // field reference to 'endMinus2' member
		jfieldID startMod2Pm1_fid; // field reference to 'startMod2Pm1' member
		jfieldID endMod2Pm1_fid; // field reference to 'endMod2Pm1' member
		jfieldID endTime_fid; // field reference to 'endTime' member
	};

	struct StatFields* getFieldIds();

	// stats gathering methods
	int setStatsCounter(JNIEnv* env, jobject self, jfieldID fieldId);
	int setLongField(JNIEnv* env, jobject self, jfieldID fieldId, LONGLONG value);
	int setDoubleField(JNIEnv* env, jobject self, jfieldID fieldId, double value);

#ifdef __cplusplus
}
#endif

#endif