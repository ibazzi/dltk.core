/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.console.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dltk.console.IScriptConsoleShell;
import org.eclipse.dltk.console.ScriptConsoleCompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

public abstract class ScriptConsoleCompletionProcessor implements IContentAssistProcessor {
	protected static interface IProposalDecorator {
		String formatProposal(ScriptConsoleCompletionProposal c);

		Image getImage(ScriptConsoleCompletionProposal c);
	}

	private static IProposalDecorator defaultDecorator;

	protected IProposalDecorator getDefaultDecorator() {
		if (defaultDecorator == null) {
			defaultDecorator = new IProposalDecorator() {
				@Override
				public String formatProposal(ScriptConsoleCompletionProposal c) {
					return c.getDisplay();
				}

				@Override
				public Image getImage(ScriptConsoleCompletionProposal c) {
					return null;
				}
			};
		}

		return defaultDecorator;
	}

	private IScriptConsoleShell interpreterShell;

	public ScriptConsoleCompletionProcessor(IScriptConsoleShell interpreterShell) {
		this.interpreterShell = interpreterShell;
	}

	protected IScriptConsoleShell getInterpreterShell() {
		return this.interpreterShell;
	}

	protected List<CompletionProposal> createProposalsFromString(List<ScriptConsoleCompletionProposal> list, int offset,
			IProposalDecorator decorator) {

		if (decorator == null) {
			decorator = getDefaultDecorator();
		}

		List<CompletionProposal> result = new ArrayList<>();

		Iterator<ScriptConsoleCompletionProposal> it = list.iterator();
		while (it.hasNext()) {
			ScriptConsoleCompletionProposal c = it.next();

			CompletionProposal proposal = new CompletionProposal(c.getInsert(), // replacementString
					offset, // replacementOffset
					0, // replacementLength
					c.getInsert().length(), // cursorPosition
					decorator.getImage(c), // image
					decorator.formatProposal(c), // displayString
					null, // contextInformation
					null); // additionalContextInformation

			result.add(proposal);
		}

		Collections.sort(result, (p1, p2) -> p1.getDisplayString().compareTo(p2.getDisplayString()));

		return result;
	}

	protected abstract ICompletionProposal[] computeCompletionProposalsImpl(IScriptConsoleViewer viewer, int offset);

	protected abstract IContextInformation[] computeContextInformationImpl(ITextViewer viewer, int offset);

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		return computeCompletionProposalsImpl((IScriptConsoleViewer) viewer, offset);
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return computeContextInformationImpl(viewer, offset);
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
