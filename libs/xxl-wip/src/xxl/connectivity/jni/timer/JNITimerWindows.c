/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
#include <windows.h>

/**
 * Implements the Timer interface with JNI under Windows.
 * The methods QueryPerformanceFrequency and QueryPerformanceCounter
 * are used for this purpose.
 */
#include "xxl_core_util_timers_JNITimer.h"

// number of ticks per second:
static jlong frequency;
static jclass cls;
static jfieldID fid;

JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * vm, void * reserved) {
	LARGE_INTEGER counterFrequency;
	QueryPerformanceFrequency(&counterFrequency);
	frequency = counterFrequency.QuadPart;

	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL
Java_xxl_core_util_timers_JNITimer_start (JNIEnv *env, jobject obj) {
	LARGE_INTEGER counterReading;
	cls=(*env)->GetObjectClass(env, obj);
	fid=(*env)->GetFieldID(env, cls, "starttime", "J");

	QueryPerformanceCounter(&counterReading);
	(*env)->SetLongField(env, obj, fid, counterReading.QuadPart);
}

JNIEXPORT jlong JNICALL
Java_xxl_core_util_timers_JNITimer_getDuration (JNIEnv *env, jobject obj) {
	LARGE_INTEGER counterReading;

	QueryPerformanceCounter(&counterReading);
	return counterReading.QuadPart-((*env)->GetLongField(env, obj, fid));
}

JNIEXPORT jlong JNICALL
Java_xxl_core_util_timers_JNITimer_getTicksPerSecond (JNIEnv *env, jobject obj) {
	return frequency;
}
