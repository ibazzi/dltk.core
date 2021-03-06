/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.scriptview;

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

class ScriptFrameSource extends TreeViewerFrameSource {
	private ScriptExplorerPart fPackagesExplorer;

	ScriptFrameSource(ScriptExplorerPart explorer) {
		super(explorer.getTreeViewer());
		fPackagesExplorer= explorer;
	}

	@Override
	protected TreeFrame createFrame(Object input) {
		TreeFrame frame = super.createFrame(input);
		frame.setName(fPackagesExplorer.getFrameName(input));
		frame.setToolTipText(fPackagesExplorer.getToolTipText(input));
		return frame;
	}

	/**
	 * Also updates the title of the packages explorer
	 */
	@Override
	protected void frameChanged(TreeFrame frame) {
		super.frameChanged(frame);
		fPackagesExplorer.updateTitle();
	}

}
