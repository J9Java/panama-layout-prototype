/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the data layout.
 * 
 * e.g.
 * <pre>
 * struct Point {
 *     jint x;
 *     jint y;
 * }
 * </pre>
 * 
 * corresponds to 
 * 
 * <pre>
 * {@code @LayoutDesc}({ "x:jint:+0:4", "y:jint:+4:4" })
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface LayoutDesc {
	String[] value();
}
