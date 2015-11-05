/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.ld2j;

/**
 * Parsing LD(Layout Descriptor) to Java abstract class
 * -Primitive type
 * -Nested struct
 * -Array
 * -Package implementation
 * -Pointer
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {

	/**
	 * Organize all information to Struct classes, given a specific input file.
	 * @return Struct[]
	 * @throws FileNotFoundException
	 * @throws VerifierException 
	 */
	private static Struct[] ReadFromFile(String file) throws FileNotFoundException, VerifierException {
		ArrayList<Struct> structs = new ArrayList<Struct>();
		Scanner fileReader = new Scanner(new FileReader(file));
		String cursor = new String();

		while (fileReader.hasNext()) {
			cursor = fileReader.nextLine();
			String[] variables = cursor.split(",");
			variables = removeUnuseful(variables);
			if (!variables[0].contains(":")) {
				if (!variables[0].contains(".")) {
					structs.add(new Struct(variables[0], null, Variable.createVariableFromString(variables)));
				} else {
					structs.add(new Struct(variables[0].split(":")[0], null, Variable
							.createVariableFromString(variables)));
				}
			} else {
				Struct[] tmp = structs.toArray(new Struct[0]);
				Struct superclass = null;
				for (Struct s : tmp) {
					if (s.structQualifiedName
							.equals(variables[0].split(":")[1].contains(".") ? variables[0].split(":")[1]
									: Struct.defaultPackageName + "." + variables[0].split(":")[1])) {
						superclass = s;
						break;
					}
				}
				if (superclass != null) {
					structs.add(new Struct(variables[0].split(":")[0], superclass, Variable
							.createVariableFromString(variables)));
				}
			}
		}
		getPointerClass(structs);

		fileReader.close();
		return structs.toArray(new Struct[0]);
	}

	/**
	 * Remove "" from a String.
	 * @return <code>String</code>
	 */
	private static String removeUnuseful(String variable) {
		return variable.substring(1, variable.length() - 1);
	}

	/**
	 * Overloading removeUnuseful.
	 * Remove "" from a String array
	 * @return <code>String[]</code>
	 */
	private static String[] removeUnuseful(String[] variables) {

		for (int i = 0; i < variables.length; i++) {
			variables[i] = removeUnuseful(variables[i]);
		}
		return variables;
	}

	/**
	 * Generate Pointer class.
	 * @param structList an ArrayList of Struct type
	 * @throws VerifierException
	 */
	private static void getPointerClass(ArrayList<Struct> structList) throws VerifierException {
		Struct[] structs = structList.toArray(new Struct[0]);
		for (Struct s : structs) {
			for (Variable var : s.getVariables()) {
				if (var.getType().equals("Pointer")) {
					if (!Helper.isNestedStruct("Pointer")) {
						String tmp = System.getProperty("sun.arch.data.model");
						String[] t = new String[] {
								"Pointer",
								("value:" + ((tmp.equals("32")) ? "jint" : "jlong") + ":" + ((tmp.equals("32")) ? "4"
										: "8")) };
						structList
								.add(new Struct("com.ibm.layout.Pointer", null, Variable.createVariableFromString(t)));
						return;
					}
				}
			}
		}
	}

	/**
	 * Two inputs are required (one option):
	 * @param args program arguments
	 * - Path for input file.
	 * - Path for output file.
	 * - Default package.(optional)
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 2) {
				Struct.defaultPackageName = null;
			} else if (args.length == 3) {
				Struct.defaultPackageName = args[2];
			} else {
				throw new IllegalArgumentException();
			}
			Struct[] structs = ReadFromFile(args[0]);
			String[] structNames = new String[structs.length];
			BufferedWriter fileWriter;

			for (int i = 0; i < structs.length; i++) {
				structNames[i] = structs[i].getStructName();
			} //get all structs' name

			for (int i = 0; i < structs.length; i++) {
				fileWriter = new BufferedWriter(new FileWriter(args[1] + structNames[i] + ".java"));
				fileWriter.write(structs[i].toClass());
				fileWriter.close();
			} //Write each struct into different .java file
			System.out.println("Parsing Successful!");
		} catch (IllegalArgumentException e) {
			if (args.length < 2) {
				System.out.println("too few argumenets");
			} else if (args.length > 2) {
				System.out.println("too many argumenets");
			}
		} catch (FileNotFoundException e) {
			System.out.println(args[0] + " not found!\nParsing Failed!\n");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VerifierException e) {
			System.out.println(e.getMessage());
		}
	}
}
