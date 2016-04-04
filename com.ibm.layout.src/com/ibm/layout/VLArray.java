/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

public interface VLArray<T extends Layout> extends LayoutType {
	
	/**
	 * Get the number of elements in VLA.
	 * @return the number of array elements
	 */
	public long getVLALength();

	/**
	 * Get an array element
	 * @param index the element index
	 * @return a layout targeting the array element
	 */
	public abstract T at(long index);

	/**
	 * Set an array element
	 * @param index the element index
	 * @param value new value of the array element
	 */
	public abstract void put(long index, T value);
	
	/**
	 * Get the size of the array
	 * @return the data size in bytes
	 */
	public long sizeof();

	@Override
	public String toString();
}
