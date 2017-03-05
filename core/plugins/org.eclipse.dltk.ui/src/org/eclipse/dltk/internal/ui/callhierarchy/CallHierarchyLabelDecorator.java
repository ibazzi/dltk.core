/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.callhierarchy;

import org.eclipse.dltk.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


/**
 * LabelDecorator that decorates an method's image with recursion overlays.
 * The viewer using this decorator is responsible for updating the images on element changes.
 */
public class CallHierarchyLabelDecorator implements ILabelDecorator {

    /**
     * Creates a decorator. The decorator creates an own image registry to cache
     * images.
     */
    public CallHierarchyLabelDecorator() {
        // Do nothing
    }

    @Override
	public String decorateText(String text, Object element) {
        return text;
    }

    @Override
	public Image decorateImage(Image image, Object element) {
        int adornmentFlags= computeAdornmentFlags(element);
        if (adornmentFlags != 0) {
            ImageDescriptor baseImage= new ImageImageDescriptor(image);
            Rectangle bounds= image.getBounds();
            return DLTKUIPlugin.getImageDescriptorRegistry().get(new CallHierarchyImageDescriptor(baseImage, adornmentFlags, new Point(bounds.width, bounds.height)));
        }
        return image;
    }

    /**
     * Note: This method is for internal use only. Clients should not call this method.
     */
    private int computeAdornmentFlags(Object element) {
        int flags= 0;
        if (element instanceof MethodWrapper) {
            MethodWrapper methodWrapper= (MethodWrapper) element;
            if (methodWrapper.isRecursive()) {
                flags= CallHierarchyImageDescriptor.RECURSIVE;
            }
            if (isMaxCallDepthExceeded(methodWrapper)) {
                flags|= CallHierarchyImageDescriptor.MAX_LEVEL;
            }
        }
        return flags;
    }

    private boolean isMaxCallDepthExceeded(MethodWrapper methodWrapper) {
        return methodWrapper.getLevel() > CallHierarchyUI.getDefault().getMaxCallDepth();
    }

    @Override
	public void addListener(ILabelProviderListener listener) {
        // Do nothing
    }

    @Override
	public void dispose() {
        // Nothing to dispose
    }

    @Override
	public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
	public void removeListener(ILabelProviderListener listener) {
        // Do nothing
    }
}
