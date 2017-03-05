/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.dltk.dbgp.IDbgpStatus;

public class DbgpStatus implements IDbgpStatus {
	// Reasons
	public static final Integer REASON_OK = Integer.valueOf(0);

	public static final Integer REASON_ERROR = Integer.valueOf(1);

	public static final Integer REASON_ABORTED = Integer.valueOf(2);

	public static final Integer REASON_EXCEPTION = Integer.valueOf(3);

	// Status
	public static final Integer STATUS_STARTING = Integer.valueOf(0);

	public static final Integer STATUS_STOPPING = Integer.valueOf(1);

	public static final Integer STATUS_STOPPED = Integer.valueOf(2);

	public static final Integer STATUS_RUNNING = Integer.valueOf(3);

	public static final Integer STATUS_BREAK = Integer.valueOf(4);

	private static final Map statusParser = new TreeMap(
			String.CASE_INSENSITIVE_ORDER);

	private static final Map reasonParser = new TreeMap(
			String.CASE_INSENSITIVE_ORDER);

	static {
		statusParser.put("starting", STATUS_STARTING); //$NON-NLS-1$
		statusParser.put("stopping", STATUS_STOPPING); //$NON-NLS-1$
		statusParser.put("stopped", STATUS_STOPPED); //$NON-NLS-1$
		statusParser.put("running", STATUS_RUNNING); //$NON-NLS-1$
		statusParser.put("break", STATUS_BREAK); //$NON-NLS-1$

		reasonParser.put("ok", REASON_OK); //$NON-NLS-1$
		reasonParser.put("error", REASON_ERROR); //$NON-NLS-1$
		reasonParser.put("aborted", REASON_ABORTED); //$NON-NLS-1$
		reasonParser.put("exception", REASON_EXCEPTION); //$NON-NLS-1$
	}

	public static IDbgpStatus parse(String status, String reason) {
		return new DbgpStatus((Integer) statusParser.get(status),
				(Integer) reasonParser.get(reason));
	}

	private final Integer status;

	private final Integer reason;

	public DbgpStatus(Integer status, Integer reason) {
		if (status == null) {
			throw new IllegalArgumentException();
		}

		if (reason == null) {
			throw new IllegalArgumentException();
		}

		this.status = status;
		this.reason = reason;
	}

	@Override
	public boolean reasonAborred() {
		return REASON_ABORTED == reason;
	}

	@Override
	public boolean reasonError() {
		return REASON_ERROR == reason;
	}

	@Override
	public boolean reasonException() {
		return REASON_EXCEPTION == reason;
	}

	@Override
	public boolean reasonOk() {
		return REASON_OK == reason;
	}

	@Override
	public boolean isRunning() {
		return STATUS_RUNNING == status;
	}

	@Override
	public boolean isStarting() {
		return STATUS_STARTING == status;
	}

	@Override
	public boolean isStopped() {
		return STATUS_STOPPED == status;
	}

	@Override
	public boolean isStopping() {
		return STATUS_STOPPING == status;
	}

	@Override
	public boolean isBreak() {
		return STATUS_BREAK == status;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DbgpStatus) {
			DbgpStatus s = (DbgpStatus) obj;
			return this.status == s.status && this.reason == s.reason;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (status.hashCode() << 8) | reason.hashCode();
	}

	@Override
	public String toString() {
		return "Status: " + status.toString() + "; Reason: " //$NON-NLS-1$ //$NON-NLS-2$
				+ reason.toString();
	}
}
