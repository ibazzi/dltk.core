/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.core.tests.compiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.dltk.compiler.CharOperation;
import org.junit.Test;

public class CompilerCharOperationTests {
	@Test
	public void testStartsWith() {
		assertTrue(CharOperation.startsWith("abc".toCharArray(), "ab".toCharArray()));
		assertTrue(CharOperation.startsWith("abc".toCharArray(), "abc".toCharArray()));
		assertFalse(CharOperation.startsWith("abc".toCharArray(), "d".toCharArray()));
		assertFalse(CharOperation.startsWith("abc".toCharArray(), "abcd".toCharArray()));
	}

}
