/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.validators.internal.ui;

import org.eclipse.dltk.validators.core.IValidator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The installed validators preference page.
 */
public class ValidatorPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	private static final String VALIDATOR_PREFERENCE_PAGE = ValidatorsUI.PLUGIN_ID
			+ ".ValidatorPreferencePage"; //$NON-NLS-1$

	private ValidatorBlock fInterpretersBlock;

	public ValidatorPreferencePage() {
		super();

		// only used when page is shown programatically
		setTitle(ValidatorMessages.ValidatorPreferencePage_1);

		setDescription(ValidatorMessages.ValidatorPreferencePage_2);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);

		noDefaultAndApplyButton();

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);

		fInterpretersBlock = createValidatorsBlock();
		fInterpretersBlock.createControl(ancestor);
		Control control = fInterpretersBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		control.setLayoutData(data);

		fInterpretersBlock.restoreColumnSettings(getDialogSettings(),
				VALIDATOR_PREFERENCE_PAGE);

		applyDialogFont(ancestor);
		return ancestor;
	}

	private ValidatorBlock createValidatorsBlock() {
		return new ValidatorBlock();
	}

	@Override
	public boolean performOk() {
		final boolean[] canceled = new boolean[] { false };
		BusyIndicator.showWhile(null, () -> {
			IValidator[] vnterpreters = fInterpretersBlock.getValidators();
			ValidatorUpdater updater = new ValidatorUpdater();
			if (!updater.updateValidatorSettings(vnterpreters)) {
				canceled[0] = true;
			}
		});

		if (canceled[0]) {
			return false;
		}

		fInterpretersBlock.saveColumnSettings(getDialogSettings(),
				VALIDATOR_PREFERENCE_PAGE);

		return super.performOk();
	}

	private IDialogSettings getDialogSettings() {
		return ValidatorsUI.getDefault().getDialogSettings();
	}

}
