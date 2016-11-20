/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.ld2j;

import java.util.ArrayList;
import java.util.Iterator;

class Variable {
	private String name = null;
	private String type = null;
	private int[] arraySizes;
	private int size = 0;
	public static int numOfPointer = 0;
	public static String allVariableName = " ";
	public static long totalSize = 0;
	public boolean isPrimArray = false;
	public String repeatCountField = null;
	private String userClass = null;
	public String primitivePointer = null;

	/**
	 * Constructor.
	 * @param name a string that represents the name of a variable.
	 * @param type a string that represents the type of a variable.
	 * @param arraySizes an <code>int[]</code> that represents the size of a variable.
	 * @param isPrimArray <code>true</code> if the variable is a primitive array.
	 */
	private Variable(String name, String type, int[] arraySizes, boolean isPrimArray, int size, String repeatCountField,String userClass) {
		this.name = name;
		this.type = type;
		this.arraySizes = arraySizes;
		this.isPrimArray = isPrimArray;
		this.size = size;
		this.userClass = userClass;
		this.repeatCountField = repeatCountField;
		Variable.allVariableName += (name + " ");
	}

	/**
	 * Getter for field type.
	 * @return a <code>String</code> that returns variable type.
	 */
	public String getType() {
		return type;
	}

	String getUserClass(){
		return userClass;
	}
	
	/**
	 * Getter for repeat count field.
	 * @return a <code>String</code> that returns the name of the repeat count field for this VLA.
	 */
	public String getRepeatCountField() {
		return this.repeatCountField;
	}
	
	/**
	 * Getter for field name.
	 * @return a <code>String</code> that returns variable name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for field size.
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * Getter for array field arraySize.
	 * @return an <code>int[]</code> that represents the size of a variable.
	 */
	public int[] getArraySize() {
		return arraySizes;
	}

	/**
	 * Parse a variable from a string.
	 * eg. "A",["x:jint:4"],"y:jbyte:1".
	 * @return <code>Variable</code>
	 * @throws VerifierException 
	 */
	private static Variable createVariableFromString(String line) throws VerifierException {
		boolean isPrim = false;
		int size = 0;
		String[] variable = line.split(":");
		String variable_type = variable[1];
		String variable_name = variable[0];
		String repeatCountField = null;
		String userClass = null;
		
		ArrayList<Integer> sizes = new ArrayList<Integer>();

		//Treat unsigned type as signed type
		if (variable_type.contains("unsigned") || variable_type.contains("signed")) {
			variable_type = variable_type.split(" ")[1];
		}

		if (Helper.isArrayUnsupported(variable_type)) {
			System.out.println("Does not suuport 3D array or higher dimension in the following Layout declaration: " + line);
			return null;
		}

		if (Helper.isFieldDup(variable_name)) {
			System.out.println("Two or more variables have the same name! in the following Layout declaration: " + line);
			return null;
		}

		if (Helper.isPointer(variable_type)) {
			size = System.getProperty("sun.arch.data.model").equals("32") ? 4 : 8;
			numOfPointer++;
		} else {
			size = Integer.parseInt(variable[2]);
			totalSize += size;
		}

		if (variable_type.contains("[")) {
			String arrayPart = variable_type;
			sizes = new ArrayList<Integer>();
			if (arrayPart.contains("[")) {
				while (arrayPart.contains("[")) {
					String arraySize = arrayPart.substring(arrayPart.indexOf("[") + 1, arrayPart.indexOf("]"));
					if (null != repeatCountField) {
						throw new VerifierException("only single dimension VLAs are accepted");
					}
					if (Helper.isInteger(arraySize)) {
						sizes.add(Integer.parseInt(arraySize));
					} else {
						/* VLA */
						if (!arraySize.contains(" ")) {
							repeatCountField = arraySize;
						} else {
							/* VLA with user input */
							String strs[] = arraySize.split(" ");
							userClass = strs[0];
							repeatCountField = strs[1];
						}
						
						if (!Helper.isFieldDup(repeatCountField)) {
							throw new VerifierException("the repeatCountField '" + repeatCountField + "' for VLA '"
									+ variable_name + "' does not exist or is not preceding the VLA");
						}
					}
					arrayPart = variable_type.substring(arrayPart.indexOf("]") + 1, arrayPart.length());
				}
				variable_type = variable_type.substring(0, variable_type.indexOf("["));
			}
		}

		Helper.testValidation(variable_name, variable_type, line);

		//Convert jtype(eg. jint) to java primitive type
		if (Helper.isJPrimitiveType(variable_type) && variable_type.contains("j")) {
			variable_type = variable_type.substring(1, variable_type.length());
			if (sizes.size() != 0) {
				isPrim = true;
			}
		} else if (Helper.isCPrimitiveType(variable_type)) {
			return null;
		}//leave c primitive type(eg. int/double) unimplement.

		return new Variable(variable_name, variable_type, integerArrayListToIntArray(sizes), isPrim, size, repeatCountField, userClass);
	}

