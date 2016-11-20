/*******************************************************************************
 *  Copyright (c) 2016 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.examples.javaclassfile;

import com.ibm.layout.IntPointer ;
import com.ibm.layout.Layout;
import com.ibm.layout.LayoutDesc;

/* Generated by LD2J */
@LayoutDesc({">val:jint:4"})
public interface Int extends Layout {

	interface EffectiveAddress {

		public IntPointer val();

	}

	public Int.EffectiveAddress EA();

	public long sizeof();

	public int val();

	public void val(int val);

	@Override
	public String toString();

}