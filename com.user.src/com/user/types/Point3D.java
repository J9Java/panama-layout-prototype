/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.LayoutDesc;

/**
 * Generated interface
 * 
 * <pre>
 * struct Point3D extends Point {
 *     jint z;
 * }
 * </pre>
 */
@LayoutDesc({ "z:jint:4" })
public interface Point3D extends Point {

	public abstract int z();

	public abstract void z(int val);
}
