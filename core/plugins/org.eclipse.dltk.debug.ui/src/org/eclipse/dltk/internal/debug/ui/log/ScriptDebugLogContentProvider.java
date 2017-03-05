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
package org.eclipse.dltk.internal.debug.ui.log;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ScriptDebugLogContentProvider
		implements IStructuredContentProvider {
	@Override
	public void dispose() {
		// empty
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// empty
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			synchronized (inputElement) {
				return ((List) inputElement).toArray();
			}
		}
		return new Object[0];
	}
}
