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
package org.eclipse.dltk.core.tests.util;

import static org.junit.Assert.assertEquals;

import java.util.EmptyStackException;

import org.eclipse.dltk.utils.CharacterStack;
import org.junit.Before;
import org.junit.Test;

public class CharacterStackTests {

	private CharacterStack stack;

	@Before
	public void setUp() throws Exception {
		stack = new CharacterStack();
	}

	@Test
	public void testPush() {
		stack.push('A');
		assertEquals(1, stack.size());
	}

	@Test
	public void testPop() {
		stack.push('A');
		stack.push('B');
		assertEquals('B', stack.pop());
		assertEquals('A', stack.pop());
	}
	
	@Test
	public void testPeek() {
		stack.push('A');
		assertEquals('A', stack.peek());
		stack.push('B');
		assertEquals('B', stack.peek());
	}
	
	@Test(expected=EmptyStackException.class)
	public void testEmptyPop() {
		stack.pop();
	}
	
	@Test(expected=EmptyStackException.class)
	public void testEmptyPeek() {
		stack.peek();
	}

}
