/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.dbgp.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.eclipse.dltk.dbgp.exceptions.DbgpProtocolException;
import org.eclipse.dltk.dbgp.internal.utils.DbgpXmlParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DbgpProtocolTests {

	protected String getResourceAsString(String name) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(name)));

			StringBuffer sb = new StringBuffer();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}

			return sb.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	protected Element parseResponse(String xml) {
		try {
			Document doc = DbgpXmlParser.parseXml(xml.getBytes(StandardCharsets.ISO_8859_1));
			return (Element) doc.getFirstChild();
		} catch (DbgpProtocolException e) {
			e.printStackTrace();
		}

		return null;
	}
}
