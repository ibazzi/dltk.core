/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.ui.viewsupport;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IImportDeclaration;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParameter;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISearchPatternProcessor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.internal.core.BuiltinProjectFragment;
import org.eclipse.dltk.internal.core.ExternalProjectFragment;
import org.eclipse.dltk.ui.Messages;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * Implementation of {@link ScriptElementLabels}.
 * 
 * @since 3.5
 */
public class ScriptElementLabelComposer {

	/**
	 * An adapter for buffer supported by the label composer.
	 */
	public static abstract class FlexibleBuffer {

		/**
		 * Appends the string representation of the given character to the
		 * buffer.
		 *
		 * @param ch
		 *            the character to append
		 * @return a reference to this object
		 */
		public abstract FlexibleBuffer append(char ch);

		/**
		 * Appends the given string to the buffer.
		 *
		 * @param string
		 *            the string to append
		 * @return a reference to this object
		 */
		public abstract FlexibleBuffer append(String string);

		/**
		 * Returns the length of the the buffer.
		 *
		 * @return the length of the current string
		 */
		public abstract int length();

		/**
		 * Sets a styler to use for the given source range. The range must be
		 * subrange of actual string of this buffer. Stylers previously set for
		 * that range will be overwritten.
		 *
		 * @param offset
		 *            the start offset of the range
		 * @param length
		 *            the length of the range
		 * @param styler
		 *            the styler to set
		 *
		 * @throws StringIndexOutOfBoundsException
		 *             if <code>start</code> is less than zero, or if offset
		 *             plus length is greater than the length of this object.
		 */
		public abstract void setStyle(int offset, int length, Styler styler);
	}

	public static class FlexibleStringBuffer extends FlexibleBuffer {
		private final StringBuffer fStringBuffer;

		public FlexibleStringBuffer(StringBuffer stringBuffer) {
			fStringBuffer = stringBuffer;
		}

		public FlexibleBuffer append(char ch) {
			fStringBuffer.append(ch);
			return this;
		}

		public FlexibleBuffer append(String string) {
			fStringBuffer.append(string);
			return this;
		}

		public int length() {
			return fStringBuffer.length();
		}

		public void setStyle(int offset, int length, Styler styler) {
			// no style
		}

		public String toString() {
			return fStringBuffer.toString();
		}
	}

	public static class FlexibleStyledString extends FlexibleBuffer {
		private final StyledString fStyledString;

		public FlexibleStyledString(StyledString stringBuffer) {
			fStyledString = stringBuffer;
		}

		public FlexibleBuffer append(char ch) {
			fStyledString.append(ch);
			return this;
		}

		public FlexibleBuffer append(String string) {
			fStyledString.append(string);
			return this;
		}

		public int length() {
			return fStyledString.length();
		}

		public void setStyle(int offset, int length, Styler styler) {
			fStyledString.setStyle(offset, length, styler);
		}

		public String toString() {
			return fStyledString.toString();
		}
	}

	public static final String ADDITIONAL_DELIMITERS = "<>(),?{} "; //$NON-NLS-1$

	protected static final Styler QUALIFIER_STYLE = StyledString.QUALIFIER_STYLER;
	protected static final Styler DECORATIONS_STYLE = StyledString.DECORATIONS_STYLER;

	protected static String fgPkgNamePrefix;
	protected static String fgPkgNamePostfix;
	protected static int fgPkgNameChars;
	protected static int fgPkgNameLength = -1;

	protected final FlexibleBuffer fBuffer;

	protected static final boolean getFlag(long flags, long flag) {
		return (flags & flag) != 0;
	}

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer
	 *            the buffer
	 */
	public ScriptElementLabelComposer(FlexibleBuffer buffer) {
		fBuffer = buffer;
	}

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer
	 *            the buffer
	 */
	public ScriptElementLabelComposer(StyledString buffer) {
		this(new FlexibleStyledString(buffer));
	}

	/**
	 * Creates a new java element composer based on the given buffer.
	 *
	 * @param buffer
	 *            the buffer
	 */
	public ScriptElementLabelComposer(StringBuffer buffer) {
		this(new FlexibleStringBuffer(buffer));
	}

