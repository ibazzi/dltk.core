/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.tests.navigator.scriptexplorer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IScriptProjectFilenames;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.ui.scriptview.ScriptExplorerPart;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.tests.ScriptProjectHelper;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the PackageExplorerContentProvider. Bugs:
 * <ul>
 * <li>66694 PackageExplorer shows elements twice</li>
 * <li>35851 Content of folders with illegal package name are no shown in
 * package explorer</li>
 * <li>35851 Content of folders with illegal package name are no shown in
 * package explorer</li>
 * <li>35851 Content of folders with illegal package name are no shown in
 * package explorer</li>
 * </ul>
 */
public class ContentProviderTests5 {
	private boolean fEnableAutoBuildAfterTesting;
	private ITreeContentProvider fProvider;

	private IScriptProject fJProject;
	private IFile fDotBuildpath;
	private IFile fDotProject;

	@Before
	public void setUp() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertNotNull(workspace);
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		fEnableAutoBuildAfterTesting = workspaceDesc.isAutoBuilding();
		if (fEnableAutoBuildAfterTesting)
			ScriptProjectHelper.setAutoBuilding(false);

		// create project
		fJProject = ScriptProjectHelper
				.createScriptProject("ContentProviderProject");
		assertNotNull(fJProject);

		Object[] resource = fJProject.getForeignResources();
		for (int i = 0; i < resource.length; i++) {
			Object object = resource[i];
			if (object instanceof IFile) {
				IFile file = (IFile) object;
				if (IScriptProjectFilenames.BUILDPATH_FILENAME.equals(file
						.getName()))
					fDotBuildpath = file;
				else if (IScriptProjectFilenames.PROJECT_FILENAME.equals(file
						.getName()))
					fDotProject = file;
			}
		}
		assertNotNull(fDotBuildpath);
		assertNotNull(fDotProject);

