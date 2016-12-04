/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.ld2j;

import java.util.HashSet;

class Struct {
	private String structName;
	private Variable variables[];
	private String LDLayout = "";
	private String packageName = "";
	private Struct superclass = null;
	private Endian endian = Endian.NATIVE;
	private int containsVLA = 0;
	public String structQualifiedName = "";
	public int numOfPointer = 0;
	public static String defaultPackageName = "";
	public static String AllStructsName = " ";
	public static int count = 0;

	/**
	 * Constructor
	 * @param structName a String that represents the name of a struct class.
	 * @param variables a array of Variable object.
	 * @throws VerifierException 
	 */
	public Struct(String structQualifiedName, Struct superclass, Variable variables[]) throws VerifierException {
		this.superclass = superclass;
		this.structQualifiedName = structQualifiedName;
		
		if (this.structQualifiedName .contains(">")) {
			endian = Endian.BIG;
			this.structQualifiedName = this.structQualifiedName.substring(1, this.structQualifiedName .length());
		} else if (this.structQualifiedName.contains("<")) {
			endian = Endian.LITTLE;
			this.structQualifiedName = this.structQualifiedName.substring(1, this.structQualifiedName .length());
		} 
		
		if (!structQualifiedName.contains(".")) {
			this.packageName = defaultPackageName;
			this.structName = this.structQualifiedName ;
			this.structQualifiedName = defaultPackageName + "." + this.structQualifiedName ;
		} else {
			String[] tmp = this.structQualifiedName .split("\\.");
			for (int i = 0; i < tmp.length - 1; i++) {
				packageName += tmp[i];
				if (i != (tmp.length - 2)) {
					packageName += ".";
				}
			}
			structName = tmp[tmp.length - 1];
		}

		this.variables = variables;
		AllStructsName += this.structQualifiedName;
		AllStructsName += " ";
		Variable.totalSize = 0;
		numOfPointer = Variable.numOfPointer;
		Variable.numOfPointer = 0;
		if (variables.length != 0) {
			this.LDLayout = setLDLayout();
		} else {
			this.LDLayout = "\"\"";
		}
		count++;
	}

	/**
	 * Set LD Layout
	 * @param cursor
	 * @return
	 */
	private String setLDLayout() {
		String newString = "";
		for (int i = 0; i < variables.length; i++) {
			newString += variables[i].convertToLD(endian);
			if (i != variables.length - 1) {
				newString += ",";
			}
		}
		return newString;
	}

	/**
	 * Getter for structName, get struct's name.
	 * @return a <code>String</code> that returns a struct name.
	 */
	public String getStructName() {
		return structName;
	}

	/**
	 * Getter for Variables, get all fields.
	 * @return <code>Variable[]</code>
	 */
	public Variable[] getVariables() {
		return this.variables;
	}

	/**
	 * Getter for superclass, get current class's superclass
	 * @return a <code>Struct</code> class that is the superclass of the current class.
	 */
	public Struct getSuperclass() {
		return this.superclass;
	}

	/**
	 * getter for packageNamem, Get struct's package name.
	 * @return a <code>String</code> that returns a struct's package name.
	 */
	private String getPackageName() {
		return this.packageName;
	}

	/**
	 * Convert current struct class to a Java abstract class(String).
	 * @return a <code>String</code> that defines a java abstract class.
	 * @throws VerifierException 
	 */
	public String toClass() throws VerifierException {
		String code = getLicense();
		String repeatCountField = null;
		Variable vla = null;
		code += getPackage();
		code += getImport();
		code += generatedByLD2J();
		code += getLayoutDesc();
		code += ("public interface " + this.structName + " extends "
				+ (this.superclass == null ? "Layout" : this.superclass.getStructName()) + " {\n\n");
		code += getEAInterface();
		code += getEAMethod();
		code += getSizeOfMethod();
		System.out.println("Struct name: " + this.structName);//Unnecessary
		//getter
		for (Variable var : variables) {
			if (null != var.repeatCountField) {
				code += "\t";
				code += var.getVLAGetter();
				repeatCountField = var.getRepeatCountField();
				vla = var;
			} else if (Helper.is1DArray(var.getArraySize())) {
				code += "\t";
				if (var.isPrimArray) {
					code += var.getPrim1DArrayGetter();
				} else {
					code += var.get1DArrayGetter();
				}
			} else if (Helper.is2DArray(var.getArraySize())) {
				code += "\t";
				if (var.isPrimArray) {
					code += var.getPrim2DArrayGetter();
				} else {
					code += var.get2DArrayGetter();
				}
			} else if (Helper.isPointer(var.getType())) {
				//Do nothing
			} else {
				code += "\t";
				code += var.getGetter();
			}
			System.out.println(var);//Unnecessary
		}
		if (containsVLA == 1) {
			code += bindLocationWithInitializer(vla);
		}
		//setter
		for (Variable var : variables) {
			if (Helper.isNestedStruct(var.getType()) 
					|| Helper.isArray(var.getArraySize())
					|| (var.getName().equals(repeatCountField))
			) {
				continue;
			}
			code += "\t";
			code += var.getSetter();
		}
		for (Variable var : variables) {
			if (!Helper.isPointer(var.getType())) {
				code += getToStringMethod();
				break;
			}
		}
		code += "}\n";
		return code;
	}

