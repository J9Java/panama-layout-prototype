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

/**
 * Instantiate a 2D array layout.
 * The JVM generates a runtime class that implements the layout's stub interface.
 * 
 * @param <T> subclass of Layout, type for array
 */
public interface Array2D<T extends Layout> extends LayoutType {


	/**
	 * Get the length of 1st dimension
	 * @return number of elements in 1st dimension.
	 */
	public long dim1();

	/**
	 * Get the length of 2nd dimension
	 * @return number of elements in 2nd dimension.
	 */
	public long dim2();

	/**
	 * Get an array element
	 * @param i index in 1st dim
	 * @param j index in 2nd dim
	 * @return a layout targeting the array element
	 */
	public abstract T at(long i, long j);

	/**
	 * Set an array element
	 * @param i index in 1st dim
	 * @param j index in 2nd dim
	 * @param value new value of the array element 
	 */
	public abstract void put(long i, long j, T value);

	/**
	 * Get the total size of the array
	 * @return the data size in bytes
	 */
	public long sizeof();
	
	/**
	 * Instantiate a 2D array layout of type T
	 * 
	 * @param <T> subclass of Layout
	 * @param cls The layout class for each array element.
	 * @param dim1 The number of elements in the 1st dimension.
	 * @param dim2 The number of elements in the 2nd dimension
	 * @return an on-heap 2D array layout
	 */
	static public <T extends Layout> Array2D<T> getArray2D(Class<T> cls, long dim1, long dim2) {
		try {
			Class<Array2D<T>> implCls;
			Class<T> elementCls;
			
			LayoutHelper f = LayoutHelper.getFactory();

			elementCls = f.genLayoutImpl(cls);
			Constructor<T> elementCtor = elementCls.getDeclaredConstructor(new Class<?>[] {});
			elementCtor.setAccessible(true);
			long size = elementCtor.newInstance().sizeof();
			
			implCls = f.genArray2DImpl(cls);
			Constructor<Array2D<T>> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class, long.class, long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(dim1, dim2, size);
		} catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
			| IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
}
