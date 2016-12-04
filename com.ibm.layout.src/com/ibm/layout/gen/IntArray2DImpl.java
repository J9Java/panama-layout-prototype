/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.IntArray2D;
import com.ibm.layout.LayoutTypeImpl;

/**
 * Generated implementation of IntArray2D
 */
final class IntArray2DImpl extends LayoutTypeImpl implements IntArray2D {
	protected final long dim1;
	protected final long dim2;

	@Override
	public int at(long i, long j) {
		return UnsafeImplHelper.loadNativeInt(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 4);
	}

	@Override
	public void put(long i, long j, int val) {
		UnsafeImplHelper.storeNativeInt(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 4, val);
	}

	protected IntArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}
	
	public final long dim1() {
		return dim1;
	}
	
	public final long dim2() {
		return dim2;
	}
	
	public long sizeof() {
		return dim1 * dim2 * 4;
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
