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
package org.eclipse.dltk.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.internal.ui.wizards.ProjectWizardInitializerManager;
import org.eclipse.dltk.internal.ui.wizards.ProjectWizardState;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.ui.wizards.IProjectWizardInitializer.IProjectWizardState;
import org.eclipse.dltk.utils.ResourceUtil;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * @since 2.0
 */
public abstract class ProjectWizard extends NewElementWizard implements
		INewWizard, IExecutableExtension, IProjectWizard {

	private IConfigurationElement fConfigElement;

	@Override
	public void createPageControls(Composite pageContainer) {
		initProjectWizard();
		super.createPageControls(pageContainer);
	}

	/**
	 * @since 3.0
	 */
	protected void initProjectWizard() {
		for (IWizardPage page : getPages()) {
			if (page instanceof IProjectWizardPage) {
				((IProjectWizardPage) page).initProjectWizardPage();
			}
		}
	}

	@Override
	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		getProjectCreator().performFinish(monitor);
	}

	@Override
	public boolean performFinish() {
		updateSteps(null);
		boolean res = super.performFinish();
		if (res) {
			final IScriptProject newElement = getCreatedElement();
			IWorkingSet[] workingSets = ((ProjectWizardFirstPage) getFirstPage())
					.getWorkingSets();
			if (workingSets.length > 0) {
				getWorkbench().getWorkingSetManager().addToWorkingSets(
						newElement, workingSets);
			}
			BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
			selectAndReveal(newElement.getProject());
		}
		return res;
	}

	protected ILocationGroup getFirstPage() {
		final IWizardPage page = getPage(ProjectWizardFirstPage.PAGE_NAME);
		Assert.isNotNull(page);
		return (ILocationGroup) page;
	}

	/*
	 * Stores the configuration element for the wizard. The config element will
	 * be used in <code>performFinish</code> to set the result perspective.
	 */
	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		fConfigElement = cfig;
	}

	@Override
	public boolean performCancel() {
		getProjectCreator().removeProject();
		return super.performCancel();
	}

	@Override
	public IScriptProject getCreatedElement() {
		final IWizardPage page = getPage(ProjectWizardSecondPage.PAGE_NAME);
		Assert.isNotNull(page);
		return ((ProjectWizardSecondPage) page).getScriptProject();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		while (nextPage != null && !isEnabledPage(nextPage)) {
			nextPage = super.getNextPage(nextPage);
		}
		return nextPage;
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		IWizardPage prevPage = super.getPreviousPage(page);
		while (prevPage != null && !isEnabledPage(prevPage)) {
			prevPage = super.getPreviousPage(prevPage);
		}
		return prevPage;
	}

	@Override
	public boolean canFinish() {
		final IWizardPage[] pages = getPages();
		for (int i = 0; i < pages.length; ++i) {
			final IWizardPage page = pages[i];
			if (isEnabledPage(page)) {
				if (!page.isPageComplete()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isEnabledPage(IWizardPage page) {
		return true;
	}

	@Override
	public IEnvironment getEnvironment() {
		return getFirstPage().getEnvironment();
	}

	@Override
	public IInterpreterInstall getInterpreter() {
		return getFirstPage().getInterpreter();
	}

	@Override
	public IProject getProject() {
		return getFirstPage().getProjectHandle();
	}

	@Override
	public void createProject() {
		updateSteps(getContainer().getCurrentPage());
		getProjectCreator().changeToNewProject();
	}

	@Override
	public void removeProject() {
		getProjectCreator().removeProject();
	}

	private ProjectCreator fCreator;

	@Override
	public ProjectCreator getProjectCreator() {
		if (fCreator == null) {
			fCreator = createProjectCreator();
		}
		return fCreator;
	}

	protected ProjectCreator createProjectCreator() {
		return new ProjectCreator(this, getFirstPage());
	}

	protected void updateSteps(final IWizardPage currentPage) {
		for (IWizardPage page : getPages()) {
			if (page == currentPage) {
				break;
			}
			if (page instanceof IProjectWizardPage) {
				((IProjectWizardPage) page).updateProjectWizardPage();
			}
		}
	}

	public abstract String getScriptNature();

	private IProjectWizardState projectWizardState = null;

	/**
	 * @since 2.0
	 */
	public IProjectWizardState getWizardState() {
		if (projectWizardState == null) {
			projectWizardState = new ProjectWizardState(getScriptNature());
			for (IProjectWizardInitializer initializer : new ProjectWizardInitializerManager(
					getScriptNature())) {
				initializer.initialize(projectWizardState);
			}
		}
		return projectWizardState;
	}

	/**
	 * @since 3.0
	 */
	protected void configureNatures(IProject project, IProgressMonitor monitor)
			throws CoreException {
		ResourceUtil.addNature(project, monitor, getScriptNature());
	}

	protected void configureProject(IProject project, IProgressMonitor monitor)
			throws CoreException {
	}

}
