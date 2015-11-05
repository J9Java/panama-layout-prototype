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
 * struct Point4D extends Point {
 *     jint o;
 * }
 * </pre>
 */
@LayoutDesc({ "o:jint:4" })
public interface Point4D extends Point3D {

	public abstract int o();

	public abstract void o(int val);

}
