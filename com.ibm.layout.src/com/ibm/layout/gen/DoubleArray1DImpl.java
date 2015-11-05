/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.DoubleArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;
import com.ibm.layout.UnsafeHelper;

import sun.misc.Unsafe;

/**
 * Generated implementation of DoubleArray1D
 */
final class DoubleArray1DImpl extends LayoutTypeImpl implements DoubleArray1D {
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	protected final long length;

	protected DoubleArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public double at(long index) {
		return unsafe.getDouble(this.location.getData(), this.location.getOffset() + index * 8);
	}

	@Override
	public void put(long index, double value) {
		unsafe.putDouble(this.location.getData(), this.location.getOffset() + index * 8, value);
	}

	@Override
	public DoubleArray1D range(long startIdx, long length) {
		DoubleArray1DImpl d = new DoubleArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 8);
		d.bindLocation(loc);
		return d;
	}
	
	public final long getLength() {
		return length;
	}
	
	public long sizeof() {
		return 8 * length;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		for (long i = 0; i < length; i++) {
			sb.append(" " + at(i));
		}
		sb.append(" ]");
		return sb.toString();
	}

}
