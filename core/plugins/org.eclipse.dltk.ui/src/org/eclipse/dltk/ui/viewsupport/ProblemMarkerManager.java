/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.viewsupport;

import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.dltk.internal.ui.util.SWTUtil;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.editor.SourceModuleAnnotationModelEvent;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.swt.widgets.Display;

/**
 * Listens to resource deltas and filters for marker changes of type IMarker.PROBLEM
 * Viewers showing error ticks should register as listener to
 * this type.
 */
public class ProblemMarkerManager implements IResourceChangeListener, IAnnotationModelListener , IAnnotationModelListenerExtension {

	/**
	 * Visitors used to look if the element change delta contains a marker change.
	 */
	private static class ProjectErrorVisitor implements IResourceDeltaVisitor {

		private HashSet fChangedElements;

		public ProjectErrorVisitor(HashSet changedElements) {
			fChangedElements= changedElements;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res= delta.getResource();
			if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
				IProject project= (IProject) res;
				if (!project.isAccessible()) {
					// only track open Java projects
					return false;
				}
			}
			checkInvalidate(delta, res);
			return true;
		}

		private void checkInvalidate(IResourceDelta delta, IResource resource) {
			int kind= delta.getKind();
			if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED || (kind == IResourceDelta.CHANGED && isErrorDelta(delta))) {
				// invalidate the resource and all parents
				while (resource.getType() != IResource.ROOT && fChangedElements.add(resource)) {
					resource= resource.getParent();
				}
			}
		}

		private boolean isErrorDelta(IResourceDelta delta) {
			if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
				IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
				for (int i= 0; i < markerDeltas.length; i++) {
					if (markerDeltas[i].isSubtypeOf(IMarker.PROBLEM)) {
						int kind= markerDeltas[i].getKind();
						if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED)
							return true;
						int severity= markerDeltas[i].getAttribute(IMarker.SEVERITY, -1);
						int newSeverity= markerDeltas[i].getMarker().getAttribute(IMarker.SEVERITY, -1);
						if (newSeverity != severity)
							return true;
					}
				}
			}
			return false;
		}
	}

	private ListenerList fListeners;


	public ProblemMarkerManager() {
		fListeners= new ListenerList();
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		HashSet<IResource> changedElements = new HashSet<IResource>();

		try {
			IResourceDelta delta= event.getDelta();
			if (delta != null)
				delta.accept(new ProjectErrorVisitor(changedElements));
		} catch (CoreException e) {
			DLTKUIPlugin.log(e.getStatus());
		}

		if (!changedElements.isEmpty()) {
			IResource[] changes = changedElements
					.toArray(new IResource[changedElements.size()]);
			fireChanges(changes, true);
		}
	}

	@Override
	public void modelChanged(IAnnotationModel model) {
		// no action
	}

	@Override
	public void modelChanged(AnnotationModelEvent event) {
		if (event instanceof SourceModuleAnnotationModelEvent) {
			SourceModuleAnnotationModelEvent cuEvent= (SourceModuleAnnotationModelEvent) event;
			if (cuEvent.includesProblemMarkerAnnotationChanges()) {
				IResource[] changes= new IResource[] { cuEvent.getUnderlyingResource() };
				fireChanges(changes, false);
			}
		}
	}


	/**
	 * Adds a listener for problem marker changes.
	 */
	public void addListener(IProblemChangedListener listener) {
		if (fListeners.isEmpty()) {
			DLTKUIPlugin.getWorkspace().addResourceChangeListener(this);
			DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider().addGlobalAnnotationModelListener(this);
		}
		fListeners.add(listener);
	}

	/**
	 * Removes a <code>IProblemChangedListener</code>.
	 */
	public void removeListener(IProblemChangedListener listener) {
		fListeners.remove(listener);
		if (fListeners.isEmpty()) {
			DLTKUIPlugin.getWorkspace().removeResourceChangeListener(this);
			DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider().removeGlobalAnnotationModelListener(this);
		}
	}

	private void fireChanges(final IResource[] changes, final boolean isMarkerChange) {
		Display display= SWTUtil.getStandardDisplay();
		if (display != null && !display.isDisposed()) {
			display.asyncExec(() -> {
				Object[] listeners= fListeners.getListeners();
				for (int i= 0; i < listeners.length; i++) {
					IProblemChangedListener curr= (IProblemChangedListener) listeners[i];
					curr.problemsChanged(changes, isMarkerChange);
				}
			});
		}
	}

}
