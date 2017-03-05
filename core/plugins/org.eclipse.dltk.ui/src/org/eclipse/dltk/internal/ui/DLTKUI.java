/*******************************************************************************
 * Copyright (c) 2009, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.internal.ui;

import org.eclipse.ui.IStartup;

public class DLTKUI implements IStartup {

	private static volatile boolean started = false;

	@Override
	public void earlyStartup() {
		started = true;
	}

	public static boolean isStarted() {
		return started;
	}

}
