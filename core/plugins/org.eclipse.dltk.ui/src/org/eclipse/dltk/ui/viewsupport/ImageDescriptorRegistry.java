/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.viewsupport;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageDescriptorRegistry {

	private final HashMap<ImageDescriptor, Image> fRegistry = new HashMap<ImageDescriptor, Image>(
			10);

	private final Display fDisplay;

	/**
	 * Creates a new image descriptor registry for the current or default
	 * display, respectively.
	 */
	public ImageDescriptorRegistry() {
		this(DLTKUIPlugin.getStandardDisplay());
	}

	/**
	 * @param disposeWithDisplay
	 * @since 2.0
	 */
	public ImageDescriptorRegistry(boolean disposeWithDisplay) {
		this(DLTKUIPlugin.getStandardDisplay(), disposeWithDisplay);
	}

	public ImageDescriptorRegistry(Display display) {
		this(display, true);
	}

	/**
	 * Creates a new image descriptor registry for the given display. All images
	 * managed by this registry will be disposed when the display gets disposed.
	 *
	 * @param display
	 *            the display the images managed by this registry are allocated
	 *            for
	 * @since 2.0
	 */
	public ImageDescriptorRegistry(Display display, boolean disposeWithDisplay) {
		fDisplay = display;
		if (disposeWithDisplay) {
			hookDisplay();
		}
	}

	/**
	 * Returns the image associated with the given image descriptor.
	 *
	 * @param descriptor
	 *            the image descriptor for which the registry manages an image
	 * @return the image associated with the image descriptor or
	 *         <code>null</code> if the image descriptor can't create the
	 *         requested image.
	 */
	public Image get(ImageDescriptor descriptor) {
		if (descriptor == null)
			descriptor = ImageDescriptor.getMissingImageDescriptor();

		Image result = fRegistry.get(descriptor);
		if (result != null)
			return result;

		result = descriptor.createImage();
		if (result != null)
			fRegistry.put(descriptor, result);
		return result;
	}

	/**
	 * Disposes all images managed by this registry.
	 */
	public void dispose() {
		for (Iterator<Image> iter = fRegistry.values().iterator(); iter
				.hasNext();) {
			Image image = iter.next();
			image.dispose();
		}
		fRegistry.clear();
	}

	private void hookDisplay() {
		fDisplay.asyncExec(() -> fDisplay.disposeExec(() -> dispose()));
	}
}
