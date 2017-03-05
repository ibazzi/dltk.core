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
package org.eclipse.dltk.debug.dbgp.tests;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.IDbgpCommunicator;
import org.eclipse.dltk.debug.core.IDebugOptions;
import org.eclipse.dltk.debug.core.model.DefaultDebugOptions;

public abstract class AbstractCommunicator implements IDbgpCommunicator {

	@Override
	public void send(DbgpRequest request) {
		// empty
	}

	@Override
	public IDebugOptions getDebugOptions() {
		return DefaultDebugOptions.getDefaultInstance();
	}

	@Override
	public void configure(IDebugOptions debugOptions) {
		// empty
	}

}
