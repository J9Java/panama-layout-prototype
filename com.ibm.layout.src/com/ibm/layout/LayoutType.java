/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * Superclass of all layouts.
 * 
 * <p>
 * Types of layouts are:<br>
 * <p>
 * Types of layouts are:<br>
 * - singleton<br>
 * - (1|2)-dim primitive array<br>
 * - (1|2)-dim layout array<br>
 * </p>
 */
public interface LayoutType {
	
	public abstract void bindLocation(Location loc);
	
	/**
	 * sizeof an instance
	 * @return size of an instance, in bytes
	 */
	public abstract long sizeof();
	
	
	/**
	 * Copy the receiver's data into a byte array.
	 * 
	 * @param srcOffset The offset in the data where we should start reading. e.g. 0 means we should start
	 *            at the first field.
	 * @param buf The byte array buffer where the data should be written.
	 * @param offset The offset in buf where we should start writing.
	 * @param length The maximum number of bytes to write. The actual number of bytes written is min(length,
	 *            this.getByteSize()).
	 * @return The number of bytes written to buf, or -1 if srcOffset greater than or equal the receiver's data size.
	 */
	public abstract int writeToByteArray(long srcOffset, byte[] buf, int offset, int length);

	/**
	 * Copy from a byte array into the receiver's data.
	 * 
	 * @param dstOffset The offset in the data where we should start writing. e.g. 0 means we should start
	 *            at the first field.
	 * @param buf The byte array buffer to read the data from.
	 * @param offset The offset in buf where we should start reading.
	 * @param length The maximum number of bytes to read. The actual number of bytes read is min(length,
	 *            this.getByteSize()).
	 * @return The number of bytes read from buf and written into the receiver, or -1 if dstOffset is greater than or equal 
	 * the receiver's data size.
	 */
	public abstract int readFromByteArray(long dstOffset, byte[] buf, int offset, int length);

	/**
	 * Get a direct byte buffer to access this layout.
	 * This layout must target off-heap memory.
	 * @return a direct byte buffer
	 */
	public abstract ByteBuffer asByteBuffer();

	/**
	 * Cast the receiver as a different layout.
	 * 
	 * @param <T> subclass of Layout
	 * @param cls the cast layout class
	 * @return a non-array layout
	 */
	public <T extends Layout> T unsafeCast(Class<T> cls);
	
	/**
	 * Cast the receiver as a different array layout
	 * 
	 * @param <T> subclass of Layout
	 * @param cls the cast layout element class
	 * @param length the number of array elements
	 * @return an array layout
	 */
	public <T extends Layout> Array1D<T> unsafeCast(Class<T> cls, long length);
	
	/**
	 * Create a 1D array instance of java primitive type represented by user defined class
	 * 
	 * @param <AE> subclass of LayoutType
	 * @param userArrayCls, the user defined class
	 * @param primCls The layout class for each array element.
	 * @param length The number of array elements.
	 * @return an instance of a 1D primitive array
	 */
	public static <AE extends LayoutType> AE getPrimUserArray1D(final Class<AE> userArrayCls, final Class<?> primCls,
			final long length)
	{
		Class<AE> implCls;
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			implCls = f.genPrimUserArray1DImpl(primCls, userArrayCls);
			Constructor<AE> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(length);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
		
	/**
	 * Create a 1D array instance of java primitive type 
	 * 
	 * @param <T> subclass of Layout
	 * @param primCls The layout class for each array element.
	 * @param length The number of array elements.
	 * @return an instance of a 1D primitive array
	 */
	static public <T extends LayoutType> T getPrimArray1D(Class<?> primCls, final long length) {
		final String implClsName = LayoutHelper.getPrimArray1DName(primCls);
		Class<T> implCls;
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			implCls = f.genPrimArrayImpl(implClsName);
			Constructor<T> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(length);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create a 2D array instance of java primitive type 
	 * 
	 * @param <T> subclass of Layout
	 * @param primCls The layout class for each array element.
	 * @param dim1 The number of elements in the 1st dimension.
	 * @param dim2 The number of elements in the 2nd dimension
	 * @return an instance of a 1D primitive array
	 */
	static public <T extends LayoutType> T getPrimArray2D(Class<?> primCls, final long dim1, final long dim2) {
		final String implClsName = LayoutHelper.getPrimArray2DName(primCls);
		Class<T> implCls;
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			implCls = f.genPrimArrayImpl(implClsName);
			Constructor<T> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class, long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(dim1, dim2);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
