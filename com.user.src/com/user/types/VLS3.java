/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

import com.ibm.layout.BytePointer ;
import com.ibm.layout.Pointer;
import com.ibm.layout.Location;
import com.ibm.layout.VLArray;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/* Generated by LD2J */
@LayoutDesc({"c:jbyte:1","d:VLS2[c]:0","e:jbyte:1"})
public interface VLS3 extends Layout {

	interface EffectiveAddress {

		public BytePointer c();

		public Pointer<VLS2> d();

		public BytePointer e();

	}

	public VLS3.EffectiveAddress EA();

	public long sizeof();

	public byte c();

	public VLArray<VLS2> d();

	public byte e();

	public void bindLocation(Location loc, byte repeatCountInitializer);

	public void e(byte val);

	@Override
	public String toString();

}