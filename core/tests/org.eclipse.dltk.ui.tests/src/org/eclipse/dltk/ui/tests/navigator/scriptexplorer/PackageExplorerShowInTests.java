/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.tests.navigator.scriptexplorer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.ui.scriptview.ScriptExplorerPart;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.tests.ScriptProjectHelper;
import org.eclipse.dltk.ui.tests.core.ProjectTestSetup;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class PackageExplorerShowInTests extends TestCase {

	public PackageExplorerShowInTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return setUpTest(new TestSuite(PackageExplorerShowInTests.class));
	}
	
	private static Test setUpTest(Test someTest) {
		return new ProjectTestSetup(someTest);
	}
	
	private IScriptProject fJProject;
	private ScriptExplorerPart fPackageExplorer;
	private IWorkbenchPage fPage;
	
	@Override
	protected void setUp() throws Exception {
		fJProject= ProjectTestSetup.getProject();
		fPage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		fPackageExplorer= (ScriptExplorerPart) fPage.showView(DLTKUIPlugin.ID_SCRIPT_EXPLORER);
		fPackageExplorer.selectAndReveal(new StructuredSelection());
	}
	
	@Override
	protected void tearDown() throws Exception {
		//ScriptProjectHelper.clear(fJProject, ProjectTestSetup.getDefaultBuildpath());
		fPage.hideView(fPackageExplorer);
		fPage= null;
		fPackageExplorer= null;
		fJProject= null;
	}
	
////////////TODO: should also test non-UI part of tryToReveal(..) ///////////////////////////////
	
	
	public void testCU() throws Exception {
		IProjectFragment sourceFolder= ScriptProjectHelper.addSourceContainer(fJProject, "src");
		IScriptFolder pack= sourceFolder.createScriptFolder("p", true, null);
		ISourceModule cu= pack.createSourceModule("A.txt", "package p;\nclass A {\n\n}", true, null);
		IStructuredSelection selection= (IStructuredSelection) fPackageExplorer.convertSelection(new StructuredSelection(cu));

		assertEquals(1, selection.size());
		assertEquals(cu, selection.getFirstElement());
	}
	
	public void testCUAdaptedCU() throws Exception {
		IProjectFragment sourceFolder= ScriptProjectHelper.addSourceContainer(fJProject, "src");
		IScriptFolder pack= sourceFolder.createScriptFolder("p", true, null);
		final ISourceModule cu= pack.createSourceModule("A.txt", "package p;\nclass A {\n\n}", true, null);
		
		IAdaptable adaptable= new IAdaptable() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == IModelElement.class)
					return (T) cu;
				else
					return null;
			}
		};
		IStructuredSelection selection= (IStructuredSelection) fPackageExplorer.convertSelection(new StructuredSelection(adaptable));
		
		assertEquals(1, selection.size());
		assertEquals(cu, selection.getFirstElement());
	}
	
	
	public void testCUAdaptedResource() throws Exception {
		IProjectFragment sourceFolder= ScriptProjectHelper.addSourceContainer(fJProject, "src");
		IScriptFolder pack= sourceFolder.createScriptFolder("p", true, null);
		final ISourceModule cu= pack.createSourceModule("A.txt", "package p;\nclass A {\n\n}", true, null);
		
		IAdaptable adaptable= new IAdaptable() {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == IResource.class)
					return (T) cu.getResource();
				else
					return null;
			}
		};
		IStructuredSelection selection= (IStructuredSelection) fPackageExplorer.convertSelection(new StructuredSelection(adaptable));
		
		assertEquals(1, selection.size());
		assertEquals(cu, selection.getFirstElement());
	}
	
	
	
}
