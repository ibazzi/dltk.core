/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal;

import java.io.IOException;

import org.eclipse.dltk.debug.core.DLTKDebugPlugin;

public abstract class DbgpWorkingThread extends DbgpTermination {
	private Thread thread;
	private final String name;

	public DbgpWorkingThread(String name) {
		this.name = name;
	}

	public void start() {
		if (thread == null || !thread.isAlive()) {
			thread = new Thread((Runnable) () -> {
				try {
					workingCycle();
				} catch (Exception e) {
					if (isLoggable(e)) {
						DLTKDebugPlugin.logError(
								Messages.DbgpWorkingThread_workingCycleError,
								e);
					}
					fireObjectTerminated(e);
					return;
				}

				fireObjectTerminated(null);
			}, name);

			thread.start();
		} else {
			throw new IllegalStateException(
					Messages.DbgpWorkingThread_threadAlreadyStarted);
		}
	}

	@Override
	public void requestTermination() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}

	@Override
	public void waitTerminated() throws InterruptedException {
		if (thread != null)
			thread.join();
	}

	/**
	 * Tests if this exception should be logged. The rationale here is
	 * IOExceptions/SocketExceptions occurs always after socket is closed, so
	 * there is no point to log it.
	 *
	 * @param e
	 * @return
	 */
	protected boolean isLoggable(Exception e) {
		return !(e instanceof IOException);
	}

	// Working cycle
	protected abstract void workingCycle() throws Exception;
}