	/**
	 * Appends the label for a Java element with the flags as defined by this
	 * class.
	 *
	 * @param element
	 *            the element to render
	 * @param flags
	 *            the rendering flags.
	 */
	public void getElementLabel(IModelElement element, long flags) {
		int type = element.getElementType();
		IProjectFragment root = null;

		IScriptProject project = element.getScriptProject();

		if (type != IModelElement.SCRIPT_MODEL
				&& type != IModelElement.SCRIPT_PROJECT
				&& type != IModelElement.PROJECT_FRAGMENT) {
			IResource resource = element.getResource();
			if (resource != null) {
				root = project.getProjectFragment(resource);
			}
			if (root == null) {
				root = findProjectFragment(element);
			}
		}
		if (root != null
				&& getFlag(flags, ScriptElementLabels.PREPEND_ROOT_PATH)) {
			getProjectFragmentLabel(root, ScriptElementLabels.ROOT_QUALIFIED);
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
		}

		switch (type) {
		case IModelElement.METHOD:
			getMethodLabel((IMethod) element, flags);
			break;
		case IModelElement.FIELD:
			getFieldLabel((IField) element, flags);
			break;
		case IModelElement.TYPE:
			getTypeLabel((IType) element, flags);
			break;
		case IModelElement.SOURCE_MODULE:
			getSourceModule((ISourceModule) element, flags);
			break;
		case IModelElement.SCRIPT_PROJECT:
		case IModelElement.SCRIPT_MODEL:
			fBuffer.append(element.getElementName());
			break;
		case IModelElement.IMPORT_CONTAINER:
			getImportContainerLabel(element, flags);
			break;
		case IModelElement.IMPORT_DECLARATION:
			getImportDeclarationLabel(element, flags);
			break;
		case IModelElement.PACKAGE_DECLARATION:
			getDeclarationLabel(element, flags);
			break;
		case IModelElement.SCRIPT_FOLDER:
			getScriptFolderLabel((IScriptFolder) element, flags);
			break;
		case IModelElement.PROJECT_FRAGMENT:
			getProjectFragmentLabel((IProjectFragment) element, flags);
			break;
		default:
			fBuffer.append(element.getElementName());
		}

		ISourceModule sourceModule = (ISourceModule) element
				.getAncestor(IModelElement.SOURCE_MODULE);
		if (sourceModule != null
				&& getFlag(flags, ScriptElementLabels.APPEND_FILE)) {
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
			getSourceModule(sourceModule, flags);
		}

		if (root != null
				&& getFlag(flags, ScriptElementLabels.APPEND_ROOT_PATH)) {
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
			getProjectFragmentLabel(root, ScriptElementLabels.ROOT_QUALIFIED);
		}
	}

