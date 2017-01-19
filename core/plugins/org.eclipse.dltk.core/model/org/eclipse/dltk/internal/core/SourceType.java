/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.compiler.CharOperation;
import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.WorkingCopyOwner;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.dltk.internal.core.hierarchy.TypeHierarchyBuilders;
import org.eclipse.dltk.internal.core.util.MementoTokenizer;
import org.eclipse.dltk.internal.core.util.Messages;
import org.eclipse.dltk.utils.CorePrinter;

public class SourceType extends NamedMember implements IType {

	public SourceType(ModelElement parent, String name) {
		super(parent, name);
	}

	@Override
	public int getElementType() {
		return TYPE;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SourceType)) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public String[] getSuperClasses() throws ModelException {
		SourceTypeElementInfo info = (SourceTypeElementInfo) this
				.getElementInfo();
		if (info == null) {
			return CharOperation.NO_STRINGS;
		}
		return info.superclassNames;
	}

	@Override
	public void printNode(CorePrinter output) {
		output.formatPrint("DLTK Source Type:" + getElementName()); //$NON-NLS-1$
		output.indent();
		try {
			IModelElement modelElements[] = this.getChildren();
			for (int i = 0; i < modelElements.length; ++i) {
				IModelElement element = modelElements[i];
				if (element instanceof ModelElement) {
					((ModelElement) element).printNode(output);
				} else {
					output.print("Unknown element:" + element); //$NON-NLS-1$
				}
			}
		} catch (ModelException ex) {
			output.formatPrint(ex.getLocalizedMessage());
		}
		output.dedent();
	}

	@Override
	public IField getField(String fieldName) {
		return new SourceField(this, fieldName);
	}

	@Override
	public IField[] getFields() throws ModelException {
		List<IModelElement> list = getChildrenOfType(FIELD);
		IField[] array = new IField[list.size()];
		list.toArray(array);
		return array;
	}

	@Override
	public IType getType(String typeName) {
		return new SourceType(this, typeName);
	}

	@Override
	public IType[] getTypes() throws ModelException {
		List<IModelElement> list = getChildrenOfType(TYPE);
		IType[] array = new IType[list.size()];
		list.toArray(array);
		return array;
	}

	@Override
	public IMethod getMethod(String selector) {
		return new SourceMethod(this, selector);
	}

	@Override
	public IMethod[] getMethods() throws ModelException {
		List<IModelElement> list = getChildrenOfType(METHOD);
		IMethod[] array = new IMethod[list.size()];
		list.toArray(array);
		return array;
	}

	@Override
	public IModelElement getHandleFromMemento(String token,
			MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
		switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, workingCopyOwner);
		case JEM_FIELD:
			if (!memento.hasMoreTokens()) {
				return this;
			}
			String fieldName = memento.nextToken();
			ModelElement field = (ModelElement) getField(fieldName);
			return field.getHandleFromMemento(memento, workingCopyOwner);
			// case JEM_INITIALIZER:
			// if (!memento.hasMoreTokens()) return this;
			// String count = memento.nextToken();
			// JavaElement initializer =
			// (JavaElement)getInitializer(Integer.parseInt(count));
			// return initializer.getHandleFromMemento(memento,
			// workingCopyOwner);
		case JEM_METHOD:
			if (!memento.hasMoreTokens()) {
				return this;
			}

			String selector = memento.nextToken();
			ArrayList<String> params = new ArrayList<String>();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
				case JEM_TYPE:
				case JEM_TYPE_PARAMETER:
					break nextParam;
				case JEM_METHOD:
					if (!memento.hasMoreTokens()) {
						return this;
					}

					String param = memento.nextToken();
					StringBuffer buffer = new StringBuffer();

					// backward compatible with 3.0 mementos
					/*
					 * while (param.length() == 1 && Signature.C_ARRAY ==
					 * param.charAt(0)) { buffer.append(Signature.C_ARRAY); if
					 * (!memento.hasMoreTokens()) return this; param =
					 * memento.nextToken(); }
					 */

					params.add(buffer.toString() + param);
					break;
				default:
					break nextParam;
				}
			}
			String[] parameters = new String[params.size()];
			params.toArray(parameters);
			ModelElement method = (ModelElement) getMethod(selector);
			switch (token.charAt(0)) {
			case JEM_TYPE:
			case JEM_TYPE_PARAMETER:
			case JEM_LOCALVARIABLE:
				return method.getHandleFromMemento(token, memento,
						workingCopyOwner);
			default:
				return method;
			}
		case JEM_TYPE:
			String typeName;
			if (memento.hasMoreTokens()) {
				typeName = memento.nextToken();
				char firstChar = typeName.charAt(0);
				if (firstChar == JEM_FIELD /* || firstChar == JEM_INITIALIZER */
						|| firstChar == JEM_METHOD || firstChar == JEM_TYPE
						|| firstChar == JEM_COUNT) {
					token = typeName;
					typeName = ""; //$NON-NLS-1$
				} else {
					token = null;
				}
			} else {
				typeName = ""; //$NON-NLS-1$
				token = null;
			}
			ModelElement type = (ModelElement) getType(typeName);
			if (token == null) {
				return type.getHandleFromMemento(memento, workingCopyOwner);
			} else {
				return type.getHandleFromMemento(token, memento,
						workingCopyOwner);
			}
			// case JEM_TYPE_PARAMETER:
			// if (!memento.hasMoreTokens()) return this;
			// String typeParameterName = memento.nextToken();
			// ModelElement typeParameter = new TypeParameter(this,
			// typeParameterName);
			// return typeParameter.getHandleFromMemento(memento,
			// workingCopyOwner);

		}
		return null;
	}

	@Override
	protected char getHandleMementoDelimiter() {

		return JEM_TYPE;
	}

	@Override
	public String getFullyQualifiedName(String enclosingTypeSeparator) {
		try {
			return getFullyQualifiedName(enclosingTypeSeparator, false/*
																	 * don't
																	 * show
																	 * parameters
																	 */);
		} catch (ModelException e) {
			// exception thrown only when showing parameters
			return null;
		}
	}

	@Override
	public String getFullyQualifiedName() {
		return getFullyQualifiedName("$"); //$NON-NLS-1$
	}

	@Override
	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor) throws ModelException {
		// TODO Auto-generated method stub

	}

	@Override
	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor, WorkingCopyOwner owner)
			throws ModelException {
		// TODO Auto-generated method stub

	}

	@Override
	public IScriptFolder getScriptFolder() {
		IModelElement parentElement = this.parent;
		while (parentElement != null) {
			if (parentElement.getElementType() == IModelElement.SCRIPT_FOLDER) {
				return (IScriptFolder) parentElement;
			} else {
				parentElement = parentElement.getParent();
			}
		}
		Assert.isTrue(false); // should not happen
		return null;
	}

	/**
	 * @see IType#getTypeQualifiedName()
	 */
	@Override
	public String getTypeQualifiedName() {
		return this.getTypeQualifiedName("$"); //$NON-NLS-1$
	}

	/**
	 * @see IType#getTypeQualifiedName(char)
	 */
	@Override
	public String getTypeQualifiedName(String enclosingTypeSeparator) {
		try {
			return getTypeQualifiedName(enclosingTypeSeparator, false/*
																	 * don't
																	 * show
																	 * parameters
																	 */);
		} catch (ModelException e) {
			// exception thrown only when showing parameters
			return null;
		}
	}

	/*
	 * @see IType
	 */
	@Override
	public IMethod[] findMethods(IMethod method) {
		try {
			return findMethods(method, getMethods());
		} catch (ModelException e) {
			// if type doesn't exist, no matching method can exist
			return null;
		}
	}

	/*
	 * Type hierarchies section
	 */

	/**
	 * @see IType
	 */
	@Override
	public ITypeHierarchy loadTypeHierachy(InputStream input,
			IProgressMonitor monitor) throws ModelException {
		return loadTypeHierachy(input, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * NOTE: This method is not part of the API has it is not clear clients
	 * would easily use it: they would need to first make sure all working
	 * copies for the given owner exist before calling it. This is especially
	 * har at startup time. In case clients want this API, here is how it should
	 * be specified:
	 * <p>
	 * Loads a previously saved ITypeHierarchy from an input stream. A type
	 * hierarchy can be stored using ITypeHierachy#store(OutputStream). A
	 * compilation unit of a loaded type has the given owner if such a working
	 * copy exists, otherwise the type's compilation unit is a primary
	 * compilation unit.
	 * 
	 * Only hierarchies originally created by the following methods can be
	 * loaded:
	 * <ul>
	 * <li>IType#newSupertypeHierarchy(IProgressMonitor)</li>
	 * <li>IType#newSupertypeHierarchy(WorkingCopyOwner, IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IJavaProject, IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IJavaProject, WorkingCopyOwner,
	 * IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(WorkingCopyOwner, IProgressMonitor)</li> </u>
	 * 
	 * @param input
	 *            stream where hierarchy will be read
	 * @param monitor
	 *            the given progress monitor
	 * @return the stored hierarchy
	 * @exception JavaModelException
	 *                if the hierarchy could not be restored, reasons include: -
	 *                type is not the focus of the hierarchy or - unable to read
	 *                the input stream (wrong format, IOException during
	 *                reading, ...)
	 * @see ITypeHierarchy#store(java.io.OutputStream, IProgressMonitor)
	 * @since 3.0
	 */
	public ITypeHierarchy loadTypeHierachy(InputStream input,
			WorkingCopyOwner owner, IProgressMonitor monitor)
			throws ModelException {
		// TODO monitor should be passed to TypeHierarchy.load(...)
		return TypeHierarchy.load(this, input, owner);
	}

	/**
	 * @see IType
	 */
	@Override
	public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor)
			throws ModelException {
		return this.newSupertypeHierarchy(DefaultWorkingCopyOwner.PRIMARY,
				monitor);
	}

	/*
	 * @see IType#newSupertypeHierarchy(ICompilationUnit[], IProgressMonitor)
	 */
	@Override
	public ITypeHierarchy newSupertypeHierarchy(ISourceModule[] workingCopies,
			IProgressMonitor monitor) throws ModelException {

		CreateTypeHierarchyOperation op;
		IScriptProject scriptProject = getScriptProject();
		IDLTKSearchScope scope = SearchEngine.createSearchScope(scriptProject);
		op = new CreateTypeHierarchyOperation(this, workingCopies, scope, false);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IType#newSupertypeHierarchy(WorkingCopyOwner, IProgressMonitor)
	 */
	@Override
	public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner,
			IProgressMonitor monitor) throws ModelException {
		final ITypeHierarchy hierarchy = TypeHierarchyBuilders
				.getTypeHierarchy(this, ITypeHierarchy.Mode.SUPERTYPE, monitor);
		if (hierarchy != null) {
			return hierarchy;
		}

		ISourceModule[] workingCopies = ModelManager.getModelManager()
				.getWorkingCopies(owner, true/* add primary working copies */);
		CreateTypeHierarchyOperation op;
		IScriptProject scriptProject = getScriptProject();
		IDLTKSearchScope scope = SearchEngine.createSearchScope(scriptProject);
		op = new CreateTypeHierarchyOperation(this, workingCopies, scope, false);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IType
	 */
	@Override
	public ITypeHierarchy newTypeHierarchy(IScriptProject project,
			IProgressMonitor monitor) throws ModelException {
		return newTypeHierarchy(project, DefaultWorkingCopyOwner.PRIMARY,
				monitor);
	}

	/**
	 * @see IType#newTypeHierarchy(IJavaProject, WorkingCopyOwner,
	 *      IProgressMonitor)
	 */
	@Override
	public ITypeHierarchy newTypeHierarchy(IScriptProject project,
			WorkingCopyOwner owner, IProgressMonitor monitor)
			throws ModelException {
		if (project == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullProject);
		}
		ISourceModule[] workingCopies = ModelManager.getModelManager()
				.getWorkingCopies(owner, true/* add primary working copies */);
		ISourceModule[] projectWCs = null;
		if (workingCopies != null) {
			int length = workingCopies.length;
			projectWCs = new ISourceModule[length];
			int index = 0;
			for (int i = 0; i < length; i++) {
				ISourceModule wc = workingCopies[i];
				if (project.equals(wc.getScriptProject())) {
					projectWCs[index++] = wc;
				}
			}
			if (index != length) {
				System.arraycopy(projectWCs, 0,
						projectWCs = new ISourceModule[index], 0, index);
			}
		}
		CreateTypeHierarchyOperation op = new CreateTypeHierarchyOperation(
				this, projectWCs, project, true);
		op.runOperation(monitor);
		return op.getResult();
	}

	private IDLTKSearchScope createReferencingProjectsScope() {

		IScriptProject scriptProject = getScriptProject();
		IProject[] projects = scriptProject.getProject().getWorkspace()
				.getRoot().getProjects();
		List<IProject> result = new ArrayList<IProject>(projects.length);
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (!project.isAccessible())
				continue;
			IScriptProject p = DLTKCore.create(project);
			if (p.equals(scriptProject)) {
				result.add(project);
				continue;
			}
			IBuildpathEntry[] references;
			try {
				references = p.getRawBuildpath();
				for (int j = 0; j < references.length; j++) {
					if (references[j].getPath()
							.equals(scriptProject.getPath())) {
						result.add(projects[i]);
						break;
					}
				}
			} catch (ModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<IScriptProject> scriptProjects = new ArrayList<IScriptProject>(
				result.size());
		for (int i = 0; i < result.size(); ++i) {
			IProject p = result.get(i);

			if (p.isAccessible()) {
				scriptProjects.add(DLTKCore.create(p));
			}
		}
		return SearchEngine.createSearchScope(scriptProjects
				.toArray(new IModelElement[scriptProjects.size()]),
				DLTKLanguageManager.getLanguageToolkit(this));
	}

	/**
	 * @see IType
	 */
	@Override
	public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor)
			throws ModelException {
		final ITypeHierarchy hierarchy = TypeHierarchyBuilders
				.getTypeHierarchy(this, ITypeHierarchy.Mode.HIERARCHY, monitor);
		if (hierarchy != null) {
			return hierarchy;
		}
		CreateTypeHierarchyOperation op;
		op = new CreateTypeHierarchyOperation(this, null,
				createReferencingProjectsScope(), true);
		op.runOperation(monitor);
		return op.getResult();
	}

	/*
	 * @see IType#newTypeHierarchy(ICompilationUnit[], IProgressMonitor)
	 */
	@Override
	public ITypeHierarchy newTypeHierarchy(ISourceModule[] workingCopies,
			IProgressMonitor monitor) throws ModelException {

		CreateTypeHierarchyOperation op;
		op = new CreateTypeHierarchyOperation(this, workingCopies,
				createReferencingProjectsScope(), true);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IType#newTypeHierarchy(WorkingCopyOwner, IProgressMonitor)
	 */
	@Override
	public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner,
			IProgressMonitor monitor) throws ModelException {

		ISourceModule[] workingCopies = ModelManager.getModelManager()
				.getWorkingCopies(owner, true/* add primary working copies */);
		CreateTypeHierarchyOperation op;
		op = new CreateTypeHierarchyOperation(this, workingCopies,
				createReferencingProjectsScope(), true);
		op.runOperation(monitor);
		return op.getResult();
	}
}
