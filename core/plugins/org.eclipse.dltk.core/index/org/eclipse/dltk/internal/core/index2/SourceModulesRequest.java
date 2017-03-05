/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.dltk.internal.core.index2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexer;
import org.eclipse.dltk.core.index2.ProjectIndexer2;

/**
 * Request for re-indexing a set of source modules. First, source modules are
 * analyzed to determine which of them must be re-indexed, and what dead source
 * modules must be removed from index.
 * 
 * @author michael
 * 
 */
public class SourceModulesRequest extends AbstractIndexRequest {

	private final IPath containerPath;
	private final Set<ISourceModule> sourceModules;

	public SourceModulesRequest(ProjectIndexer2 indexer, IPath containerPath,
			Set<ISourceModule> sourceModules, ProgressJob progressJob) {
		super(indexer, progressJob);
		this.containerPath = containerPath;
		this.sourceModules = sourceModules;
	}

	@Override
	protected String getName() {
		return containerPath.toString();
	}

	@Override
	protected void run() throws CoreException, IOException {

		IIndexer indexer = IndexerManager.getIndexer();
		if (indexer == null || isCancelled) {
			return;
		}

		Set<String> toRemove = new HashSet<String>();
		List<ISourceModule> toReindex = new ArrayList<ISourceModule>();

		analyzeSourceModuleChanges(containerPath, sourceModules, toRemove,
				toReindex);

		for (final String path : toRemove) {
			if (isCancelled)
				return;
			indexer.removeDocument(containerPath, path);
		}

		Collections.sort(toReindex, (m1, m2) -> m1.getPath().toString()
				.compareTo(m2.getPath().toString()));

		for (final ISourceModule sourceModule : toReindex) {
			if (isCancelled)
				return;
			reportToProgress(sourceModule);
			indexer.indexDocument(sourceModule);
		}
	}

	@Override
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(containerPath.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((containerPath == null) ? 0 : containerPath.hashCode());
		result = prime * result
				+ ((sourceModules == null) ? 0 : sourceModules.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceModulesRequest other = (SourceModulesRequest) obj;
		if (containerPath == null) {
			if (other.containerPath != null)
				return false;
		} else if (!containerPath.equals(other.containerPath))
			return false;
		if (sourceModules == null) {
			if (other.sourceModules != null)
				return false;
		} else if (!sourceModules.equals(other.sourceModules))
			return false;
		return true;
	}
}
