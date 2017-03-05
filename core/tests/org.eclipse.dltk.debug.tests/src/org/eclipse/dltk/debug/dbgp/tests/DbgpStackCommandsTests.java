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

import java.io.IOException;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.IDbgpStackLevel;
import org.eclipse.dltk.dbgp.commands.IDbgpStackCommands;
import org.eclipse.dltk.dbgp.internal.commands.DbgpStackCommands;
import org.eclipse.osgi.util.NLS;
import org.junit.Test;
import org.w3c.dom.Element;

public class DbgpStackCommandsTests extends DbgpProtocolTests {
	protected Element getStackDepthResponse(int transaction_id, int depth) throws IOException {
		String xml = getResourceAsString("get_stack_depth.xml");

		xml = NLS.bind(xml, new Object[] { Integer.toString(transaction_id), Integer.toString(depth) });

		return parseResponse(xml);
	}

	protected Element getStackGetResponse(int transaction_id) throws IOException {
		String xml = getResourceAsString("stack_get.xml");

		xml = NLS.bind(xml, new Object[] { Integer.toString(transaction_id) });

		return parseResponse(xml);
	}

	protected Element getStackLevelsResponse() {
		return null;
	}

	@Test
	public void testStackDepth() throws Exception {
		final Element response = getStackDepthResponse(0, 3);

		IDbgpStackCommands commands = new DbgpStackCommands(new AbstractCommunicator() {
			@Override
			public Element communicate(DbgpRequest request) {

				assertEquals(1, request.optionCount());
				assertTrue(request.hasOption("-i"));

				return response;
			}
		});

		int depth = commands.getStackDepth();
		assertEquals(3, depth);
	}

	@Test
	public void testGetStackLevel() throws Exception {
		final Element response = getStackGetResponse(0);

		IDbgpStackCommands commands = new DbgpStackCommands(new AbstractCommunicator() {
			@Override
			public Element communicate(DbgpRequest request) {

				assertTrue(request.hasOption("-i"));

				return response;
			}
		});

		IDbgpStackLevel level = commands.getStackLevel(0);
		assertEquals(0, level.getLevel());
		assertEquals(8, level.getLineNumber());
	}
}
