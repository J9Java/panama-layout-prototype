package com.ibm.layout;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;


public class GenPointer {

	final private String elementInterfaceClassName;
	final private String elementImplClassName;
	final private String pointerImplClassName;
	final private String pointerInterfaceClassName;
	final private String pointerInterfaceClassSig; /* Signature of the array interface class, if it is generic */
	
	public GenPointer(Class<? extends Layout> elementInterfaceClass) {
		this(elementInterfaceClass, null);
	}
	
	public <E extends Layout, AE extends Pointer<E>>
	GenPointer(Class<E> elementInterfaceClass, Class<AE> userDefinedArrayClass) {
		elementInterfaceClassName = ImplHelper.getInterfaceClassName(elementInterfaceClass);
		elementImplClassName = ImplHelper.getImplClassName(elementInterfaceClass);
		
		if (null == userDefinedArrayClass) {
			pointerImplClassName = ImplHelper.getPointerClassImplName(elementInterfaceClass);
			pointerInterfaceClassName = "com/ibm/layout/Pointer";
			pointerInterfaceClassSig = "L" + pointerInterfaceClassName + "<L" + elementInterfaceClassName + ";>;";
		} else {
			pointerImplClassName = ImplHelper.getImplClassName(userDefinedArrayClass);
			pointerInterfaceClassName = userDefinedArrayClass.getName().replace('.', '/');
			pointerInterfaceClassSig = null;
		}
	}


	public byte[] genBytecode() {
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		FieldVisitor fv = null;

		cw.visit(52, ACC_FINAL + ACC_SUPER, pointerImplClassName, pointerInterfaceClassSig, "java/lang/Object", new String[] {pointerInterfaceClassName});
		
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "sizeof", "()J", null, null);
			mv.visitCode();
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/gen/UnsafeImplHelper", "getUnsafe", "()Lsun/misc/Unsafe;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "addressSize", "()I", false);
			mv.visitInsn(I2L);
			mv.visitInsn(LRETURN);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}	
		{
			mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Pointer base: ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getData", "()[B", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
			mv.visitLdcInsn(" offset: ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "com/ibm/layout/Location", "getOffset", "()J", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
	
		{
			mv = cw.visitMethod(ACC_PUBLIC, "getLocation", "()Lcom/ibm/layout/Location;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "castTo", "(Ljava/lang/Class;)Lcom/ibm/layout/PointerType;", null, null);
			mv.visitCode();
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLdcInsn(Type.getType("Lcom/ibm/layout/Layout;"));
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z", false);
			Label l0 = new Label();
			mv.visitJumpInsn(IFEQ, l0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutType", "getPointer", "(Ljava/lang/Class;)Lcom/ibm/layout/Pointer;", true);
			mv.visitVarInsn(ASTORE, 2);
			Label l1 = new Label();
			mv.visitJumpInsn(GOTO, l1);
			mv.visitLabel(l0);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"com/ibm/layout/LayoutType"}, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, "com/ibm/layout/LayoutType", "getPrimPointer", "(Ljava/lang/Class;)Lcom/ibm/layout/LayoutType;", true);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLabel(l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/LayoutType", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "lValue", "()L" + elementInterfaceClassName + ";", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, elementImplClassName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, elementImplClassName, "<init>", "()V", false);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, elementImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", false);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
			}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "lValue", "()Lcom/ibm/layout/LayoutType;", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, pointerImplClassName, "lValue", "()L" + elementInterfaceClassName + ";", false);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "atOffset", "(J)Lcom/ibm/layout/Pointer;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, elementImplClassName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, elementImplClassName, "<init>", "()V", false);
			mv.visitVarInsn(ASTORE, 3);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitMethodInsn(INVOKEVIRTUAL, elementImplClassName, "bindLocation", "(Lcom/ibm/layout/Location;)V", false);
			mv.visitTypeInsn(NEW, pointerImplClassName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, pointerImplClassName, "<init>", "()V", false);
			mv.visitVarInsn(ASTORE, 4);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitTypeInsn(NEW, "com/ibm/layout/Location");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, pointerImplClassName, "location", "Lcom/ibm/layout/Location;");
			mv.visitVarInsn(LLOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, elementImplClassName, "sizeof", "()J", false);
			mv.visitInsn(LMUL);
			mv.visitMethodInsn(INVOKESPECIAL, "com/ibm/layout/Location", "<init>", "(Lcom/ibm/layout/Location;J)V", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "com/ibm/layout/Pointer", "bindLocation", "(Lcom/ibm/layout/Location;)V", true);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(8, 5);
			mv.visitEnd();
		}

		
		ImplHelper.genLayoutTypeImpl(cw, mv, fv, pointerImplClassName, 2, false);
		cw.visitEnd();
		return cw.toByteArray();
		
	}

}
