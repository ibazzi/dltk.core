/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.ui.formatter.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.ui.formatter.IFormatterControlManager;
import org.eclipse.dltk.ui.preferences.ControlBindingManager;
import org.eclipse.dltk.ui.preferences.FieldValidators;
import org.eclipse.dltk.ui.preferences.IPreferenceDelegate;
import org.eclipse.dltk.ui.util.IStatusChangeListener;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FormatterControlManager implements IFormatterControlManager,
		IStatusChangeListener {

	private final IPreferenceDelegate<String> delegate;
	private final ControlBindingManager<String> bindingManager;
	private final IStatusChangeListener listener;

	public FormatterControlManager(IPreferenceDelegate<String> delegate,
			IStatusChangeListener listener) {
		this.delegate = delegate;
		this.bindingManager = new ControlBindingManager<String>(delegate, this);
		this.listener = listener;
	}

	@Override
	public Button createCheckbox(Composite parent, String key, String text) {
		return createCheckbox(parent, key, text, 1);
	}

	@Override
	public Button createCheckbox(Composite parent, String key, String text,
			int hspan) {
		Button button = SWTFactory.createCheckButton(parent, text, null, false,
				hspan);
		bindingManager.bindControl(button, key, null);
		return button;
	}

	@Override
	public Combo createCombo(Composite parent, String key, String label,
			String[] items) {
		final Label labelControl = SWTFactory.createLabel(parent, label, 1);
		Combo combo = SWTFactory.createCombo(parent,
				SWT.READ_ONLY | SWT.BORDER, 1, items);
		bindingManager.bindControl(combo, key);
		registerAssociatedLabel(combo, labelControl);
		return combo;
	}

	@Override
	public Combo createCombo(Composite parent, String key, String label,
			String[] itemValues, String[] itemLabels) {
		final Label labelControl = SWTFactory.createLabel(parent, label, 1);
		Combo combo = SWTFactory.createCombo(parent,
				SWT.READ_ONLY | SWT.BORDER, 1, itemLabels);
		bindingManager.bindControl(combo, key, itemValues);
		registerAssociatedLabel(combo, labelControl);
		return combo;
	}

	@Override
	public Text createNumber(Composite parent, String key, String label) {
		final Label labelControl = SWTFactory.createLabel(parent, label, 1);
		Text text = SWTFactory.createText(parent, SWT.BORDER, 1,
				Util.EMPTY_STRING);
		bindingManager.bindControl(text, key,
				FieldValidators.POSITIVE_NUMBER_VALIDATOR);
		registerAssociatedLabel(text, labelControl);
		return text;
	}

	private final Map<Control, Label> labelAssociations = new HashMap<Control, Label>();

	/**
	 * @param control
	 * @param label
	 */
	private void registerAssociatedLabel(Control control, Label label) {
		labelAssociations.put(control, label);
	}

	@Override
	public void enableControl(Control control, boolean enabled) {
		control.setEnabled(enabled);
		final Label label = labelAssociations.get(control);
		if (label != null) {
			label.setEnabled(enabled);
		}
	}

	private final ListenerList initListeners = new ListenerList();

	@Override
	public void addInitializeListener(IInitializeListener listener) {
		initListeners.add(listener);
	}

	@Override
	public void removeInitializeListener(IInitializeListener listener) {
		initListeners.remove(listener);
	}

	private boolean initialization;

	public void initialize() {
		initialization = true;
		try {
			bindingManager.initialize();
			final Object[] listeners = initListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IInitializeListener) listeners[i]).initialize();
			}
		} finally {
			initialization = false;
		}
		listener.statusChanged(bindingManager.getStatus());
	}

	@Override
	public void statusChanged(IStatus status) {
		if (!initialization) {
			listener.statusChanged(status);
		}
	}

	@Override
	public boolean getBoolean(String key) {
		return delegate.getBoolean(key);
	}

	@Override
	public String getString(String key) {
		return delegate.getString(key);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		delegate.setBoolean(key, value);
	}

	@Override
	public void setString(String key, String value) {
		delegate.setString(key, value);
	}

}
