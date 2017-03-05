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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKContentTypeManager;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKFeatures;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.corext.refactoring.Checks;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringArguments;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptor;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringDescriptorComment;
import org.eclipse.dltk.internal.corext.refactoring.changes.DynamicValidationStateChange;
import org.eclipse.dltk.internal.corext.refactoring.changes.RenameResourceChange;
import org.eclipse.dltk.internal.corext.refactoring.changes.RenameSourceModuleChange;
import org.eclipse.dltk.internal.corext.refactoring.code.ScriptableRefactoring;
import org.eclipse.dltk.internal.corext.refactoring.participants.ScriptProcessors;
import org.eclipse.dltk.internal.corext.refactoring.tagging.IQualifiedNameUpdating;
import org.eclipse.dltk.internal.corext.refactoring.tagging.IReferenceUpdating;
import org.eclipse.dltk.internal.corext.refactoring.tagging.ISimilarDeclarationUpdating;
import org.eclipse.dltk.internal.corext.refactoring.tagging.ITextUpdating;
import org.eclipse.dltk.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.dltk.internal.corext.util.Messages;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IResourceMapper;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;

public class RenameSourceModuleProcessor extends ScriptRenameProcessor
		implements IReferenceUpdating, ITextUpdating, IQualifiedNameUpdating,
		ISimilarDeclarationUpdating, IResourceMapper {

	private static final String ID_RENAME_COMPILATION_UNIT = "org.eclipse.dltk.ui.rename.sourcemodule"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$

	private ISourceModule fCu;

	public static final String IDENTIFIER = "org.eclipse.dltk.ui.renameSourceModulerocessor"; //$NON-NLS-1$

	/**
	 * Creates a new rename compilation unit processor.
	 *
	 * @param unit
	 *            the compilation unit, or <code>null</code> if invoked by
	 *            scripting
	 * @throws CoreException
	 */
	public RenameSourceModuleProcessor(ISourceModule unit) throws CoreException {
		fCu = unit;
		if (fCu != null) {
			computeRenameTypeRefactoring();
			setNewElementName(fCu.getElementName());
		}
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public boolean isApplicable() {
		return RefactoringAvailabilityTester.isRenameAvailable(fCu);
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.RenameSourceModuleRefactoring_name;
	}

	@Override
	protected String[] getAffectedProjectNatures() throws CoreException {
		return ScriptProcessors.computeAffectedNatures(fCu);
	}

	@Override
	public Object[] getElements() {
		return new Object[] { fCu };
	}

	@Override
	protected RenameModifications computeRenameModifications() {
		RenameModifications result = new RenameModifications();
		result.rename(fCu, new RenameArguments(getNewElementName(),
				getUpdateReferences()));
		if (DLTKCore.DEBUG) {
			System.err.println("TODO: Add type renaming here if required..."); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	protected IFile[] getChangedFiles() throws CoreException {
		IFile file = ResourceUtil.getFile(fCu);
		if (file != null) {
			return new IFile[] { file };
		}
		return new IFile[0];
	}

	// ---- IRenameProcessor -------------------------------------

	@Override
	public String getCurrentElementName() {
		if (isFileExtensionRequired()) {
			// return initial name without extension
			return getSimpleCUName();
		} else {
			// return initial name with extension
			return fCu.getElementName();
		}
	}

	/**
	 * Tests if script file must have an extension for this language
	 *
	 * @return
	 */
	public boolean isFileExtensionRequired() {
		final IDLTKLanguageToolkit tk = DLTKLanguageManager
				.getLanguageToolkit(fCu);
		return tk != null && tk.get(DLTKFeatures.FILE_EXTENSION_REQUIRED);
	}

	@Override
	public String getCurrentElementQualifier() {
		IScriptFolder pack = (IScriptFolder) fCu.getParent();
		return pack.getElementName();
	}

	@Override
	public RefactoringStatus checkNewElementName(String newName)
			throws CoreException {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		// String typeName= removeFileNameExtension(newName);
		if (DLTKCore.DEBUG) {
			System.err.println("TODO: Add correct name cheking here..."); //$NON-NLS-1$
		}
		RefactoringStatus result = new RefactoringStatus();// Checks.checkSourceModuleName(newName);
		if (Checks.isAlreadyNamed(fCu, newName))
			result
					.addFatalError(RefactoringCoreMessages.RenameSourceModuleRefactoring_same_name);
		return result;
	}

	@Override
	public void setNewElementName(String newName) {
		super.setNewElementName(newName);
	}

	@Override
	public Object getNewElement() {
		IModelElement parent = fCu.getParent();
		if (parent.getElementType() != IModelElement.SCRIPT_FOLDER)
			return fCu; // ??
		IScriptFolder pack = (IScriptFolder) parent;
		IDLTKLanguageToolkit tk = null;
		tk = DLTKLanguageManager.getLanguageToolkit(pack);
		if (tk != null
				&& !DLTKContentTypeManager.isValidFileNameForContentType(tk,
						getNewElementName())) {
			return fCu; // ??
		}
		return pack.getSourceModule(getNewElementName());
	}

	// ---- ITextUpdating ---------------------------------------------

	@Override
	public boolean canEnableTextUpdating() {
		return false;
	}

	@Override
	public boolean getUpdateTextualMatches() {
		return false;
	}

	@Override
	public void setUpdateTextualMatches(boolean update) {
	}

	// ---- IReferenceUpdating -----------------------------------

	@Override
	public boolean canEnableUpdateReferences() {
		return false;
	}

	@Override
	public void setUpdateReferences(boolean update) {
	}

	@Override
	public boolean getUpdateReferences() {
		return false;
	}

	// ---- IQualifiedNameUpdating -------------------------------

	@Override
	public boolean canEnableQualifiedNameUpdating() {
		return false;
	}

	@Override
	public boolean getUpdateQualifiedNames() {
		return false;
	}

	@Override
	public void setUpdateQualifiedNames(boolean update) {
	}

	@Override
	public String getFilePatterns() {
		return null;
	}

	@Override
	public void setFilePatterns(String patterns) {
	}

	// ---- ISimilarDeclarationUpdating ------------------------------

	@Override
	public boolean canEnableSimilarDeclarationUpdating() {
		return false;
	}

	@Override
	public void setUpdateSimilarDeclarations(boolean update) {
		return;
	}

	@Override
	public boolean getUpdateSimilarDeclarations() {
		return false;
	}

	@Override
	public int getMatchStrategy() {
		return RenamingNameSuggestor.STRATEGY_EXACT; // method should not be
		// called in this case
		// anyway ...
	}

	@Override
	public void setMatchStrategy(int selectedStrategy) {
		return;
	}

	public IModelElement[] getSimilarElements() {
		return null;
	}

	@Override
	public IResource getRefactoredResource(IResource element) {
		return element;
	}

	public IModelElement getRefactoredScriptElement(IModelElement element) {
		return element;
	}

	// --- preconditions ----------------------------------

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException {

		// we purposely do not check activation of the renameTypeRefactoring
		// here.
		return new RefactoringStatus();
	}

	@Override
	protected RefactoringStatus doCheckFinalConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws CoreException {
		try {
			return Checks.checkSourceModuleNewName(fCu, getNewElementName());
		} finally {
			pm.done();
		}
	}

	private void computeRenameTypeRefactoring() throws CoreException {
		if (getSimpleCUName().indexOf(".") != -1) { //$NON-NLS-1$
			return;
		}
	}

	private String getSimpleCUName() {
		return removeFileNameExtension(fCu.getElementName());
	}

	/**
	 * Removes the extension (whatever comes after the last '.') from the given
	 * file name.
	 */
	public static String removeFileNameExtension(String fileName) {
		if (fileName.lastIndexOf(".") == -1) //$NON-NLS-1$
			return fileName;
		return fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException {
		// renaming the file is taken care of in renameTypeRefactoring
		final String newName = getNewElementName();
		final IResource resource = ResourceUtil.getResource(fCu);
		if (resource != null && resource.isLinked()) {
			final Map arguments = new HashMap();
			final IProject project = resource.getProject();
			final String name = project.getName();
			final String description = Messages
					.format(
							RefactoringCoreMessages.RenameSourceModuleChange_descriptor_description_short,
							resource.getName());
			final String header = Messages
					.format(
							RefactoringCoreMessages.RenameSourceModuleChange_descriptor_description,
							new String[] { resource.getFullPath().toString(),
									newName });
			final String comment = new ScriptRefactoringDescriptorComment(this,
					header).asString();
			final ScriptRefactoringDescriptor descriptor = new ScriptRefactoringDescriptor(
					RenameResourceProcessor.ID_RENAME_RESOURCE,
					name,
					description,
					comment,
					arguments,
					(RefactoringDescriptor.STRUCTURAL_CHANGE
							| RefactoringDescriptor.MULTI_CHANGE | RefactoringDescriptor.BREAKING_CHANGE));
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT,
					ScriptRefactoringDescriptor
							.resourceToHandle(name, resource));
			arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, newName);
			return new DynamicValidationStateChange(new RenameResourceChange(
					descriptor, resource, newName, comment));
		}
		String label = null;
		if (fCu != null) {
			final IScriptFolder fragment = (IScriptFolder) fCu.getParent();
			if (!fragment.isRootFolder())
				label = fragment.getElementName() + "." + fCu.getElementName(); //$NON-NLS-1$
			else
				label = fCu.getElementName();
		} else
			label = fCu.getElementName();
		final Map arguments = new HashMap();
		final String name = fCu.getScriptProject().getElementName();
		final String description = Messages
				.format(
						RefactoringCoreMessages.RenameSourceModuleChange_descriptor_description_short,
						fCu.getElementName());
		final String header = Messages
				.format(
						RefactoringCoreMessages.RenameSourceModuleChange_descriptor_description,
						new String[] { label, newName });
		final String comment = new ScriptRefactoringDescriptorComment(this,
				header).asString();
		final ScriptRefactoringDescriptor descriptor = new ScriptRefactoringDescriptor(
				RenameSourceModuleProcessor.ID_RENAME_COMPILATION_UNIT, name,
				description, comment, arguments,
				ScriptRefactoringDescriptor.ARCHIVE_IMPORTABLE
						| ScriptRefactoringDescriptor.ARCHIVE_REFACTORABLE
						| RefactoringDescriptor.STRUCTURAL_CHANGE
						| RefactoringDescriptor.MULTI_CHANGE);
		arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_INPUT, descriptor
				.elementToHandle(fCu));
		arguments.put(ScriptRefactoringDescriptor.ATTRIBUTE_NAME, newName);
		return new DynamicValidationStateChange(new RenameSourceModuleChange(
				descriptor, fCu, newName, comment));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Change postCreateChange(Change[] participantChanges,
			IProgressMonitor pm) throws CoreException {
		return super.postCreateChange(participantChanges, pm);
	}

	@Override
	public RefactoringStatus initialize(RefactoringArguments arguments) {
		if (arguments instanceof ScriptRefactoringArguments) {
			final ScriptRefactoringArguments generic = (ScriptRefactoringArguments) arguments;
			final String path = generic.getAttribute(ATTRIBUTE_PATH);
			if (path != null) {
				final IResource resource = ResourcesPlugin.getWorkspace()
						.getRoot().findMember(new Path(path));
				if (resource == null || !resource.exists())
					return ScriptableRefactoring.createInputFatalStatus(
							resource, getRefactoring().getName(),
							ID_RENAME_COMPILATION_UNIT);
				else {
					fCu = (ISourceModule) DLTKCore.create(resource);
					try {
						computeRenameTypeRefactoring();
					} catch (CoreException exception) {
						DLTKUIPlugin.log(exception);
					}
				}
			} else
				return RefactoringStatus
						.createFatalErrorStatus(Messages
								.format(
										RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
										ATTRIBUTE_PATH));
			final String name = generic.getAttribute(ATTRIBUTE_NAME);
			if (name != null && !"".equals(name)) //$NON-NLS-1$
				setNewElementName(name);
			else
				return RefactoringStatus
						.createFatalErrorStatus(Messages
								.format(
										RefactoringCoreMessages.InitializableRefactoring_argument_not_exist,
										ATTRIBUTE_NAME));
		} else
			return RefactoringStatus
					.createFatalErrorStatus(RefactoringCoreMessages.InitializableRefactoring_inacceptable_arguments);
		return new RefactoringStatus();
	}
}