	/**
	 * Overloading createVariableFromString.
	 * Parse variables from a String array.
	 * @return <code>Variable[]</code>
	 * @throws VerifierException 
	 */
	public static Variable[] createVariableFromString(String[] line) throws VerifierException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		Variable var;

		for (int i = 1; i < line.length; i++) {
			var = createVariableFromString(line[i]);
			if (var != null) {
				variables.add(var);
			}
		}
		Variable.allVariableName = " ";
		return variables.toArray(new Variable[0]);
	}

	/**
	 * Override toString().
	 */
	@Override
	public String toString() {
		return "Variable name: " + this.name + "  Variable type: " + this.type;
	}//Unnecessary

	/**
	 * Convert current Variable class to a getter in a Java abstract class.
	 * @return a <code>String</code> that defines a getter in a Java abstract class.
	 */
	public String getGetter() {
		return "public " + this.type + " " + this.name + "();\n\n";
	}

	/**
	 * Convert current Variable class to an 1D array getter in a Java abstract class.
	 * @return a <code>String</code> that defines an 1D array getter in a Java abstract class.
	 */
	public String get1DArrayGetter() {
		return "public Array1D<" + toUpperCaseLetter(this.type) + "> " + this.name + "();\n\n";
	}

	/**
	 * Convert current Variable class to an 2D array getter in a Java abstract class .
	 * @return a <code>String</code> that defines an 2D array getter in a Java abstract class.
	 */
	public String get2DArrayGetter() {
		return "public Array2D<" + toUpperCaseLetter(this.type) + "> " + this.name + "();\n\n";
	}

	/**
	 * Convert current Variable class to an 1D primitive array getter in a Java abstract class .
	 * @return a <code>String</code> that defines an 1D primitive array getter in a Java abstract class.
	 */
	public String getPrim1DArrayGetter() {
		return "public " + toUpperCaseLetter(this.type) + "Array1D " + this.name + "();\n\n";
	}
	
	/**
	 * Convert current Variable class to an 1D primitive array getter in a Java abstract class .
	 * @return a <code>String</code> that defines an 1D primitive array getter in a Java abstract class.
	 */
	public String getVLAGetter() {
		return "public " + ((this.userClass == null)  ? "VLArray<" : this.userClass + "<") + this.type + "> " + this.name + "();\n\n";
	}

	/**
	 * Convert current Variable class to an 2D primitive array getter in a Java abstract class .
	 * @return a <code>String</code> that defines an 2D primitive array getter in a Java abstract class.
	 */
	public String getPrim2DArrayGetter() {
		return "public " + toUpperCaseLetter(this.type) + "Array2D " + this.name + "();\n\n";
	}

	/**
	 * Convert current Variable class to a setter in a Java abstract class.
	 * @return a <code>String</code> that defines a setter in a Java abstract class.
	 */
	public String getSetter() {
		return "public void " + this.name + "(" + this.type + " val" + ");\n\n";
	}

	String getEAGetter() {
		String pointerType = "Pointer<" + this.type +"> ";
		if (Helper.isJPrimitiveType(this.type)) {
			pointerType = this.type.substring(0,1).toUpperCase() + this.type.substring(1) + "Pointer ";
			this.primitivePointer = pointerType;
		}
		
		return "\t\tpublic " + pointerType + this.name + "();\n\n";
	}
	
	/**
	 * Convert ArrayList to array, specifically for ArrayList<Integer> to int[].
	 * @param list a ArrayList of Integer type.
	 * @return int[]
	 */
	private static int[] integerArrayListToIntArray(ArrayList<Integer> list) {
		int[] array = new int[list.size()];
		Iterator<Integer> it = list.iterator();
		int index = 0;

		while (it.hasNext()) {
			array[index] = (Integer)it.next();
			index++;
		}
		return array;
	}

	/**
	 * Convert the first letter of a word to upper case.
	 * @param str a String
	 * @return <code>String</code>
	 */
	public static String toUpperCaseLetter(String str) {
		char[] stringArray = str.trim().toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		str = new String(stringArray);
		return str;
	}

	/**
	 * Convert current variable to LD.
	 * @return a <code>String</code> that contains the information of a variable.
	 */
	public String convertToLD(Endian endian) {
		String arrayPart = "";
		if (null == repeatCountField) {
			for (int i : arraySizes) {
				arrayPart += ("[" + i + "]");
			}
		} else {
			arrayPart += ("[" + repeatCountField + "]");
		}
		return "\"" + endian + this.name + ":" + (Helper.isJPrimitiveType(this.type) ? ("j" + this.type) : this.type)
				+ arrayPart + ":" + (this.type.equals("Pointer") ? "pointer" : this.size) + "\"";
	}
}
