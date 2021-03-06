/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.core.manipulation.ScriptManipulationPlugin;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class DynamicValidationStateChange extends CompositeChange implements WorkspaceTracker.Listener {

	private boolean fListenerRegistered = false;
	private RefactoringStatus fValidationState = null;
	private long fTimeStamp;

	// 30 minutes
	private static final long LIFE_TIME = 30 * 60 * 1000;

	public DynamicValidationStateChange(Change change) {
		super(change.getName());
		add(change);
		markAsSynthetic();
	}

	public DynamicValidationStateChange(String name) {
		super(name);
		markAsSynthetic();
	}

	public DynamicValidationStateChange(String name, Change[] changes) {
		super(name, changes);
		markAsSynthetic();
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
		super.initializeValidationData(pm);
		WorkspaceTracker.INSTANCE.addListener(this);
		fListenerRegistered = true;
		fTimeStamp = System.currentTimeMillis();
	}

	@Override
	public void dispose() {
		if (fListenerRegistered) {
			WorkspaceTracker.INSTANCE.removeListener(this);
			fListenerRegistered = false;
		}
		super.dispose();
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException {
		if (fValidationState == null) {
			return super.isValid(pm);
		}
		return fValidationState;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		final Change[] result = new Change[1];
		IWorkspaceRunnable runnable = monitor -> result[0] = DynamicValidationStateChange.super.perform(monitor);
		DLTKCore.run(runnable, pm);
		return result[0];
	}

	@Override
	protected Change createUndoChange(Change[] childUndos) {
		DynamicValidationStateChange result = new DynamicValidationStateChange(getName());
		for (int i = 0; i < childUndos.length; i++) {
			result.add(childUndos[i]);
		}
		return result;
	}

	@Override
	public void workspaceChanged() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - fTimeStamp < LIFE_TIME)
			return;
		fValidationState = RefactoringStatus
				.createFatalErrorStatus(RefactoringCoreMessages.DynamicValidationStateChange_workspace_changed);
		// remove listener from workspace tracker
		WorkspaceTracker.INSTANCE.removeListener(this);
		fListenerRegistered = false;
		// clear up the children to not hang onto too much memory
		Change[] children = clear();
		for (int i = 0; i < children.length; i++) {
			final Change change = children[i];
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					change.dispose();
				}

				@Override
				public void handleException(Throwable exception) {
					ScriptManipulationPlugin.log(exception);
				}
			});
		}
	}
}
