/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package com.user.examples.variablelengtharray;

import java.lang.reflect.Field;

import com.ibm.layout.Layout;
import com.ibm.layout.Location;
import com.user.types.Int;

import sun.misc.Unsafe;

public class TestVariableLengthArray {
	static Unsafe unsafe = null;
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe)field.get(null);
		} catch (Exception e) {
			System.out.println("Could not get unsafe\n Test could not run.");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		System.out.println("== testVariableLengthArray ==");
		
		//create variable length array with repeatCountInitializer
		VariableLengthArray variableLengthArray = Layout.getLayout(VariableLengthArray.class);
		//sizeof(jint) + sizeof(ArrayElement)*10
		ArrayElement element = Layout.getLayout(ArrayElement.class);
		long ptr = unsafe.allocateMemory(4 + element.sizeof() * 10);
		variableLengthArray.bindLocation(new Location(ptr), 10);

		System.out.println("lengthOfArray field is " + variableLengthArray.lengthOfArray());
		System.out.println("length of VLArray is " + variableLengthArray.elements().getVLALength());

		//overlay array on existing memory
		VariableLengthArray variableLengthArray2 = Layout.getLayout(VariableLengthArray.class);
		//sizeof(jint) + sizeof(ArrayElement)*5
		long ptr2 = unsafe.allocateMemory(4 + element.sizeof() * 5);
		Location loc = new Location(ptr2);
		//set lengthOfArray to 5
		Int lengthOfArray = Layout.getLayout(Int.class);
		lengthOfArray.bindLocation(loc);
		lengthOfArray.value(5);
		//bind to location
		variableLengthArray2.bindLocation(loc);
		
		System.out.println("lengthOfArray field is " + variableLengthArray2.lengthOfArray());
		System.out.println("length of VLArray is " + variableLengthArray2.elements().getVLALength());
	}
	
}
