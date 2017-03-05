/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.dltk.compiler.CharOperation;

public class NoWatchExpressionResult implements IWatchExpressionResult {

	private final String expressionText;

	/**
	 * @param expressionText
	 */
	public NoWatchExpressionResult(String expressionText) {
		this.expressionText = expressionText;
	}

	@Override
	public String[] getErrorMessages() {
		return CharOperation.NO_STRINGS;
	}

	@Override
	public DebugException getException() {
		return null;
	}

	@Override
	public String getExpressionText() {
		return expressionText;
	}

	@Override
	public IValue getValue() {
		return null;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

}
