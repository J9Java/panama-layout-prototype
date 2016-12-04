/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.gen;

import com.ibm.layout.FloatArray2D;
import com.ibm.layout.LayoutTypeImpl;

/**
 * Generated implementation of FloatArray2D
 */
final class FloatArray2DImpl extends LayoutTypeImpl implements FloatArray2D {
	protected final long dim1;
	protected final long dim2;
	
	
	protected FloatArray2DImpl(long dim1, long dim2) {
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	@Override
	public float at(long i, long j) {
		return UnsafeImplHelper.loadNativeFloat(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 4);
	}

	@Override
	public void put(long i, long j, float val) {
		UnsafeImplHelper.storeNativeFloat(this.location.getData(), this.location.getOffset() + (i * dim2 + j) * 4, val);
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
	public boolean containsVLA() {
		return false;
	}
}
