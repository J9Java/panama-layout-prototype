/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Helper methods for runtime class generators (Gen*.class) and FactoryMethods
 */
final class ImplHelper implements Opcodes {
	
	/**
	 * Class to describe field properties
	 */
	public static final class FieldDesc {
		@Override
		public String toString() {
			return "FieldDesc [name=" + name + ", offset=" + offset + ", size=" + size + ", rawType=" + rawType
					+ ", sig=" + sig + ", sigGeneric=" + sigGeneric + ", impl=" + impl + ", dims="
					+ Arrays.toString(dims) + "]";
		}

		public String name;
		public long offset;
		public long size;
		public String rawType; // type from descriptor
		public String sig; // public signature string
		public String sigGeneric;
		public String impl; // impl class for non-primitives
		public long[] dims; // array dim
		public String elementImpl; // element impl class for non-primitives
		public String element;
		public String repeatCountMember;
		public String returnClass;
		public Endian endian = Endian.NATIVE;
		public boolean complexUserClass = false;
		public boolean priviliedgedUserClass = false;
		public String enclosingTypeName = null;
		public String pointerSig = null;
		public boolean isVarSized = false;
	}

	static final class TypeInfo {
		public Type type;
		public String repeatCountMember;
		public String fixedHeaderType;
		public String enclosingType;
		
		public TypeInfo(Type type, String header) {
			this.type = type;
			this.repeatCountMember = header;
		}
		
		public TypeInfo(Type type, String header, String fixedHeaderType, String enclosingType) {
			this.type = type;
			this.repeatCountMember = header;
			this.fixedHeaderType = fixedHeaderType;
			this.enclosingType = enclosingType;
		}
		
		public TypeInfo(Type type) {
			this(type, (String) null);
		}
	}
	
	/**
	 * Find classes that must be generated before the current one. 
	 * @param interfaceCls A layout interface class
	 * @return Set of required classes.
	 */
	static public TypeInfo[] getRequiredClasses(Class<? extends Layout> interfaceCls) {
		/* Return types of field getter methods */
		Method[] mm = interfaceCls.getDeclaredMethods();
		LinkedHashSet<TypeInfo> classSet = new LinkedHashSet<TypeInfo>(mm.length);
		for (int i = 0; i < mm.length; i++) {
			if (Modifier.isAbstract(mm[i].getModifiers())) {
				TypeInfo t = new TypeInfo(mm[i].getGenericReturnType());
				if (t.type instanceof ParameterizedType) {
					String memberName = mm[i].getName();
					TypeInfo fixedHeader = findHeaderAndType(interfaceCls, memberName);
					if (null != fixedHeader) {
						t.repeatCountMember = fixedHeader.repeatCountMember;
						t.fixedHeaderType = fixedHeader.fixedHeaderType;
						t.enclosingType = fixedHeader.enclosingType;
					}
				}
				classSet.add(t);
			}
		}
		
		/* Superinterface, if it is not a builtin type */
		Class<?> superInterface = getSuperInterface(interfaceCls);
		String superInterfaceName = superInterface.getName();
		if (!superInterfaceName.contains("com.ibm.layout")) {
			classSet.add(new TypeInfo(superInterface));
		}
		return classSet.toArray(new TypeInfo[0]);
	}
	
