/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.filters;

import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * filters out all empty package fragments.
 */
public class EmptyPackageFilter extends ViewerFilter {

	/**
	 * Returns the result of this filter, when applied to the
	 * given inputs.
	 *
	 * @return Returns true if element should be included in filtered set
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IScriptFolder) {
			IScriptFolder pkg= (IScriptFolder)element;
			try {
				Object[] foreignResources = pkg.getForeignResources();
				boolean hasForeign = false;
				if( foreignResources != null ) {
					hasForeign = foreignResources.length > 0;
				}
				return pkg.hasChildren() || hasForeign;
			} catch (ModelException e) {
				return false;
			}
		}
		return true;
	}


}
