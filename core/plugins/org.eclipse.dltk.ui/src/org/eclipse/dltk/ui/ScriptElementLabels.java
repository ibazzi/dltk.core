/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.BuildpathContainerInitializer;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathContainer;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IImportDeclaration;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISearchPatternProcessor;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.corext.util.Strings;
import org.eclipse.dltk.internal.ui.UIModelProviderManager;
import org.eclipse.dltk.ui.viewsupport.ScriptElementLabelComposer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <code>ScriptElementLabels</code> provides helper methods to render names of
 * Script elements.
 * 
 * @since 2.0
 */
public class ScriptElementLabels {
	/**
	 * Method names contain parameter types. e.g. <code>foo(int)</code>
	 */
	public final static long M_PARAMETER_TYPES = 1L << 0;

	/**
	 * Method names contain parameter names. e.g. <code>foo(index)</code>
	 */
	public final static long M_PARAMETER_NAMES = 1L << 1;

	public final static long M_PARAMETER_INITIALIZERS = 1L << 49;

	/**
	 * Method names contain type parameters prepended. e.g.
	 * <code><A> foo(A index)</code>
	 */
	public final static long M_PRE_TYPE_PARAMETERS = 1L << 2;

	/**
	 * Method names contain type parameters appended. e.g.
	 * <code>foo(A index) <A></code>
	 */
	public final static long M_APP_TYPE_PARAMETERS = 1L << 3;

	/**
	 * Method names contain thrown exceptions. e.g.
	 * <code>foo throws IOException</code>
	 */
	public final static long M_EXCEPTIONS = 1L << 4;

	/**
	 * Method names contain return type (appended) e.g. <code>foo : int</code>
	 */
	public final static long M_APP_RETURNTYPE = 1L << 5;

	/**
	 * Method names contain return type (appended) e.g. <code>int foo</code>
	 */
	public final static long M_PRE_RETURNTYPE = 1L << 6;

	/**
	 * Method names are fully qualified. e.g. <code>java.util.Vector.size</code>
	 */
	public final static long M_FULLY_QUALIFIED = 1L << 7;

	/**
	 * Method names are post qualified. e.g.
	 * <code>size - java.util.Vector</code>
	 */
	public final static long M_POST_QUALIFIED = 1L << 8;

	/**
	 * Initializer names are fully qualified. e.g.
	 * <code>java.util.Vector.{ ... }</code>
	 */
	public final static long I_FULLY_QUALIFIED = 1L << 10;

	/**
	 * Type names are post qualified. e.g. <code>{ ... } - java.util.Map</code>
	 */
	public final static long I_POST_QUALIFIED = 1L << 11;

	/**
	 * Field names contain the declared type (appended) e.g.
	 * <code>fHello : int</code>
	 */
	public final static long F_APP_TYPE_SIGNATURE = 1L << 14;

	/**
	 * Field names contain the declared type (prepended) e.g.
	 * <code>int fHello</code>
	 */
	public final static long F_PRE_TYPE_SIGNATURE = 1L << 15;

	/**
	 * Fields names are fully qualified. e.g. <code>java.lang.System.out</code>
	 */
	public final static long F_FULLY_QUALIFIED = 1L << 16;

	/**
	 * Fields names are post qualified. e.g. <code>out - java.lang.System</code>
	 */
	public final static long F_POST_QUALIFIED = 1L << 17;

	/**
	 * Type names are fully qualified. e.g. <code>java.util.Map.MapEntry</code>
	 */
	public final static long T_FULLY_QUALIFIED = 1L << 18;

	/**
	 * Type names are type container qualified. e.g. <code>Map.MapEntry</code>
	 */
	public final static long T_CONTAINER_QUALIFIED = 1L << 19;

	/**
	 * Type names are post qualified. e.g. <code>MapEntry - java.util.Map</code>
	 */
	public final static long T_POST_QUALIFIED = 1L << 20;

	/**
	 * Type names contain type parameters. e.g. <code>Map&lt;S, T&gt;</code>
	 */
	public final static long T_TYPE_PARAMETERS = 1L << 21;

	/**
	 * Declarations (import container / declaration, package declaration) are
	 * qualified. e.g. <code>java.util.Vector.class/import container</code>
	 */
	public final static long D_QUALIFIED = 1L << 24;

