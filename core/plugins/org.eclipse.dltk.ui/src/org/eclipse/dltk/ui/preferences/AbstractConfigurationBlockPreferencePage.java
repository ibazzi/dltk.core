/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.preferences;

import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Generic base class for preference pages.
 *
 * <p>
 * A number of {@link IPreferenceConfigurationBlock} implementations already
 * exist that can be used to provide standard preference options for editor
 * color options, code folding, etc.
 * </p>
 */
public abstract class AbstractConfigurationBlockPreferencePage extends
		PreferencePage implements IWorkbenchPreferencePage {

	private IPreferenceConfigurationBlock fConfigurationBlock;
	private OverlayPreferenceStore fOverlayStore;

	public AbstractConfigurationBlockPreferencePage() {
		// empty constructor
	}

	@Override
	public void init(IWorkbench workbench) {
		/*
		 * delay setup until here so sub-classes implementing the
		 * IExecutableExtension can look up the plugin specific preference store
		 */
		setDescription();
		setPreferenceStore();

		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(),
				new OverlayPreferenceStore.OverlayKey[] {});
		fConfigurationBlock = createConfigurationBlock(fOverlayStore);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				getHelpId());
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		Control content = fConfigurationBlock.createControl(parent);

		initialize();

		Dialog.applyDialogFont(content);
		return content;
	}

	private void initialize() {
		fConfigurationBlock.initialize();
	}

	@Override
	public boolean performOk() {
		fConfigurationBlock.performOk();
		fOverlayStore.propagate();

		DLTKUIPlugin.getDefault().savePluginPreferences();

		return true;
	}

	@Override
	public void performDefaults() {
		fOverlayStore.loadDefaults();
		fConfigurationBlock.performDefaults();

		super.performDefaults();
	}

	@Override
	public void dispose() {
		fConfigurationBlock.dispose();

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}

		super.dispose();
	}

	/**
	 * Returns the help id for the preference page
	 */
	protected String getHelpId() {
		return null;
	}

	/**
	 * Set the preference page description.
	 *
	 * <p>
	 * Sub-classes should make a call to {@link #setDescription(String)} to set
	 * description.
	 * </p>
	 */
	protected void setDescription() {
	}

	/**
	 * Set the preference store that will hold the preference settings.
	 *
	 * <p>
	 * Sub-classes should make a call to
	 * {@link #setPreferenceStore(IPreferenceStore)} to set the preference
	 * store.
	 * </p>
	 */
	protected abstract void setPreferenceStore();

	protected abstract IPreferenceConfigurationBlock createConfigurationBlock(
			OverlayPreferenceStore overlayPreferenceStore);
}
