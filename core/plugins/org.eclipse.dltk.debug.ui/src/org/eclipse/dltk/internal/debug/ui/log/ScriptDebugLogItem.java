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
package org.eclipse.dltk.internal.debug.ui.log;

import org.eclipse.dltk.dbgp.IDbgpRawPacket;

public class ScriptDebugLogItem {

	private final long timestamp;
	private final String type;
	private final int sessionId;
	private final String message;

	public ScriptDebugLogItem(String type, String message) {
		this.timestamp = System.currentTimeMillis();
		this.type = type;
		this.sessionId = 0;
		this.message = message;
	}

	public ScriptDebugLogItem(String type, int sessionId,
			IDbgpRawPacket message) {
		this(System.currentTimeMillis(), type, sessionId, message);
	}

	/**
	 * @param message
	 * @param timestamp
	 * @param type
	 */
	public ScriptDebugLogItem(long timestamp, String type, int sessionId,
			IDbgpRawPacket message) {
		this.timestamp = timestamp;
		this.type = type;
		this.sessionId = sessionId;
		this.message = message.getPacketAsString();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public int getSessionId() {
		return sessionId;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return type + '\t' + message;
	}

}
