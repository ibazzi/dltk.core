/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.typehierarchy;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.ui.DLTKUIMessages;
import org.eclipse.dltk.internal.ui.actions.OpenActionUtil;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.util.ExceptionHandler;
import org.eclipse.dltk.utils.AdaptUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class OpenTypeHierarchyUtil {

	private OpenTypeHierarchyUtil() {
	}

	public static TypeHierarchyViewPart open(IModelElement element, IWorkbenchWindow window) {
		IModelElement[] candidates= getCandidates(element);
		if (candidates != null) {
			return open(candidates, window);
		}
		return null;
	}

	public static TypeHierarchyViewPart open(IModelElement[] candidates, IWorkbenchWindow window) {
		Assert.isNotNull(candidates);
		Assert.isTrue(candidates.length != 0);

		IModelElement input= null;
		if (candidates.length > 1) {
			String title= DLTKUIMessages.OpenTypeHierarchyUtil_selectionDialog_title;
			String message= DLTKUIMessages.OpenTypeHierarchyUtil_selectionDialog_message;
			input= OpenActionUtil.selectModelElement(candidates, window.getShell(), title, message);
		} else {
			input= candidates[0];
		}
		if (input instanceof IMember
				&& input.getElementType() != IModelElement.TYPE) {
			input = ((IMember) input).getDeclaringType();
		}

		if (input == null)
			return null;

//		try {
//			if (PreferenceConstants.OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE.equals(
//					PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.OPEN_TYPE_HIERARCHY))) {
//				return openInPerspective(window, input);
//			} else {
				return openInViewPart(window, input);
//			}

//		} catch (WorkbenchException e) {
//			ExceptionHandler.handle(e, window.getShell(),
//					DLTKUIMessages.OpenTypeHierarchyUtil_error_open_perspective,
//				e.getMessage());
//		} catch (ModelException e) {
//			ExceptionHandler.handle(e, window.getShell(),
//					DLTKUIMessages.OpenTypeHierarchyUtil_error_open_editor,
//				e.getMessage());
//		}
//		return null;
	}

	private static TypeHierarchyViewPart openInViewPart(IWorkbenchWindow window, IModelElement input) {
		IWorkbenchPage page= window.getActivePage();
		try {
			TypeHierarchyViewPart result= (TypeHierarchyViewPart) page.findView(DLTKUIPlugin.ID_TYPE_HIERARCHY);
			if (result != null) {
				result.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
			}
			result= (TypeHierarchyViewPart) page.showView(DLTKUIPlugin.ID_TYPE_HIERARCHY);
			result.setInputElement(input);
			return result;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, window.getShell(),
				DLTKUIMessages.OpenTypeHierarchyUtil_error_open_view, e.getMessage());
		}
		return null;
	}

	/**
	 * Converts the input to a possible input candidates
	 */
	public static IModelElement[] getCandidates(Object input) {
		if (!(input instanceof IModelElement)) {
			return null;
		}
		try {
			IModelElement elem= (IModelElement) input;
			switch (elem.getElementType()) {
				case IModelElement.METHOD:
				case IModelElement.FIELD:
				case IModelElement.TYPE:
				case IModelElement.SCRIPT_PROJECT:
					return new IModelElement[] { elem };
			case IModelElement.LOCAL_VARIABLE: {
				final IType type = AdaptUtils.getAdapter(elem, IType.class);
				if (type != null) {
					return new IModelElement[] { type };
				} else {
					return null;
				}
			}
//				case IModelElement.PROJECT_FRAGMENT:
//					if (((IProjectFragment)elem).containsJavaResources())
//						return new IModelElement[] {elem};
//					break;
//				case IModelElement.PACKAGE_DECLARATION:
//					return new IModelElement[] { elem.getAncestor(IModelElement.PACKAGE_FRAGMENT) };
//				case IModelElement.IMPORT_DECLARATION:
//					IImportDeclaration decl= (IImportDeclaration) elem;
//					if (decl.isOnDemand()) {
//						elem= JavaModelUtil.findTypeContainer(elem.getJavaProject(), Signature.getQualifier(elem.getElementName()));
//					} else {
//						elem= elem.getJavaProject().findType(elem.getElementName());
//					}
//					if (elem == null)
//						return null;
//					return new IModelElement[] {elem};
//
//				case IModelElement.CLASS_FILE:
//					return new IModelElement[] { ((IClassFile)input).getType() };
				case IModelElement.SOURCE_MODULE: {
					ISourceModule cu= (ISourceModule) elem.getAncestor(IModelElement.SOURCE_MODULE);
					if (cu != null) {
						IType[] types= cu.getTypes();
						if (types.length > 0) {
							return types;
						}
					}
					break;
				}
				default:
			}
		} catch (ModelException e) {
			DLTKUIPlugin.log(e);
		}
		return null;
	}
}
