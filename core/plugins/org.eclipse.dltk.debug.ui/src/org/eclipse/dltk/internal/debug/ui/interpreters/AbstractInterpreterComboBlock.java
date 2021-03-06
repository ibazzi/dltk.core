/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.ui.interpreters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
import org.eclipse.dltk.debug.ui.IDLTKDebugUIConstants;
import org.eclipse.dltk.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.internal.ui.util.SWTUtil;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.InterpreterStandin;
import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.ui.DLTKUILanguageManager;
import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A composite that displays installed InterpreterEnvironment's in a combo box,
 * with a 'manage...' button to modify installed InterpreterEnvironments.
 * <p>
 * This block implements ISelectionProvider - it sends selection change events
 * when the checked InterpreterEnvironment in the table changes, or when the
 * "use default" button check state changes.
 * </p>
 */
public class AbstractInterpreterComboBlock {

	public static final String PROPERTY_INTERPRETER = "PROPERTY_INTERPRETER"; //$NON-NLS-1$

	/**
	 * This block's control
	 */
	private Composite fControl;

	/**
	 * Interpreters being displayed
	 */
	private List<IInterpreterInstall> fInterpreters = new ArrayList<>();

	/**
	 * The main control
	 */
	private Combo fCombo;

	// Action buttons
	private Button fManageButton;

	/**
	 * InterpreterEnvironment change listeners
	 */
	private ListenerList fListeners = new ListenerList();

	/**
	 * Default InterpreterEnvironment descriptor or <code>null</code> if none.
	 */
	private InterpreterDescriptor fDefaultDescriptor = null;

	/**
	 * Specific InterpreterEnvironment descriptor or <code>null</code> if none.
	 */
	private InterpreterDescriptor fSpecificDescriptor = null;

	/**
	 * Default InterpreterEnvironment radio button or <code>null</code> if none
	 */
	private Button fDefaultButton = null;

	/**
	 * Selected InterpreterEnvironment radio button
	 */
	private Button fSpecificButton = null;

	/**
	 * The title used for the InterpreterEnvironment block
	 */
	private String fTitle = null;

	private IStatus fStatus = OK_STATUS;

	private static final IStatus OK_STATUS = new Status(IStatus.OK,
			DLTKDebugUIPlugin.getUniqueIdentifier(), 0, "", null); //$NON-NLS-1$

	private final IInterpreterComboBlockContext fContext;

	/**
	 * @since 2.0
	 */
	public AbstractInterpreterComboBlock(
			IInterpreterComboBlockContext context) {
		this.fContext = context;
	}

	/**
	 * @since 2.0
	 */
	protected IInterpreterComboBlockContext getContext() {
		return fContext;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
	}

