package org.eclipse.dltk.debug.ui.tests.core;

import org.eclipse.dltk.debug.ui.AbstractDebugUILanguageToolkit;
import org.eclipse.dltk.debug.ui.tests.DLTKDebugUITestsPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

public class DLTKDebugUITestLanguageToolkit extends
		AbstractDebugUILanguageToolkit {

	@Override
	public String getDebugModelId() {
		return null;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return DLTKDebugUITestsPlugin.getDefault().getPreferenceStore();
	}

}
