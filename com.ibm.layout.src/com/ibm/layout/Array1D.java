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
 * A 1D array layout.
 * @param <T> a layout class.
 */
public interface Array1D<T extends Layout> extends LayoutType {

	
	/**
	 * Instantiate a 1D array layout of type T
	 * The JVM generates a runtime class that implements the layout's stub interface.
	 * @param <T> subclass of Layout, type for array
	 * @param cls The layout class for each array element.
	 * @param length The number of array elements.
	 * @return a 1D array layout
	 */
	static public <T extends Layout> Array1D<T> getArray1D(final Class<T> cls, final long length) {
		Class<Array1D<T>> implCls;
		Class<T> elementCls;
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			// load the element class first (maybe this should be done in the classloader)
			elementCls = f.genLayoutImpl(cls);
			Constructor<T> elementCtor = elementCls.getDeclaredConstructor(new Class<?>[] {});
			elementCtor.setAccessible(true);
			long size = elementCtor.newInstance().sizeof();
			implCls = f.genArray1DImpl(cls);
			Constructor<Array1D<T>> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class, long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(length, size);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Instantiate a user extension of a 1D array layout.
	 * The JVM generates a runtime class that implements the layout's stub interface.
	 * 
	 * @param <E> subclass of Layout
	 * @param <AE> subclass of Array1D
	 * @param userArrayCls A user-extension of an Array1D interface.
	 * @param elementLayout The element type of the array.
	 * @param length The number of array elements.
	 * @return a 1D array layout
	 */
	static public <E extends Layout, AE extends Array1D<E>> AE getUserArray1D(final Class<AE> userArrayCls,
		final Class<E> elementLayout, final long length)
	{
		Class<AE> implCls;
		Class<E> elementCls;
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			// load the element class first (maybe this should be done in the classloader)
			elementCls = f.genLayoutImpl(elementLayout);
			Constructor<E> elementCtor = elementCls.getDeclaredConstructor(new Class<?>[] {});
			elementCtor.setAccessible(true);
			long size = elementCtor.newInstance().sizeof();
			implCls = f.genArray1DImpl(elementLayout, userArrayCls);
			Constructor<AE> ctor = implCls.getDeclaredConstructor(new Class<?>[] { long.class, long.class });
			ctor.setAccessible(true);
			return ctor.newInstance(length, size);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the length of the array.
	 * @return the length of array elements
	 */
	public long getLength();

	/**
	 * Get an array element
	 * @param index the element index
	 * @return a layout targeting the array element
	 */
	public abstract T at(long index);

	/**
	 * Set an array element
	 * @param index index in array
	 * @param value new value of the array element 
	 */
	public abstract void put(long index, T value);


	/**
	 * Extract a sub range of the array
	 * @param startIdx the starting array index
	 * @param length the number of array elements to extract
	 * @return a layout targeting a sub range of the array
	 */
	public abstract Array1D<T> range(long startIdx, long length);

	/**
	 * Get the size of the array
	 * @return the data size in bytes
	 */
	public long sizeof();

	@Override
	public String toString();
}
