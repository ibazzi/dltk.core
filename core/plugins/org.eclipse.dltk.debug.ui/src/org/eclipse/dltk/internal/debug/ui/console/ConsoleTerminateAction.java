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
package org.eclipse.dltk.internal.debug.ui.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.commands.actions.DebugCommandService;
import org.eclipse.dltk.debug.ui.ScriptDebugConsole;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * ConsoleTerminateAction
 */
public class ConsoleTerminateAction extends Action implements IUpdate {

	private final class TerminateListener implements ILaunchesListener2 {
		@Override
		public void launchesRemoved(ILaunch[] launches) {

		}

		@Override
		public void launchesChanged(ILaunch[] launches) {
		}

		@Override
		public void launchesAdded(ILaunch[] launches) {

		}

		@Override
		public void launchesTerminated(ILaunch[] launches) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> update());
		}
	}

	private ScriptDebugConsole fConsole;
	private IWorkbenchWindow fWindow;
	private TerminateListener listener;

	/**
	 * Creates a terminate action for the console
	 */
	public ConsoleTerminateAction(IWorkbenchWindow window,
			ScriptDebugConsole console) {
		super(ConsoleMessages.ConsoleTerminateAction_0);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		fConsole = console;
		fWindow = window;
		setToolTipText(ConsoleMessages.ConsoleTerminateAction_1);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(
				IInternalDebugUIConstants.IMG_LCL_TERMINATE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(
				IInternalDebugUIConstants.IMG_DLCL_TERMINATE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(
				IInternalDebugUIConstants.IMG_LCL_TERMINATE));

		listener = new TerminateListener();
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(listener);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		// IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		update();
	}

	@Override
	public void update() {
		if (fConsole != null) {
			ILaunch launch = fConsole.getLaunch();
			setEnabled(launch.canTerminate());
		}
	}

	@Override
	public void run() {
		final List<ITerminate> targets = new ArrayList<>();
		final ILaunch launch = fConsole.getLaunch();
		final IDebugTarget[] debugTargets = launch.getDebugTargets();
		for (int k = 0; k < debugTargets.length; k++) {
			targets.add(debugTargets[k]);
		}
		final IProcess[] processes = launch.getProcesses();
		for (int k = 0; k < processes.length; ++k) {
			targets.add(processes[k]);
		}

		DebugCommandService service = DebugCommandService.getService(fWindow);
		service.executeCommand(ITerminateHandler.class, targets.toArray(),
				null);

	}

	public void dispose() {
		fConsole = null;
		DebugPlugin.getDefault().getLaunchManager()
				.removeLaunchListener(listener);
	}

}
