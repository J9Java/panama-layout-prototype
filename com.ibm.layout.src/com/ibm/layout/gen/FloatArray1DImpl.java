/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.FloatArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;

import sun.misc.Unsafe;

/**
 * Generated implementation of FloatArray1D
 */
final class FloatArray1DImpl extends LayoutTypeImpl implements FloatArray1D {
	private static final Unsafe unsafe = null;
	
	protected final long length;

	protected FloatArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public float at(long index) {
		return unsafe.getFloat(this.location.getData(), this.location.getOffset() + index * 4);
	}

	@Override
	public void put(long index, float value) {
		unsafe.putFloat(this.location.getData(), this.location.getOffset() + index * 4, value);
	}

	@Override
	public FloatArray1D range(long startIdx, long length) {
		FloatArray1DImpl d = new FloatArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 4);
		d.bindLocation(loc);
		return d;
	}
	
	public final long getLength() {
		return length;
	}
	
	public long sizeof() {
		return 4 * length;
	}
	
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
