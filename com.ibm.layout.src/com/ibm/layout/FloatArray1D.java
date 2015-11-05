/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

/**
 * 1D array of primitive float.
 */
public interface FloatArray1D extends LayoutType {
	
	/**
	 * Get the number of array elements.
	 * @return the number of array elements
	 */
	public long getLength();

	/**
	 * Get an array element
	 * @param index the element index
	 * @return a layout targeting the array element
	 */
	public abstract float at(long index);

	/**
	 * Set an array element
	 * @param index the element index
	 * @param value new value of the array element
	 */
	public abstract void put(long index, float value);

	/**
	 * Extract a sub range of the array
	 * @param startIdx the starting array index
	 * @param length the number of array elements to extract
	 * @return a layout targeting a sub range of the array
	 */
	public abstract FloatArray1D range(long startIdx, long length);

	/**
	 * Get the size of the array
	 * @return the data size in bytes
	 */
	public long sizeof();
	
	@Override
	public String toString();
}
