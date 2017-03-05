/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.ui.preferences;

import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.internal.ui.text.PreferencesAdapter;
import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPreferencePage;
import org.eclipse.dltk.ui.preferences.IPreferenceConfigurationBlock;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;

public class ScriptDebugPreferencePage
		extends AbstractConfigurationBlockPreferencePage {

	public static final String PAGE_ID = "org.eclipse.dltk.preferences.debug"; //$NON-NLS-1$

	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(
			OverlayPreferenceStore overlayPreferenceStore) {
		return new ScriptDebugConfigurationBlock(overlayPreferenceStore, this);
	}

	@Override
	protected String getHelpId() {
		return null;
	}

	@Override
	protected void setDescription() {
		setDescription(
				ScriptDebugPreferencesMessages.GeneralPreferencesDescription);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(new PreferencesAdapter(
				DLTKDebugPlugin.getDefault().getPluginPreferences()));
	}

	@Override
	public boolean performOk() {
		super.performOk();
		DLTKDebugPlugin.getDefault().savePluginPreferences();
		return true;
	}
}
