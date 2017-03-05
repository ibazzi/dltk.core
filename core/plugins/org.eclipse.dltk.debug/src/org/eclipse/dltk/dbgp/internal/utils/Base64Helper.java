/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and implementation
 *     xored software, Inc. - fix decode chunked base64 (Bug# 230825) (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal.utils;

import java.nio.charset.StandardCharsets;

public class Base64Helper {

	/**
	 * Empty string constant
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static String encodeString(String s) {
		if (s != null && s.length() != 0) {
			final byte[] encode = Base64
					.encode(s.getBytes(StandardCharsets.UTF_8));
			return new String(encode, StandardCharsets.ISO_8859_1);
		}
		return EMPTY;
	}

	public static String decodeString(String base64) {
		if (base64 != null && base64.length() != 0) {
			final byte[] bytes = base64.getBytes(StandardCharsets.ISO_8859_1);
			final int length = discardWhitespace(bytes);
			if (length > 0) {
				final int decodedLength = Base64.decodeInlplace(bytes, length);
				return new String(bytes, 0, decodedLength,
						StandardCharsets.UTF_8);
			}
		}
		return EMPTY;
	}

	/**
	 * Discards any whitespace from a base-64 encoded block. The base64 data in
	 * responses could be chunked in the multiple lines, so we need to remove
	 * extra whitespaces.
	 *
	 * The bytes are copied in-place and the length of the actual data bytes is
	 * returned.
	 *
	 * @param bytes
	 * @return
	 */
	private static int discardWhitespace(byte[] data) {
		final int length = data.length;
		int i = 0;
		while (i < length) {
			byte c = data[i++];
			if (c == (byte) ' ' || c == (byte) '\n' || c == (byte) '\r'
					|| c == (byte) '\t') {
				int count = i - 1;
				while (i < length) {
					c = data[i++];
					if (c != (byte) ' ' && c != (byte) '\n' && c != (byte) '\r'
							&& c != (byte) '\t') {
						data[count++] = c;
					}
				}
				return count;
			}
		}
		return length;
	}

	public static String encodeBytes(byte[] bytes) {
		return new String(Base64.encode(bytes));
	}

	public static byte[] decodeBytes(String base64) {
		return Base64.decode(base64.getBytes());
	}
}
