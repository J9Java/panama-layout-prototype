/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.ByteArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;

/**
 * Generated implementation of ByteArray1D
 */
final class ByteArray1DImpl extends LayoutTypeImpl implements ByteArray1D {	
	protected final long length;
	
	public ByteArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public byte at(long index) {
		return UnsafeImplHelper.loadNativeByte(this.location.getData(), this.location.getOffset() + index);
	}

	@Override
	public void put(long index, byte value) {
		UnsafeImplHelper.storeNativeByte(this.location.getData(), this.location.getOffset() + index, value);
	}

	@Override
	public final ByteArray1D range(long startIdx, long length) {
		ByteArray1DImpl b = new ByteArray1DImpl(length);
		Location loc = new Location(this.location, startIdx);
		b.bindLocation(loc);
		return b;
	}

	@Override
	public final long getLength() {
		return length;
	}

	@Override
	public final long sizeof() {
		return length;
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
