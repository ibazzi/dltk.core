/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal.managers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.dltk.dbgp.IDbgpStreamListener;
import org.eclipse.dltk.dbgp.internal.DbgpWorkingThread;
import org.eclipse.dltk.dbgp.internal.IDbgpDebugingEngine;
import org.eclipse.dltk.dbgp.internal.packets.DbgpStreamPacket;

public class DbgpStreamManager extends DbgpWorkingThread
		implements IDbgpStreamManager {
	private final ListenerList listeners = new ListenerList();

	private final IDbgpDebugingEngine engine;

	protected void fireStderrReceived(String data) {
		if (data == null || data.length() == 0)
			return;
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; ++i) {
			((IDbgpStreamListener) list[i]).stderrReceived(data);
		}
	}

	protected void fireStdoutReceived(String data) {
		if (data == null || data.length() == 0)
			return;
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; ++i) {
			((IDbgpStreamListener) list[i]).stdoutReceived(data);
		}
	}

	@Override
	protected void workingCycle() throws Exception {
		try {
			while (!Thread.interrupted()) {
				final DbgpStreamPacket packet = engine.getStreamPacket();

				if (packet.isStderr()) {
					fireStderrReceived(packet.getTextContent());
				} else if (packet.isStdout()) {
					fireStdoutReceived(packet.getTextContent());
				}
			}
		} catch (InterruptedException e) {
			// OK, interrupted
		}
	}

	public DbgpStreamManager(IDbgpDebugingEngine engine, String name) {
		super(name);

		if (engine == null) {
			throw new IllegalArgumentException();
		}

		this.engine = engine;
	}

	@Override
	public void addListener(IDbgpStreamListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IDbgpStreamListener listener) {
		listeners.remove(listener);
	}

}
