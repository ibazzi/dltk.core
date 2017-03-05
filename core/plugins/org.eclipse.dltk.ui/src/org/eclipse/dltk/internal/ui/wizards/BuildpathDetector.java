/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.core.DLTKContentTypeManager;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.internal.core.BuildpathEntry;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.ui.wizards.IBuildpathDetector;

import com.ibm.icu.text.Collator;

/**
 */
public class BuildpathDetector implements IBuildpathDetector {
	private HashMap<IPath, List<IPath>> fSourceFolders;
	// private List fSourceFiles;
	private HashSet<IPath> fZIPFiles;
	private IProject fProject;
	private IBuildpathEntry[] fResultBuildpath;
	private IProgressMonitor fMonitor;
	private IDLTKLanguageToolkit fToolkit;

	private static class BPSorter implements Comparator<IBuildpathEntry> {
		private Collator fCollator = Collator.getInstance();

		@Override
		public int compare(IBuildpathEntry e1, IBuildpathEntry e2) {
			return fCollator.compare(e1.getPath().toString(), e2.getPath()
					.toString());
		}
	}

	public BuildpathDetector(IProject project, IDLTKLanguageToolkit toolkit) {
		fSourceFolders = new HashMap<IPath, List<IPath>>();
		fZIPFiles = new HashSet<IPath>(10);
		// fSourceFiles = new ArrayList(100);
		fProject = project;
		fResultBuildpath = null;
		fToolkit = toolkit;
	}

	private boolean isNested(IPath path, Iterator<IPath> iter) {
		while (iter.hasNext()) {
			IPath other = iter.next();
			if (other.isPrefixOf(path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method detectBuildpath.
	 *
	 * @param monitor
	 *            The progress monitor (not null)
	 * @throws CoreException
	 */
	@Override
	public void detectBuildpath(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(Messages.BuildpathDetector_detectingBuildpath,
					120);
			fMonitor = monitor;
			final List<IFile> correctFiles = new ArrayList<IFile>();
			fProject.accept(proxy -> BuildpathDetector.this.visit(proxy, correctFiles), IResource.NONE);
			monitor.worked(10);
			SubProgressMonitor sub = new SubProgressMonitor(monitor, 80);
			processSources(correctFiles, sub);
			sub.done();
			ArrayList<IBuildpathEntry> cpEntries = new ArrayList<IBuildpathEntry>();
			detectSourceFolders(cpEntries);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(10);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(10);
			detectLibraries(cpEntries);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(10);
			addInterpreterContainer(cpEntries);
			if (cpEntries.size() == 1) {
				IBuildpathEntry entry = cpEntries.get(0);
				if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
					cpEntries.add(0, DLTKCore.newSourceEntry(fProject
							.getFullPath()));
				}

			}
			if (cpEntries.isEmpty() /* && fSourceFiles.isEmpty() */) {
				return;
			}

			IBuildpathEntry[] entries = cpEntries
					.toArray(new IBuildpathEntry[cpEntries.size()]);
			if (!BuildpathEntry.validateBuildpath(DLTKCore.create(fProject),
					entries).isOK()) {
				return;
			}
			fResultBuildpath = entries;
		} finally {
			monitor.done();
		}
	}

	protected void processSources(List<IFile> correctFiles,
			SubProgressMonitor sub) {
	}

	protected void addInterpreterContainer(ArrayList<IBuildpathEntry> cpEntries) {
		cpEntries.add(DLTKCore.newContainerEntry(new Path(
				ScriptRuntime.INTERPRETER_CONTAINER)));
	}

	private void detectLibraries(ArrayList<IBuildpathEntry> cpEntries) {
		if (this.fToolkit.languageSupportZIPBuildpath()) {
			ArrayList<IBuildpathEntry> res = new ArrayList<IBuildpathEntry>();
			Set<IPath> sourceFolderSet = fSourceFolders.keySet();
			for (Iterator<IPath> iter = fZIPFiles.iterator(); iter.hasNext();) {
				IPath path = iter.next();
				if (isNested(path, sourceFolderSet.iterator())) {
					continue;
				}
				IBuildpathEntry entry = DLTKCore.newLibraryEntry(path);
				res.add(entry);
			}
			Collections.sort(res, new BPSorter());
			cpEntries.addAll(res);
		}
	}

	private void detectSourceFolders(ArrayList<IBuildpathEntry> resEntries) {
		ArrayList<IBuildpathEntry> res = new ArrayList<IBuildpathEntry>();
		Set<IPath> sourceFolderSet = fSourceFolders.keySet();
		for (Iterator<IPath> iter = sourceFolderSet.iterator(); iter.hasNext();) {
			IPath path = iter.next();
			// ArrayList excluded = new ArrayList();
			boolean primary = true;
			for (Iterator<IPath> inner = sourceFolderSet.iterator(); inner
					.hasNext();) {
				IPath other = inner.next();
				if (!path.equals(other) && other.isPrefixOf(path)) {
					primary = false;
					break;
				}
			}
			if (primary) {
				boolean isHidden = false;
				// Hidden file filtering.
				for (int i = 0; i < path.segmentCount(); i++) {
					if (path.segment(i).startsWith(".")) { //$NON-NLS-1$
						isHidden = true;
						break;
					}
				}
				if (!isHidden) {
					IBuildpathEntry entry = DLTKCore.newSourceEntry(path);
					res.add(entry);
				}
			}
		}
		Collections.sort(res, new BPSorter());
		resEntries.addAll(res);
	}

	private void addToMap(HashMap<IPath, List<IPath>> map, IPath folderPath,
			IPath relPath) {
		List<IPath> list = map.get(folderPath);
		if (list == null) {
			list = new ArrayList<IPath>(50);
			map.put(folderPath, list);
		}
		list.add(relPath);
	}

	// private IPath getFolderPath(IPath packPath, IPath relpath) {
	// int remainingSegments = packPath.segmentCount()
	// - relpath.segmentCount();
	// if (remainingSegments >= 0) {
	// IPath common = packPath.removeFirstSegments(remainingSegments);
	// if (common.equals(relpath)) {
	// return packPath.uptoSegment(remainingSegments);
	// }
	// }
	// return null;
	// }

	private boolean hasExtension(String name, String ext) {
		return name.endsWith(ext) && (ext.length() != name.length());
	}

	public boolean visit(IResourceProxy proxy, List<IFile> files) {
		if (fMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		if (proxy.getType() == IResource.FILE) {
			String name = proxy.getName();
			IFile res = (IFile) proxy.requestResource();
			if (visitSourceModule(res)) {
				files.add(res);
			} else if (res.getType() == IResource.FILE
					&& hasExtension(name, ".zip")) { //$NON-NLS-1$
				fZIPFiles.add(proxy.requestFullPath());
			}
			return false;
		}
		return true;
	}

	protected boolean visitSourceModule(IFile file) {
		if (DLTKContentTypeManager
				.isValidResourceForContentType(fToolkit, file)) {
			IPath packPath = file.getParent().getFullPath();
			String cuName = file.getName();
			addToMap(fSourceFolders, packPath, new Path(cuName));
			return true;
		}
		return false;
	}

	@Override
	public IBuildpathEntry[] getBuildpath() {
		return fResultBuildpath;
	}
}
