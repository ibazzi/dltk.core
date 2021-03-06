/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.corext.util;

import org.eclipse.dltk.core.search.TypeNameMatch;
import org.eclipse.dltk.ui.dialogs.ITypeInfoRequestor;


public class TypeInfoRequestorAdapter implements ITypeInfoRequestor {

	private TypeNameMatch fMatch;

	public void setMatch(TypeNameMatch type) {
		fMatch= type;
	}

	@Override
	public int getModifiers() {
		return fMatch.getModifiers();
	}

	@Override
	public String getPackageName() {
		return fMatch.getPackageName();
	}

	@Override
	public String getTypeName() {
		return fMatch.getSimpleTypeName();
	}


}