	/**
	 * Declarations (import container / declaration, package declaration) are
	 * post qualified. e.g.
	 * <code>import container - java.util.Vector.class</code>
	 */
	public final static long D_POST_QUALIFIED = 1L << 25;

	/**
	 * Class file names are fully qualified. e.g.
	 * <code>java.util.Vector.class</code>
	 */
	public final static long CF_QUALIFIED = 1L << 27;

	/**
	 * Class file names are post qualified. e.g.
	 * <code>Vector.class - java.util</code>
	 */
	public final static long CF_POST_QUALIFIED = 1L << 28;

	/**
	 * Compilation unit names are fully qualified. e.g.
	 * <code>java.util.Vector.java</code>
	 */
	public final static long CU_QUALIFIED = 1L << 31;

	/**
	 * Compilation unit names are post qualified. e.g.
	 * <code>Vector.java - java.util</code>
	 */
	public final static long CU_POST_QUALIFIED = 1L << 32;

	/**
	 * Package names are qualified. e.g. <code>MyProject/src/java.util</code>
	 */
	public final static long P_QUALIFIED = 1L << 35;

	/**
	 * Package names are post qualified. e.g.
	 * <code>java.util - MyProject/src</code>
	 */
	public final static long P_POST_QUALIFIED = 1L << 36;

	/**
	 * Package names are compressed. e.g. <code>o*.e*.search</code>
	 */
	public final static long P_COMPRESSED = 1L << 37;

	/**
	 * Package Fragment Roots contain variable name if from a variable.
	 */
	public final static long ROOT_VARIABLE = 1L << 40;

	/**
	 * Package Fragment Roots contain the project name if not an archive
	 * (prepended). e.g. <code>MyProject/src</code>
	 */
	public final static long ROOT_QUALIFIED = 1L << 41;

	/**
	 * Package Fragment Roots contain the project name if not an archive
	 * (appended). e.g. <code>src - MyProject</code>
	 */
	public final static long ROOT_POST_QUALIFIED = 1L << 42;

	/**
	 * Add root path to all elements except Package Fragment Roots and script
	 * projects. Option only applies to getElementLabel
	 */
	public final static long APPEND_ROOT_PATH = 1L << 43;

	/**
	 * Add root path to all elements except Package Fragment Roots and script
	 * projects. Option only applies to getElementLabel
	 */
	public final static long PREPEND_ROOT_PATH = 1L << 44;

	/**
	 * Post qualify referenced package fragment roots.
	 */
	public final static long REFERENCED_ROOT_POST_QUALIFIED = 1L << 45;

	/**
	 * Post qualify referenced archive fragment roots.
	 * 
	 * @since 2.0
	 */
	public final static long REFERENCED_ARCHIVE_POST_QUALIFIED = 1L << 46;

	/**
	 * Specified to use the resolved information of a IType, IMethod or IField.
	 * See {@link IType#isResolved()}. If resolved information is available,
	 * types will be rendered with type parameters of the instantiated type.
	 * Resolved method render with the parameter types of the method instance.
	 * <code>Vector<String>.get(String)</code>
	 */
	public final static long USE_RESOLVED = 1L << 48;

	public final static long APPEND_FILE = 1L << 63;

	/**
	 * Qualify all elements
	 */
	public final static long ALL_FULLY_QUALIFIED = Long.valueOf(F_FULLY_QUALIFIED
			| M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED
			| D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED | P_QUALIFIED
			| ROOT_QUALIFIED | M_PRE_RETURNTYPE).longValue();

	/**
	 * Post qualify all elements
	 */
	public final static long ALL_POST_QUALIFIED = Long.valueOf(F_POST_QUALIFIED
			| M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED
			| D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED
			| P_POST_QUALIFIED | ROOT_POST_QUALIFIED).longValue();

	/**
	 * Default options (M_PARAMETER_TYPES, M_APP_TYPE_PARAMETERS &
	 * T_TYPE_PARAMETERS enabled)
	 */
	public final static long ALL_DEFAULT = new Long(
			M_PARAMETER_TYPES | M_APP_TYPE_PARAMETERS | T_TYPE_PARAMETERS)
					.longValue();