		setUpView();
	}

	private void setUpView() throws PartInitException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		assertNotNull(workbench);

		IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();
		assertNotNull(page);

		IViewPart myPart = page.showView(DLTKUIPlugin.ID_SCRIPTEXPLORER);
		assertTrue("Unable to get view", myPart instanceof ScriptExplorerPart);
		ScriptExplorerPart part = (ScriptExplorerPart) myPart;
		if (!part.isFlatLayout()) {
			part.setFlatLayout(true);
		}
		fProvider = (ITreeContentProvider) part.getTreeViewer()
				.getContentProvider();
		setFolding(false);
		assertNotNull(fProvider);
	}

	private void setFolding(boolean fold) {
		DLTKUIPlugin
				.getDefault()
				.getPreferenceStore()
				.setValue(
						PreferenceConstants.APPEARANCE_FOLD_PACKAGES_IN_PACKAGE_EXPLORER,
						fold);
	}

	@After
	public void tearDown() throws Exception {
		ScriptProjectHelper.delete(fJProject);

		if (fEnableAutoBuildAfterTesting)
			ScriptProjectHelper.setAutoBuilding(true);
	}

	private ByteArrayInputStream asInputStream(String string)
			throws UnsupportedEncodingException {
		return new ByteArrayInputStream(string.getBytes(ResourcesPlugin
				.getEncoding()));
	}

	public void REM_testProjectSource1() throws Exception { // bug 35851, 66694
		IPath[] inclusionFilters = { new Path("**"), new Path("excl/incl/") };
		IPath[] exclusionFilters = { new Path("excl/*"), new Path("x/*.txt"),
				new Path("y/") };
		IProjectFragment root = ScriptProjectHelper.addSourceContainer(
				fJProject, "", inclusionFilters, exclusionFilters);

		IScriptFolder defaultPackage = root.createScriptFolder("", true, null);

		IFolder ab = fJProject.getProject().getFolder("a-b");
		CoreUtility.createFolder(ab, true, true, null);
		IFile description = ab.getFile("description.txt");
		description.create(asInputStream("description"), true, null);

		IScriptFolder exclInclPackage = root.createScriptFolder("excl/incl",
				true, null);
		ISourceModule In = exclInclPackage.createSourceModule("In.txt",
				"package excl.incl;\r\n" + "public class In {\r\n" + "}\r\n",
				true, null);

		IFolder excl = fJProject.getProject().getFolder("excl");
		IFile Ex = excl.getFile("Ex.txt");
		Ex.create(asInputStream("package excl;\npublic class Ex{}"), false,
				null);

		IScriptFolder xPackage = root.createScriptFolder("x", true, null);
		IFolder x = fJProject.getProject().getFolder("x");
		IFile xhidden = x.getFile(".hidden");
		xhidden.create(asInputStream(""), true, null);
		IFile X = x.getFile("X.txt");
		X.create(
				asInputStream("package x;\r\npublic class X {\r\n\t\r\n}\r\n"),
				true, null);

		x.copy(new Path("y"), true, null);
		IFolder y = fJProject.getProject().getFolder("y");
		IFile yX = y.getFile("X.txt");
		IFile yhidden = y.getFile(".hidden");

		IScriptFolder zPackage = root.createScriptFolder("z", true, null);
		ISourceModule Z = zPackage.createSourceModule("Z.txt",
				"package z;public class Z{}", true, null);

		assertArrayEquals(new Object[] { defaultPackage, exclInclPackage,
				xPackage, zPackage, ab, excl, y, fDotBuildpath, fDotProject },
				fProvider.getChildren(fJProject));
		assertArrayEquals(new Object[0], fProvider
				.getChildren(defaultPackage));
		assertArrayEquals(new Object[] { In }, fProvider
				.getChildren(exclInclPackage));
		assertArrayEquals(new Object[] { Ex }, fProvider.getChildren(excl));
		assertArrayEquals(new Object[] { X, xhidden }, fProvider
				.getChildren(xPackage));
		assertEquals(xPackage, fProvider.getParent(X));
		assertEquals(xPackage, fProvider.getParent(xhidden));
		assertArrayEquals(new Object[] { Z }, fProvider.getChildren(zPackage));
		assertArrayEquals(new Object[] { description }, fProvider
				.getChildren(ab));
		assertArrayEquals(new Object[] { Ex }, fProvider.getChildren(excl));
		assertArrayEquals(new Object[] { yX, yhidden }, fProvider
				.getChildren(y));
	}

	public void REM_testNestedSource1() throws Exception { // bug 35851, 66694
		// <BuildpathEntry excluding="a-b/a/b/" kind="src" path="src"/>
		// <BuildpathEntry kind="src" path="src/a-b/a/b"/>
		IPath[] inclusionFilters = {};
		IPath[] exclusionFilters = { new Path("a-b/a/b/") };
		IProjectFragment src = ScriptProjectHelper.addSourceContainer(
				fJProject, "src", inclusionFilters, exclusionFilters);
		IProjectFragment srcabab = ScriptProjectHelper.addSourceContainer(
				fJProject, "src/a-b/a/b", new IPath[0], new IPath[0]);

		IScriptFolder defaultSrc = src.createScriptFolder("", true, null);
		IScriptFolder p = src.createScriptFolder("p", true, null);
		IFile file = ((IFolder) p.getCorrespondingResource())
				.getFile("file.txt");
		file.create(asInputStream("f"), true, null);

		IFolder ab = ((IFolder) src.getUnderlyingResource()).getFolder("a-b");
		CoreUtility.createFolder(ab, true, true, null);
		IFolder aba = ab.getFolder("a");
		CoreUtility.createFolder(aba, true, true, null);
		IFile abaTxt = aba.getFile("aba.txt");
		abaTxt.create(asInputStream("x"), true, null);

		IScriptFolder defaultAbab = srcabab.createScriptFolder("", true, null);
		ISourceModule b = defaultAbab.createSourceModule("B.txt",
				"public class B {}", true, null);

		assertArrayEquals(new Object[] { src, srcabab, fDotBuildpath,
				fDotProject }, fProvider.getChildren(fJProject));
		assertArrayEquals(new Object[] { defaultSrc, p, ab }, fProvider
				.getChildren(src));
		assertArrayEquals(new Object[] {}, fProvider.getChildren(defaultSrc));
		assertArrayEquals(new Object[] { file }, fProvider.getChildren(p));
		assertArrayEquals(new Object[] { aba }, fProvider.getChildren(ab));
		assertArrayEquals(new Object[] { abaTxt }, fProvider.getChildren(aba));

		assertArrayEquals(new Object[] { defaultAbab }, fProvider
				.getChildren(srcabab));
		assertArrayEquals(new Object[] { b }, fProvider
				.getChildren(defaultAbab));
	}
	@Test
	public void testInclExcl1() throws Exception { // bug 35851, 66694
		// <BuildpathEntry including="a/b/c/" excluding="a/b/c/d/" kind="src"
		// path="src2"/>
		IPath[] inclusionFilters = { new Path("a/b/c/") };
		IPath[] exclusionFilters = { new Path("a/b/c/d/") };
		IProjectFragment src = ScriptProjectHelper.addSourceContainer(
				fJProject, "src", inclusionFilters, exclusionFilters);

		IScriptFolder abc = src.createScriptFolder("a/b/c", true, null);
		ISourceModule x = abc.createSourceModule("X.txt", "", true, null);
		IFolder d = ((IFolder) abc.getUnderlyingResource()).getFolder("d");
		CoreUtility.createFolder(d, false, true, null);
		IFile dTxt = d.getFile("d.txt");
		dTxt.create(asInputStream(""), true, null);

		IContainer b = d.getParent().getParent();
		IContainer a = b.getParent();

		assertArrayEquals(new Object[] { src, fDotBuildpath, fDotProject },
				fProvider.getChildren(fJProject));
		assertArrayEquals(new Object[] { abc, a }, fProvider.getChildren(src));
		assertArrayEquals(new Object[] { x, d }, fProvider.getChildren(abc));
		assertArrayEquals(new Object[] { dTxt }, fProvider.getChildren(d));
		assertArrayEquals(new Object[] { b }, fProvider.getChildren(a));
	}
}
