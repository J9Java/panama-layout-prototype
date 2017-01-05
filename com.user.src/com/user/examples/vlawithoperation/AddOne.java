/*******************************************************************************
 *  Copyright (c) 2017 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package com.user.examples.vlawithoperation;

import com.ibm.layout.ComplexVLArray;
import com.ibm.layout.Layout;

public interface AddOne <T extends Layout> extends ComplexVLArray<T> {
	default long arrayLength(long value) {
		return (value + 1);
	}
}
