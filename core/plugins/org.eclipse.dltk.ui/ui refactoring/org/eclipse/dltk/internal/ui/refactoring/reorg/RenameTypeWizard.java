/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *

 *******************************************************************************/
package org.eclipse.dltk.internal.ui.refactoring.reorg;

import org.eclipse.dltk.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.Refactoring;

/**
 * The type renaming wizard.
 */
public class RenameTypeWizard extends RenameRefactoringWizard {

	public RenameTypeWizard(Refactoring refactoring) {
		this(refactoring, RefactoringMessages.RenameTypeWizard_defaultPageTitle, RefactoringMessages.RenameTypeWizardInputPage_description, DLTKPluginImages.DESC_WIZBAN_REFACTOR_TYPE,
				""/*IScriptHelpContextIds.RENAME_TYPE_WIZARD_PAGE*/); //$NON-NLS-1$
	}

	public RenameTypeWizard(Refactoring refactoring, String defaultPageTitle, String inputPageDescription, ImageDescriptor inputPageImageDescriptor, String pageContextHelpId) {
		super(refactoring, defaultPageTitle, inputPageDescription, inputPageImageDescriptor, pageContextHelpId);
	}

	@Override
	protected void addUserInputPages() {
		super.addUserInputPages();
//		if (isRenameType())
//			addPage(new RenameTypeWizardSimilarElementsPage());

	}


	protected boolean isRenameType() {
		return true;
	}

}
