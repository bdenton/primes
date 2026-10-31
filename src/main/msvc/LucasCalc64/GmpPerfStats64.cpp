// GmpPerfStats64.cpp : Defines the exported functions for the DLL.
//

#include "framework.h"
#include <stdlib.h>
#include <string.h>
#include "GmpPerfStats64.h"

#ifdef __cplusplus
extern "C" {
#endif

	// field refernces from GmpPerfStats64 superclass

	static const char* STATS_CLASSNAME = "net/wdenton/primes/math/GmpPerfStats64";
	static const char* STATS_CLASSTYPE = "Lnet/wdenton/primes/math/GmpPerfStats64;";

	static const char* LONG_FIELDTYPE = "J";
	static const char* DOUBLE_FIELDTYPE = "D";

	static const char* STATS_TICKSPERSEC_FIELDNAME = "ticksPerSec";
	static const char* STATS_NANOSPERTICK_FIELDNAME = "nanosPerTick";
	static const char* STATS_CLOCKSPEEDMHZ_FIELDNAME = "clockSpeedMHz";

	static const char* STATS_STARTTIME_FIELDNAME = "startTime";
	static const char* STATS_ENDTIME_FIELDNAME = "endTime";

	static const char* STATS_STARTSPOW2_FIELDNAME = "startSpow2";
	static const char* STATS_ENDSPOW2_FIELDNAME = "endSpow2";

	static const char* STATS_STARTMINUS2_FIELDNAME = "startMinus2";
	static const char* STATS_ENDMINUS2_FIELDNAME = "endMinus2";

	static const char* STATS_STARTMOD2PM1_FIELDNAME = "startMod2Pm1";
	static const char* STATS_ENDMOD2PM1_FIELDNAME = "endMod2Pm1";

	static struct StatFields statFields = {};


	/**
	 * Initialize the Java PerfStats object
	 */
	JNIEXPORT void JNICALL Java_net_wdenton_primes_math_GmpPerfStats64_internalInitStats(JNIEnv* env, jobject self) {
		statFields.statsClass = env->FindClass(STATS_CLASSNAME);
		if (statFields.statsClass != NULL) {
			statFields.statsClass = (jclass)env->NewGlobalRef(statFields.statsClass);
		}
		if (statFields.statsClass != NULL) {
			statFields.ticksPerSec_fid = env->GetFieldID(statFields.statsClass, STATS_TICKSPERSEC_FIELDNAME, LONG_FIELDTYPE);
			statFields.nanosPerTick_fid = env->GetFieldID(statFields.statsClass, STATS_NANOSPERTICK_FIELDNAME, LONG_FIELDTYPE);
			statFields.clockSpeedMHz_fid = env->GetFieldID(statFields.statsClass, STATS_CLOCKSPEEDMHZ_FIELDNAME, DOUBLE_FIELDTYPE);

			statFields.startTime_fid = env->GetFieldID(statFields.statsClass, STATS_STARTTIME_FIELDNAME, LONG_FIELDTYPE);
			statFields.startSpow2_fid = env->GetFieldID(statFields.statsClass, STATS_STARTSPOW2_FIELDNAME, LONG_FIELDTYPE);
			statFields.endSpow2_fid = env->GetFieldID(statFields.statsClass, STATS_ENDSPOW2_FIELDNAME, LONG_FIELDTYPE);
			statFields.startMinus2_fid = env->GetFieldID(statFields.statsClass, STATS_STARTMINUS2_FIELDNAME, LONG_FIELDTYPE);
			statFields.endMinus2_fid = env->GetFieldID(statFields.statsClass, STATS_ENDMINUS2_FIELDNAME, LONG_FIELDTYPE);
			statFields.startMod2Pm1_fid = env->GetFieldID(statFields.statsClass, STATS_STARTMOD2PM1_FIELDNAME, LONG_FIELDTYPE);
			statFields.endMod2Pm1_fid = env->GetFieldID(statFields.statsClass, STATS_ENDMOD2PM1_FIELDNAME, LONG_FIELDTYPE);
			statFields.endTime_fid = env->GetFieldID(statFields.statsClass, STATS_ENDTIME_FIELDNAME, LONG_FIELDTYPE);
		}

		LARGE_INTEGER ticksPerSecond;

		// get the high resolution counter's accuracy
		QueryPerformanceFrequency(&ticksPerSecond);

		double MHz = 0.0 + ticksPerSecond.QuadPart;
		MHz /= 1000000.0;

		LONGLONG nanosecs = 1000000000 / ticksPerSecond.QuadPart;

		setLongField(env, self, statFields.ticksPerSec_fid, ticksPerSecond.QuadPart);
		setLongField(env, self, statFields.nanosPerTick_fid, nanosecs);
		setDoubleField(env, self, statFields.clockSpeedMHz_fid, MHz);
	}

	struct StatFields* getFieldIds() {
		return &statFields;
	}

	int setStatsCounter(JNIEnv* env, jobject self, jfieldID fieldId) {
		if (NULL == fieldId) return 0;
		LARGE_INTEGER tick;
		QueryPerformanceCounter(&tick);
		env->SetLongField(self, fieldId, (jlong)tick.QuadPart);
		return 1;
	}

	int setLongField(JNIEnv* env, jobject self, jfieldID fieldId, LONGLONG value) {
		if (NULL == fieldId) return 0;
		env->SetLongField(self, fieldId, (jlong)value);
		return 1;
	}

	int setDoubleField(JNIEnv* env, jobject self, jfieldID fieldId, double value) {
		if (NULL == fieldId) return 0;
		env->SetDoubleField(self, fieldId, (jdouble)value);
		return 1;
	}

#ifdef __cplusplus
}
#endif
