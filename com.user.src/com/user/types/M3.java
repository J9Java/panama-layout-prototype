/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.Array2D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;
/**
 * Generated interface
 */
@LayoutDesc({ "m:Int[2][2]:16", "n:Int[2][2]:16", "p:Int[2][2]:16" })
public interface M3 extends Layout {
	
	public abstract Array2D<Int> m();

	public abstract Array2D<Int> n();

	public abstract Array2D<Int> p();
}
