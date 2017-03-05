/*******************************************************************************
 * Copyright (c) 2007, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation
 *******************************************************************************/
package org.eclipse.dltk.debug.core.model;

/**
 * Represents an 'complex' script type
 */
public class ComplexScriptType extends AtomicScriptType {

	public ComplexScriptType(String name) {
		super(name);
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Override
	public String formatDetails(IScriptValue value) {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());

		String address = value.getMemoryAddress();
		if (address == null) {
			address = ScriptModelMessages.unknownMemoryAddress;
		}

		sb.append("@" + address); //$NON-NLS-1$

		return sb.toString();
	}

	@Override
	public String formatValue(IScriptValue value) {
		StringBuffer sb = new StringBuffer();
		sb.append(getName());

		appendInstanceId(value, sb);

		return sb.toString();
	}
}
