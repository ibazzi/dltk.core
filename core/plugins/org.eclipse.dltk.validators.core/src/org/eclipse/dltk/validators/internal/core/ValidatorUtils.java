/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.validators.internal.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IExternalSourceModule;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.IParent;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.core.BuiltinProjectFragment;
import org.eclipse.dltk.internal.core.BuiltinSourceModule;
import org.eclipse.dltk.internal.core.ExternalProjectFragment;
import org.eclipse.dltk.internal.core.util.HandleFactory;

public class ValidatorUtils {
	private static class ResourceVisitor implements IResourceVisitor {
		private Set resources;

		public ResourceVisitor(Set resources) {
			this.resources = resources;
		}

		@Override
		public boolean visit(IResource resource) {
			if (!this.resources.contains(resource) && resource.getType() == IResource.FILE) {
				resources.add(resource);
				return false;
			}
			return true;
		}
	}

	private static class SourceModuleVisitor implements IModelElementVisitor {
		private Set<IModelElement> elements;

		public SourceModuleVisitor(Set<IModelElement> elements) {
			this.elements = elements;
		}

		/**
		 * Visit only external source modules, witch we aren't builded yet.
		 */
		@Override
		public boolean visit(IModelElement element) {
			if (element.getElementType() == IModelElement.PROJECT_FRAGMENT) {
				if ((element instanceof ExternalProjectFragment) || (element instanceof BuiltinProjectFragment)) {
					return false;
				}
			}
			if (element.getElementType() == IModelElement.SOURCE_MODULE
					&& !(element instanceof IExternalSourceModule || element instanceof BuiltinSourceModule)
					&& element.getResource() != null) {
				if (!elements.contains(element)) {
					elements.add(element);
				}
				return false; // do not enter into source module content.
			}
			return true;
		}
	}

	public static void processResourcesToElements(Object o, final Set elements, final Set resources) {
		if (o instanceof IResource) {
			Set els = new HashSet();
			ResourceVisitor visitor = new ResourceVisitor(els);
			try {
				((IResource) o).accept(visitor);
			} catch (CoreException e) {
				if (DLTKCore.DEBUG) {
					e.printStackTrace();
				}
			}
			for (Iterator iterator = els.iterator(); iterator.hasNext();) {
				Object object = iterator.next();

				Object eo = convertResourceToModelElement(object);
				if (eo != null) {
					if (eo instanceof IModelElement) {
						elements.add(eo);
					} else if (eo instanceof IResource) {
						resources.add(eo);
					}
				}
			}
		} else if (o instanceof IModelElement) {
			if (o instanceof IParent) {
				SourceModuleVisitor visitor = new SourceModuleVisitor(elements);
				try {
					((IModelElement) o).accept(visitor);
				} catch (ModelException e) {
					if (DLTKCore.DEBUG) {
						e.printStackTrace();
					}
				}
			} else if (!(o instanceof ISourceModule)) {
				ISourceModule module = (ISourceModule) ((IModelElement) o).getAncestor(IModelElement.SOURCE_MODULE);
				if (module != null) {
					elements.add(module);
				}
			} else if (o instanceof ISourceModule) {
				elements.add(o);
			}
		}
	}

	private static final HandleFactory factory = new HandleFactory();

	private static Object convertResourceToModelElement(Object o) {
		if (o instanceof IModelElement) {
			return o;
		}
		if (!(o instanceof IResource)) {
			return null;
		}
		IResource res = (IResource) o;
		IProject project = res.getProject();
		if (!DLTKLanguageManager.hasScriptNature(project)) {
			return null; // Lets pass not script projects.
		}
		IScriptProject scriptProject = DLTKCore.create(project);
		IDLTKSearchScope scope = SearchEngine.createSearchScope(scriptProject);

		IModelElement element = factory.createOpenable(res.getFullPath().toString(), scope);
		if (element != null && element.getElementType() == IModelElement.SOURCE_MODULE && element.exists()) {
			// elements.add(element);
			return element;
		} else {
			return res;
		}
	}

	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}
}