	public final static long F_CATEGORY = 1L << 49;

	/**
	 * Prepend first category (if any) to method.
	 */
	public final static long M_CATEGORY = 1L << 50;

	/**
	 * Prepend first category (if any) to type.
	 */
	public final static long T_CATEGORY = 1L << 51;

	/**
	 * Specifies to apply color styles to labels. This flag only applies to
	 * methods taking or returning a {@link StyledString}.
	 */
	public final static long COLORIZE = 1L << 55;

	public final static long ALL_CATEGORY = Long.valueOf(
			ScriptElementLabels.F_CATEGORY | ScriptElementLabels.M_CATEGORY
					| ScriptElementLabels.T_CATEGORY).longValue();

	/**
	 * Default qualify options (All except Root and Package)
	 */
	public final static long DEFAULT_QUALIFIED = Long.valueOf(F_FULLY_QUALIFIED
			| M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED
			| D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED).longValue();

	/**
	 * Default post qualify options (All except Root and Package)
	 */
	public final static long DEFAULT_POST_QUALIFIED = Long.valueOf(F_POST_QUALIFIED
			| M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED
			| D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED)
			.longValue();

	/**
	 * User-readable string for separating post qualified names (e.g. " - ").
	 */
	public final static String CONCAT_STRING = " - "; //$NON-NLS-1$

	/**
	 * User-readable string for separating list items (e.g. ", ").
	 */
	public final static String COMMA_STRING = ", "; //$NON-NLS-1$

	/**
	 * User-readable string for separating the return type (e.g. " : ").
	 */
	public final static String DECL_STRING = " : "; //$NON-NLS-1$

	/**
	 * User-readable string for ellipsis ("...").
	 */
	public final static String ELLIPSIS_STRING = "..."; //$NON-NLS-1$

	/**
	 * User-readable string for the default package name (e.g. "(default
	 * package)").
	 */
	public final static String DEFAULT_PACKAGE = "(default package)"; //$NON-NLS-1$

	public final static String BUILTINS_FRAGMENT = "(builtins)"; //$NON-NLS-1$

	/**
	 * @since 2.0
	 */
	public final static long QUALIFIER_FLAGS = P_COMPRESSED | USE_RESOLVED;

	private static ScriptElementLabels sInstanceO = new ScriptElementLabels() {
	};
	private static ScriptElementLabels sInstance = new ScriptElementLabels() {
		private ScriptElementLabels getLabels(IModelElement element) {
			IDLTKUILanguageToolkit languageToolkit = DLTKUILanguageManager
					.getLanguageToolkit(element);
			if (languageToolkit != null) {
				ScriptElementLabels scriptElementLabels = languageToolkit
						.getScriptElementLabels();
				if (scriptElementLabels != null) {
					return scriptElementLabels;
				}
			}
			return sInstanceO;
		}

		@Override
		public String getContainerEntryLabel(IPath containerPath,
				IScriptProject project) throws ModelException {
			return getLabels(project).getContainerEntryLabel(containerPath,
					project);
		}

		@Override
		public void getDeclarationLabel(IModelElement declaration, long flags,
				StringBuffer buf) {
			getLabels(declaration).getDeclarationLabel(declaration, flags, buf);
		}

		@Override
		public void getElementLabel(IModelElement element, long flags,
				StringBuffer buf) {
			getLabels(element).getElementLabel(element, flags, buf);
		}

		@Override
		public String getElementLabel(IModelElement element, long flags) {
			return getLabels(element).getElementLabel(element, flags);
		}

		@Override
		public void getProjectFragmentLabel(IProjectFragment root, long flags,
				StringBuffer buf) {
			getLabels(root).getProjectFragmentLabel(root, flags, buf);
		}

		@Override
		public void getScriptFolderLabel(IProjectFragment pack, long flags,
				StringBuffer buf) {
			getLabels(pack).getScriptFolderLabel(pack, flags, buf);
		}

		@Override
		protected void getTypeLabel(IType type, long flags, StringBuffer buf) {
			getLabels(type).getTypeLabel(type, flags, buf);
		}

		@Override
		public StyledString getStyledTextLabel(Object obj, long flags) {
			if (obj instanceof IModelElement) {
				return getStyledElementLabel((IModelElement) obj, flags);

			} else {
				return super.getStyledTextLabel(obj, flags);
			}
		}

		/**
		 * Returns the styled label for a Java element with the flags as defined
		 * by this class.
		 * 
		 * @param element
		 *            the element to render
		 * @param flags
		 *            the rendering flags
		 * @return the label of the Java element
		 * 
		 * @since 3.4
		 */
		@Override
		public StyledString getStyledElementLabel(IModelElement element,
				long flags) {
			return getLabels(element).getStyledTextLabel(element, flags);

		}
	};

