/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.corext;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.core.ISourceRange;

/**
 * Use {@link org.eclipse.dltk.core.SourceRange}.
 */
@Deprecated
public class SourceRange implements ISourceRange {

	private final int fOffset;
	private final int fLength;

	public SourceRange(int offset, int length) {
		fLength = length;
		fOffset = offset;
	}

	public SourceRange(ASTNode node) {
		this(node.sourceStart(), node.sourceEnd() - node.sourceStart());
	}

	public SourceRange(IProblem problem) {
		this(problem.getSourceStart(),
				problem.getSourceEnd() - problem.getSourceStart() + 1);
	}

	public SourceRange(ISourceRange range) {
		this(range.getOffset(), range.getLength());
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	public int getEndExclusive() {
		return getOffset() + getLength();
	}

	public int getEndInclusive() {
		return getEndExclusive() - 1;
	}

	@Override
	public String toString() {
		return "<offset: " + fOffset + " length: " + fLength + "/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Sorts the given ranges by offset (backwards). Note: modifies the
	 * parameter.
	 * 
	 * @param ranges
	 *            the ranges to sort
	 * @return the sorted ranges, which are identical to the parameter ranges
	 */
	public static ISourceRange[] reverseSortByOffset(ISourceRange[] ranges) {
		Comparator<ISourceRange> comparator = (o1, o2) -> o2.getOffset()
				- o1.getOffset();
		Arrays.sort(ranges, comparator);
		return ranges;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ISourceRange))
			return false;
		return ((ISourceRange) obj).getOffset() == fOffset
				&& ((ISourceRange) obj).getLength() == fLength;
	}

	@Override
	public int hashCode() {
		return fLength ^ fOffset;
	}

	public boolean covers(ASTNode node) {
		return covers(new SourceRange(node));
	}

	public boolean covers(SourceRange range) {
		return getOffset() <= range.getOffset()
				&& getEndInclusive() >= range.getEndInclusive();
	}

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=130161
	 * (Script Model returns ISourceRanges [-1, 0] if source not available).
	 *
	 * @param range
	 *            a source range, can be <code>null</code>
	 * @return <code>true</code> iff range is not null and range.getOffset() is
	 *         not -1
	 */
	public static boolean isAvailable(ISourceRange range) {
		return range != null && range.getOffset() != -1;
	}
}
