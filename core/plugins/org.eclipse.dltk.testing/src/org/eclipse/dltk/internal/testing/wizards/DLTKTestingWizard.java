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
package org.eclipse.dltk.internal.testing.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dltk.internal.testing.util.ExceptionHandler;
import org.eclipse.dltk.testing.DLTKTestingPlugin;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * The wizard base class for JUnit creation wizards.
 */
public abstract class DLTKTestingWizard extends Wizard implements INewWizard {

	private IWorkbench fWorkbench;
	protected static final String DIALOG_SETTINGS_KEY = "JUnitWizards"; //$NON-NLS-1$
	private IStructuredSelection fSelection;

	public DLTKTestingWizard() {
		setNeedsProgressMonitor(true);
		initializeDefaultPageImageDescriptor();
	}

	@Override
	public abstract boolean performFinish();

	/*
	 * Run a runnable
	 */
	protected boolean finishPage(IRunnableWithProgress runnable) {
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(
				runnable);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(
					getContainer(), op,
					ResourcesPlugin.getWorkspace().getRoot());

		} catch (InvocationTargetException e) {
			Shell shell = getShell();
			String title = WizardMessages.NewJUnitWizard_op_error_title;
			String message = WizardMessages.NewJUnitWizard_op_error_message;
			ExceptionHandler.handle(e, shell, title, message);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	protected void openResource(final IResource resource) {
		if (resource.getType() == IResource.FILE) {
			final IWorkbenchPage activePage = DLTKTestingPlugin.getActivePage();
			if (activePage != null) {
				final Display display = Display.getDefault();
				if (display != null) {
					display.asyncExec(() -> {
						try {
							IDE.openEditor(activePage, (IFile) resource, true);
						} catch (PartInitException e) {
							DLTKTestingPlugin.log(e);
						}
					});
				}
			}
		}
	}

	@Override
	public void init(IWorkbench workbench,
			IStructuredSelection currentSelection) {
		fWorkbench = workbench;
		fSelection = currentSelection;
	}

	public IStructuredSelection getSelection() {
		return fSelection;
	}

	protected void selectAndReveal(IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource,
				fWorkbench.getActiveWorkbenchWindow());
	}

	protected void initDialogSettings() {
		IDialogSettings pluginSettings = DLTKTestingPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = pluginSettings
				.getSection(DIALOG_SETTINGS_KEY);
		if (wizardSettings == null) {
			wizardSettings = new DialogSettings(DIALOG_SETTINGS_KEY);
			pluginSettings.addSection(wizardSettings);
		}
		setDialogSettings(wizardSettings);
	}

	protected abstract void initializeDefaultPageImageDescriptor();
}
