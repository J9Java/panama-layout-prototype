/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.LongArray2D;

/**
 * Generated implementation of LongArray2D
 */
final class LongArray2DImpl extends LayoutTypeImpl implements LongArray2D {	
	protected final long dim1;
	protected final long dim2;

	protected LongArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public long at(long i, long j) {
		return UnsafeImplHelper.loadNativeLong(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 8);
	}

	@Override
	public void put(long i, long j, long val) {
		UnsafeImplHelper.storeNativeLong(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 8, val);
	}
	
	public final long dim1() {
		return dim1;
	}
	
	public final long dim2() {
		return dim2;
	}
	
	public long sizeof() {
		return dim1 * dim2 * 8;
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
