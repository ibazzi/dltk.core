/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.ui.DLTKUIStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;


/**
 * An <code>IRunnableWithProgress</code> that adapts and  <code>IWorkspaceRunnable</code>
 * so that is can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code>
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress, IThreadListener {

	private boolean fTransfer= false;
	private IWorkspaceRunnable fWorkspaceRunnable;
	private ISchedulingRule fRule;

	/**
	 * Runs a workspace runnable with the workspace lock.
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
		this(runnable, ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at all.
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
		fWorkspaceRunnable= runnable;
		fRule= rule;
	}

	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at all.
	 * @param transfer <code>true</code> if the rule is to be transfered
	 *  to the model context thread. Otherwise <code>false</code>
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule, boolean transfer) {
		fWorkspaceRunnable= runnable;
		fRule= rule;
		fTransfer= transfer;
	}

	public ISchedulingRule getSchedulingRule() {
		return fRule;
	}

	@Override
	public void threadChange(Thread thread) {
		if (fTransfer)
			Job.getJobManager().transferRule(fRule, thread);
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			DLTKCore.run(fWorkspaceRunnable, fRule, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	public void runAsUserJob(String name, final Object jobFamiliy) {
		Job buildJob = new Job(name){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					WorkbenchRunnableAdapter.this.run(monitor);
				} catch (InvocationTargetException e) {
					Throwable cause= e.getCause();
					if (cause instanceof CoreException) {
						return ((CoreException) cause).getStatus();
					} else {
						return DLTKUIStatus.createError(IStatus.ERROR, cause);
					}
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return jobFamiliy == family;
			}
		};
		buildJob.setRule(fRule);
		buildJob.setUser(true);
		buildJob.schedule();

		// TODO: should block until user pressed 'to background'
	}
}
