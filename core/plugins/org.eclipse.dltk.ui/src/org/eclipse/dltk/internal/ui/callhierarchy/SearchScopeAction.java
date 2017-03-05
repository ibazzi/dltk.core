/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.callhierarchy;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.jface.action.Action;



abstract class SearchScopeAction extends Action {
	protected final SearchScopeActionGroup fGroup;

	public SearchScopeAction(SearchScopeActionGroup group, String text) {
		super(text, AS_RADIO_BUTTON);
		this.fGroup = group;
	}

	public abstract IDLTKSearchScope getSearchScope();

	public abstract int getSearchScopeType();

	@Override
	public void run() {
		this.fGroup.setSelected(this, true);
	}

	public IDLTKLanguageToolkit getLanguageToolkit() {
		return fGroup.getLangaugeToolkit();
	}

	public abstract String getFullDescription();
}
