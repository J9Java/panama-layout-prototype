/*******************************************************************************
 *  Copyright (c) 2014, 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class GenLayout implements Opcodes {
	private final Class<? extends Layout> clazz;
	private final String interfaceClassName;
	private final String implClassName;

	/**
	 * Instantiate GenLayout
	 *  
	 * @param elementInterfaceClass, element type for Array
	 */
	public GenLayout(Class<? extends Layout> clazz) {
		this.clazz = clazz;
		this.interfaceClassName = ImplHelper.getInterfaceClassName(clazz);
		this.implClassName = ImplHelper.getImplClassName(clazz);
	}

	private long getClassSize(ImplHelper.FieldDesc[] fields) {
		long classSize = 0;
		
		for (ImplHelper.FieldDesc f : fields) {
			classSize += f.size;
		}
		
		return classSize;
	}
	
	/**
	 * Generate bytecodes for runtime class
	 * 
	 * @return byte array containing bytecodes for runtime class
	 * @throws Exception
	 */
	public byte[] genBytecode() throws Exception {

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		FieldVisitor fv = null;
		MethodVisitor mv = null;
		ImplHelper.FieldDesc[] fieldDesc = ImplHelper.getFieldDesc(clazz);
		boolean isVariableLengthLayout = false;
		final boolean itf = false;
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "sizeof", "()J", null, null);
			mv.visitCode();
			mv.visitLdcInsn(new Long(getClassSize(fieldDesc)));
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		
		
		cw.visit(V1_8, ACC_FINAL + ACC_SUPER, implClassName, null, "java/lang/Object", new String[] {interfaceClassName});

		{
			mv = cw.visitMethod(0, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		// getters
		for (int i = 0; i < fieldDesc.length; i++) {
			if (!ImplHelper.isPointerType(fieldDesc[i].rawType)) {
				if (ImplHelper.isTypePrimitive(fieldDesc[i].sig)) {
					mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, null, null);
					mv.visitCode();
					mv.visitFieldInsn(GETSTATIC, implClassName, "unsafe", "Lsun/misc/Unsafe;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
					mv.visitLdcInsn(fieldDesc[i].offset);
					mv.visitInsn(LADD);
					
					mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe",
							"get" + ImplHelper.fieldSig2MethodType(fieldDesc[i].sig), "(Ljava/lang/Object;J)"
									+ fieldDesc[i].sig, itf);
					if (fieldDesc[i].sig == "D") {
						mv.visitInsn(DRETURN);
					} else if (fieldDesc[i].sig == "J") {
						mv.visitInsn(LRETURN);
					} else if (fieldDesc[i].sig == "F") {
						mv.visitInsn(FRETURN);
					} else {
						mv.visitInsn(IRETURN);
					}
					mv.visitMaxs(6, 1);
					mv.visitEnd();
				} else if (fieldDesc[i].repeatCountMember != null) {
					{
						isVariableLengthLayout = true;
						ImplHelper.FieldDesc repeatCountField = ImplHelper.findField(fieldDesc[i].repeatCountMember, fieldDesc);
						mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, "()" + fieldDesc[i].elementImpl, null);
						mv.visitCode();
						if ("" != fieldDesc[i].elementImpl) {
							mv.visitTypeInsn(NEW, fieldDesc[i].elementImpl);
							mv.visitInsn(DUP);
							mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].elementImpl, "<init>", "()V", false);
							mv.visitVarInsn(ASTORE, 1);
							mv.visitTypeInsn(NEW, fieldDesc[i].impl);
							mv.visitInsn(DUP);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].repeatCountMember, "()" + repeatCountField.sig, false);
							if (!repeatCountField.sig.equals("J")) {
								mv.visitInsn(I2L);
							}
							mv.visitVarInsn(ALOAD, 1);
							mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].elementImpl, "sizeof", "()J", false);
							mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(JJ)V", false);
							mv.visitVarInsn(ASTORE, 2);
							mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
							mv.visitInsn(DUP);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
							mv.visitLdcInsn(fieldDesc[i].offset);
							mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", false);
							mv.visitVarInsn(ASTORE, 3);
							mv.visitVarInsn(ALOAD, 2);
							mv.visitVarInsn(ALOAD, 3);
							mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/VLArray", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
							mv.visitVarInsn(ALOAD, 2);
							mv.visitInsn(ARETURN);
							mv.visitMaxs(6, 4);
							mv.visitEnd();
						} else {
							throw new RuntimeException("primitive VLAs are unsupported");
						}
						{
							mv = cw.visitMethod(ACC_PUBLIC, "bindLocation", "(Lcom/ibm/layout/Location;" + repeatCountField.sig + ")V", null, new String[] { "java/lang/UnsupportedOperationException" });
							mv.visitCode();
							mv.visitVarInsn(ALOAD, 1);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, "sizeof", "()J", false);
							mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "checkDataFits", "(J)Z", false);
							Label l0 = new Label();
							mv.visitJumpInsn(IFEQ, l0);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitVarInsn(ALOAD, 1);
							mv.visitFieldInsn(PUTFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
							mv.visitLabel(l0);
							mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
							mv.visitVarInsn(ALOAD, 0);
							if (repeatCountField.sig.equals("J")) {
								mv.visitVarInsn(LLOAD, 2);
							} else {
								mv.visitVarInsn(ILOAD, 2);
							}
							mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].repeatCountMember, "(" + repeatCountField.sig + ")V", false);
							mv.visitInsn(RETURN);
							mv.visitMaxs(3, 3);
							mv.visitEnd();
						}
					}
				} else if (fieldDesc[i].dims == null) {
					/* nested field */
					mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, null, null);
					mv.visitCode();
					mv.visitTypeInsn(NEW, ImplHelper.getImplClassName(fieldDesc[i].rawType));
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, ImplHelper.getImplClassName(fieldDesc[i].rawType), "<init>", "()V", itf);
					mv.visitVarInsn(ASTORE, 1);
					mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, this.implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitLdcInsn(fieldDesc[i].offset);
					mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
					mv.visitVarInsn(ASTORE, 2);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, ImplHelper.getImplClassName(fieldDesc[i].rawType), "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitInsn(ARETURN);
					mv.visitMaxs(5, 3);
					mv.visitEnd();
				} else if (fieldDesc[i].dims.length == 1) {
					mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, "()"
							+ fieldDesc[i].sigGeneric, null);
					mv.visitCode();
					if ("" != fieldDesc[i].elementImpl) {
						mv.visitTypeInsn(NEW, fieldDesc[i].elementImpl);
						mv.visitInsn(DUP);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].elementImpl, "<init>", "()V", itf);
						mv.visitVarInsn(ASTORE, 1);
						mv.visitTypeInsn(NEW, fieldDesc[i].impl);
						mv.visitInsn(DUP);
						mv.visitLdcInsn(fieldDesc[i].dims[0]);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].elementImpl, "sizeof", "()J", itf);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(JJ)V", itf);
						mv.visitVarInsn(ASTORE, 2);
						mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, this.implClassName, "location", "Lcom/ibm/layout/Location;");
						mv.visitLdcInsn(fieldDesc[i].offset);
						mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
						mv.visitVarInsn(ASTORE, 3);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitVarInsn(ALOAD, 3);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].impl, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(6, 4);
						mv.visitEnd();
					} else {
						mv.visitTypeInsn(NEW, fieldDesc[i].impl);
						mv.visitInsn(DUP);
						mv.visitLdcInsn(fieldDesc[i].dims[0]);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(J)V", itf);
						mv.visitVarInsn(ASTORE, 1);
						mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, this.implClassName, "location", "Lcom/ibm/layout/Location;");
						mv.visitLdcInsn(fieldDesc[i].offset);
						mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
						mv.visitVarInsn(ASTORE, 2);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].impl, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(5, 3);
						mv.visitEnd();
					}
				} else if (fieldDesc[i].dims.length == 2) {
					if ("" != fieldDesc[i].elementImpl) {
						mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, "()"
								+ fieldDesc[i].sigGeneric, null);
						mv.visitCode();
						mv.visitTypeInsn(NEW, fieldDesc[i].elementImpl);
						mv.visitInsn(DUP);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].elementImpl, "<init>", "()V", itf);
						mv.visitVarInsn(ASTORE, 1);
						mv.visitTypeInsn(NEW, fieldDesc[i].impl);
						mv.visitInsn(DUP);
						mv.visitLdcInsn(fieldDesc[i].dims[0]);
						mv.visitLdcInsn(fieldDesc[i].dims[1]);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].elementImpl, "sizeof", "()J", itf);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(JJJ)V", itf);
						mv.visitVarInsn(ASTORE, 2);
						mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, this.implClassName, "location", "Lcom/ibm/layout/Location;");
						mv.visitLdcInsn(fieldDesc[i].offset);
						mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
						mv.visitVarInsn(ASTORE, 3);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitVarInsn(ALOAD, 3);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].impl, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(8, 4);
						mv.visitEnd();
					} else {
						mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()"
								+ fieldDesc[i].sig, "()"
								+ fieldDesc[i].sigGeneric, null);
						mv.visitCode();
						mv.visitTypeInsn(NEW, fieldDesc[i].impl);
						mv.visitInsn(DUP);
						mv.visitLdcInsn(fieldDesc[i].dims[0]);
						mv.visitLdcInsn(fieldDesc[i].dims[1]);
						mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl,
								"<init>", "(JJ)V", itf);
						mv.visitVarInsn(ASTORE, 1);
						mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
						mv.visitInsn(DUP);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitFieldInsn(GETFIELD, this.implClassName,
								"location", "Lcom/ibm/layout/Location;");
						mv.visitLdcInsn(fieldDesc[i].offset);
						mv.visitMethodInsn(INVOKESPECIAL,
								"com/ibm/layout/Location", "<init>",
								"(Lcom/ibm/layout/Location;J)V", itf);
						mv.visitVarInsn(ASTORE, 2);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].impl,
								"bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
						mv.visitVarInsn(ALOAD, 1);
						mv.visitInsn(ARETURN);
						mv.visitMaxs(6, 3);
						mv.visitEnd();
					}
				}
			}
		}

		// setters
		for (int i = 0; i < fieldDesc.length; i++) {
			if (ImplHelper.isTypePrimitive(fieldDesc[i].sig)) {
				mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "(" + fieldDesc[i].sig + ")V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, implClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitLdcInsn(fieldDesc[i].offset);
				mv.visitInsn(LADD);
				if (fieldDesc[i].sig == "D") {
					mv.visitVarInsn(DLOAD, 1);
				} else if (fieldDesc[i].sig == "J") {
					mv.visitVarInsn(LLOAD, 1);
				} else if (fieldDesc[i].sig == "F") {
					mv.visitVarInsn(FLOAD, 1);
				} else {
					mv.visitVarInsn(ILOAD, 1);
				}
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe",
						"put" + ImplHelper.fieldSig2MethodType(fieldDesc[i].sig), "(Ljava/lang/Object;J"
								+ fieldDesc[i].sig + ")V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(6, 2);
				mv.visitEnd();
			}
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "copyFrom", "(L" + implClassName + ";)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, implClassName, "copyFrom", "(Lcom/ibm/layout/Layout;)V", itf);
			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("(");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			for (int i = 0; i < fieldDesc.length; i++) {
				if (ImplHelper.isTypePrimitive(fieldDesc[i].sig)) {
					mv.visitLdcInsn(fieldDesc[i].name + ": ");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].name, "()" + fieldDesc[i].sig, false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "("+ ImplHelper.getByteCodeSig(fieldDesc[i].sig) +")Ljava/lang/StringBuilder;", false);
					if (i < (fieldDesc.length - 1)) {
						mv.visitLdcInsn(", ");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
					}
				} else {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].name, "()" + fieldDesc[i].sig, false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
				}
			}
			mv.visitLdcInsn(")");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		
		ImplHelper.genLayoutTypeImpl(cw, mv, fv, implClassName, isVariableLengthLayout);
		ImplHelper.getLayoutImpl(cw, mv, implClassName);
		
		cw.visitEnd();

		return cw.toByteArray();
	}
}
