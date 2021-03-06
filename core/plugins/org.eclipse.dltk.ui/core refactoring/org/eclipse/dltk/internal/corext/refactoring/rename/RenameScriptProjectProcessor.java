/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.corext.refactoring.rename;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IScriptProjectFilenames;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringArguments;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptor;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptorComment;
import org.eclipse.dltk.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.dltk.internal.corext.refactoring.changes.RenameScriptProjectChange;
import org.eclipse.dltk.internal.corext.refactoring.code.ScriptableRefactoring;
import org.eclipse.dltk.internal.corext.refactoring.participants.ScriptProcessors;
import org.eclipse.dltk.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.dltk.internal.corext.util.Messages;
import org.eclipse.dltk.internal.corext.util.Resources;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;


public class RenameScriptProjectProcessor extends ScriptRenameProcessor implements IReferenceUpdating {

	private static final String ID_RENAME_SCRIPT_PROJECT= "org.eclipse.dltk.ui.rename.script.project"; //$NON-NLS-1$
	private static final String ATTRIBUTE_REFERENCES= "references"; //$NON-NLS-1$

	private IScriptProject fProject;
	private boolean fUpdateReferences;

	public static final String IDENTIFIER= "org.eclipse.dltk.ui.renameScriptProjectProcessor"; //$NON-NLS-1$

	/**
	 * Creates a new renamescriptproject processor.
	 * @param project thescriptproject, or <code>null</code> if invoked by scripting
	 */
	public RenameScriptProjectProcessor(IScriptProject project) {
		fProject= project;
		if (fProject != null)
			setNewElementName(fProject.getElementName());
		fUpdateReferences= true;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return RefactoringAvailabilityTester.isRenameAvailable(fProject);
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.RenameScriptProjectRefactoring_rename;
	}

	@Override
	protected String[] getAffectedProjectNatures() throws CoreException {
		return ScriptProcessors.computeAffectedNatures(fProject);
	}

	@Override
	public Object[] getElements() {
		return new Object[] {fProject};
	}

	@Override
	public Object getNewElement() throws CoreException {
		IPath newPath= fProject.getPath().removeLastSegments(1).append(getNewElementName());
		return DLTKCore.create(ResourcesPlugin.getWorkspace().getRoot().findMember(newPath));
	}

	@Override
	protected RenameModifications computeRenameModifications() throws CoreException {
		RenameModifications result= new RenameModifications();
		result.rename(fProject, new RenameArguments(getNewElementName(), getUpdateReferences()));
		return result;
	}

	@Override
	protected IFile[] getChangedFiles() throws CoreException {
		IFile projectFile = fProject.getProject().getFile(
				IScriptProjectFilenames.PROJECT_FILENAME);
		if (projectFile != null && projectFile.exists()) {
			return new IFile[] {projectFile};
		}
		return new IFile[0];
	}

	//---- IReferenceUpdating --------------------------------------

	@Override
	public boolean canEnableUpdateReferences() {
		return true;
	}

	@Override
	public void setUpdateReferences(boolean update) {
		fUpdateReferences= update;
	}

	@Override
	public boolean getUpdateReferences() {
		return fUpdateReferences;
	}

	//---- IRenameProcessor ----------------------------------------------

	@Override
	public String getCurrentElementName() {
		return fProject.getElementName();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkNewElementName(String newName) throws CoreException {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		RefactoringStatus result= RefactoringStatus.create(ResourcesPlugin.getWorkspace().validateName(newName, IResource.PROJECT));
		if (result.hasFatalError())
			return result;

		if (projectNameAlreadyExists(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameScriptProjectRefactoring_already_exists);
		if (projectFolderAlreadyExists(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameScriptProjectProcessor_folder_already_exists);

		return new RefactoringStatus();
	}

	@Override
	protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try{
			if (isReadOnly()){
				String message= Messages.format(RefactoringCoreMessages.RenameScriptProjectRefactoring_read_only,
									fProject.getElementName());
				return RefactoringStatus.createErrorStatus(message);
			}
			return new RefactoringStatus();
		} finally{
			pm.done();
		}
	}

	private boolean isReadOnly() {
		return Resources.isReadOnly(fProject.getResource());
	}

	private boolean projectNameAlreadyExists(String newName){
		return ResourcesPlugin.getWorkspace().getRoot().getProject(newName).exists();
	}

	private boolean projectFolderAlreadyExists(String newName) throws CoreException {
		boolean isNotInWorkpace= fProject.getProject().getDescription().getLocationURI() != null;
		if (isNotInWorkpace)
			return false; // projects outside of the workspace are not renamed
		URI locationURI= fProject.getProject().getLocationURI();
		IFileStore projectStore= EFS.getStore(locationURI);
		IFileStore newProjectStore= projectStore.getParent().getChild(newName);
		return newProjectStore.fetchInfo().exists();
	}

	//--- changes

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			final Map arguments= new HashMap();
			final String description= Messages.format(RefactoringCoreMessages.RenameScriptProjectProcessor_descriptor_description_short, fProject.getElementName());
			final String header= Messages.format(RefactoringCoreMessages.RenameScriptProjectChange_descriptor_description, new String[] { fProject.getElementName(), getNewElementName()});
			final String comment= new ScriptRefactoringDescriptorComment(this, header).asString();
			final ScriptRefactoringDescriptor descriptor= new ScriptRefactoringDescriptor(RenameScriptProjectProcessor.ID_RENAME_SCRIPT_PROJECT, null, description, comment, arguments, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE);
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT, descriptor.elementToHandle(fProject));
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, getNewElementName());
			arguments.put(ATTRIBUTE_REFERENCES, Boolean.valueOf(fUpdateReferences).toString());
			return new DynamicValidationStateChange(new RenameScriptProjectChange(descriptor, fProject, getNewElementName(), comment, fUpdateReferences));
		} finally {
			pm.done();
		}
	}

	@Override
	public RefactoringStatus initialize(RefactoringArguments arguments) {
		if (arguments instanceof ScriptRefactoringArguments) {
			final ScriptRefactoringArguments extended= (ScriptRefactoringArguments) arguments;
			final String handle= extended.getAttribute(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT);
			if (handle != null) {
				final IModelElement element= ScriptRefactoringDescriptor.handleToElement(extended.getProject(), handle, false);
				if (element == null || !element.exists() || element.getElementType() != IModelElement.SCRIPT_PROJECT)
					return ScriptableRefactoring.createInputFatalStatus(element, getRefactoring().getName(), ID_RENAME_SCRIPT_PROJECT);
				else
					fProject= (IScriptProject) element;
			} else
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ScriptRefactoringDescriptor.ATTRIBUTE_INPUT));
			final String name= extended.getAttribute(ScriptRefactoringDescriptor.ATTRIBUTE_NAME);
			if (name != null && !"".equals(name)) //$NON-NLS-1$
				setNewElementName(name);
			else
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ScriptRefactoringDescriptor.ATTRIBUTE_NAME));
			final String references= extended.getAttribute(ATTRIBUTE_REFERENCES);
			if (references != null) {
				fUpdateReferences= Boolean.valueOf(references).booleanValue();
			} else
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_REFERENCES));
		} else
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.InitializableRefactoring_inacceptable_arguments);
		return new RefactoringStatus();
	}
}
