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
package org.eclipse.dltk.core.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.dltk.compiler.CharOperation;
import org.junit.Test;

public class CharOperationTests {

	@Test
    public void testSplitOnNull() {
        char[][] result = CharOperation.splitOn("::".toCharArray(), null);
        assertNull(result);
    }
	@Test
    public void testSplitOn1() {
        char[][] result = CharOperation.splitOn("::".toCharArray(), "AAA".toCharArray());
        assertEquals(1, result.length);
        assertEquals("AAA", new String(result[0]));
    }
	@Test
    public void testSplitOn2() {
        char[][] result = CharOperation.splitOn("::".toCharArray(), "A::B".toCharArray());
        assertEquals(2, result.length);
        assertEquals("A", new String(result[0]));
        assertEquals("B", new String(result[1]));
    }
	@Test
    public void testSplitOn3() {
        char[][] result = CharOperation.splitOn("::".toCharArray(), "AA::BB::CC".toCharArray());
        assertEquals(3, result.length);
        assertEquals("AA", new String(result[0]));
        assertEquals("BB", new String(result[1]));
        assertEquals("CC", new String(result[2]));
    }
	@Test
    public void testStringConcatWith() {
        char[] result = CharOperation.concatWith(new String[] { "A", "B" }, '.');
        assertEquals(3, result.length);
        assertEquals('A', result[0]);
        assertEquals('.', result[1]);
        assertEquals('B', result[2]);
    }
	@Test
    public void testStringConcatWithString() {
        char[] result = CharOperation.concatWith(new String[] { "A", "B" }, "::");
        assertEquals(4, result.length);
        assertEquals('A', result[0]);
        assertEquals(':', result[1]);
        assertEquals(':', result[2]);
        assertEquals('B', result[3]);
    }
	@Test
    public void testStringEmptyConcatWithString() {
        char[] result = CharOperation.concatWith(new String[] { "A", "", "B" }, "::");
        assertEquals(4, result.length);
        assertEquals('A', result[0]);
        assertEquals(':', result[1]);
        assertEquals(':', result[2]);
        assertEquals('B', result[3]);
    }
	@Test
    public void testConcatCharArrays() {
        String result = new String(CharOperation.concatWith(new char[][] { "A".toCharArray(), "B".toCharArray() }, new char[][] {
                "C".toCharArray(), "D".toCharArray() }, '.'));
        assertEquals("A.B.C.D", result);
    }
}
