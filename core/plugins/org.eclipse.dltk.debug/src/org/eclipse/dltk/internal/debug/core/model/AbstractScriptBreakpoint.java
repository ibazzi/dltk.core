/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.dbgp.IDbgpSession;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;

public abstract class AbstractScriptBreakpoint extends Breakpoint
		implements IScriptBreakpoint {

	/**
	 * Debugging engine breakpoint identifier (available only during debug
	 * session)
	 *
	 * @deprecated
	 */
	@Deprecated
	public static final String ENGINE_IDENTIFIER = DLTKDebugPlugin.PLUGIN_ID
			+ ".id"; //$NON-NLS-1$

	/**
	 * The number of breakpoint hits during debug session (available only during
	 * debug session)
	 *
	 * @deprecated
	 */
	@Deprecated
	public static final String HIT_COUNT = DLTKDebugPlugin.PLUGIN_ID
			+ ".hit_count"; //$NON-NLS-1$

	/**
	 * Condition expression that should be valid for suspend on this breakpoint
	 */
	public static final String EXPRESSION = DLTKDebugPlugin.PLUGIN_ID
			+ ".expression"; //$NON-NLS-1$

	/**
	 * State of condition expression (enabled or disabled)
	 */
	public static final String EXPRESSION_STATE = EXPRESSION + ".state"; //$NON-NLS-1$

	/**
	 * The number of hits for suspend on this breakpoint
	 */
	public static final String HIT_VALUE = DLTKDebugPlugin.PLUGIN_ID
			+ ".hit_value"; //$NON-NLS-1$

	/**
	 * The hit condition related to hit value
	 */
	public static final String HIT_CONDITION = DLTKDebugPlugin.PLUGIN_ID
			+ ".hit_condition"; //$NON-NLS-1$

	public static URI makeUri(IPath location) {
		try {
			String path = EnvironmentPathUtils.getLocalPath(location)
					.toString();
			if (path.length() != 0 && path.charAt(0) != '/') {
				path = '/' + path;
			}
			return new URI("file", "", path, null); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			DLTKDebugPlugin.log(e);
		}
		return null;
	}

	private String debugModelId;

	protected void addScriptBreakpointAttributes(Map attributes,
			String debugModelId, boolean enabled) {
		this.debugModelId = debugModelId;
		attributes.put(IBreakpoint.ID, debugModelId);
		attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
	}

	public AbstractScriptBreakpoint() {

	}

	@Override
	public String getModelIdentifier() {
		if (debugModelId == null) {
			try {
				debugModelId = ensureMarker().getAttribute(IBreakpoint.ID,
						null);
			} catch (DebugException e) {
				if (DLTKCore.DEBUG) {
					e.printStackTrace();
				}
				return null;
			}
		}
		return debugModelId;
	}

	private static class PerSessionInfo {
		String identifier;
		int hitCount = -1;
	}

	private final Map sessions = new IdentityHashMap(1);

	@Override
	public String getId(IDbgpSession session) {
		final PerSessionInfo info;
		synchronized (sessions) {
			info = (PerSessionInfo) sessions.get(session);
		}
		return info != null ? info.identifier : null;
	}

	@Override
	public void setId(IDbgpSession session, String identifier) {
		synchronized (sessions) {
			PerSessionInfo info = (PerSessionInfo) sessions.get(session);
			if (info == null) {
				info = new PerSessionInfo();
				sessions.put(session, info);
			}
			info.identifier = identifier;
		}
	}

	@Override
	public String removeId(IDbgpSession session) {
		final PerSessionInfo info;
		synchronized (sessions) {
			info = (PerSessionInfo) sessions.remove(session);
		}
		return info != null ? info.identifier : null;
	}

	@Override
	public void clearSessionInfo() {
		synchronized (sessions) {
			sessions.clear();
		}
	}

	@Override
	public String[] getIdentifiers() {
		final PerSessionInfo[] infos;
		synchronized (sessions) {
			infos = (PerSessionInfo[]) sessions.values()
					.toArray(new PerSessionInfo[sessions.size()]);
		}
		int count = 0;
		for (int i = 0; i < infos.length; ++i) {
			if (infos[i] != null && infos[i].identifier != null) {
				++count;
			}
		}
		if (count > 0) {
			final String[] result = new String[count];
			int index = 0;
			for (int i = 0; i < infos.length; ++i) {
				if (infos[i] != null && infos[i].identifier != null) {
					result[index++] = infos[i].identifier;
				}
			}
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void setHitCount(IDbgpSession session, int value)
			throws CoreException {
		synchronized (sessions) {
			PerSessionInfo info = (PerSessionInfo) sessions.get(session);
			if (info == null) {
				info = new PerSessionInfo();
				sessions.put(session, info);
			}
			info.hitCount = value;
		}
	}

	@Override
	public int getHitCount(IDbgpSession session) throws CoreException {
		final PerSessionInfo info;
		synchronized (sessions) {
			info = (PerSessionInfo) sessions.get(session);
		}
		return info != null ? info.hitCount : -1;
	}

	// Identifier
	@Override
	public String getIdentifier() throws CoreException {
		return null;
	}

	@Override
	public void setIdentifier(String id) throws CoreException {
		//
	}

	// Message
	@Override
	public String getMessage() throws CoreException {
		return ensureMarker().getAttribute(IMarker.MESSAGE, null);
	}

	@Override
	public void setMessage(String message) throws CoreException {
		setAttribute(IMarker.MESSAGE, message);
	}

	// Hit count
	@Override
	public int getHitCount() throws CoreException {
		synchronized (sessions) {
			if (sessions.isEmpty()) {
				return -1;
			}
			int result = 0;
			for (Iterator i = sessions.values().iterator(); i.hasNext();) {
				PerSessionInfo info = (PerSessionInfo) i.next();
				if (info.hitCount > 0) {
					result += info.hitCount;
				}
			}
			return result > 0 ? result : -1;
		}
	}

	@Override
	public void setHitCount(int value) throws CoreException {
		//
	}

	// Hit value
	@Override
	public int getHitValue() throws CoreException {
		return ensureMarker().getAttribute(HIT_VALUE, -1);
	}

	@Override
	public void setHitValue(int hitValue) throws CoreException {
		if (getHitValue() != hitValue) {
			setAttribute(HIT_VALUE, hitValue);
		}
	}

	// Hit condition
	@Override
	public int getHitCondition() throws CoreException {
		return ensureMarker().getAttribute(HIT_CONDITION, -1);
	}

	@Override
	public void setHitCondition(int condition) throws CoreException {
		if (getHitCondition() != condition) {
			setAttribute(HIT_CONDITION, condition);
		}
	}

	// Resource name
	@Override
	public String getResourceName() throws CoreException {
		return ensureMarker().getResource().getName();
	}

	// Expression
	@Override
	public String getExpression() throws CoreException {
		return ensureMarker().getAttribute(EXPRESSION, null);
	}

	@Override
	public void setExpression(String expression) throws CoreException {
		if (!StrUtils.equals(getExpression(), expression)) {
			setAttribute(EXPRESSION, expression);
		}
	}

	@Override
	public boolean getExpressionState() throws CoreException {
		return ensureMarker().getAttribute(EXPRESSION_STATE, false);
	}

	@Override
	public void setExpressionState(boolean state) throws CoreException {
		if (getExpressionState() != state) {
			setAttribute(EXPRESSION_STATE, state);
		}
	}

	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as
	 * unregistered.
	 */
	public void register(boolean register) throws CoreException {
		DebugPlugin plugin = DebugPlugin.getDefault();
		if (plugin != null && register) {
			plugin.getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}
}
