/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.BooleanArray1D;
/**
 * User defined layout
 */
public interface MyPrimBooleanArray1DCharSeq extends BooleanArray1D, CharSequence {


	@Override
	public default char charAt(int index) {
		if(at(index)) {
			return 'T';
		} else {
			return 'F';
		}
	}

	@Override
	public default int length() {
		return (int)getLength();
	}

	@Override
	public default CharSequence subSequence(int start, int end) {
		return (CharSequence)this.range(start, (end - start));
	}

}
