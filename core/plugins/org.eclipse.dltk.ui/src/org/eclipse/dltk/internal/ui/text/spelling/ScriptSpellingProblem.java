/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.text.spelling;

import org.eclipse.dltk.compiler.problem.CategorizedProblem;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

/**
 * Spelling problem to be accepted by problem requesters.
 *
 * @since 4.0
 */
public class ScriptSpellingProblem extends CategorizedProblem {

	// spelling 'marker type' name. Only virtual as spelling problems are never
	// persisted in markers.
	// marker type is used in the quickFixProcessor extension point
	public static final String MARKER_TYPE = "org.eclipse.dltk.ui.spelling"; //$NON-NLS-1$

	/** The end offset of the problem */
	private int fSourceEnd = 0;

	/** The line number of the problem */
	private int fLineNumber = 1;

	/** The start offset of the problem */
	private int fSourceStart = 0;

	/** The description of the problem */
	private String fMessage;

	/** The misspelled word */
	private String fWord;

	/** Was the word found in the dictionary? */
	private boolean fMatch;

	/** Does the word start a new sentence? */
	private boolean fSentence;

	/** The associated document */
	private IDocument fDocument;

	/** The originating file name */
	private String fOrigin;

	private SpellingProblem fSpellingProblem;

	/**
	 * Initialize with the given parameters.
	 *
	 * @param start
	 *            the start offset
	 * @param end
	 *            the end offset
	 * @param line
	 *            the line
	 * @param message
	 *            the message
	 * @param word
	 *            the word
	 * @param match
	 *            <code>true</code> iff the word was found in the dictionary
	 * @param sentence
	 *            <code>true</code> iff the word starts a sentence
	 * @param document
	 *            the document
	 * @param origin
	 *            the originating file name
	 * @param problem
	 */
	public ScriptSpellingProblem(int start, int end, int line, String message,
			String word, boolean match, boolean sentence, IDocument document,
			String origin, SpellingProblem problem) {
		super();
		fSourceStart = start;
		fSourceEnd = end;
		fLineNumber = line;
		fMessage = message;
		fWord = word;
		fMatch = match;
		fSentence = sentence;
		fDocument = document;
		fOrigin = origin;
		fSpellingProblem = problem;
	}

	public SpellingProblem getSpellingProblem() {
		return fSpellingProblem;
	}

	@Override
	public String[] getArguments() {

		String prefix = ""; //$NON-NLS-1$
		String postfix = ""; //$NON-NLS-1$

		try {

			IRegion line = fDocument.getLineInformationOfOffset(fSourceStart);
			prefix = fDocument.get(line.getOffset(),
					fSourceStart - line.getOffset());
			int postfixStart = fSourceEnd + 1;
			postfix = fDocument.get(postfixStart,
					line.getOffset() + line.getLength() - postfixStart);

		} catch (BadLocationException exception) {
			// Do nothing
		}
		return new String[] { fWord, prefix, postfix,
				fSentence ? Boolean.toString(true) : Boolean.toString(false),
				fMatch ? Boolean.toString(true) : Boolean.toString(false) };
	}

	@Override
	public IProblemIdentifier getID() {
		return SpellingProblems.SPELLING_PROBLEM;
	}

	@Override
	public String getMessage() {
		return fMessage;
	}

	@Override
	public String getOriginatingFileName() {
		return fOrigin;
	}

	@Override
	public int getSourceEnd() {
		return fSourceEnd;
	}

	@Override
	public int getSourceLineNumber() {
		return fLineNumber;
	}

	@Override
	public int getSourceStart() {
		return fSourceStart;
	}

	@Override
	public void setSeverity(ProblemSeverity severity) {
		// unsupported
	}

	@Override
	public boolean isError() {
		return false;
	}

	@Override
	public boolean isWarning() {
		return true;
	}

	@Override
	public boolean isTask() {
		return false;
	}

	@Override
	public void setSourceStart(int sourceStart) {
		fSourceStart = sourceStart;
	}

	@Override
	public void setSourceEnd(int sourceEnd) {
		fSourceEnd = sourceEnd;
	}

	@Override
	public void setSourceLineNumber(int lineNumber) {
		fLineNumber = lineNumber;
	}

	@Override
	public int getCategoryID() {
		return 0;
	}

	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}
}
