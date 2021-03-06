/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.actions;

import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.ui.actions.ActionMessages;
import org.eclipse.dltk.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.dltk.internal.ui.typehierarchy.OpenTypeHierarchyUtil;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public abstract class OpenTypeInHierarchyAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	protected abstract IDLTKUILanguageToolkit getLanguageToolkit();
	public OpenTypeInHierarchyAction() {
		super();
		setText(ActionMessages.OpenTypeInHierarchyAction_label);
		setDescription(ActionMessages.OpenTypeInHierarchyAction_description);
		setToolTipText(ActionMessages.OpenTypeInHierarchyAction_tooltip);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.OPEN_TYPE_IN_HIERARCHY_ACTION);
	}

	@Override
	public void run() {
		Shell parent= DLTKUIPlugin.getActiveWorkbenchShell();
		OpenTypeSelectionDialog2 dialog= new OpenTypeSelectionDialog2(parent, false,
			PlatformUI.getWorkbench().getProgressService(),
			SearchEngine.createWorkspaceScope(this.getLanguageToolkit().getCoreToolkit()), IDLTKSearchConstants.TYPE, this.getLanguageToolkit() );

		dialog.setTitle(ActionMessages.OpenTypeInHierarchyAction_dialogTitle);
		dialog.setMessage(ActionMessages.OpenTypeInHierarchyAction_dialogMessage);
		int result= dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;

		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type= (IType)types[0];
			OpenTypeHierarchyUtil.open(new IType[] { type }, fWindow);
		}
	}

	//---- IWorkbenchWindowActionDelegate ------------------------------------------------

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void dispose() {
		fWindow= null;
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing. Action doesn't depend on selection.
	}
}
