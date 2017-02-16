/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.navigator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.dltk.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

/**
 * Provides the labels for the Package Explorer.
 * <p>
 * It provides labels for the packages in hierarchical layout and in all other
 * cases delegates it to its super class.
 * </p>
 */
public class ScriptExplorerLabelProvider extends AppearanceAwareLabelProvider {

	protected ScriptExplorerContentProvider fContentProvider;
	private Map<ImageDescriptor, Image> fWorkingSetImages;

	private boolean fIsFlatLayout;
	private ScriptExplorerProblemsDecorator fProblemDecorator;

	public ScriptExplorerLabelProvider(ScriptExplorerContentProvider cp,
			IPreferenceStore store) {
		super(DEFAULT_TEXTFLAGS | ScriptElementLabels.P_COMPRESSED
				| ScriptElementLabels.REFERENCED_ARCHIVE_POST_QUALIFIED
				| ScriptElementLabels.REFERENCED_EXTERNAL_POST_QUALIFIED
				| ScriptElementLabels.ALL_CATEGORY,
				DEFAULT_IMAGEFLAGS | ScriptElementImageProvider.SMALL_ICONS,
				store);

		fProblemDecorator = new ScriptExplorerProblemsDecorator();
		addLabelDecorator(fProblemDecorator);
		Assert.isNotNull(cp);
		fContentProvider = cp;
		fWorkingSetImages = null;
	}

	private String getSpecificText(Object element) {
		if (!fIsFlatLayout && element instanceof IScriptFolder) {
			IScriptFolder fragment = (IScriptFolder) element;
			Object parent = fContentProvider
					.getHierarchicalPackageParent(fragment);
			if (parent instanceof IScriptFolder) {
				return getNameDelta((IScriptFolder) parent, fragment);
			} else if (parent instanceof IFolder) { // bug 152735
				return getNameDelta((IFolder) parent, fragment);
			}
		} else if (element instanceof IWorkingSet) {
			return ((IWorkingSet) element).getLabel();
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		String text = getSpecificText(element);
		if (text != null) {
			return decorateText(text, element);
		}
		return super.getText(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		String text = getSpecificText(element);
		if (text != null) {
			String decorated = decorateText(text, element);
			if (decorated != null) {
				return StyledCellLabelProvider.styleDecoratedString(decorated,
						StyledString.DECORATIONS_STYLER,
						new StyledString(text));
			}
		}
		return super.getStyledText(element);
	}

	private String getNameDelta(IScriptFolder parent, IScriptFolder fragment) {
		String prefix = parent.getElementName()
				+ IScriptFolder.PACKAGE_DELIMITER;
		String fullName = fragment.getElementName();
		if (fullName.startsWith(prefix)) {
			return fullName.substring(prefix.length());
		}
		return fullName;
	}

	private String getNameDelta(IFolder parent, IScriptFolder fragment) {
		IPath prefix = parent.getFullPath();
		IPath fullPath = fragment.getPath();
		if (prefix.isPrefixOf(fullPath)) {
			StringBuffer buf = new StringBuffer();
			for (int i = prefix.segmentCount(); i < fullPath
					.segmentCount(); i++) {
				if (buf.length() > 0)
					buf.append(IScriptFolder.PACKAGE_DELIMITER);
				buf.append(fullPath.segment(i));
			}
			return buf.toString();
		}
		return fragment.getElementName();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IWorkingSet) {
			ImageDescriptor image = ((IWorkingSet) element)
					.getImageDescriptor();
			if (fWorkingSetImages == null) {
				fWorkingSetImages = new HashMap<>();
			}

			Image result = fWorkingSetImages.get(image);
			if (result == null) {
				result = image.createImage();
				fWorkingSetImages.put(image, result);
			}
			return decorateImage(result, element);
		}
		return super.getImage(element);
	}

	public void setIsFlatLayout(boolean state) {
		fIsFlatLayout = state;
		fProblemDecorator.setIsFlatLayout(state);
	}

	@Override
	public void dispose() {
		if (fWorkingSetImages != null) {
			for (Iterator<Image> iter = fWorkingSetImages.values()
					.iterator(); iter.hasNext();) {
				iter.next().dispose();
			}
		}
		super.dispose();
	}
}
