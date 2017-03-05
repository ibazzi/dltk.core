/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     xored software, Inc. - initial API and Implementation (Yuri Strot)
 *******************************************************************************/
package org.eclipse.dltk.ui.formatter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog to rename an imported profile.
 */
public class AlreadyExistsDialog extends StatusDialog {

	private Composite fComposite;
	protected Text fNameText;
	private Button fRenameRadio, fOverwriteRadio;

	private final int NUM_COLUMNS = 2;

	private final StatusInfo fOk;
	private final StatusInfo fEmpty;
	private final StatusInfo fDuplicate;

	private final IProfile fProfile;
	private final IProfileManager fProfileManager;

	public AlreadyExistsDialog(Shell parentShell, IProfile profile,
			IProfileManager profileManager) {
		super(parentShell);
		fProfile = profile;
		fProfileManager = profileManager;
		fOk = new StatusInfo();
		fDuplicate = new StatusInfo(IStatus.ERROR,
				FormatterMessages.AlreadyExistsDialog_nameExists);
		fEmpty = new StatusInfo(IStatus.ERROR,
				FormatterMessages.AlreadyExistsDialog_nameEmpty);

		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setTitle(FormatterMessages.AlreadyExistsDialog_loadProfile);
	}

	@Override
	public Control createDialogArea(Composite parent) {

		initializeComposite(parent);

		createLabel(NLS.bind(
				FormatterMessages.AlreadyExistsDialog_nameExistsQuestion,
				fProfile.getName()));

		fRenameRadio = createRadioButton(FormatterMessages.AlreadyExistsDialog_renameProfile);
		fNameText = createTextField();

		fOverwriteRadio = createRadioButton(FormatterMessages.AlreadyExistsDialog_overwriteProfile);

		fRenameRadio.setSelection(true);

		fNameText.setText(fProfile.getName());
		fNameText.setSelection(0, fProfile.getName().length());
		fNameText.setFocus();

		fNameText.addModifyListener(e -> doValidation());

		fRenameRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fNameText.setEnabled(true);
				fNameText.setFocus();
				fNameText.setSelection(0, fNameText.getText().length());
				doValidation();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		fOverwriteRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fNameText.setEnabled(false);
				doValidation();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		updateStatus(fDuplicate);

		applyDialogFont(fComposite);

		return fComposite;
	}

	private void initializeComposite(Composite parent) {
		fComposite = new Composite(parent, SWT.NULL);

		final GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = NUM_COLUMNS;

		fComposite.setLayout(layout);
	}

	private Label createLabel(String text) {
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = NUM_COLUMNS;
		gd.widthHint = convertWidthInCharsToPixels(60);
		final Label label = new Label(fComposite, SWT.WRAP);
		label.setText(text);
		label.setLayoutData(gd);
		return label;
	}

	private Button createRadioButton(String text) {
		final GridData gd = new GridData();
		gd.horizontalSpan = NUM_COLUMNS;
		gd.widthHint = convertWidthInCharsToPixels(60);
		final Button radio = new Button(fComposite, SWT.RADIO);
		radio.setLayoutData(gd);
		radio.setText(text);
		return radio;
	}

	private Text createTextField() {
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = NUM_COLUMNS;
		gd.horizontalIndent = 15;
		final Text text = new Text(fComposite, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(gd);
		return text;
	}

	/**
	 * Validate the current settings
	 */
	protected void doValidation() {

		if (fOverwriteRadio.getSelection()) {
			updateStatus(fOk);
			return;
		}

		final String name = fNameText.getText().trim();

		if (name.length() == 0) {
			updateStatus(fEmpty);
			return;
		}

		if (fProfileManager.containsName(name)) {
			updateStatus(fDuplicate);
			return;
		}

		updateStatus(fOk);
	}

	@Override
	protected void okPressed() {
		if (!getStatus().isOK())
			return;
		if (fRenameRadio.getSelection())
			fProfileManager.rename(fProfile, fNameText.getText().trim());
		super.okPressed();
	}
}
