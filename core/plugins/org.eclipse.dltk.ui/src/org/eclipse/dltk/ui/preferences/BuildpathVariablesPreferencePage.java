/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.ui.preferences;

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.ui.wizards.buildpath.VariableBlock;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BuildpathVariablesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = DLTKUIPlugin.ID_BUILDPATH_VARIABLES_PREFERENCE_PAGE;

	public static final String DATA_SELECT_VARIABLE = "BuildpathVariablesPreferencePage.select_var"; //$NON-NLS-1$

	private VariableBlock fVariableBlock;
	private String fStoredSettings;

	/**
	 * Constructor for BuildpathVariablesPreferencePage
	 */
	public BuildpathVariablesPreferencePage() {
		setPreferenceStore(DLTKUIPlugin.getDefault().getPreferenceStore());
		fVariableBlock = new VariableBlock(true, null);
		fStoredSettings = null;

		// title only used when page is shown programatically
		setTitle(PreferencesMessages.BuildpathVariablesPreferencePage_title);
		setDescription(PreferencesMessages.BuildpathVariablesPreferencePage_description);
		noDefaultAndApplyButton();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
		// IJavaHelpContextIds.CP_VARIABLES_PREFERENCE_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control result = fVariableBlock.createContents(parent);
		Dialog.applyDialogFont(result);
		return result;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		// not used (constructor calls noDefaultAndApplyButton())
		// fVariableBlock.performDefaults();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		DLTKUIPlugin.getDefault().savePluginPreferences();
		return fVariableBlock.performOk();
	}

	@Override
	public void setVisible(boolean visible) {
		// check if the stored settings have changed
		if (visible) {
			if (fStoredSettings != null
					&& !fStoredSettings.equals(getCurrentSettings())) {
				fVariableBlock.refresh(null);
			}
		} else {
			if (fVariableBlock.hasChanges()) {
				String title = PreferencesMessages.BuildpathVariablesPreferencePage_savechanges_title;
				String message = PreferencesMessages.BuildpathVariablesPreferencePage_savechanges_message;
				if (MessageDialog.openQuestion(getShell(), title, message)) {
					performOk();
				}
				fVariableBlock.setChanges(false); // forget
			}
			fStoredSettings = getCurrentSettings();
		}
		super.setVisible(visible);
	}

	private String getCurrentSettings() {
		StringBuffer buf = new StringBuffer();
		String[] names = DLTKCore.getBuildpathVariableNames();
		for (int i = 0; i < names.length; i++) {
			String curr = names[i];
			buf.append(curr).append('\0');
			IPath val = DLTKCore.getBuildpathVariable(curr);
			if (val != null) {
				buf.append(val.toString());
			}
			buf.append('\0');
		}
		return buf.toString();
	}

	@Override
	public void applyData(Object data) {
		if (data instanceof Map) {
			Object id = ((Map) data).get(DATA_SELECT_VARIABLE);
			if (id instanceof String) {
				fVariableBlock.setSelection((String) id);
			}
		}
		super.applyData(data);
	}

}
