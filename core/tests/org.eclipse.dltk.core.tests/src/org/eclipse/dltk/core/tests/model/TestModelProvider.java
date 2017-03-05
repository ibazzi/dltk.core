/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html  
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Andrei Sobolev)
 *******************************************************************************/
package org.eclipse.dltk.core.tests.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelProvider;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.core.ModelElement;

public class TestModelProvider implements IModelProvider {

	public TestModelProvider() {
	}

	@Override
	public void provideModelChanges(IModelElement parentElement, List<IModelElement> children) {
		IScriptProject project = parentElement.getScriptProject();
		if (!"ModelMembersq".equals(project.getElementName())) {
			return;
		}
		switch (parentElement.getElementType()) {
		case IModelElement.PROJECT_FRAGMENT:
			List<TestFolder> addon = new ArrayList<TestFolder>();
			for (Iterator<IModelElement> iterator = children.iterator(); iterator.hasNext();) {
				IModelElement el = iterator.next();
				if (el.getElementType() == IModelElement.SCRIPT_FOLDER) {
					addon.add(new TestFolder((ModelElement) parentElement,
							el.getPath().removeFirstSegments(
									el.getParent().getPath().segmentCount())));
				}
			}
			children.addAll(addon);
			break;
		case IModelElement.SCRIPT_FOLDER:
			break;
		}
	}

	@Override
	public boolean isModelChangesProvidedFor(IModelElement modelElement,
			String name) {
		IScriptProject project = modelElement.getScriptProject();
		if (!"ModelMembersq".equals(project.getElementName())) {
			return false;
		}
		if (modelElement.getElementType() == IModelElement.PROJECT_FRAGMENT) {
			return true;
		}
		return false;
	}
}
