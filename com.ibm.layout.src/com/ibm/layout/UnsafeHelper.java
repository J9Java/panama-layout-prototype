/*******************************************************************************
 *  Copyright (c) 2015, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sun.misc.Unsafe;
/**
 * Encapsulates all unsafe operations
 *
 */
final class UnsafeHelper {
	static Unsafe unsafe;
	static private boolean isNativeLibLoaded = false;
	static final String libraryNotFoundMessage = "The layouts library was not found in the java.library.path.\n" 
												 + "To use asByteBuffer(...) or fromByteBuffer(...) run your with"
												 + "-Djava.library.path=[workspaceDir]/com.ibm.layoutS/jni_src";	
	
	/**
	 * get theUnsafe field from sun/misc/Unsafe
	 * 
	 * @return unsafe
	 */
	static public Unsafe getUnsafe() {
		if (null == unsafe) {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				unsafe = (Unsafe)field.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return unsafe;
	}

	private static void loadLayoutLib() {
		if (!isNativeLibLoaded) {
			try {
				System.loadLibrary("layouts");
				isNativeLibLoaded = true;
			} catch (UnsatisfiedLinkError e) {
				throw new RuntimeException(libraryNotFoundMessage);
			}
		}
	}

	/**
	 * Create a driect bytebuffer at specified address with specified size
	 * @param addr, address of DBB 
	 * @param size, size of DBB in bytes
	 * @return bytebuffer
	 */
	public static ByteBuffer bufferFromAddress(long addr, long size) {
		loadLayoutLib();
		return bufferFromAddressImpl(addr, size);
	}

	/**
	 * Return the native address of a direct bytebuffer
	 * @param buffer bytebuffer
	 * @return address
	 */
	public static long getDirectByteBufferAddress(ByteBuffer buffer) {
		loadLayoutLib();
		return getDBBAddress(buffer);
	}

	/**
	 * Return the length of a direct bytebuffer
	 * 
	 * @param buffer bytebuffer
	 * @return length
	 */
	public static long getDirectByteBufferLength(ByteBuffer buffer) {
		loadLayoutLib();
		return getDBBLength(buffer);
	}

	private static native ByteBuffer bufferFromAddressImpl(long addr, long size);

	private static native long getDBBAddress(ByteBuffer buffer);

	private static native long getDBBLength(ByteBuffer buffer);

	/**
	 * Overly a directByteBuffer of a specified at an address specified by the location object
	 * @param loc location object
	 * @param sizeof size of bytebuffer
	 * @return a direct bytebuffer
	 */
	public static ByteBuffer asByteBuffer(Location loc, long sizeof) {
		if (loc.getData() != null) {
			throw new UnsupportedOperationException("not off-heap");
		}
		ByteBuffer buffer = bufferFromAddress(loc.getOffset(), sizeof);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}
}
