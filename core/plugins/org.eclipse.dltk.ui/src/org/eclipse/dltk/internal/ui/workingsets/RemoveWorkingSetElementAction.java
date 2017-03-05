/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class RemoveWorkingSetElementAction extends SelectionDispatchAction {

	public RemoveWorkingSetElementAction(IWorkbenchSite site) {
		super(site);
		setText(WorkingSetMessages.RemoveWorkingSetElementAction_label);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		IWorkingSet workingSet= getWorkingSet(selection);
		setEnabled(workingSet != null && !WorkingSetIDs.OTHERS.equals(workingSet.getId()));
	}

	private IWorkingSet getWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection))
			return null;
		ITreeSelection treeSelection= (ITreeSelection)selection;
		List elements= treeSelection.toList();
		IWorkingSet result= null;
		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			Object element= iter.next();
			TreePath[] paths= treeSelection.getPathsFor(element);
			if (paths.length != 1)
				return null;
			TreePath path= paths[0];
			if (path.getSegmentCount() != 2)
				return null;
			Object candidate= path.getSegment(0);
			if (!(candidate instanceof IWorkingSet))
				return null;
			if (result == null) {
				result= (IWorkingSet)candidate;
			} else {
				if (result != candidate)
					return null;
			}
		}
		return result;
	}

	@Override
	public void run(IStructuredSelection selection) {
		IWorkingSet ws= getWorkingSet(selection);
		if (ws == null)
			return;
		List<IAdaptable> elements = new ArrayList<IAdaptable>(
				Arrays.asList(ws.getElements()));
		List selectedElements= selection.toList();
		for (Iterator iter= selectedElements.iterator(); iter.hasNext();) {
			elements.remove(iter.next());
		}
		ws.setElements(elements.toArray(new IAdaptable[elements.size()]));
	}
}
