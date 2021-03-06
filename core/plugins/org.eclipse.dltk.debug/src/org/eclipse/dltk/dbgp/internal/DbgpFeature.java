/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal;

import org.eclipse.dltk.dbgp.IDbgpFeature;

public class DbgpFeature implements IDbgpFeature {
	private final boolean supported;

	private final String name;

	private final String value;

	public DbgpFeature(boolean supported, String name, String value) {
		this.supported = supported;
		this.name = name;
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSupported() {
		return supported;
	}

	@Override
	public String toString() {
		return "DbgpFeature (name: " + name + "; value: " + value //$NON-NLS-1$ //$NON-NLS-2$
				+ "; supported: " + supported + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
