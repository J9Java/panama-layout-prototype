/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.CharArray2D;
import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.UnsafeHelper;

import sun.misc.Unsafe;

/**
 * Generated implementation of CharArray2D
 */
final class CharArray2DImpl extends LayoutTypeImpl implements CharArray2D {
	private static final Unsafe unsafe = UnsafeHelper.getUnsafe();
	protected final long dim1;
	protected final long dim2;

	protected CharArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public char at(long i, long j) {
		return unsafe.getChar(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 2);
	}

	@Override
	public void put(long i, long j, char val) {
		unsafe.putChar(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 2, val);
	}
	
	public final long dim1() {
		return dim1;
	}
	
	public final long dim2() {
		return dim2;
	}
	
	public long sizeof() {
		return dim1 * dim2 * 2;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		for (long i = 0; i < dim1; i++) {
			for (long j = 0; j < dim2; j++) {
				sb.append(" " + at(i, j));
			}
		}
		sb.append(" ]");
		return sb.toString();
	}
}
