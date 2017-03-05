package org.eclipse.dltk.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.dltk.ast.parser.SourceParserManager;
import org.eclipse.dltk.core.DLTKContributionExtensionManager;
import org.eclipse.dltk.ui.util.IStatusChangeListener;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public abstract class AbstractSourceParserOptionsBlock extends
		ContributedExtensionOptionsBlock {

	public AbstractSourceParserOptionsBlock(IStatusChangeListener context,
			IProject project, PreferenceKey[] allKeys,
			IWorkbenchPreferenceContainer container) {
		super(context, project, allKeys, container);
	}

	@Override
	protected DLTKContributionExtensionManager getExtensionManager() {
		return SourceParserManager.getInstance();
	}

	@Override
	protected String getSelectorGroupLabel() {
		return PreferencesMessages.SourceParsers_groupLabel;
	}

	@Override
	protected String getSelectorNameLabel() {
		return PreferencesMessages.SourceParsers_nameLabel;
	}

	@Override
	protected String getPreferenceLinkMessage() {
		return PreferencesMessages.SourceParsers_LinkToPreferences;
	}

}
