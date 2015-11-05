/*******************************************************************************
 *  Copyright (c) 2014, 2015 IBM Corporation.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.ibm.layout.ld2j;

@SuppressWarnings("serial")
class VerifierException extends Exception {

	public VerifierException() {
		super();
	}

	/**
	 * Report a VerifierException for the reason specified.
	 * @param reason a <code>String</code> message indicating the reason.
	 */
	public VerifierException(String s) {
		super(s);
	}
}
