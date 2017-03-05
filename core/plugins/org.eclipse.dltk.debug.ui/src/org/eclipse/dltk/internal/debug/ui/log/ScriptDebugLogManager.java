/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.dltk.internal.debug.ui.log;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.dltk.dbgp.IDbgpRawListener;
import org.eclipse.dltk.dbgp.IDbgpRawPacket;
import org.eclipse.dltk.dbgp.internal.IDbgpDebugingEngine;
import org.eclipse.dltk.debug.core.ExtendedDebugEventDetails;
import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

public class ScriptDebugLogManager
		implements ILaunchListener, IDebugEventSetListener, IDbgpRawListener {

	private static ScriptDebugLogManager instance;

	private ScriptDebugLogView view;

	private ScriptDebugLogManager() {
		// empty constructor
	}

	public static synchronized ScriptDebugLogManager getInstance() {
		if (instance == null) {
			instance = new ScriptDebugLogManager();
		}

		return instance;
	}

	@Override
	public void dbgpPacketReceived(int sessionId, IDbgpRawPacket content) {
		append(new ScriptDebugLogItem(Messages.ItemType_Input, sessionId,
				content));
	}

	@Override
	public void dbgpPacketSent(int sessionId, IDbgpRawPacket content) {
		append(new ScriptDebugLogItem(Messages.ItemType_Output, sessionId,
				content));
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		if (view == null) {
			return;
		}

		for (int i = 0; i < events.length; ++i) {
			DebugEvent event = events[i];

			append(new ScriptDebugLogItem(Messages.ItemType_Event,
					getDebugEventKind(event) + " from " //$NON-NLS-1$
							+ event.getSource().getClass().getName()));

			if (event.getKind() == DebugEvent.CREATE) {
				handleCreateEvent(event);
			} else if (event.getKind() == DebugEvent.MODEL_SPECIFIC && event
					.getDetail() == ExtendedDebugEventDetails.DGBP_NEW_CONNECTION) {
				if (event.getSource() instanceof IDbgpDebugingEngine) {
					((IDbgpDebugingEngine) event.getSource())
							.addRawListener(this);
				}
			} else if (event.getKind() == DebugEvent.TERMINATE) {
				handleTerminateEvent(event);
			}
		}
	}

	@Override
	public void launchAdded(ILaunch launch) {
		// empty implementation
	}

	@Override
	public void launchChanged(ILaunch launch) {
		IDebugTarget target = launch.getDebugTarget();
		boolean loggingEnabled = LaunchConfigurationUtils
				.isDbgpLoggingEnabled(launch.getLaunchConfiguration());

		// bail if we're not a ScriptDebugTarget or logging isn't enabled
		if (!((target instanceof IScriptDebugTarget) && loggingEnabled)) {
			return;
		}

		Display.getDefault().asyncExec(() -> {
			IWorkbenchPage page = DLTKDebugUIPlugin.getActivePage();

			if (page != null) {
				try {
					view = (ScriptDebugLogView) page
							.showView(ScriptDebugLogView.VIEW_ID);

					DebugPlugin.getDefault()
							.addDebugEventListener(ScriptDebugLogManager.this);
				} catch (PartInitException e) {
					DLTKDebugUIPlugin.log(e);
				}
			}
		});
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		// empty implementation
	}

	protected void append(final ScriptDebugLogItem item) {
		view.append(item);
	}

	private static String getDebugEventKind(DebugEvent event) {
		switch (event.getKind()) {
		case DebugEvent.CREATE:
			return Messages.EventKind_Create;
		case DebugEvent.TERMINATE:
			return Messages.EventKind_Terminate;
		case DebugEvent.CHANGE:
			return Messages.EventKind_Change;
		case DebugEvent.SUSPEND:
			return Messages.EventKind_Suspend;
		case DebugEvent.RESUME:
			return Messages.EventKind_Resume;
		case DebugEvent.MODEL_SPECIFIC:
			return Messages.EventKind_ModelSpecific + '/' + event.getDetail();
		}
		return Messages.EventKind_Unknown + '(' + event.getKind() + ')';
	}

	private void handleCreateEvent(DebugEvent event) {
		if (event.getSource() instanceof IScriptThread) {
			((IScriptThread) event.getSource()).getDbgpSession()
					.addRawListener(this);
		}
	}

	private void handleTerminateEvent(DebugEvent event) {
		if (event.getSource() instanceof IScriptThread) {
			((IScriptThread) event.getSource()).getDbgpSession()
					.removeRawListenr(this);
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
	}

}
