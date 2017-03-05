/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.DLTKUIPlugin;


public abstract class NewPackageCreationWizard extends NewElementWizard {

	private NewPackageWizardPage fPage;

	public NewPackageCreationWizard() {
		super();
		setDefaultPageImageDescriptor(DLTKPluginImages.DESC_WIZBAN_NEWPACK);
		setDialogSettings(DLTKUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(NewWizardMessages.NewPackageCreationWizard_title);
	}

	protected abstract NewPackageWizardPage createNewPackageWizardPage();

	@Override
	public void addPages() {
		super.addPages();
		fPage= createNewPackageWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		fPage.createPackage(monitor); // use the full progress monitor
	}

	@Override
	public boolean performFinish() {
		boolean res= super.performFinish();
		if (res) {
			selectAndReveal(fPage.getModifiedResource());
		}
		return res;
	}

	@Override
	public IModelElement getCreatedElement() {
		return fPage.getNewScriptFolder();
	}

}
