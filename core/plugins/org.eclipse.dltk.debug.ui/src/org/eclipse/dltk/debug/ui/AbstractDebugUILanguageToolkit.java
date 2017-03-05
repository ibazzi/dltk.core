package org.eclipse.dltk.debug.ui;

public abstract class AbstractDebugUILanguageToolkit
		implements IDLTKDebugUILanguageToolkit {

	@Override
	public String[] getVariablesViewPreferencePages() {
		return new String[] { "" }; //$NON-NLS-1$
	}
}
