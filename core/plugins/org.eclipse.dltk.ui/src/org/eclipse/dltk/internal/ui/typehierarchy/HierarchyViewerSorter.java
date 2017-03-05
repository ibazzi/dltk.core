/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.typehierarchy;

import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;

public class HierarchyViewerSorter extends AbstractHierarchyViewerSorter {

	private final TypeHierarchyLifeCycle fHierarchy;
	private boolean fSortByDefiningType;

	public HierarchyViewerSorter(TypeHierarchyLifeCycle cycle) {
		fHierarchy= cycle;
	}

	public void setSortByDefiningType(boolean sortByDefiningType) {
		fSortByDefiningType= sortByDefiningType;
	}

	@Override
	protected int getTypeFlags(IType type) {
		ITypeHierarchy hierarchy= getHierarchy(type);
		if (hierarchy != null) {
			return fHierarchy.getHierarchy().getCachedFlags(type);
		}
		return 0;
	}

	@Override
	public boolean isSortByDefiningType() {
		return fSortByDefiningType;
	}

	@Override
	public boolean isSortAlphabetically() {
		return true;
	}

	@Override
	protected ITypeHierarchy getHierarchy(IType type) {
		return fHierarchy.getHierarchy(); // hierarchy contains all types shown
	}

}
