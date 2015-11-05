/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;


import sun.misc.Unsafe;
/**
 * Location object representing addressable memory
 *
 */
public class Location {
	
	private final byte[] data;
	private final long offset;
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	private static final long arrayBase = unsafe.arrayBaseOffset(byte[].class);
	
	/**
	 * Overlay a location on top of a byte array
	 * 
	 * @param data byte array
	 */
	public Location(byte[] data) {
		this.data = data;
		this.offset = arrayBase;
	}

	/**
	 *  Overlay a location on top of a native address
	 * 
	 * @param addr, a raw pointer to native memory
	 */
	public Location(long addr) {
		this.data = null;
		this.offset = addr;
	}

	/**
	 *  Create a new location from at an offset from an existing location
	 * 
	 * @param loc a raw pointer to native memory
	 * @param offset offset 
	 */
	public Location(Location loc, long offset) {
	   this.data = loc.getData();
	   this.offset = offset + loc.getOffset();
	}

	/**
	 * Return array that stores the data of the layout bound to it in bytes.
	 * @return array that stores the data of the layout bound to it in bytes.
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * Return the offset of the location at which memory is allocated.
	 * @return offset.
	 */
	public long getOffset() {
		return offset;
	}
	
	/**
	 * Check if there is enough space in the location to overlay a layout.
	 * 
	 * @param dataLength of the layout to be overlaid.
	 * @return True if there is enough space, false otherwise.
	 */
	public boolean checkDataFits(long dataLength) {
		if (data == null) {
			return true;
		} else {
			return (dataLength <= data.length);
		}
	}
}
