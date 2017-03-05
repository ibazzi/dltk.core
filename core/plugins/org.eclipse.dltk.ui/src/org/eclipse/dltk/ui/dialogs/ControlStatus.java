/*******************************************************************************
 * Copyright (c) 2009, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.swt.widgets.Control;

/**
 * @since 2.0
 */
public class ControlStatus implements IStatus {

	private final int fSeverity;
	private final String fMessage;
	private final Control fControl;

	public ControlStatus(int severity, String message, Control control) {
		this.fSeverity = severity;
		this.fMessage = message;
		this.fControl = control;
	}

	@Override
	public IStatus[] getChildren() {
		return Status.OK_STATUS.getChildren();
	}

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public Throwable getException() {
		return null;
	}

	@Override
	public String getMessage() {
		return fMessage;
	}

	@Override
	public String getPlugin() {
		return DLTKUIPlugin.PLUGIN_ID;
	}

	@Override
	public int getSeverity() {
		return fSeverity;
	}

	@Override
	public boolean isMultiStatus() {
		return false;
	}

	@Override
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}

	@Override
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	public Control getControl() {
		return fControl;
	}

}
