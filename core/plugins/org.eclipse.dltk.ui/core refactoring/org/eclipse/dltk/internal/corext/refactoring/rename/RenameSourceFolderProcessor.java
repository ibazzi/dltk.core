/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.corext.refactoring.rename;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringArguments;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptor;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptorComment;
import org.eclipse.dltk.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.dltk.internal.corext.refactoring.changes.RenameSourceFolderChange;
import org.eclipse.dltk.internal.corext.refactoring.code.ScriptableRefactoring;
import org.eclipse.dltk.internal.corext.refactoring.participants.ScriptProcessors;
import org.eclipse.dltk.internal.corext.util.Messages;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;


public class RenameSourceFolderProcessor extends ScriptRenameProcessor {

	private static final String ID_RENAME_SOURCE_FOLDER= "org.eclipse.dltk.ui.rename.source.folder"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PATH= "path"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME= "name"; //$NON-NLS-1$

	private IProjectFragment fSourceFolder;

	public static final String IDENTIFIER= "org.eclipse.dltk.ui.renameSourceFolderProcessor"; //$NON-NLS-1$

	/**
	 * Creates a new rename source folder processor.
	 * @param root the package fragment root, or <code>null</code> if invoked by scripting
	 */
	public RenameSourceFolderProcessor(IProjectFragment root) {
		fSourceFolder= root;
		if (root != null)
			setNewElementName(root.getElementName());
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public boolean isApplicable() throws CoreException {
		return RefactoringAvailabilityTester.isRenameAvailable(fSourceFolder);
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.RenameSourceFolderRefactoring_rename;
	}

	@Override
	protected String[] getAffectedProjectNatures() throws CoreException {
		return ScriptProcessors.computeAffectedNatures(fSourceFolder);
	}

	@Override
	public Object[] getElements() {
		return new Object[] {fSourceFolder};
	}

	@Override
	public Object getNewElement() throws CoreException {
		IProjectFragment[] roots= fSourceFolder.getScriptProject().getProjectFragments();
		for (int i= 0; i < roots.length; i++) {
			if (roots[i].getElementName().equals(getNewElementName()))
				return roots[i];
		}
		return null;
	}

	@Override
	protected RenameModifications computeRenameModifications() throws CoreException {
		RenameModifications result= new RenameModifications();
		result.rename(fSourceFolder, new RenameArguments(getNewElementName(), getUpdateReferences()));
		return result;
	}

	@Override
	protected IFile[] getChangedFiles() throws CoreException {
		return new IFile[0];
	}

	//---- IRenameProcessor ----------------------------------------------

	@Override
	public String getCurrentElementName() {
		return fSourceFolder.getElementName();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkNewElementName(String newName) throws CoreException {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		if (! newName.trim().equals(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameSourceFolderRefactoring_blank);

		IContainer c= 	fSourceFolder.getResource().getParent();
		if (! c.getFullPath().isValidSegment(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameSourceFolderRefactoring_invalid_name);

		RefactoringStatus result= RefactoringStatus.create(c.getWorkspace().validateName(newName, IResource.FOLDER));
		if (result.hasFatalError())
			return result;

		result.merge(RefactoringStatus.create(c.getWorkspace().validatePath(createNewPath(newName), IResource.FOLDER)));
		if (result.hasFatalError())
			return result;

		IScriptProject project= fSourceFolder.getScriptProject();
		IPath p= project.getProject().getFullPath().append(newName);
		if (project.findProjectFragment(p) != null)
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameSourceFolderRefactoring_already_exists);

		if (project.getProject().findMember(new Path(newName)) != null)
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.RenameSourceFolderRefactoring_alread_exists);
		return result;
	}

	private String createNewPath(String newName) {
		return fSourceFolder.getPath().removeLastSegments(1).append(newName).toString();
	}

	@Override
	protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try{
			return new RefactoringStatus();
		} finally{
			pm.done();
		}
	}

	public boolean getUpdateReferences() {
		return true;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try {
			final Map arguments= new HashMap();
			final IResource resource= fSourceFolder.getResource();
			final String project= resource.getProject().getName();
			final String newName= getNewElementName();
			final String description= Messages.format(RefactoringCoreMessages.RenameSourceFolderChange_descriptor_description_short, fSourceFolder.getElementName());
			final String header= Messages.format(RefactoringCoreMessages.RenameSourceFolderChange_descriptor_description, new String[] { resource.getFullPath().toString(), newName});
			final String comment= new ScriptRefactoringDescriptorComment(this, header).asString();
			final ScriptRefactoringDescriptor descriptor= new ScriptRefactoringDescriptor(ID_RENAME_SOURCE_FOLDER, project, description, comment, arguments, RefactoringDescriptor.NONE);
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT, descriptor.elementToHandle(DLTKCore.create(resource)));
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, newName);
			return new DynamicValidationStateChange(new RenameSourceFolderChange(descriptor, fSourceFolder, newName, comment));
		} finally {
			pm.done();
		}
	}

	@Override
	public RefactoringStatus initialize(RefactoringArguments arguments) {
		if (arguments instanceof ScriptRefactoringArguments) {
			final ScriptRefactoringArguments generic= (ScriptRefactoringArguments) arguments;
			final String path= generic.getAttribute(ATTRIBUTE_PATH);
			if (path != null) {
				final IResource resource= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
				if (resource == null || !resource.exists())
					return ScriptableRefactoring.createInputFatalStatus(resource, getRefactoring().getName(), ID_RENAME_SOURCE_FOLDER);
				else
					fSourceFolder= (IProjectFragment) DLTKCore.create(resource);
			} else
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_PATH));
			final String name= generic.getAttribute(ATTRIBUTE_NAME);
			if (name != null && !"".equals(name)) //$NON-NLS-1$
				setNewElementName(name);
			else
				return RefactoringStatus.createFatalErrorStatus(Messages.format(RefactoringCoreMessages.InitializableRefactoring_argument_not_exist, ATTRIBUTE_NAME));
		} else
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.InitializableRefactoring_inacceptable_arguments);
		return new RefactoringStatus();
	}
}