	protected void getMethodLabel(IMethod method, long flags) {

		try {
			// qualification
			if (getFlag(flags, ScriptElementLabels.M_FULLY_QUALIFIED)) {
				IType type = method.getDeclaringType();
				if (type != null) {
					getTypeLabel(type, ScriptElementLabels.T_FULLY_QUALIFIED
							| (flags & ScriptElementLabels.QUALIFIER_FLAGS));
					fBuffer.append(getTypeDelimiter(type));
				}
			}

			final String methodName = method.getElementName();
			if (methodName.length() != 0) {
				fBuffer.append(methodName);
			} else {
				fBuffer.append("function"); // TODO language specific
			}

			// parameters
			fBuffer.append('(');
			getMethodParameters(method, flags);
			fBuffer.append(')');

			if (getFlag(flags, ScriptElementLabels.M_APP_RETURNTYPE)
					&& method.exists() && !method.isConstructor()) {
				final String type = method.getType();
				if (type != null) {
					int offset = fBuffer.length();
					fBuffer.append(ScriptElementLabels.DECL_STRING);
					fBuffer.append(type);
					if (getFlag(flags, ScriptElementLabels.COLORIZE)
							&& offset != fBuffer.length()) {
						fBuffer.setStyle(offset, fBuffer.length() - offset,
								DECORATIONS_STYLE);
					}
				}
			}

			// post qualification
			if (getFlag(flags, ScriptElementLabels.M_POST_QUALIFIED)) {
				IType declaringType = method.getDeclaringType();
				int offset = fBuffer.length();
				if (declaringType != null) {
					fBuffer.append(ScriptElementLabels.CONCAT_STRING);
					getTypeLabel(declaringType,
							ScriptElementLabels.T_FULLY_QUALIFIED | (flags
									& ScriptElementLabels.QUALIFIER_FLAGS));
				}
				if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
					fBuffer.setStyle(offset, fBuffer.length() - offset,
							QUALIFIER_STYLE);
				}
			}
		} catch (ModelException e) {
			e.printStackTrace();
		}
	}

	public void getTypeLabel(IType type, long flags) {

		IScriptProject project = type.getScriptProject();

		if (getFlag(flags, ScriptElementLabels.T_FULLY_QUALIFIED)) {
			IResource resource = type.getResource();
			IProjectFragment pack = null;
			if (resource != null) {
				pack = project.getProjectFragment(resource);
			} else {
				pack = findProjectFragment(type);
			}
			if (pack == null) {
				pack = findProjectFragment(type);
			}
			// getScriptFolderLabel(pack, (flags &
			// ScriptElementLabels.QUALIFIER_FLAGS));
		}

		if (getFlag(flags, ScriptElementLabels.T_FULLY_QUALIFIED
				| ScriptElementLabels.T_CONTAINER_QUALIFIED)) {
			IModelElement elem = type.getParent();
			IType declaringType = (elem instanceof IType) ? (IType) elem : null;
			if (declaringType != null) {
				getTypeLabel(declaringType,
						ScriptElementLabels.T_CONTAINER_QUALIFIED | (flags
								& ScriptElementLabels.QUALIFIER_FLAGS));
				fBuffer.append(getTypeDelimiter(elem));
			}
			int parentType = type.getParent().getElementType();
			if (parentType == IModelElement.METHOD
					|| parentType == IModelElement.FIELD) { // anonymous
				// or
				// local
				getElementLabel(type.getParent(),
						(parentType == IModelElement.METHOD
								? ScriptElementLabels.M_FULLY_QUALIFIED
								: ScriptElementLabels.F_FULLY_QUALIFIED)
								| (flags & ScriptElementLabels.QUALIFIER_FLAGS));
				fBuffer.append(getTypeDelimiter(elem));
			}
		}

		String typeName = type.getElementName();
		if (typeName.length() == 0) { // anonymous
			try {
				if (type.getParent() instanceof IField) {
					typeName = '{' + ScriptElementLabels.ELLIPSIS_STRING + '}';
				} else {
					String[] superNames = type.getSuperClasses();
					if (superNames != null) {
						int count = 0;
						typeName += ScriptElementLabels.DECL_STRING;
						for (int i = 0; i < superNames.length; ++i) {

							if (count > 0) {
								typeName += ScriptElementLabels.COMMA_STRING
										+ " "; //$NON-NLS-1$
							}
							typeName += superNames[i];
							count++;
						}
					}
				}
			} catch (ModelException e) {
				// ignore
				typeName = ""; //$NON-NLS-1$
			}
		}

		fBuffer.append(typeName);

		// post qualification
		if (getFlag(flags, ScriptElementLabels.T_POST_QUALIFIED)) {
			int offset = fBuffer.length();
			IModelElement elem = type.getParent();
			IType declaringType = (elem instanceof IType) ? (IType) elem : null;
			if (declaringType != null) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				getTypeLabel(declaringType,
						ScriptElementLabels.T_FULLY_QUALIFIED | (flags
								& ScriptElementLabels.QUALIFIER_FLAGS));
				int parentType = type.getParent().getElementType();
				if (parentType == IModelElement.METHOD
						|| parentType == IModelElement.FIELD) { // anonymous
					// or
					// local
					fBuffer.append(getTypeDelimiter(elem));
					getElementLabel(type.getParent(), 0);
				}
			}
			int parentType = type.getParent().getElementType();
			if (parentType == IModelElement.METHOD
					|| parentType == IModelElement.FIELD) { // anonymous
				// or
				// local
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				getElementLabel(type.getParent(),
						(parentType == IModelElement.METHOD
								? ScriptElementLabels.M_FULLY_QUALIFIED
								: ScriptElementLabels.F_FULLY_QUALIFIED)
								| (flags & ScriptElementLabels.QUALIFIER_FLAGS));
			}
			if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset,
						QUALIFIER_STYLE);
			}
		}
	}

	/**
	 * Returns the string for rendering the
	 * {@link IModelElement#getElementName() element name} of the given element.
	 * 
	 * @param element
	 *            the element to render
	 * @return the string for rendering the element name
	 */
	protected String getElementName(IModelElement element) {
		return element.getElementName();
	}

	public void getProjectFragmentLabel(IProjectFragment root, long flags) {
		if (root.isArchive())
			getArchiveLabel(root, flags);
		else {
			if (root.isBuiltin()) {
				fBuffer.append(ScriptElementLabels.BUILTINS_FRAGMENT);
			} else if (root.isExternal() && !root.isBinary()) {
				getExternalFolderLabel(root, flags);
			} else {
				getFolderLabel(root, flags);
			}
		}

	}

	protected void getArchiveLabel(IProjectFragment root, long flags) {
		// Handle variables different
		boolean external = root.isExternal();
		if (external)
			getExternalArchiveLabel(root, flags);
		else
			getInternalArchiveLabel(root, flags);
	}

	protected void getExternalArchiveLabel(IProjectFragment root, long flags) {
		IPath path = root.getPath();
		path = EnvironmentPathUtils.getLocalPath(path);
		IEnvironment env = EnvironmentManager.getEnvironment(root);
		if (getFlag(flags,
				ScriptElementLabels.REFERENCED_ARCHIVE_POST_QUALIFIED)) {
			int segements = path.segmentCount();
			if (segements > 0) {
				fBuffer.append(path.segment(segements - 1));
				if (segements > 1 || path.getDevice() != null) {
					int offset = fBuffer.length();
					fBuffer.append(ScriptElementLabels.CONCAT_STRING);
					fBuffer.append(env
							.convertPathToString(path.removeLastSegments(1)));
					if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
						fBuffer.setStyle(offset, fBuffer.length() - offset,
								QUALIFIER_STYLE);
					}
				}
			} else {
				fBuffer.append(env.convertPathToString(path));
			}
		} else {
			fBuffer.append(env.convertPathToString(path));
		}
	}

	/**
	 * Returns <code>true</code> if the given package fragment root is
	 * referenced. This means it is own by a different project but is referenced
	 * by the root's parent. Returns <code>false</code> if the given root
	 * doesn't have an underlying resource.
	 * 
	 * @param root
	 *            the package fragment root
	 * @return returns <code>true</code> if the given package fragment root is
	 *         referenced
	 */
	private boolean isReferenced(IProjectFragment root) {
		IResource resource = root.getResource();
		if (resource != null) {
			IProject jarProject = resource.getProject();
			IProject container = root.getScriptProject().getProject();
			return !container.equals(jarProject);
		}
		return false;
	}

	protected void getInternalArchiveLabel(IProjectFragment root, long flags) {
		IResource resource = root.getResource();
		boolean rootQualified = getFlag(flags,
				ScriptElementLabels.ROOT_QUALIFIED);
		boolean referencedQualified = getFlag(flags,
				ScriptElementLabels.REFERENCED_ARCHIVE_POST_QUALIFIED)
				&& isReferenced(root);
		if (rootQualified) {
			fBuffer.append(EnvironmentPathUtils.getLocalPath(root.getPath())
					.makeRelative().toString());
		} else {
			fBuffer.append(root.getElementName());
			if (referencedQualified) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(resource.getParent().getFullPath().makeRelative()
						.toString());
			} else if (getFlag(flags,
					ScriptElementLabels.ROOT_POST_QUALIFIED)) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(EnvironmentPathUtils
						.getLocalPath(root.getParent().getPath()).makeRelative()
						.toString());
			}
		}
	}

	protected void getFolderLabel(IProjectFragment root, long flags) {

		IResource resource = root.getResource();
		boolean rootQualified = getFlag(flags,
				ScriptElementLabels.ROOT_QUALIFIED);
		boolean referencedQualified = getFlag(flags,
				ScriptElementLabels.REFERENCED_ROOT_POST_QUALIFIED)
				&& resource != null;
		if (rootQualified) {
			fBuffer.append(EnvironmentPathUtils.getLocalPath(root.getPath())
					.makeRelative().toString());
		} else {
			if (resource != null)
				fBuffer.append(resource.getProjectRelativePath().toString());
			else
				fBuffer.append(root.getElementName());
			if (referencedQualified) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(resource.getProject().getName());
			} else if (getFlag(flags,
					ScriptElementLabels.ROOT_POST_QUALIFIED)) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(root.getParent().getElementName());
			}
		}
	}

	protected void getExternalFolderLabel(IProjectFragment root, long flags) {

		boolean rootQualified = getFlag(flags,
				ScriptElementLabels.ROOT_QUALIFIED);
		boolean referencedQualified = getFlag(flags,
				ScriptElementLabels.REFERENCED_ROOT_POST_QUALIFIED);
		fBuffer.append(EnvironmentPathUtils.getLocalPathString(root.getPath()));
		if (!rootQualified) {
			if (referencedQualified) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(root.getScriptProject().getElementName());
			} else if (getFlag(flags,
					ScriptElementLabels.ROOT_POST_QUALIFIED)) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(root.getParent().getElementName());
			}
		}
	}

	protected IProjectFragment findProjectFragment(IModelElement element) {
		while (element != null
				&& element.getElementType() != IModelElement.PROJECT_FRAGMENT) {
			element = element.getParent();
		}
		return (IProjectFragment) element;
	}

	protected String getTypeDelimiter(IModelElement modelElement) {
		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
				.getLanguageToolkit(modelElement);
		ISearchPatternProcessor processor = DLTKLanguageManager
				.getSearchPatternProcessor(toolkit);
		if (processor != null) {
			return processor.getDelimiterReplacementString();
		}
		return "."; //$NON-NLS-1$
	}

	protected void getMethodParameters(IMethod method, long flags)
			throws ModelException {
		if (getFlag(flags, ScriptElementLabels.M_PARAMETER_TYPES
				| ScriptElementLabels.M_PARAMETER_NAMES)) {
			if (method.exists()) {
				final boolean bNames = getFlag(flags,
						ScriptElementLabels.M_PARAMETER_NAMES);
				final boolean bTypes = getFlag(flags,
						ScriptElementLabels.M_PARAMETER_TYPES);
				final boolean bInitializers = getFlag(flags,
						ScriptElementLabels.M_PARAMETER_INITIALIZERS);
				final IParameter[] params = method.getParameters();
				for (int i = 0, nParams = params.length; i < nParams; i++) {
					if (i > 0) {
						fBuffer.append(ScriptElementLabels.COMMA_STRING);
					}
					if (bNames) {
						fBuffer.append(params[i].getName());
					}
					if (bTypes) {
						if (params[i].getType() != null) {
							if (bNames) {
								fBuffer.append(':');
							}
							fBuffer.append(params[i].getType());
						} else if (!bNames) {
							fBuffer.append(params[i].getName());
						}
					}
					if (bInitializers && params[i].getDefaultValue() != null) {
						fBuffer.append("=");
						fBuffer.append(params[i].getDefaultValue());
					}
				}
			}
		} else if (method.getParameters().length > 0) {
			fBuffer.append(ScriptElementLabels.ELLIPSIS_STRING);
		}
	}

	protected void getFieldLabel(IField field, long flags) {
		// qualification
		if (getFlag(flags, ScriptElementLabels.F_FULLY_QUALIFIED)) {
			IType type = field.getDeclaringType();
			if (type != null) {
				getTypeLabel(type, ScriptElementLabels.T_FULLY_QUALIFIED
						| (flags & ScriptElementLabels.QUALIFIER_FLAGS));
				fBuffer.append(getTypeDelimiter(type));
			}
		}
		fBuffer.append(field.getElementName());
		if (getFlag(flags, ScriptElementLabels.F_APP_TYPE_SIGNATURE)
				&& field.exists()) {
			try {
				String type = field.getType();
				if (type != null) {
					int offset = fBuffer.length();
					fBuffer.append(ScriptElementLabels.DECL_STRING);
					fBuffer.append(type);
					if (getFlag(flags, ScriptElementLabels.COLORIZE)
							&& offset != fBuffer.length()) {
						fBuffer.setStyle(offset, fBuffer.length() - offset,
								DECORATIONS_STYLE);
					}
				}
			} catch (CoreException e) {
				DLTKCore.error("Failed to append type name to field", e);
			}
		}
		// // post qualification
		// if (getFlag(flags, ScriptElementLabels.F_POST_QUALIFIED)) {
		// int offset= fBuffer.length();
		// fBuffer.append(ScriptElementLabels.ScriptElementLabels.CONCAT_STRING);
		// appendTypeLabel(field.getDeclaringType(),
		// ScriptElementLabels.ScriptElementLabels.T_FULLY_QUALIFIED
		// | (flags & ScriptElementLabels.QUALIFIER_FLAGS));
		// if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
		// fBuffer.setStyle(offset, fBuffer.length() - offset, QUALIFIER_STYLE);
		// }
		// }
	}

	/**
	 * @since 2.0
	 */
	protected void getSourceModule(ISourceModule module, long flags) {
		if (getFlag(flags, ScriptElementLabels.CU_QUALIFIED)) {
			IScriptFolder pack = (IScriptFolder) module.getParent();

			getScriptFolderLabel(pack,
					(flags & ScriptElementLabels.QUALIFIER_FLAGS));
			fBuffer.append("/");
		}
		fBuffer.append(module.getElementName());

		if (getFlag(flags, ScriptElementLabels.CU_POST_QUALIFIED)) {
			int offset = fBuffer.length();
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
			getScriptFolderLabel((IScriptFolder) module.getParent(),
					flags & ScriptElementLabels.QUALIFIER_FLAGS);
			if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset,
						QUALIFIER_STYLE);
			}
		}
	}

	/**
	 * @since 2.0
	 */
	protected void getImportContainerLabel(IModelElement element, long flags) {
		fBuffer.append(Messages.ScriptElementLabels_import_declarations);
	}

	/**
	 * @since 2.0
	 */
	protected void getImportDeclarationLabel(IModelElement element,
			long flags) {
		fBuffer.append(element.getElementName());
		IImportDeclaration declaration = (IImportDeclaration) element;
		if (declaration.getVersion() != null
				&& declaration.getVersion().length() != 0) {
			fBuffer.append(" "); //$NON-NLS-1$
			fBuffer.append(declaration.getVersion());
		}
	}

	/**
	 * Appends the label for a import container, import or package declaration
	 * to a {@link StringBuffer}. Considers the D_* flags.
	 * 
	 * @param declaration
	 *            The element to render.
	 * @param flags
	 *            The rendering flags. Flags with names starting with 'D_' are
	 *            considered.
	 * @param buf
	 *            The buffer to append the resulting label to.
	 */
	public void getDeclarationLabel(IModelElement declaration, long flags) {
		if (getFlag(flags, ScriptElementLabels.D_QUALIFIED)) {
			IModelElement openable = declaration.getOpenable();
			if (openable != null) {
				fBuffer.append(ScriptElementLabels.getDefault().getElementLabel(
						openable,
						ScriptElementLabels.CF_QUALIFIED
								| ScriptElementLabels.CU_QUALIFIED | (flags
										& ScriptElementLabels.QUALIFIER_FLAGS)));
				fBuffer.append('/');
			}
		}
		// if (declaration.getElementType() == IModelElement.IMPORT_CONTAINER) {
		// buf.append(JavaUIMessages.ModelElementLabels_import_container);
		// } else {
		fBuffer.append(declaration.getElementName());
		// }
		// post qualification
		if (getFlag(flags, ScriptElementLabels.D_POST_QUALIFIED)) {
			IModelElement openable = declaration.getOpenable();
			if (openable != null) {
				fBuffer.append(ScriptElementLabels.CONCAT_STRING);
				fBuffer.append(ScriptElementLabels.getDefault().getElementLabel(
						openable,
						ScriptElementLabels.CF_QUALIFIED
								| ScriptElementLabels.CU_QUALIFIED | (flags
										& ScriptElementLabels.QUALIFIER_FLAGS)));
			}
		}
	}

	protected void getScriptFolderLabel(IScriptFolder folder, long flags) {
		if (getFlag(flags, ScriptElementLabels.P_QUALIFIED)) {
			getProjectFragmentLabel((IProjectFragment) folder.getParent(),
					ScriptElementLabels.ROOT_QUALIFIED);
			fBuffer.append('/');
		}
		// refreshPackageNamePattern();
		if (folder.isRootFolder()) {
			fBuffer.append(ScriptElementLabels.DEFAULT_PACKAGE);
		} else if (getFlag(flags, ScriptElementLabels.P_COMPRESSED)
				&& fgPkgNameLength >= 0) {
			String name = folder.getElementName();
			int start = 0;
			int dot = name.indexOf(IScriptFolder.PACKAGE_DELIMITER, start);
			while (dot > 0) {
				if (dot - start > fgPkgNameLength - 1) {
					fBuffer.append(fgPkgNamePrefix);
					if (fgPkgNameChars > 0)
						fBuffer.append(name.substring(start,
								Math.min(start + fgPkgNameChars, dot)));
					fBuffer.append(fgPkgNamePostfix);
				} else
					fBuffer.append(name.substring(start, dot + 1));
				start = dot + 1;
				dot = name.indexOf(IScriptFolder.PACKAGE_DELIMITER, start);
			}
			fBuffer.append(name.substring(start));
		} else {
			getScriptFolderLabel(folder);
		}
		if (getFlag(flags, ScriptElementLabels.P_POST_QUALIFIED)) {
			int offset = fBuffer.length();
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
			getProjectFragmentLabel((IProjectFragment) folder.getParent(),
					ScriptElementLabels.ROOT_QUALIFIED);
			if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset,
						QUALIFIER_STYLE);
			}
		}
	}

	protected void getScriptFolderLabel(IScriptFolder folder) {
		fBuffer.append(folder.getElementName()/*
												 * .replace(IScriptFolder.
												 * PACKAGE_DELIMITER , '.')
												 */);
	}

	/**
	 * Appends the label for a package fragment to a {@link StringBuffer}.
	 * Considers the P_* flags.
	 * 
	 * @param pack
	 *            The element to render.
	 * @param flags
	 *            The rendering flags. Flags with names starting with P_' are
	 *            considered.
	 * @param buf
	 *            The buffer to append the resulting label to.
	 */
	public void getScriptFolderLabel(IProjectFragment pack, long flags) {

		if (getFlag(flags, ScriptElementLabels.P_QUALIFIED)) {
			getProjectFragmentLabel((IProjectFragment) pack.getParent(),
					ScriptElementLabels.ROOT_QUALIFIED);
			fBuffer.append('/');
		}

		if (pack instanceof ExternalProjectFragment) {
			fBuffer.append(pack.getElementName().replace(
					ExternalProjectFragment.JEM_SKIP_DELIMETER, Path.SEPARATOR)
					+ " "); //$NON-NLS-1$
		} else {
			if (pack instanceof BuiltinProjectFragment) {
				fBuffer.append(ScriptElementLabels.BUILTINS_FRAGMENT + " "); //$NON-NLS-1$
			} else {
				if (pack != null) {
					fBuffer.append(pack.getElementName() + " "); //$NON-NLS-1$
				}
			}
		}
		// }
		if (getFlag(flags, ScriptElementLabels.P_POST_QUALIFIED)) {
			int offset = fBuffer.length();
			fBuffer.append(ScriptElementLabels.CONCAT_STRING);
			getProjectFragmentLabel((IProjectFragment) pack.getParent(),
					ScriptElementLabels.ROOT_QUALIFIED);
			if (getFlag(flags, ScriptElementLabels.COLORIZE)) {
				fBuffer.setStyle(offset, fBuffer.length() - offset,
						QUALIFIER_STYLE);
			}
		}
	}

}