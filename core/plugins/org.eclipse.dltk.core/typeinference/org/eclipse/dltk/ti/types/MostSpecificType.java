/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ti.types;

/**
 * Represents most specific type in language. This class is just a symbol, so
 * user algorithms should convert it to contrete type himself if needed.
 */
public final class MostSpecificType implements IEvaluatedType {

	private static MostSpecificType instance;

	private MostSpecificType() {
	}

	@Override
	public String getTypeName() {
		return null;
	}

	public static MostSpecificType getInstance() {
		if (instance == null) {
			instance = new MostSpecificType();
		}
		return instance;
	}

	@Override
	public boolean subtypeOf(IEvaluatedType type) {
		return false;
	}

}