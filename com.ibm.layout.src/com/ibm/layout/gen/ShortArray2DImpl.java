/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.LayoutTypeImpl;
import com.ibm.layout.ShortArray2D;

/**
 * Generated implementation of ShortArray2D
 */
final class ShortArray2DImpl extends LayoutTypeImpl implements ShortArray2D {
	protected final long dim1;
	protected final long dim2;
	
	protected ShortArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public short at(long i, long j) {
		return UnsafeImplHelper.loadNativeShort(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 2);
	}

	@Override
	public void put(long i, long j, short val) {
		UnsafeImplHelper.storeNativeShort(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 2, val);
	}
	
	public long dim1() {
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

	@Override
	public boolean containsVLA() {
		return false;
	}
}
