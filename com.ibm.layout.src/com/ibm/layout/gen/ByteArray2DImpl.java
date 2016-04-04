/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.ByteArray2D;
import com.ibm.layout.LayoutTypeImpl;

import sun.misc.Unsafe;

/**
 * Generated implementation of ByteArray2D
 */
final class ByteArray2DImpl extends LayoutTypeImpl implements ByteArray2D {
	private static final Unsafe unsafe = null;
	
	protected final long dim1;
	protected final long dim2;

	protected ByteArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}


	@Override
	public byte at(long i, long j) {
		return unsafe.getByte(this.location.getData(), this.location.getOffset() + (i * dim2 + j));
	}

	@Override
	public void put(long i, long j, byte val) {
		unsafe.putByte(this.location.getData(), this.location.getOffset() + (i * dim2 + j), val);
	}

	@Override
	public final long dim1() {
		return dim1;
	}
	
	@Override
	public final long dim2() {
		return dim2;
	}
	
	@Override
	public long sizeof() {
		return dim1 * dim2;
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
	
	@Override
	public boolean containsVLA() {
		return false;
	}
}
