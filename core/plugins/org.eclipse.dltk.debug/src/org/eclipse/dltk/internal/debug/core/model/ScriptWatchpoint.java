/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.core.model.IScriptWatchpoint;

public class ScriptWatchpoint extends ScriptLineBreakpoint
		implements IScriptWatchpoint {

	public static final String FIELD_NAME = DLTKDebugPlugin.PLUGIN_ID
			+ ".fieldName"; //$NON-NLS-1$

	public static final String ACCESS = DLTKDebugPlugin.PLUGIN_ID + ".access"; //$NON-NLS-1$

	public static final String MODIFICATION = DLTKDebugPlugin.PLUGIN_ID
			+ ".modification"; //$NON-NLS-1$

	public ScriptWatchpoint(final String debugModelId, final IResource resource,
			final IPath path, final int lineNumber, final int start,
			final int end, final String fieldName) throws CoreException {
		IWorkspaceRunnable wr = monitor -> {
			// create the marker
			setMarker(resource.createMarker(getMarkerId()));

			final Map attributes = new HashMap();
			// add attributes
			addScriptBreakpointAttributes(attributes, debugModelId, true);
			addLineBreakpointAttributes(attributes, path, lineNumber, start,
					end);
			attributes.put(FIELD_NAME, fieldName);

			// set attributes
			ensureMarker().setAttributes(attributes);

			// add to breakpoint manager if requested
			register(true);
		};
		run(getMarkerRule(resource), wr);
	}

	public ScriptWatchpoint() {
	}

	@Override
	public String getFieldName() throws CoreException {
		return this.getMarker().getAttribute(FIELD_NAME, ""); //$NON-NLS-1$
	}

	public void setFieldName(String name) throws CoreException {
		this.getMarker().setAttribute(FIELD_NAME, name);
	}

	@Override
	protected String getMarkerId() {
		return ScriptMarkerFactory.WATCHPOINT_MARKER_ID;
	}

	@Override
	public boolean isAccess() throws CoreException {
		return Boolean
				.parseBoolean(this.getMarker().getAttribute(ACCESS, "true")); //$NON-NLS-1$
	}

	@Override
	public boolean isModification() throws CoreException {
		return Boolean.parseBoolean(
				this.getMarker().getAttribute(MODIFICATION, "true")); //$NON-NLS-1$
	}

	@Override
	public void setAccess(boolean access) throws CoreException {
		this.getMarker().setAttribute(ACCESS, Boolean.toString(access));
	}

	@Override
	public void setModification(boolean modification) throws CoreException {
		this.getMarker().setAttribute(MODIFICATION,
				Boolean.toString(modification));
	}

	@Override
	public boolean supportsAccess() {
		return true;
	}

	@Override
	public boolean supportsModification() {
		return true;
	}

	private static final String[] UPDATABLE_ATTRS = new String[] { FIELD_NAME,
			ACCESS, MODIFICATION };

	@Override
	public String[] getUpdatableAttributes() {
		List all = new ArrayList();
		all.addAll(Arrays.asList(super.getUpdatableAttributes()));
		all.addAll(Arrays.asList(UPDATABLE_ATTRS));
		return (String[]) all.toArray(new String[all.size()]);
	}
}
