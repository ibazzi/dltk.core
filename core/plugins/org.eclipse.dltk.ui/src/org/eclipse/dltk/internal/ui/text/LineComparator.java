/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.text;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * This implementation of <code>IRangeComparator</code> compares lines of a
 * document. The lines are compared using a DJB hash function.
 *
 * @since 3.0
 */
public class LineComparator implements IRangeComparator {

	private final IDocument fDocument;
	private final ArrayList<Integer> fHashes;

	/**
	 * Create a line comparator for the given document.
	 *
	 * @param document
	 */
	public LineComparator(IDocument document) {
		fDocument = document;
		// fills the list with nulls
		Integer[] nulls = new Integer[fDocument.getNumberOfLines()];
		fHashes = new ArrayList<Integer>(Arrays.asList(nulls));
	}

	@Override
	public int getRangeCount() {
		return fDocument.getNumberOfLines();
	}

	@Override
	public boolean rangesEqual(int thisIndex, IRangeComparator other,
			int otherIndex) {
		try {
			return getHash(thisIndex).equals(
					((LineComparator) other).getHash(otherIndex));
		} catch (BadLocationException e) {
			DLTKUIPlugin.log(e);
			return false;
		}
	}

	@Override
	public boolean skipRangeComparison(int length, int maxLength,
			IRangeComparator other) {
		return false;
	}

	/**
	 * @param line
	 *            the number of the line in the document to get the hash for
	 * @return the hash of the line
	 * @throws BadLocationException
	 *             if the line number is invalid
	 */
	private Integer getHash(int line) throws BadLocationException {
		Integer hash = fHashes.get(line);
		if (hash == null) {
			IRegion lineRegion = fDocument.getLineInformation(line);
			String lineContents = fDocument.get(lineRegion.getOffset(),
					lineRegion.getLength());
			hash = Integer.valueOf(computeDJBHash(lineContents));
			fHashes.set(line, hash);
		}

		return hash;
	}

	/**
	 * Compute a hash using the DJB hash algorithm
	 *
	 * @param string
	 *            the string for which to compute a hash
	 * @return the DJB hash value of the string
	 */
	private int computeDJBHash(String string) {
		int hash = 5381;
		int len = string.length();
		for (int i = 0; i < len; i++) {
			char ch = string.charAt(i);
			hash = (hash << 5) + hash + ch;
		}

		return hash;
	}
}
