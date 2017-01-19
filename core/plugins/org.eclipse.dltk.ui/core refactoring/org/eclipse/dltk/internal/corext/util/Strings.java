/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *

 *******************************************************************************/
package org.eclipse.dltk.internal.corext.util;

import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.osgi.util.TextProcessor;

/**
 * Helper class to provide String manipulation functions not available in
 * standard JDK.
 */
public class Strings {

	private Strings() {
	}

	/**
	 * Tells whether we have to use the {@link TextProcessor}
	 * <p>
	 * This is used for performance optimization.
	 * </p>
	 *
	 * @since 3.4
	 */
	public static final boolean USE_TEXT_PROCESSOR;

	static {
		String testString = "args : String[]"; //$NON-NLS-1$
		USE_TEXT_PROCESSOR = testString != TextProcessor.process(testString);
	}

	private static final String SCRIPT_ELEMENT_DELIMITERS = TextProcessor
			.getDefaultDelimiters() + "<>(),?{} "; //$NON-NLS-1$

	public static boolean startsWithIgnoreCase(String text, String prefix) {
		int textLength = text.length();
		int prefixLength = prefix.length();
		if (textLength < prefixLength)
			return false;
		for (int i = prefixLength - 1; i >= 0; i--) {
			if (Character.toLowerCase(prefix.charAt(i)) != Character
					.toLowerCase(text.charAt(i)))
				return false;
		}
		return true;
	}

	public static boolean isLowerCase(char ch) {
		return Character.toLowerCase(ch) == ch;
	}

	public static String removeMnemonicIndicator(String string) {
		return LegacyActionTools.removeMnemonics(string);
	}

	public static String[] convertIntoLines(String input) {
		try {
			ILineTracker tracker = new DefaultLineTracker();
			tracker.set(input);
			int size = tracker.getNumberOfLines();
			String result[] = new String[size];
			for (int i = 0; i < size; i++) {
				IRegion region = tracker.getLineInformation(i);
				int offset = region.getOffset();
				result[i] = input.substring(offset,
						offset + region.getLength());
			}
			return result;
		} catch (BadLocationException e) {
			return null;
		}
	}

	public static String concatenate(String[] lines, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			if (i > 0)
				buffer.append(delimiter);
			buffer.append(lines[i]);
		}
		return buffer.toString();
	}

	public static boolean containsOnlyWhitespaces(String s) {
		int size = s.length();
		for (int i = 0; i < size; i++) {
			if (!Character.isWhitespace(s.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Sets the given <code>styler</code> to use for
	 * <code>matchingRegions</code> (obtained from
	 * {@link org.eclipse.jdt.core.search.SearchPattern#getMatchingRegions}) in
	 * the <code>styledString</code> starting from the given <code>index</code>.
	 *
	 * @param styledString
	 *            the styled string to mark
	 * @param index
	 *            the index from which to start marking
	 * @param matchingRegions
	 *            the regions to mark
	 * @param styler
	 *            the styler to use for marking
	 */
	public static void markMatchingRegions(StyledString styledString, int index,
			int[] matchingRegions, Styler styler) {
		if (matchingRegions != null) {
			int offset = -1;
			int length = 0;
			for (int i = 0; i + 1 < matchingRegions.length; i = i + 2) {
				if (offset == -1)
					offset = index + matchingRegions[i];

				// Concatenate adjacent regions
				if (i + 2 < matchingRegions.length && matchingRegions[i]
						+ matchingRegions[i + 1] == matchingRegions[i + 2]) {
					length = length + matchingRegions[i + 1];
				} else {
					styledString.setStyle(offset,
							length + matchingRegions[i + 1], styler);
					offset = -1;
					length = 0;
				}
			}
		}
	}

	/**
	 * Adds special marks so that that the given styled string is readable in a
	 * BIDI environment.
	 *
	 * @param styledString
	 *            the styled string
	 * @return the processed styled string
	 * @since 3.4
	 */
	public static StyledString markLTR(StyledString styledString) {

		/*
		 * NOTE: For performance reasons we do not call markLTR(styledString,
		 * null)
		 */

		if (!USE_TEXT_PROCESSOR)
			return styledString;

		String inputString = styledString.getString();
		String string = TextProcessor.process(inputString);
		if (string != inputString)
			insertMarks(styledString, inputString, string);
		return styledString;
	}

	/**
	 * Adds special marks so that that the given styled Java element label is
	 * readable in a BiDi environment.
	 *
	 * @param styledString
	 *            the styled string
	 * @return the processed styled string
	 * @since 3.6
	 */
	public static StyledString markScriptElementLabelLTR(
			StyledString styledString) {
		if (!USE_TEXT_PROCESSOR)
			return styledString;

		String inputString = styledString.getString();
		String string = TextProcessor.process(inputString,
				SCRIPT_ELEMENT_DELIMITERS);
		if (string != inputString)
			insertMarks(styledString, inputString, string);
		return styledString;
	}

	/**
	 * Adds special marks so that that the given styled string is readable in a
	 * BIDI environment.
	 *
	 * @param styledString
	 *            the styled string
	 * @param additionalDelimiters
	 *            the additional delimiters
	 * @return the processed styled string
	 * @since 3.4
	 */
	public static StyledString markLTR(StyledString styledString,
			String additionalDelimiters) {
		if (!USE_TEXT_PROCESSOR)
			return styledString;

		String inputString = styledString.getString();
		String string = TextProcessor.process(inputString,
				TextProcessor.getDefaultDelimiters() + additionalDelimiters);
		if (string != inputString)
			insertMarks(styledString, inputString, string);
		return styledString;
	}

	/**
	 * Inserts the marks into the given styled string.
	 *
	 * @param styledString
	 *            the styled string
	 * @param originalString
	 *            the original string
	 * @param processedString
	 *            the processed string
	 * @since 3.5
	 */
	private static void insertMarks(StyledString styledString,
			String originalString, String processedString) {
		int i = 0;
		char orig = originalString.charAt(0);
		int processedStringLength = originalString.length();
		for (int processedIndex = 0; processedIndex < processedStringLength; processedIndex++) {
			char processed = processedString.charAt(processedIndex);
			if (orig == processed)
				orig = originalString.charAt(++i);
			else
				styledString.insert(processed, processedIndex);
		}
	}
}
