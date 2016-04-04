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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class GenPrimArray1D implements Opcodes {
	final private boolean debug = false;
	/* Names are in class file format: delimiter is /, no L; decoration
	 * e.g. "com/ibm/layout/Array1D"
	 */
	final private String arrayImplClassName;
	final private String arrayInterfaceClassName;
	final private Class<?> elementInterfaceClass;
	
	/**
	 * Instantiate GenPrimArray1D for user defined array class
	 * 
	 * @param elementInterfaceClass, element type for Array
	 * @param userDefinedArrayClass, user defined class must be subclass of LayoutType 
	 */
	public <AE extends LayoutType>
	GenPrimArray1D(Class<?> elementInterfaceClass, Class<AE> userDefinedArrayClass) {
		arrayImplClassName = ImplHelper.getImplClassName(userDefinedArrayClass);
		arrayInterfaceClassName = userDefinedArrayClass.getName().replace('.', '/');
		this.elementInterfaceClass = elementInterfaceClass;
		dbgPrintNames();
	}
	
	private void dbgPrintNames() {
		if (debug) {
			System.out.println("arrayImplClassName = " + arrayImplClassName);
			System.out.println("arrayInterfaceClassName = " + arrayInterfaceClassName);
		}
	}

	/**
	 * Generate bytecodes for runtime class
	 * 
	 * @return byte array containing bytecodes for runtime class
	 * @throws Exception
	 */
	public byte[] genBytecode() throws Exception {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		FieldVisitor fv;
		MethodVisitor mv;
		final boolean itf = false;
		
		cw.visit(V1_8, ACC_FINAL + ACC_SUPER, arrayImplClassName, null, "java/lang/Object", new String[]{arrayInterfaceClassName});
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "length", "J", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(J)V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitFieldInsn(PUTFIELD, arrayImplClassName, "length", "J");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "getLength", "()J", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, arrayImplClassName, "length", "J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		if (byte.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)B", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitInsn(LCONST_1);
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getByte", "(Ljava/lang/Object;J)B", itf);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JB)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitInsn(LCONST_1);
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(ILOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putByte", "(Ljava/lang/Object;JB)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range",  "(JJ)L" + arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitInsn(LCONST_1);
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/ByteArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (boolean.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)Z", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getBoolean", "(Ljava/lang/Object;J)Z", itf);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(6, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JZ)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitInsn(LADD);
				mv.visitVarInsn(ILOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putBoolean", "(Ljava/lang/Object;JZ)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(6, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/BooleanArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (short.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)S", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName , "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getShort", "(Ljava/lang/Object;J)S", itf);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JS)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(ILOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putShort", "(Ljava/lang/Object;JS)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/ShortArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, arrayImplClassName, "range",
						"(JJ)L" + arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (char.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)C", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, arrayImplClassName, "unsafe",
						"Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getChar",
						"(Ljava/lang/Object;J)C", itf);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JC)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(ILOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putChar", "(Ljava/lang/Object;JC)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(2L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/CharArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (int.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)I", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getInt", "(Ljava/lang/Object;J)I", itf);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JI)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(ILOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putInt", "(Ljava/lang/Object;JI)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/IntArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (long.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)J", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getLong", "(Ljava/lang/Object;J)J", itf);
				mv.visitInsn(LRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JJ)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putLong", "(Ljava/lang/Object;JJ)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 5);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/LongArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (float.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)F", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, arrayImplClassName, "unsafe",
						"Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getFloat", "(Ljava/lang/Object;J)F", itf);
				mv.visitInsn(FRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JF)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(FLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putFloat", "(Ljava/lang/Object;JF)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 4);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(4L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "range", "(JJ)Lcom/ibm/layout/FloatArray1D;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else if (double.class == elementInterfaceClass) {
			{
				mv = cw.visitMethod(ACC_PUBLIC, "at", "(J)D", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getDouble", "(Ljava/lang/Object;J)D", itf);
				mv.visitInsn(DRETURN);
				mv.visitMaxs(8, 3);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "put", "(JD)V", null, null);
				mv.visitCode();
				mv.visitFieldInsn(GETSTATIC, this.arrayImplClassName, "unsafe", "Lsun/misc/Unsafe;");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitInsn(LADD);
				mv.visitVarInsn(DLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putDouble", "(Ljava/lang/Object;JD)V", itf);
				mv.visitInsn(RETURN);
				mv.visitMaxs(8, 5);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC, "range", "(JJ)L" + this.arrayInterfaceClassName + ";", null, null);
				mv.visitCode();
				mv.visitTypeInsn(NEW, this.arrayImplClassName);
				mv.visitInsn(DUP);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKESPECIAL, this.arrayImplClassName, "<init>", "(J)V", itf);
				mv.visitVarInsn(ASTORE, 5);
				mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, this.arrayImplClassName, "location", "Lcom/ibm/layout/Location;");
				mv.visitVarInsn(LLOAD, 1);
				mv.visitLdcInsn(new Long(8L));
				mv.visitInsn(LMUL);
				mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", itf);
				mv.visitVarInsn(ASTORE, 6);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitVarInsn(ALOAD, 6);
				mv.visitMethodInsn(INVOKEVIRTUAL, this.arrayImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", itf);
				mv.visitVarInsn(ALOAD, 5);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(7, 7);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
						"range", "(JJ)Lcom/ibm/layout/DoubleArray1D;", null,
						null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(LLOAD, 1);
				mv.visitVarInsn(LLOAD, 3);
				mv.visitMethodInsn(INVOKEVIRTUAL, arrayImplClassName, "range",
						"(JJ)L" + arrayInterfaceClassName + ";", itf);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(5, 5);
				mv.visitEnd();
			}
		} else {
			throw new Exception();
		}
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "sizeof", "()J", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, arrayImplClassName, "length", "J");
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		
		ImplHelper.genLayoutTypeImpl(cw, mv, fv, arrayImplClassName, false);
		cw.visitEnd();

		return cw.toByteArray();
	}
}
