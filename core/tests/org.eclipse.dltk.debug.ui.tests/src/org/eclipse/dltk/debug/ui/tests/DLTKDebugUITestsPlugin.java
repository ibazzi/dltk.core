package org.eclipse.dltk.debug.ui.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DLTKDebugUITestsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.dltk.debug.ui.tests";

	// The shared instance
	private static DLTKDebugUITestsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public DLTKDebugUITestsPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DLTKDebugUITestsPlugin getDefault() {
		return plugin;
	}

}
