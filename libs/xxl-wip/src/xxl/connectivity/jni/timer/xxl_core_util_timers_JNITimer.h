/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class xxl_core_util_timers_JNITimer */

#ifndef _Included_xxl_core_util_timers_JNITimer
#define _Included_xxl_core_util_timers_JNITimer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     xxl_core_util_timers_JNITimer
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xxl_core_util_timers_JNITimer_start
  (JNIEnv *, jobject);

/*
 * Class:     xxl_core_util_timers_JNITimer
 * Method:    getDuration
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_xxl_core_util_timers_JNITimer_getDuration
  (JNIEnv *, jobject);

/*
 * Class:     xxl_core_util_timers_JNITimer
 * Method:    getTicksPerSecond
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_xxl_core_util_timers_JNITimer_getTicksPerSecond
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
