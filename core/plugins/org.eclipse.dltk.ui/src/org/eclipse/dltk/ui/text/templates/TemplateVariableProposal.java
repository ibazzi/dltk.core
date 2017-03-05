/*******************************************************************************
 * Copyright (c) 2009, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.ui.text.templates;

import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.preferences.PreferencesMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * A proposal for insertion of template variables.
 */
public class TemplateVariableProposal implements ICompletionProposal {

	private TemplateVariableResolver fResolver;
	private int fOffset;
	private int fLength;
	private ITextViewer fViewer;

	private Point fSelection;
	private final boolean fIncludeBrace;

	/**
	 * Creates a template variable proposal.
	 *
	 * @param variable
	 *            the template variable
	 * @param offset
	 *            the offset to replace
	 * @param length
	 *            the length to replace
	 * @param viewer
	 *            the viewer
	 * @param includeBrace
	 *            whether to also replace the ${
	 */
	public TemplateVariableProposal(TemplateVariableResolver variable,
			int offset, int length, ITextViewer viewer, boolean includeBrace) {
		fResolver = variable;
		fOffset = offset;
		fLength = length;
		fViewer = viewer;
		fIncludeBrace = includeBrace;
	}

	@Override
	public void apply(IDocument document) {

		try {
			String variable;
			String type = fResolver.getType();
			if (type.equals("dollar")) //$NON-NLS-1$
				variable = "$$"; //$NON-NLS-1$
			else if (fIncludeBrace)
				variable = "${" + type + '}'; //$NON-NLS-1$
			else
				variable = type;
			document.replace(fOffset, fLength, variable);
			fSelection = new Point(fOffset + variable.length(), 0);

		} catch (BadLocationException e) {
			DLTKUIPlugin.log(e);

			Shell shell = fViewer.getTextWidget().getShell();
			MessageDialog.openError(shell,
					PreferencesMessages.TemplateVariableProposal_error_title, e
							.getMessage());
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return fSelection;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return fResolver.getDescription();
	}

	@Override
	public String getDisplayString() {
		return fResolver.getType();
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
}
