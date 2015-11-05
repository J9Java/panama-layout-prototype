/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;
import com.ibm.layout.LongArray1D;
import com.ibm.layout.UnsafeHelper;

import sun.misc.Unsafe;

/**
 * Generated implementation of LongArray1D
 */
final class LongArray1DImpl extends LayoutTypeImpl implements LongArray1D {
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	protected final long length;

	protected LongArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public long at(long index) {
		return unsafe.getLong(this.location.getData(), this.location.getOffset() + index * 8);
	}

	@Override
	public void put(long index, long value) {
		unsafe.putLong(this.location.getData(), this.location.getOffset() + index * 8, value);
	}

	@Override
	public LongArray1D range(long startIdx, long length) {
		LongArray1DImpl l = new LongArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 8);
		l.bindLocation(loc);
		return l;
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
