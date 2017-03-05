/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.wizards.buildpath;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage;
import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.dltk.ui.wizards.IBuildpathContainerPageExtension;
import org.eclipse.dltk.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BuildpathContainerDefaultPage extends NewElementWizardPage implements IBuildpathContainerPage, IBuildpathContainerPageExtension {

	private StringDialogField fEntryField;
	private ArrayList fUsedPaths;

	/**
	 * Constructor for BuildpathContainerDefaultPage.
	 */
	public BuildpathContainerDefaultPage() {
		super("BuildpathContainerDefaultPage"); //$NON-NLS-1$
		setTitle(NewWizardMessages.BuildpathContainerDefaultPage_title);
		setDescription(NewWizardMessages.BuildpathContainerDefaultPage_description);
		setImageDescriptor(DLTKPluginImages.DESC_WIZBAN_ADD_LIBRARY);

		fUsedPaths= new ArrayList();

		fEntryField= new StringDialogField();
		fEntryField.setLabelText(NewWizardMessages.BuildpathContainerDefaultPage_path_label);
		fEntryField.setDialogFieldListener(field -> validatePath());
		validatePath();
	}

	private void validatePath() {
		StatusInfo status= new StatusInfo();
		String str= fEntryField.getText();
		if (str.length() == 0) {
			status.setError(NewWizardMessages.BuildpathContainerDefaultPage_path_error_enterpath);
		} else if (!Path.ROOT.isValidPath(str)) {
			status.setError(NewWizardMessages.BuildpathContainerDefaultPage_path_error_invalidpath);
		} else {
			IPath path= new Path(str);
			if (path.segmentCount() == 0) {
				status.setError(NewWizardMessages.BuildpathContainerDefaultPage_path_error_needssegment);
			} else if (fUsedPaths.contains(path)) {
				status.setError(NewWizardMessages.BuildpathContainerDefaultPage_path_error_alreadyexists);
			}
		}
		updateStatus(status);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);

		fEntryField.doFillIntoGrid(composite, 2);
		LayoutUtil.setHorizontalGrabbing(fEntryField.getTextControl(null));

		fEntryField.setFocus();

		setControl(composite);
		Dialog.applyDialogFont(composite);
		if(DLTKCore.DEBUG) {
			System.err.println("BuildpathContainerDefaultPage: add help support"); //$NON-NLS-1$
		}
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IDLTKHelpContextIds.CLASSPATH_CONTAINER_DEFAULT_PAGE);
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public IBuildpathEntry getSelection() {
		return DLTKCore.newContainerEntry(new Path(fEntryField.getText()));
	}

	@Override
	public void initialize(IScriptProject project, IBuildpathEntry[] currentEntries) {
		for (int i= 0; i < currentEntries.length; i++) {
			IBuildpathEntry curr= currentEntries[i];
			if (curr.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
				fUsedPaths.add(curr.getPath());
			}
		}
	}

	@Override
	public void setSelection(IBuildpathEntry containerEntry) {
		if (containerEntry != null) {
			fUsedPaths.remove(containerEntry.getPath());
			fEntryField.setText(containerEntry.getPath().toString());
		} else {
			fEntryField.setText(""); //$NON-NLS-1$
		}
	}

}