	/**
	 * Override ToString().
	 */
	@Override
	public String toString() {
		String temp = "";

		for (int i = 0; i < variables.length; i++) {
			temp += variables[i].toString();
			temp += "\n";
		}

		return structName + "\n" + temp;
	}//Unnecessary

	private String getSizeOfMethod() {
		return "\tpublic long sizeof();\n\n";
	}

	private String getLayoutDesc() {
		return "@LayoutDesc({" + this.LDLayout + "})\n";
	}
	
	private String generatedByLD2J() {
		return "/* Generated by LD2J */\n";
	}
	
	private String bindLocationWithInitializer(Variable vla) throws VerifierException {
		String headerType = null;
		for (Variable var : variables) {
			if (vla.repeatCountField.equals(var.getName())) {
				headerType = var.getType();
			}
		}
		if (headerType == null) {
			throw new VerifierException("could not find the type of the repeatCountField");
		}
		return "\tpublic void bindLocation(Location loc, " + headerType + " repeatCountInitializer);\n\n";
	}
	
	private String getPackage() {
		return (this.packageName == null) ? "" : "package " + this.packageName + ";\n\n";
	}

	private String getLicense() {
		return "/*******************************************************************************\n" 
			   + " *  Copyright (c) 2016 IBM Corporation.\n"
			   + " *  All rights reserved. This program and the accompanying materials\n"
			   + " *  are made available under the terms of the Eclipse Public License v1.0\n"
			   + " *  which accompanies this distribution, and is available at\n"
			   + " *  http://www.eclipse.org/legal/epl-v10.html\n"
			   + " *******************************************************************************/\n";
	}

	private String getImport() {
		String importString = "";
		boolean flag_1D = false;
		boolean flag_2D = false;
		boolean importPointer = false;
		HashSet<String> hs_1D = new HashSet<String>(1);
		HashSet<String> hs_2D = new HashSet<String>(1);
		String[] array_1D;
		String[] array_2D;
		for (Variable var : variables) {
			if (null != var.getRepeatCountField()) {
				if (var.getUserClass() == null) {
					containsVLA++;
				} else {
					importString += "import " + this.packageName + "." + var.getUserClass() + ";\n";
				}
			} else if (var.getArraySize().length == 1) {
				if (!var.isPrimArray)
					flag_1D = true;
			} else if (var.getArraySize().length == 2) {
				if (!var.isPrimArray)
					flag_2D = true;				
			}
			var.getEAGetter();
			if (var.primitivePointer != null) {
				if (!importString.contains(var.primitivePointer)) {
					importString += "import com.ibm.layout." + var.primitivePointer + ";\n";
				}
			} else {
				importPointer = true;
			}
		}

		for (Variable var : variables) {
			if (var.getArraySize().length == 1) {
				if (var.isPrimArray) {
					hs_1D.add(var.getType());
				}
			}
			if (var.getArraySize().length == 2) {
				if (var.isPrimArray) {
					hs_2D.add(var.getType());
				}
			}
		}

		array_1D = hs_1D.toArray(new String[0]);
		array_2D = hs_2D.toArray(new String[0]);

		for (String s : array_1D) {
			importString += ("import com.ibm.layout." + Variable.toUpperCaseLetter(s) + "Array1D;\n");
		}

		for (String s : array_2D) {
			importString += ("import com.ibm.layout." + Variable.toUpperCaseLetter(s) + "Array2D;\n");
		}
		
		if (importPointer) importString += "import com.ibm.layout.Pointer;\n";
		
		importString += (((flag_1D == true) ? "import com.ibm.layout.Array1D;\n" : "")
				+ ((flag_2D == true) ? "import com.ibm.layout.Array2D;\n" : "")
				+ ((containsVLA == 1) ? "import com.ibm.layout.Location;\n" : "")
				+ ((containsVLA > 0) ? "import com.ibm.layout.VLArray;\n" : "")
				+ "import com.ibm.layout.Layout;\nimport com.ibm.layout.LayoutDesc;\n");

		String used = "";
		for (Variable v : variables) {
			if (Helper.isNestedStruct(v.getType())) {
				String[] tmp = Struct.AllStructsName.split(" ");
				for (String s : tmp) {
					String[] strs = s.split("\\.");
					if (strs[strs.length - 1].equals(v.getType())) {
						if (!s.equals(this.packageName + "." + v.getType())) {
							if (!used.contains(v.getType())) {
								importString += ("import " + s + ";\n");
								used += v.getType();
								break;
							}
						}
					}
				}
			}
		}

		return importString
				+ ((this.superclass == null) ? ""
						: ((this.superclass.packageName == null || this.superclass.packageName.equals(this.packageName)) ? ""
								: ("import "
										+ ((this.superclass == null) ? "" : this.superclass.getPackageName() + ".")
										+ this.superclass.getStructName() + ";\n"))) + "\n";
	}

	private String getToStringMethod() {
		return "\t@Override\n\tpublic String toString();\n\n";
	}
	
	private String getEAMethod() {
		return "\tpublic " + this.structName + ".EffectiveAddress EA();\n\n"; 
	}

	private String getEAInterface() {
		String code = "\tinterface EffectiveAddress {\n\n";
		for (Variable v : variables) {
			code += v.getEAGetter();
		}
		code += "\t}\n\n";
		return code;
	}

}
