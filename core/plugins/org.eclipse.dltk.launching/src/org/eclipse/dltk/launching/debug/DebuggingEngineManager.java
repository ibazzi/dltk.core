package org.eclipse.dltk.launching.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.dltk.core.DLTKContributionExtensionManager;
import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.internal.launching.debug.DebuggingEngine;
import org.eclipse.dltk.launching.IInterpreterRunnerFactory;

public class DebuggingEngineManager extends DLTKContributionExtensionManager {
	static final String DEBUGGING_ENGINE_EXT_POINT = DLTKLaunchingPlugin.PLUGIN_ID
			+ ".debuggingEngine"; //$NON-NLS-1$

	private static final String ENGINE_CONTRIBUTION = "engineContribution"; //$NON-NLS-1$
	private static final String ENGINE_TAG = "engine"; //$NON-NLS-1$

	private static DebuggingEngineManager instance;

	public static synchronized DebuggingEngineManager getInstance() {
		if (instance == null) {
			instance = new DebuggingEngineManager();
		}

		return instance;
	}

	public IDebuggingEngine getDebuggingEngine(String id) {
		return (IDebuggingEngine) getContributionById(id);
	}

	/**
	 * Returns selected debugging engine for script language with natureId. Uses
	 * default debugging engine selector (priority based) if custom selector is
	 * not contributed.
	 * 
	 * @param natureId
	 * 
	 * @return Selected debugging engine or null (if there are no debugging
	 *         engines at all or there are no selected engines)
	 */
	public IDebuggingEngine getSelectedDebuggingEngine(IProject project,
			String natureId) {
		return (IDebuggingEngine) getSelectedContribution(project, natureId);
	}

	/**
	 * Returns if script language with nature natureId has selected debugging
	 * engine. If this method returns false then getSelectedDebuggingEngine
	 * returns null.
	 * 
	 * @param natureId
	 *            nature id
	 * 
	 * @return true if the nature has a selected debugging engine, false
	 *         otherwise
	 */
	public boolean hasSelectedDebuggingEngine(IProject project,
			String natureId) {
		return getSelectedDebuggingEngine(project, natureId) != null;
	}

	@Override
	protected boolean isNatureContribution(IConfigurationElement main) {
		return ENGINE_CONTRIBUTION.equals(main.getName());
	}

	/*
	 * @see org.eclipse.dltk.core.DLTKContributionExtensionManager#
	 * configureContribution (java.lang.Object,
	 * org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected Object configureContribution(Object object,
			IConfigurationElement config) {
		return new DebuggingEngine((IInterpreterRunnerFactory) object, config);
	}

	/*
	 * @seeorg.eclipse.dltk.core.DLTKContributionExtensionManager#
	 * getContributionElementName()
	 */
	@Override
	protected String getContributionElementName() {
		return ENGINE_TAG;
	}

	/*
	 * @see
	 * org.eclipse.dltk.core.DLTKContributionExtensionManager#getExtensionPoint
	 * ()
	 */
	@Override
	protected String getExtensionPoint() {
		return DEBUGGING_ENGINE_EXT_POINT;
	}

	/*
	 * @see org.eclipse.dltk.core.DLTKContributionExtensionManager#
	 * isValidContribution (java.lang.Object)
	 */
	@Override
	protected boolean isValidContribution(Object object) {
		return (object instanceof IInterpreterRunnerFactory);
	}
}
