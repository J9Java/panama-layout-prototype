/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.Array1D;
import com.ibm.layout.Array2D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * Generated interface
 */
@LayoutDesc({ "array1:Byte[10]:10", "array2:Int[5][5]:100", "array3:Double[3]:24", "array4:Byte[2][3]:6" })
public interface ArrayCases extends Layout {

	public abstract Array1D<Byte> array1();

	public abstract Array2D<Int> array2();

	public abstract Array1D<Double> array3();

	public abstract Array2D<Byte> array4();
}
