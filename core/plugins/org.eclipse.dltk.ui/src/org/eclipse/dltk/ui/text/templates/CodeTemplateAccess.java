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
package org.eclipse.dltk.ui.text.templates;

import java.io.IOException;

import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

public abstract class CodeTemplateAccess implements ICodeTemplateAccess,
		ITemplateAccess.ITemplateAccessInternal {

	private final String preferenceQualifier;
	private final String preferenceKey;
	private final IPreferenceStore preferenceStore;

	/**
	 * @param preferenceKey
	 */
	protected CodeTemplateAccess(String preferenceQualifier,
			String preferenceKey, IPreferenceStore preferenceStore) {
		this.preferenceQualifier = preferenceQualifier;
		this.preferenceKey = preferenceKey;
		this.preferenceStore = preferenceStore;
	}

	/**
	 * The code template context type registry for the Tcl editor.
	 */
	private ContextTypeRegistry fCodeTemplateContextTypeRegistry;

	/**
	 * The coded template store for the Tcl editor.
	 */
	private TemplateStore fCodeTemplateStore;

	@Override
	public TemplateStore getTemplateStore() {
		if (fCodeTemplateStore == null) {
			fCodeTemplateStore = new ContributionTemplateStore(
					getContextTypeRegistry(), preferenceStore, preferenceKey);
			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
				DLTKUIPlugin.log(e);
			}
			fCodeTemplateStore.startListeningForPreferenceChanges();
		}
		return fCodeTemplateStore;
	}

	@Override
	public IPreferenceStore getTemplatePreferenceStore() {
		return preferenceStore;
	}

	@Override
	public String getPreferenceQualifier() {
		return preferenceQualifier;
	}

	@Override
	public String getPreferenceKey() {
		return preferenceKey;
	}

	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 *
	 * @return the template context type registry for the code generation
	 *         templates
	 */
	@Override
	public ContextTypeRegistry getContextTypeRegistry() {
		if (fCodeTemplateContextTypeRegistry == null) {
			fCodeTemplateContextTypeRegistry = createContextTypeRegistry();
		}
		return fCodeTemplateContextTypeRegistry;
	}

	protected abstract ContextTypeRegistry createContextTypeRegistry();

	@Override
	public void dispose() {
		if (fCodeTemplateStore != null) {
			fCodeTemplateStore.stopListeningForPreferenceChanges();
			fCodeTemplateStore = null;
		}
	}

}
