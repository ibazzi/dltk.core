/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.editor;


import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.dltk.compiler.problem.DefaultProblemIdentifier;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.core.CorrectionEngine;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class ScriptMarkerAnnotation extends MarkerAnnotation implements IScriptAnnotation {

	public static final String ERROR_ANNOTATION_TYPE= "org.eclipse.dltk.ui.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE= "org.eclipse.dltk.ui.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE= "org.eclipse.dltk.ui.info"; //$NON-NLS-1$
	public static final String TASK_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$

	private IScriptAnnotation fOverlay;


	public ScriptMarkerAnnotation(IMarker marker) {
		super(marker);
	}

	@Override
	public String[] getArguments() {
		IMarker marker= getMarker();
		if (marker != null && marker.exists() && isProblem())
			return CorrectionEngine.getProblemArguments(marker);
		return null;
	}

	@Override
	public IProblemIdentifier getId() {
		IMarker marker= getMarker();
		if (marker == null  || !marker.exists())
			return null;

		if (isProblem())
			return DefaultProblemIdentifier.getProblemId(marker);

//		if (TASK_ANNOTATION_TYPE.equals(getAnnotationType())) {
//			try {
//				if (marker.isSubtypeOf(IScriptModelMarker.TASK_MARKER)) {
//					return IProblem.Task;
//				}
//			} catch (CoreException e) {
//				DLTKUIPlugin.log(e); // should no happen, we test for marker.exists
//			}
//		}

		return null;
	}

	@Override
	public boolean isProblem() {
		String type= getType();
		return WARNING_ANNOTATION_TYPE.equals(type) || ERROR_ANNOTATION_TYPE.equals(type);
	}

	/**
	 * Overlays this annotation with the given javaAnnotation.
	 *
	 * @param javaAnnotation annotation that is overlaid by this annotation
	 */
	public void setOverlay(IScriptAnnotation javaAnnotation) {
		if (fOverlay != null)
			fOverlay.removeOverlaid(this);

		fOverlay= javaAnnotation;
		if (!isMarkedDeleted())
			markDeleted(fOverlay != null);

		if (fOverlay != null)
			fOverlay.addOverlaid(this);
	}

	@Override
	public boolean hasOverlay() {
		return fOverlay != null;
	}

	@Override
	public IScriptAnnotation getOverlay() {
		return fOverlay;
	}

	@Override
	public void addOverlaid(IScriptAnnotation annotation) {
		// not supported
	}

	@Override
	public void removeOverlaid(IScriptAnnotation annotation) {
		// not supported
	}

	@Override
	public Iterator getOverlaidIterator() {
		// not supported
		return null;
	}

	@Override
	public ISourceModule getSourceModule() {
		IModelElement element= DLTKCore.create(getMarker().getResource());
		if (element instanceof ISourceModule) {
			return (ISourceModule)element;
		}
		return null;
	}

	@Override
	public String getMarkerType() {
		IMarker marker= getMarker();
		if (marker == null  || !marker.exists())
			return null;

		return  MarkerUtilities.getMarkerType(getMarker());
	}

	@Override
	public int getSourceStart() {
		return MarkerUtilities.getCharStart(getMarker());
	}

	@Override
	public int getSourceEnd() {
		return MarkerUtilities.getCharEnd(getMarker());
	}
}
