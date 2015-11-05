/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.CharArray1D;
/**
 * User defined layout
 */
public interface MyPrimCharArray1DCharSeq extends CharArray1D, CharSequence {

	@Override
	public default CharSequence subSequence(int start, int end) {
		return (CharSequence)range(start, (end - start));
	}
	
	@Override
	public default char charAt(int index) {
		return (char)at(index);
	}

	@Override
	public default int length() {
		return (int)getLength();
	}
	
}
