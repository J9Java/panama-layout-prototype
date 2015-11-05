/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.Array2D;

/**
 * User defined layout
 */
@Deprecated
public interface MyM3 extends M3 {

	public static void initIdent(Array2D<Int> vv) {
		vv.at(0, 0).value(1);
		vv.at(0, 1).value(0);
		vv.at(1, 0).value(0);
		vv.at(1, 1).value(1);
	}

}
