/*******************************************************************************
 * Copyright (c) 2010, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.dltk.core.INamespace;
import org.eclipse.dltk.internal.core.SourceNamespace;
import org.junit.Test;

public class NamespaceTests {

	@Test
	public void testCreate() {
		INamespace namespace = new SourceNamespace(new String[] { "java",
				"lang" });
		assertEquals("java.lang", namespace.getQualifiedName("."));
	}
	
	@Test
	public void testStrings() {
		String[] input = new String[] { "java", "lang" };
		INamespace namespace = new SourceNamespace(input);
		assertTrue(Arrays.equals(input, namespace.getStrings()));
	}
	
	@Test
	public void testStringsReturnCopy() {
		String[] input = new String[] { "java", "lang" };
		INamespace namespace = new SourceNamespace(input);
		Arrays.fill(namespace.getStrings(), "NO");
		assertTrue(Arrays.equals(input, namespace.getStrings()));
	}
	
	@Test
	public void testEquals() {
		INamespace first = new SourceNamespace(new String[] { "java", "lang" });
		INamespace second = new SourceNamespace(new String[] { "java", "lang" });
		assertTrue(first.equals(second));
	}
}
