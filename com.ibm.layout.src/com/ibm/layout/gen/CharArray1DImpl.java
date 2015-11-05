/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.CharArray1D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.Location;
import com.ibm.layout.UnsafeHelper;

import sun.misc.Unsafe;

/**
 * Generated implementation of CharArray1D
 */
final class CharArray1DImpl extends LayoutTypeImpl implements CharArray1D {
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	protected final long length;
	
	protected CharArray1DImpl(long length) {
		this.length = length;
	}

	@Override
	public char at(long index) {
		return unsafe.getChar(this.location.getData(), this.location.getOffset() + index * 2);
	}

	@Override
	public void put(long index, char value) {
		unsafe.putChar(this.location.getData(), this.location.getOffset() + index * 2, value);
	}

	@Override
	public CharArray1D range(long startIdx, long length) {
		CharArray1DImpl c = new CharArray1DImpl(length);
		Location loc = new Location(this.location, startIdx * 2);
		c.bindLocation(loc);
		return c;
	}
	
	public final long getLength() {
		return length;
	}
	
	public long sizeof() {
		return 2 * length;
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
}
