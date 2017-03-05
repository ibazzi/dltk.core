/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.0
 */
public class MultipleInputDialog extends Dialog {
	protected static final String FIELD_NAME = "FIELD_NAME"; //$NON-NLS-1$
	protected static final int TEXT = 100;
	protected static final int BROWSE = 101;
	protected static final int VARIABLE = 102;
	protected static final int LABEL = 103;

	protected Composite panel;

	protected List<FieldSummary> fieldList = new ArrayList<FieldSummary>();
	protected List<Text> controlList = new ArrayList<Text>();
	protected List<Validator> validators = new ArrayList<Validator>();
	protected Map<String, Object> valueMap = new HashMap<String, Object>();

	private String title;

	public MultipleInputDialog(Shell shell, String title) {
		super(shell);
		this.title = title;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}

	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control bar = super.createButtonBar(parent);
		validateFields();
		return bar;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		panel = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		for (FieldSummary field : fieldList) {
			switch (field.type) {
			case TEXT:
				createTextField(field.name, field.initialValue,
						field.allowsEmpty);
				break;
			case BROWSE:
				createBrowseField(field.name, field.initialValue,
						field.allowsEmpty);
				break;
			case VARIABLE:
				createVariablesField(field.name, field.initialValue,
						field.allowsEmpty);
				break;
			case LABEL:
				createLabelField(field.name);
				break;
			}
		}

		fieldList = null; // allow it to be gc'd
		Dialog.applyDialogFont(container);
		return container;
	}

	public void addBrowseField(String labelText, String initialValue,
			boolean allowsEmpty) {
		fieldList.add(new FieldSummary(BROWSE, labelText, initialValue,
				allowsEmpty));
	}

	public void addTextField(String labelText, String initialValue,
			boolean allowsEmpty) {
		fieldList.add(new FieldSummary(TEXT, labelText, initialValue,
				allowsEmpty));
	}

	public void addVariablesField(String labelText, String initialValue,
			boolean allowsEmpty) {
		fieldList.add(new FieldSummary(VARIABLE, labelText, initialValue,
				allowsEmpty));
	}

	public void addLabelField(String labelText) {
		fieldList.add(new FieldSummary(LABEL, labelText, null, false));
	}

	protected void createTextField(String labelText, String initialValue,
			boolean allowEmpty) {
		Label label = new Label(panel, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		final Text text = new Text(panel, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setData(FIELD_NAME, labelText);

		// make sure rows are the same height on both panels.
		label.setSize(label.getSize().x, text.getSize().y);

		if (initialValue != null) {
			text.setText(initialValue);
		}

		if (!allowEmpty) {
			validators.add(new Validator() {
				@Override
				public boolean validate() {
					return !text.getText().equals(""); //$NON-NLS-1$
				}
			});
			text.addModifyListener(e -> validateFields());
		}

		controlList.add(text);
	}

	protected void createBrowseField(String labelText, String initialValue,
			boolean allowEmpty) {
		Label label = new Label(panel, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Composite comp = new Composite(panel, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 200;
		text.setLayoutData(data);
		text.setData(FIELD_NAME, labelText);

		// make sure rows are the same height on both panels.
		label.setSize(label.getSize().x, text.getSize().y);

		if (initialValue != null) {
			text.setText(initialValue);
		}

		if (!allowEmpty) {
			validators.add(new Validator() {
				@Override
				public boolean validate() {
					return !text.getText().equals(""); //$NON-NLS-1$
				}
			});

			text.addModifyListener(e -> validateFields());
		}

		Button button = createButton(comp, IDialogConstants.IGNORE_ID,
				DialogMessages.MultipleInputDialog_ignore, false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setMessage(""); //$NON-NLS-1$
				String currentWorkingDir = text.getText();
				if (!currentWorkingDir.trim().equals("")) { //$NON-NLS-1$
					File path = new File(currentWorkingDir);
					if (path.exists()) {
						dialog.setFilterPath(currentWorkingDir);
					}
				}

				String selectedDirectory = dialog.open();
				if (selectedDirectory != null) {
					text.setText(selectedDirectory);
				}
			}
		});

		controlList.add(text);

	}

	public void createVariablesField(String labelText, String initialValue,
			boolean allowEmpty) {
		Label label = new Label(panel, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

		Composite comp = new Composite(panel, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 200;
		text.setLayoutData(data);
		text.setData(FIELD_NAME, labelText);

		// make sure rows are the same height on both panels.
		label.setSize(label.getSize().x, text.getSize().y);

		if (initialValue != null) {
			text.setText(initialValue);
		}

		if (!allowEmpty) {
			validators.add(new Validator() {
				@Override
				public boolean validate() {
					return !text.getText().equals(""); //$NON-NLS-1$
				}
			});

			text.addModifyListener(e -> validateFields());
		}

		// Button button = createButton(comp, IDialogConstants.IGNORE_ID,
		// "Ignore", false);
		// button.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// StringVariableSelectionDialog dialog = new
		// StringVariableSelectionDialog(getShell());
		// int code = dialog.open();
		// if (code == IDialogConstants.OK_ID) {
		// String variable = dialog.getVariableExpression();
		// if (variable != null) {
		// text.insert(variable);
		// }
		// }
		// }
		// });

		controlList.add(text);

	}

	private void createLabelField(String labelText) {
		Label label = new Label(panel, SWT.NONE);
		label.setText(labelText);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
	}

	@Override
	protected void okPressed() {
		for (Iterator<Text> i = controlList.iterator(); i.hasNext();) {
			Control control = i.next();
			if (control instanceof Text) {
				valueMap.put((String) control.getData(FIELD_NAME),
						((Text) control).getText());
			}
		}
		controlList = null;
		super.okPressed();
	}

	@Override
	public int open() {
		applyDialogFont(panel);
		return super.open();
	}

	public Object getValue(String key) {
		return valueMap.get(key);
	}

	public String getStringValue(String key) {
		return (String) getValue(key);
	}

	public void validateFields() {
		for (Iterator<Validator> i = validators.iterator(); i.hasNext();) {
			Validator validator = i.next();
			if (!validator.validate()) {
				getButton(IDialogConstants.OK_ID).setEnabled(false);
				return;
			}
		}
		getButton(IDialogConstants.OK_ID).setEnabled(true);
	}

	protected static class FieldSummary {
		int type;
		String name;
		String initialValue;
		boolean allowsEmpty;

		public FieldSummary(int type, String name, String initialValue,
				boolean allowsEmpty) {
			this.type = type;
			this.name = name;
			this.initialValue = initialValue;
			this.allowsEmpty = allowsEmpty;
		}
	}

	protected class Validator {
		boolean validate() {
			return true;
		}
	}
}
