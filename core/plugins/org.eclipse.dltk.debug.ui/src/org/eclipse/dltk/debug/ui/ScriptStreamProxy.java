/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.debug.core.model.IScriptStreamProxy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class ScriptStreamProxy implements IScriptStreamProxy {
	private IOConsoleInputStream input;
	private IOConsoleOutputStream stdOut;
	private IOConsoleOutputStream stdErr;

	private boolean closed = false;

	public ScriptStreamProxy(IOConsole console) {
		input = console.getInputStream();
		stdOut = console.newOutputStream();
		stdErr = console.newOutputStream();

		// TODO is there a better way to access these internal preferences??
		final IPreferenceStore debugUIStore = DebugUIPlugin.getDefault()
				.getPreferenceStore();
		stdOut.setActivateOnWrite(debugUIStore
				.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT));
		stdErr.setActivateOnWrite(debugUIStore
				.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR));

		getDisplay().asyncExec(() -> {
			final DLTKDebugUIPlugin colors = DLTKDebugUIPlugin.getDefault();
			stdOut.setColor(
					colors.getColor(PreferenceConverter.getColor(debugUIStore,
							IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR)));
			stdErr.setColor(
					colors.getColor(PreferenceConverter.getColor(debugUIStore,
							IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR)));
		});
	}

	private Display getDisplay() {
		// If we are in the UI Thread use that
		if (Display.getCurrent() != null) {
			return Display.getCurrent();
		}

		if (PlatformUI.isWorkbenchRunning()) {
			return PlatformUI.getWorkbench().getDisplay();
		}

		return Display.getDefault();
	}

	@Override
	public OutputStream getStderr() {
		return stdErr;
	}

	@Override
	public OutputStream getStdout() {
		return stdOut;
	}

	@Override
	public InputStream getStdin() {
		return input;
	}

	@Override
	public synchronized void close() {
		if (!closed) {
			try {
				stdOut.close();
				stdErr.close();
				input.close();
				closed = true;
			} catch (IOException e) {
				if (DLTKCore.DEBUG) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean needsEncoding = false;
	private String encoding = null;

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
		needsEncoding = encoding != null && !encoding
				.equals(WorkbenchEncoding.getWorkbenchDefaultEncoding());
	}

	@Override
	public void writeStdout(String value) {
		write(stdOut, value);
	}

	@Override
	public void writeStderr(String value) {
		write(stdErr, value);
	}

	private void write(IOConsoleOutputStream stream, String value) {
		try {
			if (needsEncoding) {
				stream.write(value.getBytes(encoding));
			} else {
				stream.write(value);
			}
			stream.flush();
		} catch (IOException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
			DLTKDebugUIPlugin.log(e);
		}
	}

}
