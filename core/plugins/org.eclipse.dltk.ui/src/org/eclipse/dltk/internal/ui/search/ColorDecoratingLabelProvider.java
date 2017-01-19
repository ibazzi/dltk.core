/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.search;

import org.eclipse.dltk.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;

public class ColorDecoratingLabelProvider extends ColoringLabelProvider {

	public ColorDecoratingLabelProvider(IStyledLabelProvider provider,
			ILabelDecorator decorator) {
		super(provider, decorator, DecorationContext.DEFAULT_CONTEXT);

	}
}
