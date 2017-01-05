/*******************************************************************************
 *  Copyright (c) 2017 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package com.user.examples.vlawithoperation;

import com.ibm.layout.Layout;
import com.ibm.layout.Location;

public class TestVlaWitOps {
	
	public static void main(String[] args) {
		System.out.println("== testVlaWithOperation ==");
		VlaWithOperation vla = Layout.getLayout(VlaWithOperation.class);
		Location loc = new Location(new byte[] {4,1,2,3,2,1,2,3});
		vla.bindLocation(loc);
		
		System.out.println(vla);
	}
}
