/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

#ifdef __RAW_H__
#define __RAW_H__
#else

#ifdef DEBUG
#define DEBUG_OUTPUT(format,s) { printf("raw.h:"); printf(format,s); printf("\n"); }
#else
#define DEBUG_OUTPUT(format,s) 
#endif

/* for faster access (initialized only once in init()) */
static jclass cls = NULL;
static jfieldID fidMode;
static jfieldID fidSectors;
static jfieldID fidSectorSize;
static jfieldID fidFilep;
#ifdef SEQ_OPT	
static jfieldID fidLastSector;
#endif

/* Accessing fields of the Java-object */
jint getMode(JNIEnv *env, jobject obj) {
	return (*env)->GetIntField(env, obj, fidMode);
}

void setMode(JNIEnv *env, jobject obj, jint jmode) {
	(*env)->SetIntField(env, obj, fidMode, jmode);
}

jlong getsectors(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidSectors);
}

void setsectors(JNIEnv *env, jobject obj, jlong jsec) {
	(*env)->SetLongField(env, obj, fidSectors, jsec);
}

jint getsectorSize(JNIEnv *env, jobject obj) {
	return (*env)->GetIntField(env, obj, fidSectorSize);
}

/* not needed!
void setsectorSize(JNIEnv *env, jobject obj, jint jsec) {
	(*env)->SetIntField(env, obj, fidSectorSize, jsec);
}
*/

jlong getfilep(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidFilep);
}

void setfilep(JNIEnv *env, jobject obj, jlong jfilep) {
	(*env)->SetLongField(env, obj, fidFilep, jfilep);
}

#ifdef SEQ_OPT
jlong getlastSector(JNIEnv *env, jobject obj) {
	return (*env)->GetLongField(env, obj, fidLastSector);
}

void setlastSector(JNIEnv *env, jobject obj, jlong lsec) {
	(*env)->SetLongField(env, obj, fidLastSector, lsec);
}
#endif

/* reports an error */
void reportError(JNIEnv *env, jobject obj, char *errormsg) {
	jclass errorClass;
	jstring s;
	
	DEBUG_OUTPUT("Enter reportError. Message: %s",errormsg);
	s = (*env)->NewStringUTF(env,errormsg);
	
	errorClass = (*env)->FindClass(env, "xxl/core/io/raw/RawAccessException");
	
	DEBUG_OUTPUT("Errorclass %d",errorClass);
	(*env)->ThrowNew(env, errorClass, errormsg);
}

void init(JNIEnv *env, jobject obj) {
	cls=(*env)->GetObjectClass(env, obj);
	fidSectors=(*env)->GetFieldID(env, cls, "sectors", "J");
	fidSectorSize=(*env)->GetFieldID(env, cls, "sectorSize", "I");
	fidMode=(*env)->GetFieldID(env, cls, "mode", "I");
	fidFilep=(*env)->GetFieldID(env, cls, "filep", "J");
#ifdef SEQ_OPT	
	fidLastSector=(*env)->GetFieldID(env, cls, "lastSector", "J");
#endif
}

JNIEXPORT void JNICALL
JNI_UnLoad (JavaVM * vm, void * reserved) {
	cls = NULL;
}

#endif
