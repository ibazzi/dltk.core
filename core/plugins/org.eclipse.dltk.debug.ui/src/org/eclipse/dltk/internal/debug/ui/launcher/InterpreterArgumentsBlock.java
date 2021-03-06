/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.ui.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
import org.eclipse.dltk.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.dltk.debug.ui.launchConfigurations.CommonScriptLaunchTab;
import org.eclipse.dltk.debug.ui.messages.ScriptLaunchMessages;
import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Editor for Interpreter arguments of a Script launch configuration.
 */
public class InterpreterArgumentsBlock extends CommonScriptLaunchTab {

	// Interpreter arguments widgets
	protected Text fInterpreterArgumentsText;
	private Button fPgrmArgVariableButton;

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Group group = new Group(parent, SWT.NONE);
		setControl(group);
		GridLayout topLayout = new GridLayout();
		group.setLayout(topLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		group.setLayoutData(gd);
		group.setFont(font);
		group.setText(
				ScriptLaunchMessages.InterpreterArgumentsTab_Interpreter_ar_guments);

		fInterpreterArgumentsText = new Text(group,
				SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
		fInterpreterArgumentsText.setLayoutData(gd);
		fInterpreterArgumentsText.setFont(font);
		fInterpreterArgumentsText
				.addModifyListener(evt -> updateLaunchConfigurationDialog());
		ControlAccessibleListener.addListener(fInterpreterArgumentsText,
				group.getText());

		fPgrmArgVariableButton = createPushButton(group,
				ScriptLaunchMessages.InterpreterArgumentsBlock, null);
		fPgrmArgVariableButton.setFont(font);
		fPgrmArgVariableButton
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		fPgrmArgVariableButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
						getShell());
				dialog.open();
				String variable = dialog.getVariableExpression();
				if (variable != null) {
					fInterpreterArgumentsText.insert(variable);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_ARGUMENTS,
				(String) null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fInterpreterArgumentsText.setText(configuration.getAttribute(
					ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_ARGUMENTS,
					"")); //$NON-NLS-1$
		} catch (CoreException e) {
			setErrorMessage(NLS.bind(
					ScriptLaunchMessages.InterpreterArgumentsTab_Exception_occurred_reading_configuration,
					e.getStatus().getMessage()));
			DLTKDebugUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				ScriptLaunchConfigurationConstants.ATTR_INTERPRETER_ARGUMENTS,
				getAttributeValueFrom(fInterpreterArgumentsText));
	}

	@Override
	public String getName() {
		return ScriptLaunchMessages.InterpreterArgumentsBlock_Interpreter_Arguments;
	}

	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
	 *
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

	public void setEnabled(boolean enabled) {
		fInterpreterArgumentsText.setEnabled(enabled);
		fPgrmArgVariableButton.setEnabled(enabled);
	}
}
