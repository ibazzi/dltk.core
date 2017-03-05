package org.eclipse.dltk.debug.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.dltk.core.DLTKContributionExtensionManager;
import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
import org.eclipse.dltk.ui.preferences.ContributedExtensionOptionsBlock;
import org.eclipse.dltk.ui.preferences.PreferenceKey;
import org.eclipse.dltk.ui.util.IStatusChangeListener;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public abstract class AbstractDebuggingEngineOptionsBlock
		extends ContributedExtensionOptionsBlock {

	public AbstractDebuggingEngineOptionsBlock(IStatusChangeListener context,
			IProject project, PreferenceKey[] allKeys,
			IWorkbenchPreferenceContainer container) {
		super(context, project, allKeys, container);
	}

	@Override
	protected DLTKContributionExtensionManager getExtensionManager() {
		return DebuggingEngineManager.getInstance();
	}

	@Override
	protected String getSelectorGroupLabel() {
		return ScriptDebugPreferencesMessages.DebuggingEngine;
	}

	@Override
	protected String getSelectorNameLabel() {
		return ScriptDebugPreferencesMessages.NameLabel;
	}

	@Override
	protected String getPreferenceLinkMessage() {
		return ScriptDebugPreferencesMessages.LinkToDebuggingEnginePreferences;
	}

}
