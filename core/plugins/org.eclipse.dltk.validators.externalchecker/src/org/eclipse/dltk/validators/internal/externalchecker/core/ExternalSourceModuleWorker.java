/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 ******************************************************************************/
package org.eclipse.dltk.validators.internal.externalchecker.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.validators.core.IValidatorOutput;
import org.eclipse.dltk.validators.core.IValidatorReporter;
import org.eclipse.dltk.validators.core.SourceModuleValidatorWorker;

public class ExternalSourceModuleWorker extends SourceModuleValidatorWorker {

	private ExternalCheckerDelegate delegate;

	public ExternalSourceModuleWorker(IEnvironment environment, ExternalChecker externalChecker) {
		delegate = new ExternalCheckerDelegate(environment, externalChecker);
	}

	@Override
	public void clean(IResource[] resources) {
		super.clean(resources);
	}

	@Override
	protected IValidatorReporter createValidatorReporter() {
		return delegate.createValidatorReporter();
	}

	@Override
	protected String getTaskName() {
		return Messages.ExternalChecker_checkingWithExternalExecutable;
	}

	@Override
	protected String getMarkerId() {
		return delegate.getMarkerId();
	}

	@Override
	protected String getNullResourceMessage() {
		return Messages.ExternalChecker_sourceModuleResourceIsNull;
	}

	@Override
	protected String getPluginId() {
		return ExternalCheckerPlugin.PLUGIN_ID;
	}

	@Override
	protected boolean isValidatorConfigured() {
		return delegate.isValidatorConfigured();
	}

	@Override
	protected boolean isValidSourceModule(ISourceModule module) {
		IResource resource = module.getResource();
		return delegate.isValidExtension(resource.getFileExtension());
	}

	@Override
	protected void runValidator(final ISourceModule module, IValidatorOutput console, final IValidatorReporter reporter,
			IProgressMonitor monitor) throws CoreException {
		delegate.runValidator(module.getResource(), console, problem -> reporter.report(module, problem));
	}
}
