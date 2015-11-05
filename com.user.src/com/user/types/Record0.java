/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.Array1D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * Generated interface
 * 
 * Equivalent C structure:
 * <pre>
 * struct Record0 {
 * 		jint col0;
 * 		jbyte col1[23];
 * 		jbyte col2;
 * 		jbyte col3[23];
 * }
 * </pre>
 * 
 * <p>Note this class uses the non-primitive Array1D type only for demonstration purposes.
 */
@LayoutDesc({ "col0:jint:4", "col1:Byte[23]:23", "col2:jbyte:1", "col3:Byte[23]:23" })
public interface Record0 extends Layout {

	public abstract int col0();

	public abstract Array1D<Byte> col1();

	public abstract byte col2();

	public abstract Array1D<Byte> col3();

	public abstract void col0(int val);

	public abstract void col2(byte val);

}
