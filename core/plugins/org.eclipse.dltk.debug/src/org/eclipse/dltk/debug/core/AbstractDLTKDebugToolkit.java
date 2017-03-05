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
package org.eclipse.dltk.debug.core;

public abstract class AbstractDLTKDebugToolkit implements IDLTKDebugToolkit2 {

	@Override
	public boolean isAccessWatchpointSupported() {
		return false;
	}

	@Override
	public boolean isWatchpointComplexSupported() {
		return false;
	}

}
