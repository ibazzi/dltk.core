/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.dbgp.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.commands.IDbgpPropertyCommands;
import org.eclipse.dltk.dbgp.internal.commands.DbgpPropertyCommands;
import org.eclipse.osgi.util.NLS;
import org.junit.Test;
import org.w3c.dom.Element;

public class DbgpPropertyCommandsTests extends DbgpProtocolTests {
	private static final String GET_PROPERTY_RESPONSE = "property_get.xml";
	private static final String SET_PROPERTY_RESPONSE = "property_set.xml";

	private IDbgpPropertyCommands commands;

	protected Element makePropertyGetResponse(String name, String fullName, String type) throws IOException {
		String xml = getResourceAsString(GET_PROPERTY_RESPONSE);
		xml = NLS.bind(xml, new Object[] { name, fullName, type, "_size", "_children" });
		return parseResponse(xml);
	}

	protected Element makePropertySetResponse(int transaction_id, boolean success) throws IOException {
		String xml = getResourceAsString(SET_PROPERTY_RESPONSE);
		xml = NLS.bind(xml, new Object[] { success ? "1" : "0", Integer.toString(transaction_id) });
		return parseResponse(xml);
	}

	@Test
	public void testGetPropertyByName() throws Exception {
		final Element response = makePropertyGetResponse("xxx", "test::xxx", "string");

		commands = new DbgpPropertyCommands(new AbstractCommunicator() {
			@Override
			public Element communicate(DbgpRequest request) {

				assertTrue(request.optionCount() == 2);
				assertTrue(request.hasOption("-i"));
				assertTrue(request.hasOption("-n"));

				return response;
			}
		});

		// IDbgpProperty property = commands.getPropery("my_var");
		// System.out.println(property);
	}

	@Test
	public void testSetProperty() throws Exception {
		final Element response = makePropertySetResponse(123, true);

		commands = new DbgpPropertyCommands(new AbstractCommunicator() {

			@Override
			public Element communicate(DbgpRequest request) {

				assertTrue(request.hasOption("-n"));

				return response;
			}
		});

		// boolean success = commands.setPropery("prop", 1, "val");
		// assertTrue(success);
	}
}
