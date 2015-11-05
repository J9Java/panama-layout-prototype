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
 * struct AllPoints {
 *     Point a;
 *     Point3D b;
 *     Point4D c;
 *     Point5D d;
 * }
 * </pre>
 */
@LayoutDesc({"a:Point:8","b:Point3D:12","c:Point4D:16","d:Point5D:20" })
public interface AllPoints extends Layout {

	public abstract Point a();

	public abstract Point3D b();
	
	public abstract Point4D c();
	
	public abstract Point5D d();

}
