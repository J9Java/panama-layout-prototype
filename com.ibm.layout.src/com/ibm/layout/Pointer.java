/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

/**
 * Pointer Layout
 */
public interface Pointer<T extends LayoutType> extends PointerType {
	
	public T lValue();
	
	public Pointer<T> atOffset(long offset);
}