	public static TypeInfo findHeaderAndType(Class<? extends Layout> interfaceCls, String memberName) {
		String header = null;
		
		//search for varArray header
		for (String m : getAnnotation(interfaceCls)) {
			String[] member = m.split(":");
			if (member[0].contains(">") || member[0].contains("<")) {
				member[0] = member[0].substring(1);
			}
			if (member[0].equals(memberName)) {
				int start = member[1].indexOf('[');
				int end = member[1].indexOf(']');
				if ((start > 0) && (end > 0)) {
					header = member[1].substring(start + 1, end);
				}
			}
		}
		
		//search for type of header
		for (String m : getAnnotation(interfaceCls)) {
			String[] member = m.split(":");
			if (member[0].contains(">") || member[0].contains("<")) {
				member[0] = member[0].substring(1);
			}
			if (member[0].equals(header)) {
				return new TypeInfo(null, member[0], member[1], interfaceCls.getSimpleName());
			}
		}
		
		return null;
	}
	
	
	/**
	 * Find the field description specified by fieldName in array of field description fields
	 * 
	 * @param fieldName name of field to search for
	 * @param fields array of field descriptions
	 * @return field description or null if not found
	 */
	static public ImplHelper.FieldDesc findField(String fieldName, ImplHelper.FieldDesc[] fields) {
		for (ImplHelper.FieldDesc f : fields) {
			if (f.name.equals(fieldName)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * Return a hashtable containing the classes of all fields in the interface class
	 * 
	 * @param interfaceCls, interface class
	 * @return hashtable where key is fieldName, and value is fieldType
	 */
	static public HashMap<String, String> getFieldClasses(Class<? extends Layout> interfaceCls) {
		Class<?> superInterface = getSuperInterface(interfaceCls);
		String superInterfaceName = superInterface.getName();
		HashMap<String, String> fieldClasses = new HashMap<>(1);

		if (!superInterfaceName.contains("com.ibm.layout")) {
			Class<?> cls = superInterface;
			while (!cls.getName().contains("com.ibm.layout")) {
				for (Method i : cls.getDeclaredMethods()) {
					if (Modifier.isAbstract(i.getModifiers())) {
						fieldClasses.put(i.getName(), i.getReturnType().getName());
						Type returnType = i.getGenericReturnType();
						if (returnType instanceof ParameterizedType) {
							fieldClasses.put(i.getName() + "ElementType", ((ParameterizedType) returnType).getActualTypeArguments()[0].getTypeName());
						}
					}
				}
				cls = getSuperInterface(cls);
			}
		}
		for (Method i : interfaceCls.getDeclaredMethods()) {
			if (Modifier.isAbstract(i.getModifiers())) {
				fieldClasses.put(i.getName(), i.getReturnType().getName());
				Type returnType = i.getGenericReturnType();
				if (returnType instanceof ParameterizedType) {
					fieldClasses.put(i.getName() + "ElementType", ((ParameterizedType) returnType).getActualTypeArguments()[0].getTypeName());
				}
			}
		}

		return fieldClasses;
	}
	
	static boolean isVariableLengthType(Class<?> interfaceClass) {
		String[] desc = getAnnotation(interfaceClass);
		FieldDesc[] fldDesc = new FieldDesc[desc.length];
		for (int i = 0; i < desc.length; i++) {
			String[] split = desc[i].split(":");
			fldDesc[i] = new FieldDesc();
			fldDesc[i].name = split[0];
			if (fldDesc[i].name.contains(">")) {
				fldDesc[i].name = fldDesc[i].name.substring(1);
				fldDesc[i].endian = Endian.BIG;
			} else if (fldDesc[i].name.contains(">")) {
				fldDesc[i].name = fldDesc[i].name.substring(1);
				fldDesc[i].endian = Endian.LITTLE;
			}
			fldDesc[i].rawType = split[1];
			fldDesc[i].size = Long.parseLong(split[2]);
			parseFieldDescDims(fldDesc[i]);
			if (null != fldDesc[i].repeatCountMember) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Get field descriptions for all fields in interface class
	 * 
	 * @param interfaceCls, interface class
	 * @return Array of fieldDescriptions 
	 */
	static public FieldDesc[] getFieldDesc(Class<? extends Layout> interfaceCls) {
		LayoutDesc ld = interfaceCls.getAnnotation(LayoutDesc.class);
		if (null == ld) {
			return null;
		}
		String[] desc = ld.value();
		
		desc = removePointerFromDesc(desc);
		FieldDesc[] fldDesc = new FieldDesc[desc.length];
		HashMap<String, String> fieldClasses = getFieldClasses(interfaceCls);

		for (int i = 0; i < desc.length; i++) {
			String[] split = desc[i].split(":");
			fldDesc[i] = new FieldDesc();
			fldDesc[i].name = split[0];
			if (fldDesc[i].name.contains(">")) {
				fldDesc[i].name = fldDesc[i].name.substring(1);
				fldDesc[i].endian = Endian.BIG;
			} else if (fldDesc[i].name.contains(">")) {
				fldDesc[i].name = fldDesc[i].name.substring(1);
				fldDesc[i].endian = Endian.LITTLE;
			}
			fldDesc[i].rawType = split[1];
			fldDesc[i].size = Long.parseLong(split[2]);
			fldDesc[i].returnClass = fieldClasses.get(fldDesc[i].name);
			fldDesc[i].enclosingTypeName = interfaceCls.getSimpleName();
			fldDesc[i].element = fieldClasses.get(fldDesc[i].name + "ElementType");
			
			if (i == 0) {
				fldDesc[i].offset = 0;
			} else {
				if ((fldDesc[i - 1].size == 0) && (i > 1)) {
					//the last field is variable sized, set offset to zero
					//bytecode generation will do (this.offset + waypoint) to figure out real offset
					fldDesc[i].offset = 0;
				} else {
					fldDesc[i].offset = fldDesc[i - 1].offset + fldDesc[i - 1].size;
				}
			}

			parseFieldDescDims(fldDesc[i]);
			try {
				Class<?> clazz = Class.forName(fldDesc[i].returnClass);
				if (ComplexVLArray.class.isAssignableFrom(clazz)) {
					fldDesc[i].complexUserClass = true;
					clazz = Class.forName(fldDesc[i].element);
					fldDesc[i].isVarSized = ImplHelper.isVariableLengthType(clazz);
				} else if (PriviledgedVLArray.class.isAssignableFrom(clazz)){
					fldDesc[i].complexUserClass = true;
					fldDesc[i].priviliedgedUserClass = true;
					clazz = Class.forName(fldDesc[i].element);
					fldDesc[i].isVarSized = ImplHelper.isVariableLengthType(clazz);
				} else if (Layout.class.isAssignableFrom(clazz)) {
					fldDesc[i].isVarSized = ImplHelper.isVariableLengthType(clazz);
				} else if (VLArray.class.isAssignableFrom(clazz)) {
					clazz = Class.forName(fldDesc[i].element);
					fldDesc[i].isVarSized = ImplHelper.isVariableLengthType(clazz);
				}
			} catch (ClassNotFoundException e) {/* do nothing */}
			
			if (fldDesc[i].dims == null) {
				parseNonArraySig(fldDesc[i], fieldClasses);
			} else {
				parseArraySig(fldDesc[i]);
			}

			if (isTypePrimitive(fldDesc[i].sig)) {
				fldDesc[i].pointerSig = "com/ibm/layout/" + fieldSig2MethodType(fldDesc[i].sig) + "Pointer";
			} else {
				if (null != fldDesc[i].element) {
					fldDesc[i].pointerSig = "com/ibm/layout/Pointer<L"+  fldDesc[i].element.replace(".", "/") + ";>";
				} else {
					fldDesc[i].pointerSig = "com/ibm/layout/Pointer<"+  fldDesc[i].sig + ">";
				}
			}

		}

		
		return fldDesc;
	}
	
	/**
	 * 
	 * Tests whether input is an integer or not
	 * @param val String containing value to be tested
	 * @return a <code>boolean</code> with the result.
	 */
	private static boolean isInteger(String val) {
		for (int i = 0; i < val.length(); i++) {
			if (!Character.isDigit(val.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	static private void parseFieldDescDims(FieldDesc fldDesc) {
		// count the dimensions
		int dims = getArrayDim(fldDesc.rawType);
		if (dims > 2) {
			throw new UnsupportedOperationException("dims > 2");
		}

		if (dims > 0) {
			// get each dimension
			fldDesc.dims = new long[dims];

			int open = -1;
			int close = -1;
			int dimIdx = 0;

			while (-1 != (open = fldDesc.rawType.indexOf('[', close + 1))) {
				close = fldDesc.rawType.indexOf(']', open + 1);
				String size = fldDesc.rawType.substring(open + 1, close);
				if (isInteger(size)) {
					fldDesc.dims[dimIdx] = Long.parseLong(size);
				} else {
					fldDesc.repeatCountMember = size;
				}
				dimIdx++;
			}
		}
	}

	static private void parseArraySig(FieldDesc fldDesc) {
		String nonArrayType = getNonArrayType(fldDesc.rawType);
		int dim = fldDesc.dims.length;
		fldDesc.elementImpl = "";
		
		switch (nonArrayType) {
		case "jbyte":
			fldDesc.sig = "Lcom/ibm/layout/ByteArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/ByteArray" + dim + "DImpl";
			break;
		case "jint":
			fldDesc.sig = "Lcom/ibm/layout/IntArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/IntArray" + dim + "DImpl";
			break;
		case "jboolean":
			fldDesc.sig = "Lcom/ibm/layout/BooleanArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/BooleanArray" + dim + "DImpl";
			break;
		case "jchar":
			fldDesc.sig = "Lcom/ibm/layout/CharArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/CharArray" + dim + "DImpl";
			break;
		case "jshort":
			fldDesc.sig = "Lcom/ibm/layout/ShortArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/ShortArray" + dim + "DImpl";
			break;
		case "jlong":
			fldDesc.sig = "Lcom/ibm/layout/LongArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/LongArray" + dim + "DImpl";
			break;
		case "jfloat":
			fldDesc.sig = "Lcom/ibm/layout/FloatArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/FloatArray" + dim + "DImpl";
			break;
		case "jdouble":
			fldDesc.sig = "Lcom/ibm/layout/DoubleArray" + dim + "D;";
			fldDesc.impl = "com/ibm/layout/gen/DoubleArray" + dim + "DImpl";
			break;
		default:
			if (null == fldDesc.repeatCountMember) {
				fldDesc.sig = "Lcom/ibm/layout/Array" + dim + "D;";
				fldDesc.sigGeneric = "Lcom/ibm/layout/Array" + dim + "D<" + nonArrayType + ">;";
				fldDesc.impl = getArrayClassImplName(nonArrayType, dim);
				fldDesc.elementImpl = "com/ibm/layout/gen/" + nonArrayType +"Impl";
			} else {
				if (fldDesc.complexUserClass) {
					String classPackageName = getBinaryPackageName(fldDesc.returnClass);
					String userArrayName = fldDesc.returnClass.substring(fldDesc.returnClass.lastIndexOf(".") + 1); 				
					fldDesc.sig = "L" + classPackageName + "/" + userArrayName + ";";
					fldDesc.sigGeneric = "L" + fldDesc.returnClass.replace('.', '/') + "<" + nonArrayType +">;";
					fldDesc.impl = getVLArrayClassImplName(userArrayName, fldDesc.repeatCountMember, fldDesc.enclosingTypeName);
				} else {
					fldDesc.sig = "Lcom/ibm/layout/VLArray;";
					fldDesc.sigGeneric = "Lcom/ibm/layout/VLArray<" + nonArrayType + ">;";
					fldDesc.impl = getVLArrayClassImplName(nonArrayType);
				}
				fldDesc.elementImpl = "com/ibm/layout/gen/" + nonArrayType +"Impl";
			}
			break;
		}
	}

	static private void parseNonArraySig(FieldDesc fldDesc, HashMap<String, String> fieldClasses) {
		switch (fldDesc.rawType) {
		case "jboolean":
			fldDesc.sig = "Z";
			break;
		case "jbyte":
			fldDesc.sig = "B";
			break;
		case "jchar":
			fldDesc.sig = "C";
			break;
		case "jshort":
			fldDesc.sig = "S";
			break;
		case "jint":
			fldDesc.sig = "I";
			break;
		case "jlong":
			fldDesc.sig = "J";
			break;
		case "jfloat":
			fldDesc.sig = "F";
			break;
		case "jdouble":
			fldDesc.sig = "D";
			break;
		default:
			// signature of an interface class
			fldDesc.sig = "L" + getBinaryPackageName(fieldClasses.get(fldDesc.name)) + "/" + fldDesc.rawType + ";";
			fldDesc.impl = getImplClassName(fldDesc.rawType);
			break;
		}
	}

	static private int getArrayDim(String fieldType) {
		int dim = 0;
		int indexOf = -1;

		while (-1 != (indexOf = fieldType.indexOf('[', indexOf + 1))) {
			dim++;
		}
		return dim;
	}

	static private String getNonArrayType(String fieldType) {
		return fieldType.substring(0, fieldType.indexOf('['));
	}

	static private String getBinaryPackageName(String className) {
		int lastIndex = className.lastIndexOf('.');
		if (lastIndex == -1) return "";
		return className.substring(0, lastIndex).replace('.', '/');
	}

	static private String[] removePointerFromDesc(String[] desc) {
		ArrayList<String> tmp = new ArrayList<String>();
		for (String s : desc) {
			if (!isPointerType(s.split(":")[1])) {
				tmp.add(s);
			}
		}
		return tmp.toArray(new String[0]);
	}

	/**
	 * Get the value of the @LayoutDesc annotation for an interface class.
	 * Concatenate the LayoutDesc with all its superclass's.
	 * @param interfaceCls
	 * @return
	 */
	public static String[] getAnnotation(Class<?> interfaceCls) {
		String[] desc;
		Class<?> cls = interfaceCls;
		if (null == interfaceCls.getAnnotation(LayoutDesc.class)) {
			do {
				cls = getSuperInterface(cls);
			} while (null == cls.getAnnotation(LayoutDesc.class));
			desc = getAnnotation(cls);
		} else {
			desc = interfaceCls.getAnnotation(LayoutDesc.class).value();
			cls = getSuperInterface(cls);
			if (null != cls.getAnnotation(LayoutDesc.class)) {
				desc = concatTwoStringArray(desc, getAnnotation(cls));
			}
		}
		return desc;
	}
	
	/**
	 * Get the binary name of an interface class
	 */
	static public String getInterfaceClassName(Class<? extends LayoutType> interfaceClass) {
		return interfaceClass.getName().replace('.', '/');
	}
	
	/**
	 * Get the binary name of the impl's super class
	 */
	static public String getImplSuperClassName(Class<? extends LayoutType> interfaceClass) {
		return "com/ibm/layout/gen/" + interfaceClass.getInterfaces()[0].getSimpleName() + "Impl";
	}

	/**
	 * Get the datalength for singleton layout
	 * @return
	 */
	static public long getByteSize(FieldDesc[] fldDesc) {
		if (fldDesc.length == 0) {
			return 0;
		}
		return fldDesc[fldDesc.length - 1].offset + fldDesc[fldDesc.length - 1].size;
	}

	/**
	 * Get the type suffix for the setter/getter for a primitive type
	 * @param fieldSig a field signature
	 * @return
	 */
	static public String fieldSig2MethodType(String fieldSig) {
		char c = fieldSig.charAt(0);
		switch (c) {
		case 'Z':
			return "Boolean";
		case 'B':
			return "Byte";
		case 'C':
			return "Char";
		case 'S':
			return "Short";
		case 'I':
			return "Int";
		case 'J':
			return "Long";
		case 'F':
			return "Float";
		case 'D':
			return "Double";
		default:
			throw new RuntimeException("unknown fieldSig");
		}
	}
	
	static String fieldSig2MethodTypeLongForm(String fieldSig) {
		char c = fieldSig.charAt(0);
		switch (c) {
		case 'Z':
			return "Boolean";
		case 'B':
			return "Byte";
		case 'C':
			return "Character";
		case 'S':
			return "Short";
		case 'I':
			return "Integer";
		case 'J':
			return "Long";
		case 'F':
			return "Float";
		case 'D':
			return "Double";
		default:
			throw new RuntimeException("unknown fieldSig");
		}
	}
	

	/**
	 * Get the Signature for a JNI type
	 * @param type string containing JNI type 
	 * @return signature for JNI type
	 */
	static public String convertJNITypeToSig(String type) {
		switch (type) {
		case "jboolean":
			return "Z";
		case "jbyte":
			return "B";
		case "jchar":
			return "C";
		case "jshort":
			return "S";
		case "jint":
			return "I";
		case "jlong":
			return "J";
		case "jfloat":
			return "F";
		case "jdouble":
			return "D";
		default:
			return null;
		}
	}
	
	/**
	 * Return whether a field type is nested
	 * @param fieldSig a field signature
	 * @return whether a field type is nested
	 */
	static public boolean isTypePrimitive(String fieldSig) {
		char c = fieldSig.charAt(0);
		return (c != 'L');
	}

	/**
	 * Get the binary Impl name for a singleton layout.
	 * All Impl classes reside in the same package.
	 * @param simpleName Simple class name
	 * @return Impl class name
	 */
	static public String getImplClassName(String simpleName) {
		return "com/ibm/layout/gen/" + simpleName + "Impl";
	}
	
	/**
	 * Get the binary Impl name for a singleton layout.
	 * All Impl classes reside in the same package.
	 * @param layoutClass A singleton layout class
	 * @return Impl class name
	 */
	static public String getImplClassName(Class<?> layoutClass) {
		return getImplClassName(layoutClass.getSimpleName());
	}
	
	
	/**
	 * Get the binary Impl name for a 2D array layout.
	 * All Impl classes reside in the same package.
	 * @param layoutClass A 2D array layout class
	 * @return Impl class name
	 */
	static public String getArray2DClassImplName(Class<? extends Layout> elementInterfaceClass) {
		return getArrayClassImplName(elementInterfaceClass.getSimpleName(), 2);
	}

	/**
	 * Get the binary Impl name for a 1D array layout.
	 * All Impl classes reside in the same package.
	 * @param layoutClass A 1D array layout class
	 * @return Impl class name
	 */
	static public String getArray1DClassImplName(Class<? extends Layout> elementInterfaceClass) {
		return getArrayClassImplName(elementInterfaceClass.getSimpleName(), 1);
	}

	/**
	 * Get the Impl name for an Array layout
	 * @param simpleName simple name of element interface class (must extend Layout)
	 * @param dim number of dimensions
	 * @return
	 */
	static public String getArrayClassImplName(String simpleName, int dim) {
		return "com/ibm/layout/gen/" + simpleName + dim + "DImpl";
	}
	
	/**
	 * Get the binary Impl name for a pointer layout.
	 * All Impl classes reside in the same package.
	 * @param layoutClass A pointer layout class
	 * @return Impl class name
	 */
	static public String getPointerClassImplName(Class<? extends Layout> elementInterfaceClass) {
		return getPointerClassImplName(elementInterfaceClass.getSimpleName());
	}

	/**
	 * Get the Impl name for an pointer layout
	 * @param simpleName simple name of pointer interface class
	 * @return
	 */
	static public String getPointerClassImplName(String simpleName) {
		return "com/ibm/layout/gen/Layout" + simpleName + "PointerImpl";
	}

	/**
	 * Get the Impl name for a VLArray layout
	 * @param simpleName simple name of element interface class (must extend Layout)
	 * @param repeatCountMember repeat count name
	 * @param enclosingTypeName name of enclosing type
	 * @return
	 */
	static public String getVLArrayClassImplName(String simpleName) {
		return "com/ibm/layout/gen/" + simpleName +"VLArrayImpl";
	}
	
	/**
	 * Get the Impl name for a VLArray layout
	 * @param simpleName simple name of element interface class (must extend Layout)
	 * @param repeatCountMember repeat count name
	 * @param enclosingTypeName name of enclosing type
	 * @return
	 */
	static public String getVLArrayClassImplName(String simpleName, String repeatCountName, String enclosingClassName) {
		return "com/ibm/layout/gen/" + simpleName + repeatCountName + enclosingClassName + "VLArrayImpl";
	}
	
	/**
	 * Get the Impl name for a VLArray layout
	 * @param elementInterfaceClass a VLA layout class
	 * @param repeatCountMember repeat count name
	 * @param enclosingTypeName name of enclosing type
	 * @return
	 */
	static public String getVLArrayClassImplName(Class<? extends Layout> elementInterfaceClass) {
		return getVLArrayClassImplName(elementInterfaceClass.getSimpleName());
	}
	
	static public boolean isPointerType(String type) {
		return type.equals("Pointer");
	}

	/**
	 * Merge Two String arrays together into one.
	 * @param first first String[]
	 * @param second second String[]
	 * @return Merged String[]
	 */
	static public String[] concatTwoStringArray(String[] first, String[] second) {
		List<String> both = new ArrayList<String>(first.length + second.length);
		Collections.addAll(both, first);
		Collections.addAll(both, second);
		return both.toArray(new String[both.size()]);
	}
	
	static String getByteCodeSig(String sig) {
		if (sig.equals("B") || sig.equals("Z") || sig.equals("C") || sig.equals("S")) {
			return "I";
		}
			
		return sig;
	}
	
	static private Class<?> getSuperInterface(Class<?> interfaceCls) {
		Class<?>[] interfaces = interfaceCls.getInterfaces();
		
		for (Class<?> superInterface : interfaces) {
			if (Layout.class.isAssignableFrom(superInterface)) {
				return superInterface;
			}
		}
		return null;
	}
	

	static void getLayoutImpl(ClassVisitor cw, MethodVisitor mv, String typeName) {
		{
			mv = cw.visitMethod(ACC_PUBLIC, "copyFrom", "(Lcom/ibm/layout/Layout;)V", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, typeName, "unsafe", "Lsun/misc/Unsafe;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, typeName);
			mv.visitFieldInsn(GETFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, typeName);
			mv.visitFieldInsn(GETFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof", "()J", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "copyMemory", "(Ljava/lang/Object;JLjava/lang/Object;JJ)V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(9, 2);
			mv.visitEnd();
			}
	}
	
	static void genLayoutTypeImpl(ClassVisitor cw, MethodVisitor mv, FieldVisitor fv, String typeName, int layoutContainsVLA, boolean layoutIsVLA) 
	{
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "unsafe", "Lsun/misc/Unsafe;", null, null);
			fv.visitEnd();
		}
		
		{
			fv = cw.visitField(ACC_PROTECTED, "location", "Lcom/ibm/layout/Location;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_STATIC, "classFlags", "J", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitInsn(LCONST_1);
			mv.visitFieldInsn(PUTSTATIC, typeName, "classFlags", "J");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "containsVLA", "()Z", null, null);
			mv.visitCode();
			//if layout contains VLA return true, otherwise false
			if (layoutContainsVLA > 0) {
				mv.visitInsn(ICONST_1);
			} else {
				mv.visitInsn(ICONST_0);
			}
			mv.visitInsn(IRETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "bindLocationNoCheck", "(Lcom/ibm/layout/Location;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
			mv = cw.visitMethod(ACC_PUBLIC, "bindLocation", "(Lcom/ibm/layout/Location;)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, typeName, "location", "Lcom/ibm/layout/Location;");
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "writeToByteArray",
					"(J[BII)I", null, null);
			mv.visitCode();
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(I2L);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof",
					"()J", false);
			mv.visitInsn(LCMP);
			Label l0 = new Label();
			mv.visitJumpInsn(IFLE, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof",
					"()J", false);
			mv.visitInsn(L2I);
			mv.visitVarInsn(ISTORE, 5);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			Label l1 = new Label();
			mv.visitJumpInsn(IFNULL, l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(L2I);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy",
					"(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitFieldInsn(GETSTATIC, typeName, "unsafe",
					"Lsun/misc/Unsafe;");
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getOffset", "()J", false);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LADD);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(GETSTATIC, typeName, "unsafe",
					"Lsun/misc/Unsafe;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
					"()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe",
					"arrayBaseOffset", "(Ljava/lang/Class;)I", false);
			mv.visitInsn(I2L);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(I2L);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "copyMemory",
					"(Ljava/lang/Object;JLjava/lang/Object;JJ)V", false);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(9, 6);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "readFromByteArray",
					"(J[BII)I", null, null);
			mv.visitCode();
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(I2L);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof",
					"()J", false);
			mv.visitInsn(LCMP);
			Label l0 = new Label();
			mv.visitJumpInsn(IFLE, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof",
					"()J", false);
			mv.visitInsn(L2I);
			mv.visitVarInsn(ISTORE, 5);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			Label l1 = new Label();
			mv.visitJumpInsn(IFNULL, l1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(L2I);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy",
					"(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitFieldInsn(GETSTATIC, typeName, "unsafe",
					"Lsun/misc/Unsafe;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(GETSTATIC, typeName, "unsafe",
					"Lsun/misc/Unsafe;");
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass",
					"()Ljava/lang/Class;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe",
					"arrayBaseOffset", "(Ljava/lang/Class;)I", false);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitInsn(IADD);
			mv.visitInsn(I2L);
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getOffset", "()J", false);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LADD);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(I2L);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "copyMemory",
					"(Ljava/lang/Object;JLjava/lang/Object;JJ)V", false);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(IRETURN);
			mv.visitMaxs(9, 6);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "asByteBuffer",
					"()Ljava/nio/ByteBuffer;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("not off-heap");
			mv.visitMethodInsn(INVOKESPECIAL,
					"java/lang/UnsupportedOperationException", "<init>",
					"(Ljava/lang/String;)V", false);
			mv.visitInsn(ATHROW);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getOffset", "()J", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "sizeof",
					"()J", false);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/UnsafeHelper",
					"bufferFromAddress", "(JJ)Ljava/nio/ByteBuffer;", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "java/nio/ByteOrder",
					"nativeOrder", "()Ljava/nio/ByteOrder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "order",
					"(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;", false);
			mv.visitInsn(POP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(4, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
					"asByteBuffer",
					"(Lcom/ibm/layout/Location;J)Ljava/nio/ByteBuffer;", null,
					null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getData", "()[B", false);
			Label l0 = new Label();
			mv.visitJumpInsn(IFNULL, l0);
			mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("not off-heap");
			mv.visitMethodInsn(INVOKESPECIAL,
					"java/lang/UnsupportedOperationException", "<init>",
					"(Ljava/lang/String;)V", false);
			mv.visitInsn(ATHROW);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location",
					"getOffset", "()J", false);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/UnsafeHelper",
					"bufferFromAddress", "(JJ)Ljava/nio/ByteBuffer;", false);
			mv.visitVarInsn(ASTORE, 3);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESTATIC, "java/nio/ByteOrder",
					"nativeOrder", "()Ljava/nio/ByteOrder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "order",
					"(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;", false);
			mv.visitInsn(POP);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(4, 4);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "unsafeCast",
					"(Ljava/lang/Class;)Lcom/ibm/layout/Layout;",
					"<T::Lcom/ibm/layout/Layout;>(Ljava/lang/Class<TT;>;)TT;",
					null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NoSuchMethodException");
			Label l3 = new Label();
			mv.visitTryCatchBlock(l0, l1, l3, "java/lang/SecurityException");
			Label l4 = new Label();
			mv.visitTryCatchBlock(l0, l1, l4,
					"java/lang/InstantiationException");
			Label l5 = new Label();
			mv.visitTryCatchBlock(l0, l1, l5,
					"java/lang/IllegalAccessException");
			Label l6 = new Label();
			mv.visitTryCatchBlock(l0, l1, l6,
					"java/lang/IllegalArgumentException");
			Label l7 = new Label();
			mv.visitTryCatchBlock(l0, l1, l7,
					"java/lang/reflect/InvocationTargetException");
			mv.visitLabel(l0);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutHelper",
					"getFactory", "()Lcom/ibm/layout/LayoutHelper;", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/LayoutHelper",
					"genLayoutImpl", "(Ljava/lang/Class;)Ljava/lang/Class;",
					false);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
					"getDeclaredConstructor",
					"([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
					false);
			mv.visitVarInsn(ASTORE, 3);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"setAccessible", "(Z)V", false);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;",
					false);
			mv.visitTypeInsn(CHECKCAST, "com/ibm/layout/Layout");
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/Layout",
					"bindLocation", "(Lcom/ibm/layout/Location;)V", true);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitLabel(l1);
			mv.visitInsn(ARETURN);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/NoSuchMethodException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/NoSuchMethodException", "printStackTrace",
					"()V", false);
			Label l8 = new Label();
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/SecurityException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/SecurityException",
					"printStackTrace", "()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l4);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/InstantiationException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/InstantiationException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/IllegalAccessException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/IllegalAccessException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l6);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/IllegalArgumentException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/IllegalArgumentException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l7);
			mv.visitFrame(
					Opcodes.F_SAME1,
					0,
					null,
					1,
					new Object[] { "java/lang/reflect/InvocationTargetException" });
			mv.visitVarInsn(ASTORE, 2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/reflect/InvocationTargetException",
					"printStackTrace", "()V", false);
			mv.visitLabel(l8);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 5);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(
					ACC_PUBLIC + ACC_FINAL,
					"unsafeCast",
					"(Ljava/lang/Class;J)Lcom/ibm/layout/Array1D;",
					"<T::Lcom/ibm/layout/Layout;>(Ljava/lang/Class<TT;>;J)Lcom/ibm/layout/Array1D<TT;>;",
					null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NoSuchMethodException");
			Label l3 = new Label();
			mv.visitTryCatchBlock(l0, l1, l3, "java/lang/SecurityException");
			Label l4 = new Label();
			mv.visitTryCatchBlock(l0, l1, l4,
					"java/lang/InstantiationException");
			Label l5 = new Label();
			mv.visitTryCatchBlock(l0, l1, l5,
					"java/lang/IllegalAccessException");
			Label l6 = new Label();
			mv.visitTryCatchBlock(l0, l1, l6,
					"java/lang/IllegalArgumentException");
			Label l7 = new Label();
			mv.visitTryCatchBlock(l0, l1, l7,
					"java/lang/reflect/InvocationTargetException");
			mv.visitLabel(l0);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutHelper",
					"getFactory", "()Lcom/ibm/layout/LayoutHelper;", false);
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/LayoutHelper",
					"genLayoutImpl", "(Ljava/lang/Class;)Ljava/lang/Class;",
					false);
			mv.visitVarInsn(ASTORE, 5);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
					"getDeclaredConstructor",
					"([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
					false);
			mv.visitVarInsn(ASTORE, 6);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"setAccessible", "(Z)V", false);
			mv.visitVarInsn(ALOAD, 6);
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;",
					false);
			mv.visitTypeInsn(CHECKCAST, "com/ibm/layout/Layout");
			mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/Layout",
					"sizeof", "()J", true);
			mv.visitVarInsn(LSTORE, 7);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/LayoutHelper",
					"genArray1DImpl", "(Ljava/lang/Class;)Ljava/lang/Class;",
					false);
			mv.visitVarInsn(ASTORE, 9);
			mv.visitVarInsn(ALOAD, 9);
			mv.visitInsn(ICONST_2);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE",
					"Ljava/lang/Class;");
			mv.visitInsn(AASTORE);
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE",
					"Ljava/lang/Class;");
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
					"getDeclaredConstructor",
					"([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
					false);
			mv.visitVarInsn(ASTORE, 10);
			mv.visitVarInsn(ALOAD, 10);
			mv.visitInsn(ICONST_1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"setAccessible", "(Z)V", false);
			mv.visitVarInsn(ALOAD, 10);
			mv.visitInsn(ICONST_2);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(LLOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf",
					"(J)Ljava/lang/Long;", false);
			mv.visitInsn(AASTORE);
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_1);
			mv.visitVarInsn(LLOAD, 7);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf",
					"(J)Ljava/lang/Long;", false);
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Constructor",
					"newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;",
					false);
			mv.visitTypeInsn(CHECKCAST, "com/ibm/layout/Array1D");
			mv.visitVarInsn(ASTORE, 11);
			mv.visitVarInsn(ALOAD, 11);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, typeName, "location",
					"Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/Array1D",
					"bindLocation", "(Lcom/ibm/layout/Location;)V", true);
			mv.visitVarInsn(ALOAD, 11);
			mv.visitLabel(l1);
			mv.visitInsn(ARETURN);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/NoSuchMethodException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/NoSuchMethodException", "printStackTrace",
					"()V", false);
			Label l8 = new Label();
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/SecurityException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/SecurityException",
					"printStackTrace", "()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l4);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/InstantiationException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/InstantiationException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/IllegalAccessException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/IllegalAccessException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l6);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1,
					new Object[] { "java/lang/IllegalArgumentException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/IllegalArgumentException", "printStackTrace",
					"()V", false);
			mv.visitJumpInsn(GOTO, l8);
			mv.visitLabel(l7);
			mv.visitFrame(
					Opcodes.F_SAME1,
					0,
					null,
					1,
					new Object[] { "java/lang/reflect/InvocationTargetException" });
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					"java/lang/reflect/InvocationTargetException",
					"printStackTrace", "()V", false);
			mv.visitLabel(l8);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(6, 12);
			mv.visitEnd();
		}
	}
}
