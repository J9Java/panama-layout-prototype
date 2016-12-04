/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.DoubleArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;

/**
 * Generated implementation of DoubleArray1D
 */
final class DoubleArray1DImpl extends LayoutTypeImpl implements DoubleArray1D {
	protected final long length;

	protected DoubleArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public double at(long index) {
		return UnsafeImplHelper.loadNativeDouble(this.location.getData(), this.location.getOffset() + index * 8);
	}

	@Override
	public void put(long index, double value) {
		UnsafeImplHelper.storeNativeDouble(this.location.getData(), this.location.getOffset() + index * 8, value);
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

	@Override
	public boolean containsVLA() {
		return false;
	}
}
