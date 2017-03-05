/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.callhierarchy;

import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.jface.action.Action;


class RefreshAction extends Action {
    private CallHierarchyViewPart fPart;

    public RefreshAction(CallHierarchyViewPart part) {
		fPart= part;
		setText(CallHierarchyMessages.RefreshAction_text);
		setToolTipText(CallHierarchyMessages.RefreshAction_tooltip);
		DLTKPluginImages.setLocalImageDescriptors(this, "refresh_nav.png");//$NON-NLS-1$
		setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_REFRESH_ACTION);
		if (DLTKCore.DEBUG) {
			System.err.println("Add help support here..."); //$NON-NLS-1$
		}
    }

    @Override
	public void run() {
        fPart.refresh();
    }
}
