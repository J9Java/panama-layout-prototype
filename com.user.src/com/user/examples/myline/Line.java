/*******************************************************************************
 *  Copyright (c) 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.examples.myline;

import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/**
 * Generated interface class
 */
@LayoutDesc({"start:Point:8","end:Point:8"})
public interface Line extends Layout {
	public long sizeof();

	public abstract Point start();

	public abstract Point end();

	@Override
	public String toString();

}
