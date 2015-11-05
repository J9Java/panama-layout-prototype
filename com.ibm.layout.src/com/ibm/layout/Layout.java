/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.reflect.Constructor;

/**
 * A singleton layout.
 * 
 *	<h2>Example Usage</h2>
 *	<pre>
 *	Point c = Point.getLayout(Point.class);
 *	Location loc = new Location(new byte[(int)c.sizeof()]);
 *	c.bindLocation(loc);
 *	c.x(10);
 *	c.y(20);
 *	</pre>
 */
public interface Layout extends LayoutType {
	//TODO need to make sure src is of the same type as current layout
	void copyFrom(Layout src);
	
	/**
	 * Create a singleton layout instance
	 * @param cls The layout class.
	 * @param <T> subclass of Layout
	 * @return a layout instance
	 */
	public static <T extends Layout> T getLayout(final Class<T> cls) {
		try {
			LayoutHelper f = LayoutHelper.getFactory();
			Class<T> implCls = f.genLayoutImpl(cls);
			Constructor<T> ctor = implCls.getDeclaredConstructor();
			ctor.setAccessible(true);
			T newInst = ctor.newInstance();
			return newInst;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
