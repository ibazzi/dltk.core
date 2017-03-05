/*******************************************************************************
 * Copyright (c) 2008 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.debug.dbgp.tests.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class TestInitPacket {

	public static byte[] build(String ideKey) {
		final Map<String, String> attributes = createTestInitAttributes();
		attributes.put("idekey", ideKey);
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<init");
		appendAttributes(sb, attributes);
		sb.append("/>");
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static Map<String, String> createTestInitAttributes() {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("appid", "APPID");
		attributes.put("session", "DBGP_COOKIE");
		attributes.put("thread", "THREAD_ID");
		attributes.put("parent", "PARENT_APPID");
		attributes.put("language", "LANGUAGE_NAME");
		attributes.put("protocol_version", "1.0");
		attributes.put("fileuri", "file://path/to/file");
		return attributes;
	}

	private static void appendAttributes(StringBuffer sb, Map<String, String> attributes) {
		for (Iterator<Map.Entry<String, String>> i = attributes.entrySet().iterator(); i.hasNext();) {
			final Entry<String, String> entry = i.next();
			appendAttribute(sb, entry.getKey(), entry.getValue());
		}
	}

	private static void appendAttribute(StringBuffer sb, Object name, Object value) {
		sb.append(' ');
		sb.append(name);
		sb.append("=\"");
		sb.append(value);
		sb.append('"');
	}

}
