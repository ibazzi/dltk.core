/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.dbgp.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.junit.Before;
import org.junit.Test;

public class DbgpRequestTests {

	private DbgpRequest request;

	@Before
	public void setUp() {

		request = new DbgpRequest("test_command");
	}

	@Test
	public void testOptions() {
		request.addOption("-t", 324);

		assertTrue(request.hasOption("-t"));
		assertEquals(Integer.toString(324), request.getOption("-t"));

		assertEquals("test_command", request.getCommand());
	}

	@Test
	public void testData() {
		request.setData("my_data");
		assertEquals("my_data", request.getData());
	}

	@Test
	public void testStringRepresentation() {
		request.addOption("-i", 324);
		request.setData("my_data");

		assertEquals("test_command -i 324 -- bXlfZGF0YQ==", request.toString());
	}

	@Test
	public void testEquals() {
		DbgpRequest r1 = new DbgpRequest("step_command_xxx");
		r1.addOption("-a", 32);
		r1.addOption("-b", 12);
		r1.setData("my_data");

		DbgpRequest r2 = new DbgpRequest("step_command_xxx");
		r2.setData("my_data");
		r2.addOption("-b", 12);
		r2.addOption("-a", 32);

		assertEquals(r1, r2);
		assertEquals(r2, r1);
	}
}