	public static ScriptElementLabels getDefault() {
		return sInstance;
	}

	protected ScriptElementLabels() {

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

	protected static final boolean getFlag(long flags, long flag) {
		return (flags & flag) != 0;
	}

	/*
	 * Package name compression
	 */

	/**
	 * @since 2.0
	 */
	protected static String fgPkgNamePrefix;

	/**
	 * @since 2.0
	 */
	protected static String fgPkgNamePostfix;

	/**
	 * @since 2.0
	 */
	protected static int fgPkgNameChars;

	/**
	 * @since 2.0
	 */
	protected static int fgPkgNameLength = -1;

	/**
	 * Returns the label of the given object. The object must be of type
	 * {@link IScriptElement} or adapt to {@link IWorkbenchAdapter}. The empty
	 * string is returned if the element type is not known.
	 * 
	 * @param obj
	 *            Object to get the label from.
	 * @param flags
	 *            The rendering flags
	 * @return Returns the label or the empty string if the object type is not
	 *         supported.
	 */
	public String getTextLabel(Object obj, long flags) {
		if (obj instanceof IModelElement) {
			IModelElement element = (IModelElement) obj;
			if (this.equals(sInstance)) {
				IDLTKUILanguageToolkit uiToolkit = DLTKUILanguageManager
						.getLanguageToolkit(element);
				if (uiToolkit != null) {
					ScriptElementLabels labels = uiToolkit
							.getScriptElementLabels();
					if (labels != null) {
						return labels.getElementLabel(element, flags);
					}
				}
			}
			return getElementLabel((IModelElement) obj, flags);
		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter = ((IAdaptable) obj)
					.getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return wbadapter.getLabel(obj);
			}
		}
		return ""; //$NON-NLS-1$
	}

	private ILabelProvider[] getProviders(Object element) {
		String idtoolkit = null;
		if (element instanceof IModelElement) {
			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
					.getLanguageToolkit((IModelElement) element);
			if (toolkit != null) {
				idtoolkit = toolkit.getNatureId();
			}
		}
		ILabelProvider[] providers = UIModelProviderManager
				.getLabelProviders(idtoolkit);
		return providers;
	}

	/**
	 * Returns the label for a model element with the flags as defined by this
	 * class.
	 * 
	 * @param element
	 *            The element to render.
	 * @param flags
	 *            The rendering flags.
	 * @return the label of the model element
	 */
	public String getElementLabel(IModelElement element, long flags) {
		ILabelProvider[] providers = getProviders(element);
		if (providers != null) {
			for (int i = 0; i < providers.length; i++) {
				String text = providers[i].getText(element);
				if (text != null) {
					return text;
				}
			}
		}
		StyledString buf = new StyledString();
		getElementLabel(element, flags, buf);
		return buf.toString();
	}

	/**
	 * Returns the styled label for a Java element with the flags as defined by
	 * this class.
	 * 
	 * @param element
	 *            the element to render
	 * @param flags
	 *            the rendering flags
	 * @param result
	 *            the buffer to append the resulting label to
	 * 
	 * @since 3.4
	 */
	public void getElementLabel(IModelElement element, long flags,
			StyledString result) {
		getScriptElementLabelComposer(result).getElementLabel(element, flags);
	}

	/**
	 * Returns the label for a model element with the flags as defined by this
	 * class.
	 * 
	 * @param element
	 *            The element to render.
	 * @param flags
	 *            The rendering flags.
	 * @param buf
	 *            The buffer to append the resulting label to.
	 */
	public void getElementLabel(IModelElement element, long flags,
			StringBuffer buf) {
		getScriptElementLabelComposer(buf).getElementLabel(element, flags);
	}

