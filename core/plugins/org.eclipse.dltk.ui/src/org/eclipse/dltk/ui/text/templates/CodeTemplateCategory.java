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
package org.eclipse.dltk.ui.text.templates;

import org.eclipse.jface.text.templates.TemplateContextType;

public class CodeTemplateCategory implements ICodeTemplateCategory {

	private final String name;
	private final boolean group;
	private TemplateContextType[] contextTypes;

	public CodeTemplateCategory(String name, boolean group,
			TemplateContextType[] contextTypes) {
		this.name = name;
		this.group = group;
		this.contextTypes = contextTypes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isGroup() {
		return group;
	}

	/**
	 * Returns {@link TemplateContextType}s for this group.
	 *
	 * @return
	 */
	@Override
	public TemplateContextType[] getTemplateContextTypes() {
		return contextTypes;
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
