/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
#include "jni.h"
#include "com_ibm_layout_UnsafeHelper.h"

jobject JNICALL
Java_com_ibm_layout_UnsafeHelper_bufferFromAddressImpl(JNIEnv *env, jclass receiver, jlong addr, jlong size)
{
	/* If an error occurs here, an exception will be set */
	return (*env)->NewDirectByteBuffer(env, (void *)addr, size);
}

jlong JNICALL
Java_com_ibm_layout_UnsafeHelper_getDBBAddress(JNIEnv *env, jclass receiver, jobject buffer)
{
	/* Get the data pointer */
	return (jlong) (*env)->GetDirectBufferAddress(env, buffer);
}

jlong JNICALL
Java_com_ibm_layout_UnsafeHelper_getDBBLength(JNIEnv *env, jclass receiver, jobject buffer)
{
	/* Get the data pointer */
	return (*env)->GetDirectBufferCapacity(env, buffer);
}
