/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2005 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

/* 
 * This is the JNI implementation of the direct disc access
 * with Windows NT, 2000 and XP
 * There exists a parallel version for Linux.
 */

/* #define DEBUG */
#undef DEBUG
/* The optimization is only implemented for read-operations so far. */
#undef SEQ_OPT



/* For the java datatypes */
#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
/* The prototypes of the interface */
#include <fcntl.h>
#include "xxl_core_io_raw_NativeRawAccess.h"
#include "raw.h"


JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_open(JNIEnv *env, jobject obj, jstring jfilename) {
	/* int of the file/device */
	int jfilep;
	jlong sectors, length;
	/* BOOL isDevice=FALSE; */
	/* Java needs UTF-coded strings, c needs ASCII */
	const jbyte *filename;
	jint sectorSize;
	
	DEBUG_OUTPUT("Enter open",0);
	init(env, obj);
	
	/* do not call get methods before init! */
	jfilep = (int) getfilep(env, obj);
	sectorSize = getsectorSize(env, obj);
	/* Converts utf to ASCII */
	filename = (const jbyte *) (*env)->GetStringUTFChars(env, jfilename, NULL);
	/* got it? */
	
	if (filename==NULL) {
		reportError(env,obj,"Filename NULL");
		return;
	}
	
	/* Already a device open? */
	if (jfilep!=0) {
		reportError(env,obj,"File already open");
		return;
	}

  	DEBUG_OUTPUT("Filename: %s",filename);
  	
  	/* if ( (filename[0]=='\\') && (filename[1]=='\\') && (filename[2]=='.') && (filename[3]=='\\') )
  		isDevice=TRUE; */
  		
	/* O_BINARY */
	jfilep = open((char*) filename,O_RDWR);
	
	/* Open failed? */
	if (jfilep==-1) {
		reportError(env,obj,"Open failed - file not found");
		return;
	}

	/* Set the int inside the java object */
	setfilep(env, obj, (jlong) jfilep);

  	DEBUG_OUTPUT("Filepointer: %d",(long) jfilep);

	/* does not work with _lseeki64! */
	length = lseek(jfilep,0,SEEK_END);
	if (length==-1) {
		/* output textual system error message */
		/* perror("lseek error"); */ 
		reportError(env,obj,"Size returned 0");
		return;
	}
	else  
		sectors = length/sectorSize;
		
  	DEBUG_OUTPUT("Sektoren: %d\n", (long) sectors);
	setsectors(env, obj, (jlong) sectors);

	/* Mode always says that hard drive caches are turned on. */
	setMode(env, obj, 20 | getMode(env, obj));

}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_close (JNIEnv *env, jobject obj){
	jlong jfilep = (jlong) getfilep(env, obj);
	if (close(jfilep)==-1) {
		reportError(env,obj,"Close failed");
		return;
	}
	setfilep(env, obj, 0);
}

JNIEXPORT void JNICALL 
Java_xxl_core_io_raw_NativeRawAccess_write (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	int jfilep = (int) getfilep(env, obj);
	/* length of delivered java byte array */
	jlong len = (*env)->GetArrayLength(env, jblock);
	jint sectorSize = getsectorSize(env, obj);
	/* Convert it to a c array our sectorblock */
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0);
	/* Position for writing */
	jlong fpos;
	
	DEBUG_OUTPUT ("write: sector=%d", (long) sector);

	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not hava sector size");
               	return;
	}
	
	if ((sector<0) || (sector >= getsectors(env, obj))) {
		reportError(env,obj,"filepointer outside area");
               	return;
	}

	/* Position for writing */
	fpos = (jlong) sector * sectorSize;
 
	/* Set position, use lseek64 instead? */
	if (lseek(jfilep,fpos,SEEK_SET)==-1) {
		reportError(env,obj,"filepointer could not be set");
		return;
	}

	/* Write the block down to the device */
	if (write(jfilep, block, sectorSize)!=sectorSize) {
		reportError(env,obj,"error writing block");
               	return;
	}
	
	/* force writing to disc */
	if (fsync(jfilep)) {
		reportError(env,obj,"error flushing buffers");
               	return;
	}
	
	(*env)->ReleaseByteArrayElements(env, jblock, block, 0);
}

JNIEXPORT void JNICALL
Java_xxl_core_io_raw_NativeRawAccess_read (JNIEnv *env, jobject obj, jbyteArray jblock, jlong sector) {
	int jfilep = (int) getfilep(env, obj);
	/* The position to read on the device */
	jlong fpos;
	jint sectorSize = getsectorSize(env, obj);
	jlong len = (*env)->GetArrayLength(env, jblock); 
	jbyte *block = (*env)->GetByteArrayElements(env, jblock, 0); 
#ifdef SEQ_OPT	
	jlong lastSector = getlastSector(env, obj);
#endif

	DEBUG_OUTPUT("read: sector==%d", (long) sector);
	
	if (jfilep==0) {
		reportError(env,obj,"file not open");
               	return;
	}

	/* Is it exactly one block */
	if (len!=sectorSize) {
		reportError(env,obj,"byte array does not have sector size");
               	return;
	}

#ifdef SEQ_OPT	
	if (sector != lastSector+1) {
		/* non sequential access! */
#endif
		fpos = (jlong) sector * sectorSize;
		if (lseek(jfilep,fpos,SEEK_SET)==-1) {
			reportError(env,obj,"filepointer could not be set");
        	       	return;
        	}
#ifdef SEQ_OPT	
	}
#endif

	DEBUG_OUTPUT("read the block",0);
	/* Read the block */
	if (read(jfilep, block, sectorSize)!=sectorSize) {
		reportError(env,obj,"read failed");
               	return;
	}
	
	/* Convert the c block array to a java byte array */
	(*env)->SetByteArrayRegion(env, jblock, 0, sectorSize, block);
#ifdef SEQ_OPT	
	setlastSector(env, obj, sector);
#endif
	(*env)->ReleaseByteArrayElements(env, jblock, block, 0);
}


JNIEXPORT void JNICALL
Java_xxl_core_io_raw_NativeRawAccess_setHardDriveCacheMode (JNIEnv *env, jobject obj, jint newCacheMode) {
}
