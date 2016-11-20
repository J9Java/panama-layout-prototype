/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.IntArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;

/**
 * Generated implementation of IntArray1D
 */
final class IntArray1DImpl extends LayoutTypeImpl implements IntArray1D {
	protected final long length;

	protected IntArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public int at(long index) {
		return UnsafeImplHelper.loadNativeInt(this.location.getData(), this.location.getOffset() + index * 4);
	}

	@Override
	public void put(long index, int value) {
		UnsafeImplHelper.storeNativeInt(this.location.getData(), this.location.getOffset() + index * 4, value);
	}

	@Override
	public IntArray1D range(long startIdx, long length) {
		IntArray1DImpl i =  new IntArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 4);
		i.bindLocation(loc);
		return i;
	}
	
	public final long getLength() {
		return length;
	}

	public long sizeof() {
		return 4 * length;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		for (long i = 0; i < length; i++) {
			sb.append(" " + at(i));
		}
		sb.append(" ]");
		return sb.toString();
	}

	@Override
	public boolean containsVLA() {
		return false;
	}
}
