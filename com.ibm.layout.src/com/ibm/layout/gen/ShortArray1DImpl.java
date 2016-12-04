/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;
import com.ibm.layout.ShortArray1D;

/**
 * Generated implementation of ShortArray1D
 */
final class ShortArray1DImpl extends LayoutTypeImpl implements ShortArray1D {
	protected final long length;
	
	protected ShortArray1DImpl(long length) {
		this.length = length;
	}
	@Override
	public short at(long index) {
		return UnsafeImplHelper.loadNativeShort(this.location.getData(), this.location.getOffset() + index * 2);
	}

	@Override
	public void put(long index, short value) {
		UnsafeImplHelper.storeNativeShort(this.location.getData(), this.location.getOffset() + index * 2, value);
	}

	@Override
	public ShortArray1D range(long startIdx, long length) {
		ShortArray1DImpl s =  new ShortArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 2);
		s.bindLocation(loc);
		return s;
	}
	
	public long getLength() {
		return length;
	}
	
	public long sizeof() {
		return 2 * length;
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
