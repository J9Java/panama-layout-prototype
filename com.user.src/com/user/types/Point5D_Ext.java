/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.user.types;

/**
 * Generated interface
 */
public interface Point5D_Ext extends Point5D {

	public default int sum() {
		return this.x() + this.y() + this.z() + this.o() + this.p();
	}

	public default void set(int x, int y, int z, int o, int p) {
		this.x(x);
		this.y(y);
		this.z(z);
		this.o(o);
		this.p(p);
	}
}
