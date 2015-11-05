/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.IntArray2D;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * Generated interface
 * 
 * <pre>
 * struct IntM3 {
 *     jint m[2][2];
 *     jint n[2][2];
 *     jint p[2][2];
 * }
 * </pre>
 */
@LayoutDesc({ "m:jint[2][2]:16", "n:jint[2][2]:16", "p:jint[2][2]:16" })
public interface IntM3 extends Layout {
	public abstract IntArray2D m();

	public abstract IntArray2D n();

	public abstract IntArray2D p();
}
