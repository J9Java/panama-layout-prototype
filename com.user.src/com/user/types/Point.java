/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * Generated interface
 * 
 * <pre>
 * struct Point {
 *     jint x;
 *     jint y;
 * }
 * </pre>
 */
@LayoutDesc({"x:jint:4","y:jint:4"})
public interface Point extends Layout {
	/**
	 * Get the size of this layout
	 * @return size of this layout, in bytes
	 */
	public long sizeof();

	public abstract int x();

	public abstract int y();

	public abstract void x(int val);

	public abstract void y(int val);

	@Override
	public String toString(); /*{
		return "(x: " + x() + ", y: " + y() + ")";
	}*/

}
