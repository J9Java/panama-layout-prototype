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
 * struct Line {
 *     struct Point st;  // "nested field"
 *     struct Point en;
 * }
 * </pre>
 * 
 * Note no setters for nested fields.
 */
@LayoutDesc({ "st:Point:8", "en:Point:8" })
public interface Line extends Layout {
	/**
	 * Get the size of this layout
	 * @return size of this layout, in bytes
	 */
	public abstract Point st();

	public abstract Point en();
}
