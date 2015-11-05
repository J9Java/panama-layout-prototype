/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * 32-Bits Pointer Layout
 */
@LayoutDesc({ "value:jint:4" })
public interface Pointer extends Layout {
	public abstract int value();

	public abstract void value(int val);

	@Override
	public String toString();
	
}
