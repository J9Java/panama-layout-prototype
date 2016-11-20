/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.BooleanArray2D;
import com.ibm.layout.LayoutTypeImpl;

/**
 * Generated implementation of BooleanArray2D
 */
final class BooleanArray2DImpl extends LayoutTypeImpl implements BooleanArray2D {
	protected final long dim1;
	protected final long dim2;

	public BooleanArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public boolean at(long i, long j) {
		return UnsafeImplHelper.loadNativeBoolean(this.location.getData(), this.location.getOffset() + (i * dim2 + j));
	}

	@Override
	public void put(long i, long j, boolean val) {
		UnsafeImplHelper.storeNativeBoolean(this.location.getData(), this.location.getOffset() + (i * dim2 + j), val);
	}
	
	@Override
	public final long dim1() {
		return dim1;
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
	public long sizeof() {
		return dim1 * dim2;
	}
	
	@Override
	public final long dim2() {
		return dim2;
	}
	
	@Override
	public boolean containsVLA() {
		return false;
	}
}
