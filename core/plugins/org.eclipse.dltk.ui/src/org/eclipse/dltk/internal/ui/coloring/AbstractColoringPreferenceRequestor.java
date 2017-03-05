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
package org.eclipse.dltk.internal.ui.coloring;

import org.eclipse.dltk.ui.coloring.ColoringPreferenceKey;
import org.eclipse.dltk.ui.coloring.EnablementStyle;
import org.eclipse.dltk.ui.coloring.FontStyle;
import org.eclipse.dltk.ui.coloring.IColoringPreferenceKey;
import org.eclipse.dltk.ui.coloring.IColoringPreferenceRequestor;
import org.eclipse.swt.graphics.RGB;

public abstract class AbstractColoringPreferenceRequestor implements
		IColoringPreferenceRequestor {

	@Override
	public void enterCategory(String category) {
	}

	@Override
	public void addPreference(String baseKey, String name, RGB color,
			FontStyle... fontStyles) {
		addPreference(baseKey, name, color, EnablementStyle.ALWAYS_ON,
				fontStyles);
	}

	@Override
	public void addPreference(String baseKey, String name, RGB color,
			EnablementStyle enablementStyle, FontStyle... fontStyles) {
		addPreference(ColoringPreferenceKey.create(baseKey), name, color,
				enablementStyle, fontStyles);
	}

	@Override
	public void addPreference(IColoringPreferenceKey key, String name,
			RGB color, FontStyle... fontStyles) {
		addPreference(key, name, color, EnablementStyle.ALWAYS_ON, fontStyles);
	}

}
