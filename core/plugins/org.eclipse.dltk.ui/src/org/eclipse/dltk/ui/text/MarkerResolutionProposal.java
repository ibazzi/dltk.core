/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.ui.text;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class MarkerResolutionProposal implements IScriptCompletionProposal {

	private IMarkerResolution fResolution;
	private IMarker fMarker;

	/**
	 * Constructor for MarkerResolutionProposal.
	 *
	 * @param resolution
	 *            the marker resolution
	 * @param marker
	 *            the marker
	 */
	public MarkerResolutionProposal(IMarkerResolution resolution, IMarker marker) {
		fResolution = resolution;
		fMarker = marker;
	}

	@Override
	public void apply(IDocument document) {
		fResolution.run(fMarker);
	}

	@Override
	public String getAdditionalProposalInfo() {
		if (fResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2) fResolution).getDescription();
		}
		if (fResolution instanceof IScriptCompletionProposal) {
			return ((IScriptCompletionProposal) fResolution)
					.getAdditionalProposalInfo();
		}
		try {
			String problemDesc = (String) fMarker.getAttribute(IMarker.MESSAGE);
			return problemDesc;
		} catch (CoreException e) {
			DLTKUIPlugin.log(e);
		}
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return fResolution.getLabel();
	}

	@Override
	public Image getImage() {
		if (fResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2) fResolution).getImage();
		}
		if (fResolution instanceof IScriptCompletionProposal) {
			return ((IScriptCompletionProposal) fResolution).getImage();
		}
		return DLTKPluginImages.get(DLTKPluginImages.IMG_CORRECTION_CHANGE);
	}

	@Override
	public int getRelevance() {
		if (fResolution instanceof IScriptCompletionProposal) {
			return ((IScriptCompletionProposal) fResolution).getRelevance();
		}
		return 10;
	}

	@Override
	public Point getSelection(IDocument document) {
		if (fResolution instanceof IScriptCompletionProposal) {
			return ((IScriptCompletionProposal) fResolution)
					.getSelection(document);
		}
		return null;
	}

}
