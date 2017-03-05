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
package org.eclipse.dltk.internal.ui.wizards.buildpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathContainer;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.ModelManager;
import org.eclipse.dltk.internal.core.UserLibrary;
import org.eclipse.dltk.internal.core.UserLibraryManager;
import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPage;
import org.eclipse.dltk.internal.ui.wizards.IBuildpathContainerPageExtension2;
import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.dltk.ui.preferences.UserLibraryPreferencePage;
import org.eclipse.dltk.ui.wizards.IBuildpathContainerPageExtension;
import org.eclipse.dltk.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.ibm.icu.text.Collator;

public class UserLibraryWizardPage extends NewElementWizardPage implements
		IBuildpathContainerPage, IBuildpathContainerPageExtension,
		IBuildpathContainerPageExtension2 {

	private CheckedListDialogField fLibrarySelector;
	private BPUserLibraryElement fEditResult;
	private Set fUsedPaths;
	private boolean fIsEditMode;
	private IScriptProject fProject;
	private boolean fIsExported;
	private IDLTKLanguageToolkit languageToolkit;

	public UserLibraryWizardPage() {
		super("UserLibraryWizardPage"); //$NON-NLS-1$
		setTitle(NewWizardMessages.UserLibraryWizardPage_title);
		setImageDescriptor(DLTKPluginImages.DESC_WIZBAN_ADD_LIBRARY);
		updateDescription(null);
		fUsedPaths = new HashSet();
		fProject = createPlaceholderProject();

		LibraryListAdapter adapter = new LibraryListAdapter();
		String[] buttonLabels = new String[] { NewWizardMessages.UserLibraryWizardPage_list_config_button };
		IDecoratorManager decoratorManager = PlatformUI.getWorkbench()
				.getDecoratorManager();
		fLibrarySelector = new CheckedListDialogField(adapter, buttonLabels,
				new DecoratingLabelProvider(new BPListLabelProvider(),
						decoratorManager));
		fLibrarySelector.setDialogFieldListener(adapter);
		fLibrarySelector
				.setLabelText(NewWizardMessages.UserLibraryWizardPage_list_label);
		fEditResult = null;
		updateStatus(validateSetting(Collections.EMPTY_LIST));
	}

	private static IScriptProject createPlaceholderProject() {
		String name = "####internal"; //$NON-NLS-1$
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		while (true) {
			IProject project = root.getProject(name);
			if (!project.exists()) {
				return DLTKCore.create(project);
			}
			name += '1';
		}
	}

	private void updateDescription(IBuildpathEntry containerEntry) {
		if (containerEntry == null
				|| containerEntry.getPath().segmentCount() != 2) {
			setDescription(NewWizardMessages.UserLibraryWizardPage_description_new);
		} else {
			setDescription(NewWizardMessages.UserLibraryWizardPage_description_edit);
		}
	}

	private List updateLibraryList() {
		HashSet oldNames = new HashSet();
		HashSet oldCheckedNames = new HashSet();
		List oldElements = fLibrarySelector.getElements();
		for (int i = 0; i < oldElements.size(); i++) {
			BPUserLibraryElement curr = (BPUserLibraryElement) oldElements
					.get(i);
			oldNames.add(curr.getName());
			if (fLibrarySelector.isChecked(curr)) {
				oldCheckedNames.add(curr.getName());
			}
		}

		ArrayList entriesToCheck = new ArrayList();

		IDLTKLanguageToolkit toolkit = null;
		toolkit = DLTKLanguageManager.getLanguageToolkit(fProject);
		if( toolkit == null ) {
			toolkit = this.languageToolkit;
		}
		String[] names = DLTKCore.getUserLibraryNames(toolkit);
		Arrays.sort(names, Collator.getInstance());

		ArrayList elements = new ArrayList(names.length);
		for (int i = 0; i < names.length; i++) {
			String curr = names[i];
			UserLibrary lib = ModelManager.getUserLibraryManager()
					.getUserLibrary(curr, toolkit);
			if (lib != null) {
				IPath path = new Path(DLTKCore.USER_LIBRARY_CONTAINER_ID)
						.append(UserLibraryManager.makeLibraryName(curr,
								toolkit));
				try {
					IBuildpathContainer container = DLTKCore
							.getBuildpathContainer(path, fProject);
					BPUserLibraryElement elem = new BPUserLibraryElement(curr,
							container, fProject, lib.getAttributes());
					elements.add(elem);
					if (!oldCheckedNames.isEmpty()) {
						if (oldCheckedNames.contains(curr)) {
							entriesToCheck.add(elem);
						}
					} else {
						if (!oldNames.contains(curr)) {
							entriesToCheck.add(elem);
						}
					}
				} catch (ModelException e) {
					DLTKUIPlugin.log(e);
					// ignore
				}
			}
		}
		fLibrarySelector.setElements(elements);
		return entriesToCheck;
	}

	private void doDialogFieldChanged(DialogField field) {
		if (field == fLibrarySelector) {
			List list = fLibrarySelector.getCheckedElements();
			if (fIsEditMode) {
				if (list.size() > 1) {
					if (fEditResult != null && list.remove(fEditResult)) {
						fLibrarySelector.setCheckedWithoutUpdate(fEditResult,
								false);
					}
					fEditResult = (BPUserLibraryElement) list.get(0); // take
					// the
					// first
					for (int i = 1; i < list.size(); i++) { // uncheck the rest
						fLibrarySelector.setCheckedWithoutUpdate(list.get(i),
								false);
					}
				} else if (list.size() == 1) {
					fEditResult = (BPUserLibraryElement) list.get(0);
				}
			}
			updateStatus(validateSetting(list));
		}
	}

	private IStatus validateSetting(List selected) {
		int nSelected = selected.size();
		if (nSelected == 0) {
			return new StatusInfo(IStatus.ERROR,
					NewWizardMessages.UserLibraryWizardPage_error_selectentry);
		} else if (fIsEditMode && nSelected > 1) {
			return new StatusInfo(IStatus.ERROR,
					NewWizardMessages.UserLibraryWizardPage_error_selectonlyone);
		}
		for (int i = 0; i < selected.size(); i++) {
			BPUserLibraryElement curr = (BPUserLibraryElement) selected.get(i);
			if (fUsedPaths.contains(curr.getPath())) {
				return new StatusInfo(
						IStatus.ERROR,
						NewWizardMessages.UserLibraryWizardPage_error_alreadyoncp);
			}
		}
		return new StatusInfo();
	}

	private void doButtonPressed(int index) {
		if (index == 0) {
			HashMap data = new HashMap(3);
			if (fEditResult != null) {
				data.put(UserLibraryPreferencePage.DATA_LIBRARY_TO_SELECT,
						fEditResult.getName());
			}
			String id = UserLibraryPreferencePage.getPreferencePageId(languageToolkit);
			PreferencesUtil.createPreferenceDialogOn(getShell(), id,
					new String[] { id }, data).open();

			List newEntries = updateLibraryList();
			if (newEntries.size() > 0) {
				if (fIsEditMode) {
					fLibrarySelector.setChecked(newEntries.get(0), true);
				} else {
					fLibrarySelector.setCheckedElements(newEntries);
				}
			}
		} else {
			fLibrarySelector.setCheckedElements(fLibrarySelector
					.getSelectedElements());
		}
	}

	private void doDoubleClicked(ListDialogField field) {
		if (field == fLibrarySelector) {
			List list = fLibrarySelector.getSelectedElements();
			if (list.size() == 1) {
				Object elem = list.get(0);
				boolean state = fLibrarySelector.isChecked(elem);
				if (!state || !fIsEditMode) {
					fLibrarySelector.setChecked(elem, !state);
				}
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		LayoutUtil.doDefaultLayout(composite,
				new DialogField[] { fLibrarySelector }, true, SWT.DEFAULT,
				SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(fLibrarySelector.getListControl(null));
		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public IBuildpathEntry getSelection() {
		if (fEditResult != null) {
			return DLTKCore.newContainerEntry(fEditResult.getPath(),
					fIsExported);
		}
		return null;
	}

	@Override
	public IBuildpathEntry[] getNewContainers() {
		List selected = fLibrarySelector.getCheckedElements();
		IBuildpathEntry[] res = new IBuildpathEntry[selected.size()];
		for (int i = 0; i < res.length; i++) {
			BPUserLibraryElement curr = (BPUserLibraryElement) selected.get(i);
			res[i] = DLTKCore.newContainerEntry(curr.getPath(), fIsExported);
		}
		return res;
	}

	@Override
	public void setSelection(IBuildpathEntry containerEntry) {
		fIsExported = containerEntry != null && containerEntry.isExported();

		updateDescription(containerEntry);
		fIsEditMode = (containerEntry != null);
		if (containerEntry != null) {
			fUsedPaths.remove(containerEntry.getPath());
		}

		String selected = null;
		if (containerEntry != null
				&& containerEntry.getPath().segmentCount() == 2) {
			selected = containerEntry.getPath().segment(1);
		} else {
			// get from dialog store
		}
		updateLibraryList();
		if (selected != null) {
			List elements = fLibrarySelector.getElements();
			for (int i = 0; i < elements.size(); i++) {
				BPUserLibraryElement curr = (BPUserLibraryElement) elements
						.get(i);
				if (curr.getName().equals(selected)) {
					fLibrarySelector.setChecked(curr, true);
					return;
				}
			}
		}
	}

	private class LibraryListAdapter implements IListAdapter,
			IDialogFieldListener {

		public LibraryListAdapter() {
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
			doDialogFieldChanged(field);
		}

		@Override
		public void customButtonPressed(ListDialogField field, int index) {
			doButtonPressed(index);
		}

		@Override
		public void selectionChanged(ListDialogField field) {
		}

		@Override
		public void doubleClicked(ListDialogField field) {
			doDoubleClicked(field);
		}
	}

	@Override
	public void initialize(IScriptProject project,
			IBuildpathEntry[] currentEntries) {
		this.languageToolkit = DLTKLanguageManager
				.getLanguageToolkit(project);
		for (int i = 0; i < currentEntries.length; i++) {
			IBuildpathEntry curr = currentEntries[i];
			if (curr.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
				fUsedPaths.add(curr.getPath());
			}
		}
	}
}
