/*******************************************************************************
 *  Copyright (c) 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import sun.misc.Unsafe;

/**
 * Implements LayoutType
 *
 */
public abstract class LayoutTypeImpl implements LayoutType {
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	protected Location location;
	
	/**
	 * Bind this layout to the location object
	 * 
	 * @param loc, location object
	 */
	public void bindLocation(Location loc) {
		if(true == loc.checkDataFits(this.sizeof())) {
			location = loc;
		}
	}
	
	/**
	 * Copy the receiver's data into a byte array.
	 * 
	 * @param srcOffset The offset in the data where we should start reading. e.g. 0 means we should start
	 *            at the first field.
	 * @param buf The byte array buffer where the data should be written.
	 * @param offset The offset in buf where we should start writing.
	 * @param length The maximum number of bytes to write. The actual number of bytes written is min(length,
	 *            this.getByteSize()).
	 * @return The number of bytes written to buf, or -1 if srcOffset greater than or equl to the receiver's data size.
	 */
	public final int writeToByteArray(long srcOffset, byte[] buf, int offset, int length) {
		if (length > sizeof()) {
			length = (int)sizeof();
		}
		if (location.getData() != null) {
			System.arraycopy(location.getData(), (int)srcOffset, buf, offset, length);
		} else {
			unsafe.copyMemory(null, location.getOffset() + srcOffset, buf, unsafe.arrayBaseOffset(buf.getClass()), length);
		}
		return length;
	}
	
	/**
	 * Copy from a byte array into the receiver's data.
	 * 
	 * @param dstOffset The offset in the data where we should start writing. e.g. 0 means we should start
	 *            at the first field.
	 * @param buf The byte array buffer to read the data from.
	 * @param offset The offset in buf where we should start reading.
	 * @param length The maximum number of bytes to read. The actual number of bytes read is min(length,
	 *            this.getByteSize()).
	 * @return The number of bytes read from buf and written into the receiver, or -1 if dstOffset greater than or equal to 
	 * the receiver's data size.
	 */
	public final int readFromByteArray(long dstOffset, byte[] buf, int offset, int length) {
		if (length > sizeof()) {
			length = (int)sizeof();
		}
		if (location.getData() != null) {
			System.arraycopy(buf, offset, location.getData(), (int)dstOffset, length);
		} else {
			unsafe.copyMemory(buf, unsafe.arrayBaseOffset(buf.getClass()) + offset, null, this.location.getOffset() + dstOffset, length);
		}
		return length;
	}
	
	/**
	 * Get a direct byte buffer to access this layout.
	 * This layout must target off-heap memory.
	 * @return a direct byte buffer
	 */
	public final ByteBuffer asByteBuffer() {
		if (this.location.getData() != null) {
			throw new UnsupportedOperationException("not off-heap");
		}
		ByteBuffer buffer = UnsafeHelper.bufferFromAddress(this.location.getOffset(), this.sizeof());
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}
		
	/**
	 * Cast the receiver as a different layout.
	 * @param cls the cast layout class
	 * @return a non-array layout
	 */
	public final <T extends Layout> T unsafeCast(Class<T> cls) {
		try {
			final Class<T> implCls = LayoutHelper.getFactory().genLayoutImpl(cls);
			Constructor<T> ctor = implCls.getDeclaredConstructor();
			ctor.setAccessible(true);
		    T newInst = ctor.newInstance();
		    newInst.bindLocation(this.location);
			return newInst;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Cast the receiver as a different array layout
	 * @param cls the cast layout element class
	 * @param length the number of array elements
	 * @return an array layout
	 */
	public final <T extends Layout> Array1D<T> unsafeCast(Class<T> cls, long length) {
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			
			Class<T> elementCls = f.genLayoutImpl(cls);
			Constructor<T> elementCtor = elementCls.getDeclaredConstructor(new Class<?>[] {});
			elementCtor.setAccessible(true);
			long size = elementCtor.newInstance().sizeof();
			
			Class<Array1D<T>> implCls = f.genArray1DImpl(cls);
			Constructor<Array1D<T>> ctor = implCls.getDeclaredConstructor(new Class<?>[] {
					long.class, long.class });
			ctor.setAccessible(true);
			Array1D<T> newInst = ctor.newInstance(length, size);
			newInst.bindLocation(this.location);
			return newInst;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	@Override
	public abstract long sizeof();	

}
