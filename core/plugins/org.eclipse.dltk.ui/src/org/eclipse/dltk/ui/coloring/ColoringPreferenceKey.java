/*******************************************************************************
 * Copyright (c) 2010, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.ui.coloring;

import org.eclipse.dltk.ui.PreferenceConstants;

/**
 * @since 3.0
 */
public class ColoringPreferenceKey implements IColoringPreferenceKey {

	public static IColoringPreferenceKey create(String baseKey) {
		return new ColoringPreferenceKey(baseKey);
	}

	private final String baseKey;

	private ColoringPreferenceKey(String baseKey) {
		this.baseKey = baseKey;
	}

	@Override
	public String getColorKey() {
		return baseKey;
	}

	@Override
	public String getBoldKey() {
		return baseKey + PreferenceConstants.EDITOR_BOLD_SUFFIX;
	}

	@Override
	public String getItalicKey() {
		return baseKey + PreferenceConstants.EDITOR_ITALIC_SUFFIX;
	}

	@Override
	public String getStrikethroughKey() {
		return baseKey + PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
	}

	@Override
	public String getUnderlineKey() {
		return baseKey + PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
	}

	@Override
	public String getEnableKey() {
		return baseKey
				+ PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
	}

	@Override
	public String toString() {
		return baseKey;
	}

}
