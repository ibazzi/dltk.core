/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.viewsupport;

import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

public class AppearanceAwareLabelProvider extends ScriptUILabelProvider
		implements IPropertyChangeListener {
	public final static long DEFAULT_TEXTFLAGS = ScriptElementLabels.ROOT_VARIABLE
			| ScriptElementLabels.M_PARAMETER_NAMES
			| ScriptElementLabels.M_PARAMETER_TYPES
			| ScriptElementLabels.M_APP_TYPE_PARAMETERS
			| ScriptElementLabels.M_APP_RETURNTYPE
			| ScriptElementLabels.M_PARAMETER_INITIALIZERS;
	// |ScriptElementLabels.REFERENCED_ROOT_POST_QUALIFIED;
	public final static int DEFAULT_IMAGEFLAGS = ScriptElementImageProvider.OVERLAY_ICONS;

	private long fTextFlagMask;

	private int fImageFlagMask;

	private final IPreferenceStore fStore;

	/**
	 * Constructor for AppearanceAwareLabelProvider.
	 *
	 * @param preferenceStore
	 */
	public AppearanceAwareLabelProvider(long textFlags, int imageFlags,
			IPreferenceStore store) {
		super(textFlags, imageFlags);
		fStore = store != null ? store : DLTKUIPlugin.getDefault()
				.getPreferenceStore();
		initMasks();
		fStore.addPropertyChangeListener(this);
	}

	/**
	 * Creates a labelProvider with DEFAULT_TEXTFLAGS and DEFAULT_IMAGEFLAGS
	 */
	public AppearanceAwareLabelProvider(IPreferenceStore store) {
		this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS, store);
	}

	private void initMasks() {
		fTextFlagMask = -1;
		if (!fStore
				.getBoolean(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)) {
			fTextFlagMask ^= ScriptElementLabels.M_APP_RETURNTYPE;
		}
		if (!fStore
				.getBoolean(PreferenceConstants.APPEARANCE_METHOD_PARAMETER_NAMES)) {
			fTextFlagMask ^= ScriptElementLabels.M_PARAMETER_NAMES;
		}
		if (!fStore
				.getBoolean(PreferenceConstants.APPEARANCE_METHOD_PARAMETER_TYPES)) {
			fTextFlagMask ^= ScriptElementLabels.M_PARAMETER_TYPES;
		}
		if (!fStore
				.getBoolean(PreferenceConstants.APPEARANCE_METHOD_TYPEPARAMETERS)) {
			fTextFlagMask ^= ScriptElementLabels.M_APP_TYPE_PARAMETERS;
		}
		if (!fStore
				.getBoolean(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
			fTextFlagMask ^= ScriptElementLabels.P_COMPRESSED;
		}
		fImageFlagMask = -1;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)
				|| property
						.equals(PreferenceConstants.APPEARANCE_METHOD_PARAMETER_NAMES)
				|| property
						.equals(PreferenceConstants.APPEARANCE_METHOD_PARAMETER_TYPES)
				|| property
						.equals(PreferenceConstants.APPEARANCE_METHOD_TYPEPARAMETERS)
				|| property
						.equals(PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW)
				|| property
						.equals(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
			initMasks();
			LabelProviderChangedEvent lpEvent = new LabelProviderChangedEvent(
					this, null); // refresh
			// all
			fireLabelProviderChanged(lpEvent);
		}
	}

	@Override
	public void dispose() {
		fStore.removePropertyChangeListener(this);
		super.dispose();
	}

	@Override
	protected int evaluateImageFlags(Object element) {
		return getImageFlags() & fImageFlagMask;
	}

	@Override
	protected long evaluateTextFlags(Object element) {
		return getTextFlags() & fTextFlagMask;
	}
}
