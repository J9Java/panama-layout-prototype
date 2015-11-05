/*******************************************************************************
 *  Copyright (c) 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.examples.myline;

import com.ibm.layout.Layout;
import com.ibm.layout.Location;
import com.user.types.MyLine;

public class TestMyLine {

	public static void main(String[] args) {
		System.out.println("== testMyLine ==");
		MyLine line = Layout.getLayout(MyLine.class);
		Location loc = new Location(new byte[(int) (line.sizeof())]);
		line.bindLocation(loc);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
		line.st(10, 11);
		line.en(20, 21);
		System.out.println("line: " + line);
		System.out.println(" length: " + line.length());
	}
}
