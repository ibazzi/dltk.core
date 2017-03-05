package org.eclipse.dltk.debug.ui.handlers;

import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;

/**
 * Abstract handler implementation that can be used to toggle the display of
 * 'class' debugging variables.
 */
public abstract class AbstractToggleClassVariableHandler
		extends AbstractToggleVariableHandler {

	@Override
	protected String getVariableDisplayPreferenceKey() {
		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS;
	}

	@Override
	protected final void toggleVariableDisplay(IScriptDebugTarget target,
			boolean enabled) {
		target.toggleClassVariables(enabled);
	}
}
