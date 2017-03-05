/*******************************************************************************
 * Copyright (c) 2012, 2017 NumberFour AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     NumberFour AG - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.core.tests.utils;

import static org.junit.Assert.assertEquals;

import org.eclipse.dltk.utils.IntList;
import org.junit.Test;

public class IntListTests {
	@Test
	public void testRemoveAt() {
		IntList list = new IntList();
		list.add(100);
		list.add(200);
		list.add(300);
		assertEquals(3, list.size());
		list.removeAt(1);
		assertEquals(2, list.size());
		assertEquals(100, list.get(0));
		assertEquals(300, list.get(1));
	}

}
