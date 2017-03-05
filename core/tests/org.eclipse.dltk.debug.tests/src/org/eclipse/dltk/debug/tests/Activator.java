/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.tests;

import org.eclipse.core.runtime.Plugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.dltk.debug.tests";

	private static Activator plugin;

	public Activator() {
		plugin = this;
	}

	public static Activator getDefault() {
		return plugin;
	}
}