	private void firePropertyChange() {
		PropertyChangeEvent event = new PropertyChangeEvent(this,
				PROPERTY_INTERPRETER, null, getInterpreterPath());
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			listener.propertyChange(event);
		}
	}

	/**
	 * Creates this block's control in the given control.
	 *
	 * @param anscestor
	 *            containing control
	 */
	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite comp = new Composite(ancestor, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fControl = comp;
		comp.setFont(font);

		Group group = new Group(comp, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setFont(font);

		GridData data;

		if (fTitle == null) {
			fTitle = InterpretersMessages.InterpretersComboBlock_3;
		}
		group.setText(fTitle);

		// display a' use default InterpreterEnvironment' check box
		if (fDefaultDescriptor != null) {
			fDefaultButton = new Button(group, SWT.RADIO);
			fDefaultButton.setText(fDefaultDescriptor.getDescription());
			fDefaultButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (fDefaultButton.getSelection()) {
						setUseDefaultInterpreter();
						if (fInterpreters.isEmpty()) {
							setStatus(new Status(IStatus.ERROR,
									DLTKLaunchingPlugin.getUniqueIdentifier(),
									ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
									InterpretersMessages.InterpretersComboBlock_0,
									null));
						} else {
							setStatus(OK_STATUS);
						}
						firePropertyChange();
					}
				}
			});
			data = new GridData();
			data.horizontalSpan = 3;
			fDefaultButton.setLayoutData(data);
			fDefaultButton.setFont(font);
		}

		fSpecificButton = new Button(group, SWT.RADIO);
		if (fSpecificDescriptor != null) {
			fSpecificButton.setText(fSpecificDescriptor.getDescription());
		} else {
			fSpecificButton
					.setText(InterpretersMessages.InterpretersComboBlock_1);
		}
		fSpecificButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSpecificButton.getSelection()) {
					fCombo.setEnabled(true);
					if (fCombo.getText().length() == 0
							&& !fInterpreters.isEmpty()) {
						fCombo.select(0);
					}
					if (fInterpreters.isEmpty()) {
						setStatus(new Status(IStatus.ERROR,
								DLTKLaunchingPlugin.getUniqueIdentifier(),
								ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
								InterpretersMessages.InterpretersComboBlock_0,
								null));
					} else {
						setStatus(OK_STATUS);
					}
					firePropertyChange();
				}
			}
		});
		fSpecificButton.setFont(font);
		data = new GridData(GridData.BEGINNING);
		fSpecificButton.setLayoutData(data);

		fCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		fCombo.setFont(font);
		data = new GridData(GridData.FILL_HORIZONTAL);
		// data.minimumWidth = 100;
		// data.widthHint = 100;
		data.horizontalSpan = 1;
		fCombo.setLayoutData(data);
		ControlAccessibleListener.addListener(fCombo,
				fSpecificButton.getText());

		fCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				firePropertyChange();
			}
		});

		fManageButton = createPushButton(group,
				InterpretersMessages.InterpretersComboBlock_2);
		fManageButton.addListener(SWT.Selection,
				event -> showInterpreterPreferencePage());

		fillWithWorkspaceInterpreters();
		if (fInterpreters.isEmpty()) {
			setStatus(new Status(IStatus.ERROR,
					DLTKLaunchingPlugin.getUniqueIdentifier(),
					ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
					InterpretersMessages.InterpretersComboBlock_0, null));
		} else {
			setStatus(OK_STATUS);
		}

	}

	/**
	 * Opens the given preference page, and updates when closed.
	 *
	 * @param id
	 *            pref page id
	 * @param page
	 *            pref page
	 */
	protected void showPrefPage(String pageId) {
		PreferencesUtil.createPreferenceDialogOn(getShell(), pageId,
				new String[] { pageId }, null).open();

		refreshInterpreters();
	}

	private void restoreCombo(List<?> elements, Object element, Combo combo) {
		int index = -1;
		if (element != null) {
			index = elements.indexOf(element);
		}
		if (index >= 0) {
			combo.select(index);
		} else {
			combo.select(0);
		}
	}

	protected Button createPushButton(Composite parent, String label) {
		return SWTUtil.createPushButton(parent, label, null);
	}

	/**
	 * Returns this block's control
	 *
	 * @return control
	 */
	public Control getControl() {
		return fControl;
	}

	/**
	 * Sets the InterpreterEnvironments to be displayed in this block
	 *
	 * @param Interpreters
	 *            InterpreterEnvironments to be displayed
	 */
	protected void setInterpreters(List<IInterpreterInstall> interpreters) {
		fInterpreters.clear();
		fInterpreters.addAll(interpreters);
		// sort by name
		Collections.sort(fInterpreters,
				(o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		// now make an array of names
		String[] names = new String[fInterpreters.size()];
		for (int i = 0, size = fInterpreters.size(); i < size; ++i) {
			names[i] = fInterpreters.get(i).getName();
		}
		fCombo.setItems(names);
		fCombo.setVisibleItemCount(Math.min(names.length, 20));
		// if (names.length == 0) {
		// fCombo.setEnabled(names.length > 0);
		// fSpecificButton.setEnabled(names.length > 0);
		// }
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	/**
	 * Selects a specific InterpreterEnvironment based on type/name.
	 *
	 * @param Interpreter
	 *            InterpreterEnvironment
	 */
	private void selectInterpreter(IInterpreterInstall Interpreter) {
		fSpecificButton.setSelection(true);
		fDefaultButton.setSelection(false);
		fCombo.setEnabled(true);
		int index = fInterpreters.indexOf(Interpreter);
		if (index >= 0) {
			fCombo.select(index);
		}
		firePropertyChange();
	}

	/**
	 * Returns the selected InterpreterEnvironment or <code>null</code> if none.
	 *
	 * @return the selected InterpreterEnvironment or <code>null</code> if none
	 */
	public IInterpreterInstall getInterpreter() {
		if (this.isDefaultInterpreter()) {
			return fDefaultDescriptor.getInterpreter();
		}
		int index = fCombo.getSelectionIndex();
		if (index >= 0) {
			return fInterpreters.get(index);
		}
		return null;
	}

	/**
	 * Sets the Default InterpreterEnvironment Descriptor for this block.
	 *
	 * @param descriptor
	 *            default InterpreterEnvironment descriptor
	 */
	public void setDefaultInterpreterDescriptor(
			InterpreterDescriptor descriptor) {
		fDefaultDescriptor = descriptor;
		setButtonTextFromDescriptor(fDefaultButton, descriptor);
	}

	private void setButtonTextFromDescriptor(Button button,
			InterpreterDescriptor descriptor) {
		if (button != null) {
			// update the description & InterpreterEnvironment in case it has
			// changed
			String currentText = button.getText();
			String newText = descriptor.getDescription();
			if (!newText.equals(currentText)) {
				button.setText(newText);
				fControl.layout();
			}
		}
	}

	/**
	 * Sets the specific InterpreterEnvironment Descriptor for this block.
	 *
	 * @param descriptor
	 *            specific InterpreterEnvironment descriptor
	 */
	public void setSpecificInterpreterDescriptor(
			InterpreterDescriptor descriptor) {
		fSpecificDescriptor = descriptor;
		setButtonTextFromDescriptor(fSpecificButton, descriptor);
	}

	/**
	 * Returns whether the 'use default InterpreterEnvironment' button is
	 * checked.
	 *
	 * @return whether the 'use default InterpreterEnvironment' button is
	 *         checked
	 */
	public boolean isDefaultInterpreter() {
		if (fDefaultButton != null) {
			return fDefaultButton.getSelection();
		}
		return false;
	}

	/**
	 * Sets this control to use the 'default' InterpreterEnvironment.
	 */
	public void setUseDefaultInterpreter() {
		if (fDefaultDescriptor != null) {
			fDefaultButton.setSelection(true);
			fSpecificButton.setSelection(false);
			fCombo.setEnabled(false);
			firePropertyChange();
		}
	}

	/**
	 * Sets the title used for this InterpreterEnvironment block
	 *
	 * @param title
	 *            title for this InterpreterEnvironment block
	 */
	public void setTitle(String title) {
		fTitle = title;
	}

	/**
	 * Refresh the default InterpreterEnvironment description.
	 */
	public void refresh() {
		setDefaultInterpreterDescriptor(fDefaultDescriptor);
	}

	public void refreshInterpreters() {
		IInterpreterInstall prevInterpreter = getInterpreter();
		fillWithWorkspaceInterpreters();
		if (fInterpreters.isEmpty()) {
			setStatus(new Status(IStatus.ERROR,
					DLTKLaunchingPlugin.getUniqueIdentifier(),
					ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
					InterpretersMessages.InterpretersComboBlock_0, null));
		} else {
			setStatus(OK_STATUS);
		}
		restoreCombo(fInterpreters, prevInterpreter, fCombo);

		// update text
		setDefaultInterpreterDescriptor(fDefaultDescriptor);
		if (isDefaultInterpreter()) {
			// reset in case default has changed
			setUseDefaultInterpreter();
		}
		setPath(getInterpreterPath());
		firePropertyChange();
	}

	/**
	 * Returns a buildpath container path identifying the selected
	 * InterpreterEnvironment.
	 *
	 * @return buildpath container path or <code>null</code>
	 */
	public IPath getInterpreterPath() {
		if (fSpecificButton.getSelection()) {
			int index = fCombo.getSelectionIndex();
			if (index >= 0) {
				IInterpreterInstall Interpreter = fInterpreters.get(index);
				return ScriptRuntime.newInterpreterContainerPath(Interpreter);
			}
			return null;
		}
		return ScriptRuntime.newDefaultInterpreterContainerPath();
	}

	public IBuildpathEntry getEntry() {
		return DLTKCore.newContainerEntry(getInterpreterPath());
	}

	/**
	 * Sets the selection based on the given container path and returns a status
	 * indicating if the selection was successful.
	 *
	 * @param containerPath
	 * @return status
	 */
	public void setPath(IPath containerPath) {
		if (fInterpreters.isEmpty()) {
			setStatus(new Status(IStatus.ERROR,
					DLTKLaunchingPlugin.getUniqueIdentifier(),
					ScriptLaunchConfigurationConstants.ERR_NO_DEFAULT_INTERPRETER_INSTALL,
					InterpretersMessages.InterpretersComboBlock_0, null));
		} else {
			setStatus(OK_STATUS);
		}
		if (ScriptRuntime.newDefaultInterpreterContainerPath()
				.equals(containerPath)) {
			setUseDefaultInterpreter();
		} else {
			final IEnvironment environment = fContext.getEnvironment();
			final IInterpreterInstall install = environment != null
					? ScriptRuntime.getInterpreterInstall(
							fContext.getNatureId(), environment.getId(),
							containerPath)
					: null;
			if (install == null) {
				setError(InterpretersMessages.InterpretersComboBlock_8);
			} else {
				selectInterpreter(install);
				IFileHandle location = install.getInstallLocation();
				if (location == null) {
					setError(InterpretersMessages.InterpretersComboBlock_12);
				} else if (!location.exists()) {
					setError(InterpretersMessages.InterpretersComboBlock_13);
				}
			}
		}
	}

	private void setError(String message) {
		setStatus(new Status(IStatus.ERROR,
				DLTKDebugUIPlugin.getUniqueIdentifier(),
				IDLTKDebugUIConstants.INTERNAL_ERROR, message, null));
	}

	/**
	 * Returns the status of the interpreter selection.
	 *
	 * @return status
	 */
	public IStatus getStatus() {
		return fStatus;
	}

	private void setStatus(IStatus status) {
		fStatus = status;
	}

	/**
	 * Shows window with appropriate language preference page.
	 */
	protected void showInterpreterPreferencePage() {
		IDLTKUILanguageToolkit toolkit = DLTKUILanguageManager
				.getLanguageToolkit(getCurrentLanguageNature());
		if (toolkit != null) {
			final String pageId = toolkit.getInterpreterPreferencePage();
			if (pageId != null) {
				showPrefPage(pageId);
			}
		}
	}

	protected void fillWithWorkspaceInterpreters() {
		// fill with interpreters
		final List<IInterpreterInstall> standins = new ArrayList<>();
		final IInterpreterInstallType[] types = ScriptRuntime
				.getInterpreterInstallTypes(getCurrentLanguageNature());
		final IEnvironment environment = fContext.getEnvironment();

		for (int i = 0; i < types.length; i++) {
			IInterpreterInstallType type = types[i];
			IInterpreterInstall[] installs = type.getInterpreterInstalls();
			for (int j = 0; j < installs.length; j++) {
				final IInterpreterInstall install = installs[j];
				if (environment != null && environment.getId()
						.equals(install.getEnvironmentId())) {
					standins.add(new InterpreterStandin(install));
				}
			}
		}
		setInterpreters(standins);
	}

	protected final String getCurrentLanguageNature() {
		return fContext.getNatureId();
	}
}
