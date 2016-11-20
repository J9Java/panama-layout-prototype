/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.examples.javaclassfile;

import com.ibm.layout.IntPointer ;
import com.ibm.layout.ShortPointer ;
import com.user.examples.javaclassfile.ConstantPool;
import com.ibm.layout.Pointer;
import com.ibm.layout.VLArray;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/* Generated by LD2J */
@LayoutDesc({">magic:jint:4",">minor:jshort:2",">major:jshort:2",">cpCount:jshort:2",">cp:CPEntry[cpCount]:0",">accessFlags:jshort:2",">thisClass:jshort:2",">superClass:jshort:2",">interfaceCount:jshort:2",">interfaces:Short[interfaceCount]:0",">fieldsCount:jshort:2",">fieldInfo:Fields[fieldsCount]:0",">methodsCount:jshort:2",">methodInfo:Methods[methodsCount]:0",">attributesCount:jshort:2",">attributeInfo:Attributes[attributesCount]:0"})
public interface JavaClassFileFormat extends Layout {

	interface EffectiveAddress {

		public IntPointer magic();

		public ShortPointer minor();

		public ShortPointer major();

		public ShortPointer cpCount();

		public Pointer<CPEntry> cp();

		public ShortPointer accessFlags();

		public ShortPointer thisClass();

		public ShortPointer superClass();

		public ShortPointer interfaceCount();

		public Pointer<Short> interfaces();

		public ShortPointer fieldsCount();

		public Pointer<Fields> fieldInfo();

		public ShortPointer methodsCount();

		public Pointer<Methods> methodInfo();

		public ShortPointer attributesCount();

		public Pointer<Attributes> attributeInfo();

	}

	public JavaClassFileFormat.EffectiveAddress EA();

	public long sizeof();

	public int magic();

	public short minor();

	public short major();

	public short cpCount();

	public ConstantPool<CPEntry> cp();

	public short accessFlags();

	public short thisClass();

	public short superClass();

	public short interfaceCount();

	public VLArray<Short> interfaces();

	public short fieldsCount();

	public VLArray<Fields> fieldInfo();

	public short methodsCount();

	public VLArray<Methods> methodInfo();

	public short attributesCount();

	public VLArray<Attributes> attributeInfo();

	public void magic(int val);

	public void minor(short val);

	public void major(short val);

	public void cpCount(short val);

	public void accessFlags(short val);

	public void thisClass(short val);

	public void superClass(short val);

	public void interfaceCount(short val);

	public void fieldsCount(short val);

	public void methodsCount(short val);

	@Override
	public String toString();

}