/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.ld2j;

class Helper {
	private enum primitiveType {
		JINT, INT, // Integer
		JBOOLEAN, BOOLEAN, // Boolean
		JBYTE, BYTE, //Byte
		JFLOAT, JDOUBLE, FLOAT, DOUBLE, // Float
		JCHAR, CHAR, // Char
		JLONG, LONG, // Long
		JSHORT, SHORT, // Short
		POINTER //Pointer
	};

	/**
	 * Test whether the input is valid.
	 * @param variable_name a string that represents the name of a variable
	 * @param variable_type a string that represents the type of a variable
	 * @return boolean
	 * @throws VerifierException
	 */
	public static boolean testValidation(String variable_name, String variable_type, String structDecl) throws VerifierException {
		if (variable_name.contains("\"") || variable_name.contains(",") || variable_name.contains(":")) {
			throw new VerifierException("Invalid file format: " + variable_name + ":" + variable_type + " in Layout "
					+ structDecl.split(":")[0]);
		}
		for (primitiveType typeIndex : primitiveType.values()) {
			if (typeIndex.name().toLowerCase().equals(variable_type)) {
				return true;
			}
		}
		if (isPointer(variable_type)) {
			return true;
		}//Ignore Pointer class
		if (isNestedStruct(variable_type)) {
			return true;
		}
		throw new VerifierException("Neither a primitive type nor Nested struct: " + variable_name + ":"
				+ variable_type + " in Layout '" + structDecl.split(":")[0] + "'");
	}

	/**
	 * Check whether a type is C primitive type(eg. int/double)
	 * @param variable_type a string that represents the type of a variable
	 * @return <code>true</code> if a type is C primitive type
	 */
	public static boolean isCPrimitiveType(String variable_type) {
		for (primitiveType typeIndex : primitiveType.values()) {
			if (typeIndex.name().toLowerCase().equals("j" + variable_type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether a type is Java primitive type(eg. jint/jdouble)
	 * @param variable_type a string that represents the type of a variable
	 * @return <code>true</code> if a type is J primitive type
	 */
	public static boolean isJPrimitiveType(String variable_type) {
		for (primitiveType typeIndex : primitiveType.values()) {
			if (typeIndex.name().toLowerCase().equals(variable_type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the struct is nested.
	 * @param variable_type a string that represents the type of a variable
	 * @return <code>true</code> if struct is nested
	 */
	public static boolean isNestedStruct(String variable_type) {
		return Struct.AllStructsName.contains("." + variable_type + " ");
	}

	/**
	 * Check if array is empty.
	 * @param array int array
	 */
	public static boolean isArray(int[] array) {
		return (array.length != 0) ? true : false;
	}

	/**
	 * Check if array is 1D.
	 * @param array int array
	 */
	public static boolean is1DArray(int[] array) {
		return (array.length == 1) ? true : false;
	}

	/**
	 * Check if array is 2D.
	 * @param array int array
	 */
	public static boolean is2DArray(int[] array) {
		return (array.length == 2) ? true : false;
	}

	/**
	 * Check if array is 3D or above.
	 * @param str
	 */
	public static boolean isArrayUnsupported(String str) {
		if (str.contains("[")) {
			if (str.split("\\[").length > 3) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the type is supported.
	 * @param str
	 */
	public static boolean isTypeUnsupported(String str) {
		return isCPrimitiveType(str);
	}

	/**
	 * Check if the type is a Pointer
	 * @param str
	 */
	public static boolean isPointer(String str) {
		return str.equals("Pointer");
	}

	/**
	 * Check whether the fields are duplicate.
	 * @param variable_name
	 */
	public static boolean isFieldDup(String variable_name) {
		return Variable.allVariableName.contains(" " + variable_name + " ");
	}

	/**
	 * Tests whether input is an integer or not
	 * @param val String contain value to be tested
	 * @return an <code>boolean</code> that with the result.
	 */
	public static boolean isInteger(String val) {
		for (int i = 0; i < val.length(); i++) {
			if (!Character.isDigit(val.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