	public void getProjectFragmentLabel(IProjectFragment root, long flags,
			StringBuffer buf) {
		getScriptElementLabelComposer(buf).getProjectFragmentLabel(root, flags);
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
	public void getScriptFolderLabel(IProjectFragment pack, long flags,
			StringBuffer buf) {

		getScriptElementLabelComposer(buf).getScriptFolderLabel(pack, flags);
	}

	/**
	 * Returns the label of a buildpath container
	 * 
	 * @param containerPath
	 *            The path of the container.
	 * @param project
	 *            The project the container is resolved in.
	 * @return Returns the label of the buildpath container
	 * @throws ModelException
	 *             Thrown when the resolving of the container failed.
	 */
	public String getContainerEntryLabel(IPath containerPath,
			IScriptProject project) throws ModelException {
		IBuildpathContainer container = DLTKCore.getBuildpathContainer(
				containerPath, project);
		if (container != null) {
			return container.getDescription();
		}
		BuildpathContainerInitializer initializer = DLTKCore
				.getBuildpathContainerInitializer(containerPath.segment(0));
		if (initializer != null) {
			return initializer.getDescription(containerPath, project);
		}
		return containerPath.toString();
	}

	protected void getTypeLabel(IType type, long flags, StringBuffer buf) {
		getScriptElementLabelComposer(buf).getTypeLabel(type, flags);
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
	public void getDeclarationLabel(IModelElement declaration, long flags,
			StringBuffer buf) {
		getScriptElementLabelComposer(buf).getDeclarationLabel(declaration,
				flags);
	}

	/**
	 * @since 2.0 Returns the styled label of the given object. The object must
	 *        be of type {@link IJavaElement} or adapt to
	 *        {@link IWorkbenchAdapter}. If the element type is not known, the
	 *        empty string is returned. The returned label is BiDi-processed
	 *        with {@link TextProcessor#process(String, String)}.
	 * 
	 * @param obj
	 *            object to get the label for
	 * @param flags
	 *            the rendering flags
	 * @return the label or the empty string if the object type is not supported
	 * 
	 * @since 3.4
	 * 
	 */
	public StyledString getStyledTextLabel(Object obj, long flags) {
		if (obj instanceof IModelElement) {
			return getStyledElementLabel((IModelElement) obj, flags);

		} else if (obj instanceof IAdaptable) {
			IWorkbenchAdapter wbadapter = (IWorkbenchAdapter) ((IAdaptable) obj)
					.getAdapter(IWorkbenchAdapter.class);
			if (wbadapter != null) {
				return Strings
						.markLTR(new StyledString(wbadapter.getLabel(obj)));
			}
		}
		return new StyledString();
	}

	/**
	 * Returns the styled label for a Java element with the flags as defined by
	 * this class.
	 * 
	 * @param element
	 *            the element to render
	 * @param flags
	 *            the rendering flags
	 * @return the label of the Java element
	 * 
	 * @since 3.4
	 */
	public StyledString getStyledElementLabel(IModelElement element,
			long flags) {
		StyledString result = new StyledString();
		getElementLabel(element, flags, result);
		return Strings.markLTR(result,
				ScriptElementLabelComposer.ADDITIONAL_DELIMITERS);

	}

	protected ScriptElementLabelComposer getScriptElementLabelComposer(
			StringBuffer buf) {

		return new ScriptElementLabelComposer(buf);
	}

	protected ScriptElementLabelComposer getScriptElementLabelComposer(
			StyledString buf) {
		return new ScriptElementLabelComposer(buf);
	}

	/**
	 * @since 2.0
	 */
	protected void getImportContainerLabel(IModelElement element, long flags,
			StringBuffer buf) {
		buf.append(Messages.ScriptElementLabels_import_declarations);
	}

	/**
	 * @since 2.0
	 */
	protected void getImportDeclarationLabel(IModelElement element, long flags,
			StringBuffer buf) {
		buf.append(element.getElementName());
		IImportDeclaration declaration = (IImportDeclaration) element;
		if (declaration.getVersion() != null
				&& declaration.getVersion().length() != 0) {
			buf.append(" "); //$NON-NLS-1$
			buf.append(declaration.getVersion());
		}
	}

}
