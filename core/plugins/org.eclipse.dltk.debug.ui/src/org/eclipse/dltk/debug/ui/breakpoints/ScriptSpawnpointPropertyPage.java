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
package org.eclipse.dltk.debug.ui.breakpoints;

public class ScriptSpawnpointPropertyPage extends ScriptBreakpointPropertyPage {

	@Override
	protected boolean hasHitCountAttribute() {
		return false;
	}

	@Override
	protected boolean hasHitCountEditor() {
		return false;
	}

	@Override
	protected boolean hasExpressionEditor() {
		return false;
	}

}
