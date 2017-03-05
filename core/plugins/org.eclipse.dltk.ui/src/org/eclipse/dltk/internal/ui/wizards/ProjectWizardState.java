/*******************************************************************************
 * Copyright (c) 2009, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.ui.wizards.IProjectWizardInitializer.IProjectWizardState;

public class ProjectWizardState implements IProjectWizardState {

	private final String nature;
	private IEnvironment environment;
	private String externalLocation;
	private String projectName;
	private String mode;
	private final Map<String, String> tooltips = new HashMap<String, String>();
	private final Map<String, String> attributes = new HashMap<String, String>();

	public ProjectWizardState(String nature) {
		this.nature = nature;
	}

	@Override
	public String getScriptNature() {
		return nature;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public void setProjectName(String name) {
		this.projectName = name;
	}

	@Override
	public String getMode() {
		return mode;
	}

	@Override
	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	public String getToolTipText(String mode) {
		return tooltips.get(mode);
	}

	@Override
	public void setToolTipText(String mode, String tooltip) {
		tooltips.put(mode, tooltip);
	}

	@Override
	public void setEnvironment(IEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}

	@Override
	public String getExternalLocation() {
		return externalLocation;
	}

	@Override
	public void setExternalLocation(String externalLocation) {
		this.externalLocation = externalLocation;
	}

	@Override
	public String getString(String key) {
		return attributes.get(key);
	}

	@Override
	public void setString(String key, String value) {
		attributes.put(key, value);
	}

}
