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
import org.objectweb.asm.Type;

import com.ibm.layout.ImplHelper.FieldDesc;

class GenLayout implements Opcodes {
	private final Class<? extends Layout> clazz;
	private final String interfaceClassName;
	private final String implClassName;
	private long wayPointCounter = 0;
	private long startWayPointCounter = 0;
	private ClassWriter eaCW = new ClassWriter(0);
	private MethodVisitor eaMV;
	private boolean createBody = true;

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

	public byte[] genEABytecode() throws Exception {
		byte[] bytecodes = null;
		if (createBody) {
			eaCW.visitEnd();
			bytecodes = eaCW.toByteArray();
		}
		return bytecodes;
	}
	
	private long getClassSize(ImplHelper.FieldDesc[] fields) {
		long classSize = 0;
		
		for (ImplHelper.FieldDesc f : fields) {
			if (f.size == 0) {
				classSize = 0;
			} else {
				classSize += f.size;
			}
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
		int isVariableLengthLayout = 0;
		final boolean itf = false;
		String superImpl = "java/lang/Object";
		
		if (null == fieldDesc) {
			createBody = false;
			/* find layout superType */
			for (Class<?> c : clazz.getInterfaces()) {
				if (Layout.class.isAssignableFrom(c)) {
					superImpl = ImplHelper.getImplClassName(c);
				}
			}
		}
		
		if (createBody) {
			eaCW.visit(52, ACC_SUPER, implClassName + "$EffectiveAddressImpl", null, "java/lang/Object", new String[] { interfaceClassName + "$EffectiveAddress" });
			
			eaCW.visitInnerClass(interfaceClassName + "$EffectiveAddress" , interfaceClassName, "EffectiveAddress", ACC_PUBLIC + ACC_STATIC + ACC_ABSTRACT + ACC_INTERFACE);

			eaCW.visitInnerClass(implClassName + "$EffectiveAddressImpl", implClassName, "EffectiveAddressImpl", 0);
			
			{
				fv = eaCW.visitField(ACC_FINAL + ACC_SYNTHETIC, "this$0", "L" + implClassName + ";", null, null);
				fv.visitEnd();
			}
			
			{
				eaMV = eaCW.visitMethod(0, "<init>", "(L" + implClassName + ";)V", null, null);
				eaMV.visitCode();
				eaMV.visitVarInsn(ALOAD, 0);
				eaMV.visitVarInsn(ALOAD, 1);
				eaMV.visitFieldInsn(PUTFIELD, implClassName + "$EffectiveAddressImpl", "this$0", "L" + implClassName + ";");
				eaMV.visitVarInsn(ALOAD, 0);
				eaMV.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
				eaMV.visitInsn(RETURN);
				eaMV.visitMaxs(2, 2);
				eaMV.visitEnd();
			}
		}
		
		
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, implClassName, null, superImpl, new String[] {interfaceClassName});
		if (createBody) {
			cw.visitInnerClass(implClassName + "$EffectiveAddressImpl", implClassName, "EffectiveAddressImpl", 0);
		}
		{
			mv = cw.visitMethod(0, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, superImpl, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		if (!createBody) {
			{
				fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "unsafe", "Lsun/misc/Unsafe;", null, null);
				fv.visitEnd();
			}
			cw.visitEnd();
			return cw.toByteArray();
		}
		
		for (int i = 0; i < fieldDesc.length; i++) {
			
			startWayPointCounter = wayPointCounter;
			if (!ImplHelper.isPointerType(fieldDesc[i].rawType)) {
				if (ImplHelper.isTypePrimitive(fieldDesc[i].sig)) {
					// getter
					mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "()" + fieldDesc[i].sig, null, null);
					mv.visitCode();
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
					if (wayPointCounter > 0) {
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
						mv.visitInsn(LADD);
					}
					mv.visitLdcInsn(fieldDesc[i].offset);
					mv.visitInsn(LADD);
					
					mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/gen/UnsafeImplHelper",
							"load" + fieldDesc[i].endian + ImplHelper.fieldSig2MethodType(fieldDesc[i].sig), "(Ljava/lang/Object;J)"
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
					mv.visitMaxs(5, 1);
					mv.visitEnd();
					
					//setter
					mv = cw.visitMethod(ACC_PUBLIC, fieldDesc[i].name, "(" + fieldDesc[i].sig + ")V", null, null);
					mv.visitCode();
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", itf);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
					mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", itf);
					mv.visitLdcInsn(fieldDesc[i].offset);
					if (wayPointCounter > 0) {
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
						mv.visitInsn(LADD);
					}
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
					mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/gen/UnsafeImplHelper",
							"store" + fieldDesc[i].endian + ImplHelper.fieldSig2MethodType(fieldDesc[i].sig), "(Ljava/lang/Object;J"
									+ fieldDesc[i].sig + ")V", itf);
					mv.visitInsn(RETURN);
					mv.visitMaxs(5, 2);
					mv.visitEnd();
				
				} else if (fieldDesc[i].repeatCountMember != null) {
					{
						isVariableLengthLayout++;
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
							if (fieldDesc[i].priviliedgedUserClass) {
								FieldDesc header = ImplHelper.findField(fieldDesc[i].repeatCountMember, fieldDesc);
								boolean primitivePointer = ImplHelper.isTypePrimitive(header.sig);
								
								mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, "EA", "()L" + interfaceClassName + "$EffectiveAddress;", false);
								
								if (primitivePointer) {
									mv.visitMethodInsn(INVOKEINTERFACE, interfaceClassName + "$EffectiveAddress", fieldDesc[i].repeatCountMember, "()L" + header.pointerSig + ";", true);
								} else {
									mv.visitMethodInsn(INVOKEINTERFACE, interfaceClassName + "$EffectiveAddress", fieldDesc[i].repeatCountMember, "()Lcom/ibm/layout/Pointer;", true);
								}
								if (fieldDesc[i].isVarSized) {
									mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(Lcom/ibm/layout/PointerType;)V", false);
								} else {
									mv.visitVarInsn(ALOAD, 1);
									mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].elementImpl, "sizeof", "()J", false);
									mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(Lcom/ibm/layout/PointerType;J)V", false);
								}
							} else {
								mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].repeatCountMember, "()" + repeatCountField.sig, false);
								if (!repeatCountField.sig.equals("J")) {
									mv.visitInsn(I2L);
								}
								if (fieldDesc[i].isVarSized) {
									mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(J)V", false);
								}  else {
									mv.visitVarInsn(ALOAD, 1);
									mv.visitMethodInsn(INVOKEVIRTUAL, fieldDesc[i].elementImpl, "sizeof", "()J", false);
									mv.visitMethodInsn(INVOKESPECIAL, fieldDesc[i].impl, "<init>", "(JJ)V", false);
								}
							}
							mv.visitVarInsn(ASTORE, 2);
							mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
							mv.visitInsn(DUP);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
							mv.visitLdcInsn(fieldDesc[i].offset);
							if (wayPointCounter > 0) {
								mv.visitVarInsn(ALOAD, 0);
								mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
								mv.visitInsn(LADD);
							}
							mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", false);
							mv.visitVarInsn(ASTORE, 3);
							mv.visitVarInsn(ALOAD, 2);
							mv.visitVarInsn(ALOAD, 3);
							mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/VLArray", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
							mv.visitVarInsn(ALOAD, 2);
							mv.visitInsn(ARETURN);
							if (fieldDesc[i].priviliedgedUserClass) {
								mv.visitMaxs(7, 4);
							} else {
								mv.visitMaxs(6, 4);
							}
							
							mv.visitEnd();
						} else {
							throw new RuntimeException("primitive VLAs are unsupported");
						}
						if ((isVariableLengthLayout == 1) && ((fieldDesc.length - 1) == i)) {
							mv = cw.visitMethod(ACC_PUBLIC, "bindLocation", "(Lcom/ibm/layout/Location;" + repeatCountField.sig + ")V", null, new String[] { "java/lang/UnsupportedOperationException" });
							mv.visitCode();
							mv.visitVarInsn(ALOAD, 0);
							mv.visitVarInsn(ALOAD, 1);
							mv.visitFieldInsn(PUTFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
							mv.visitVarInsn(ALOAD, 0);
							if (repeatCountField.sig.equals("J")) {
								mv.visitVarInsn(LLOAD, 2);
							} else {
								mv.visitVarInsn(ILOAD, 2);
							}
							mv.visitMethodInsn(INVOKEVIRTUAL, implClassName, fieldDesc[i].repeatCountMember, "(" + repeatCountField.sig + ")V", false);
							mv.visitInsn(RETURN);
							mv.visitMaxs(2, 3);
							mv.visitEnd();
						}
						if (wayPointCounter == 0) {
							mv = cw.visitMethod(ACC_PRIVATE, "waypoint" + wayPointCounter++, "()J", null, null);
							mv.visitCode();
							mv.visitLdcInsn(fieldDesc[i].offset);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKEVIRTUAL, this.implClassName, fieldDesc[i].name, "()" + fieldDesc[i].sig, false);
							mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/VLArray", "sizeof", "()J", true);
							mv.visitInsn(LADD);
							mv.visitInsn(LRETURN);
							mv.visitMaxs(4, 1);
							mv.visitEnd();
						} else {
							long oldWayPoint = wayPointCounter - 1;
							mv = cw.visitMethod(ACC_PRIVATE, "waypoint" + wayPointCounter++, "()J", null, null);
							mv.visitCode();
							mv.visitLdcInsn(fieldDesc[i].offset);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKEVIRTUAL, this.implClassName, fieldDesc[i].name, "()" + fieldDesc[i].sig, false);
							mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/VLArray", "sizeof", "()J", true);
							mv.visitInsn(LADD);
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, this.implClassName, "waypoint" + oldWayPoint, "()J", false);
							mv.visitInsn(LADD);
							mv.visitInsn(LRETURN);
							mv.visitMaxs(4, 1);
							mv.visitEnd();
						}		
						{
							//access for EA class
							mv = cw.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "access$" + (wayPointCounter), "(L" + this.implClassName + ";)J", null, null);
							mv.visitCode();
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, this.implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
							mv.visitInsn(LRETURN);
							mv.visitMaxs(2, 1);
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
					if (wayPointCounter > 0) {
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
						mv.visitInsn(LADD);
					}
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
						if (wayPointCounter > 0) {
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
							mv.visitInsn(LADD);
						}
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
						if (wayPointCounter > 0) {
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
							mv.visitInsn(LADD);
						}
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
						if (wayPointCounter > 0) {
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
							mv.visitInsn(LADD);
						}
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
						if (wayPointCounter > 0) {
							mv.visitVarInsn(ALOAD, 0);
							mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
							mv.visitInsn(LADD);
						}
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
			{
				boolean primitivePointer = ImplHelper.isTypePrimitive(fieldDesc[i].sig);
				eaMV = eaCW.visitMethod(ACC_PUBLIC, fieldDesc[i].name, 
							primitivePointer ? "()L" + fieldDesc[i].pointerSig + ";" : "()Lcom/ibm/layout/Pointer;", 
							primitivePointer ? null : "()L" + fieldDesc[i].pointerSig + ";", 
							null);
				eaMV.visitCode();
				if (primitivePointer) {
					eaMV.visitFieldInsn(GETSTATIC,"java/lang/" + ImplHelper.fieldSig2MethodTypeLongForm(fieldDesc[i].sig), "TYPE", "Ljava/lang/Class;");
					eaMV.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutType", "getPrimPointer", "(Ljava/lang/Class;)Lcom/ibm/layout/LayoutType;", true);
					eaMV.visitTypeInsn(CHECKCAST, fieldDesc[i].pointerSig);
				} else {
					if (null != fieldDesc[i].element) {
						eaMV.visitLdcInsn(Type.getType("L" + fieldDesc[i].element.replace(".", "/") + ";"));
					} else {
						eaMV.visitLdcInsn(Type.getType(fieldDesc[i].sig));
					}
					eaMV.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutType", "getPointer", "(Ljava/lang/Class;)Lcom/ibm/layout/Pointer;", true);
				}
				eaMV.visitVarInsn(ASTORE, 1);
				eaMV.visitVarInsn(ALOAD, 1);
				eaMV.visitTypeInsn(NEW, "com/ibm/layout/Location");
				eaMV.visitInsn(DUP);
				eaMV.visitVarInsn(ALOAD, 0);
				eaMV.visitFieldInsn(GETFIELD, implClassName + "$EffectiveAddressImpl", "this$0", "L" + implClassName + ";");
				eaMV.visitMethodInsn(INVOKESTATIC, implClassName, "access$0", "(L" + implClassName + ";)Lcom/ibm/layout/Location;", false);
				if (startWayPointCounter > 0) {
					eaMV.visitVarInsn(ALOAD, 0);
					eaMV.visitFieldInsn(GETFIELD, implClassName + "$EffectiveAddressImpl", "this$0", "L" + implClassName + ";");
					eaMV.visitMethodInsn(INVOKESTATIC, implClassName, "access$" + (startWayPointCounter), "(L" + implClassName + ";)J", false);
				}
				eaMV.visitLdcInsn(new Long(fieldDesc[i].offset));
				if (startWayPointCounter > 0) {
					eaMV.visitInsn(LADD);
				}
				eaMV.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", false);
				if (primitivePointer) {
					eaMV.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/" + ImplHelper.fieldSig2MethodType(fieldDesc[i].sig) + "Pointer", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
				} else {
					eaMV.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/Pointer", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
				}
				eaMV.visitVarInsn(ALOAD, 1);
				eaMV.visitInsn(ARETURN);
				if (startWayPointCounter > 0) {
					eaMV.visitMaxs(8, 2);
				} else {
					eaMV.visitMaxs(6, 2);
				}
				eaMV.visitEnd();
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

		if (isVariableLengthLayout > 0) {
			mv = cw.visitMethod(ACC_PUBLIC, "sizeof", "()J", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, implClassName, "waypoint" + (wayPointCounter - 1), "()J", false);
			mv.visitLdcInsn(new Long(getClassSize(fieldDesc)));
			mv.visitInsn(LADD);
			mv.visitInsn(LRETURN);
			mv.visitMaxs(4, 1);
			mv.visitEnd();
		} else {
			mv = cw.visitMethod(ACC_PUBLIC, "sizeof", "()J", null, null);
			mv.visitCode();
			mv.visitLdcInsn(new Long(getClassSize(fieldDesc)));
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "EA", "()L" + interfaceClassName + "$EffectiveAddress;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, implClassName + "$EffectiveAddressImpl");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, implClassName + "$EffectiveAddressImpl", "<init>", "(L" + implClassName + ";)V", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "access$0", "(L" + implClassName + ";)Lcom/ibm/layout/Location;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, implClassName, "getLocation", "()Lcom/ibm/layout/Location;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PRIVATE + ACC_FINAL, "getLocation", "()Lcom/ibm/layout/Location;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, implClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		if (isVariableLengthLayout == 1) {
			if (fieldDesc[fieldDesc.length - 1].repeatCountMember == null) {
				//not fixed header var tail
				isVariableLengthLayout++;
			}
		}
		
		ImplHelper.genLayoutTypeImpl(cw, mv, fv, implClassName, isVariableLengthLayout, false);
		ImplHelper.getLayoutImpl(cw, mv, implClassName);
		
		cw.visitEnd();

		return cw.toByteArray();
	}
}
