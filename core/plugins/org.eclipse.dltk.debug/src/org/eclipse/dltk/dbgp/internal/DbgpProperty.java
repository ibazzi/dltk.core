/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal;

import org.eclipse.dltk.dbgp.IDbgpProperty;

public class DbgpProperty implements IDbgpProperty {

	private final String address;

	private final String name;

	private final String fullName;

	private final String type;

	private final String value;

	private final boolean constant;

	private final int childrenCount;

	private final IDbgpProperty[] availableChildren;

	private final boolean hasChildren;

	private final String key;

	private int page;

	private int pageSize;

	public DbgpProperty(String name, String fullName, String type, String value,
			int childrenCount, boolean hasChildren, boolean constant,
			String key, String address, IDbgpProperty[] availableChildren,
			int page, int pageSize) {
		this.name = name;
		this.fullName = fullName;
		this.type = type;
		this.value = value;
		this.address = address;
		this.childrenCount = childrenCount;
		this.availableChildren = availableChildren;
		this.hasChildren = hasChildren;
		this.constant = constant;
		this.key = key;
		this.page = page;
		this.pageSize = pageSize;
	}

	@Override
	public String getEvalName() {
		return fullName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean hasChildren() {
		return hasChildren;
	}

	@Override
	public int getChildrenCount() {
		return childrenCount;
	}

	@Override
	public IDbgpProperty[] getAvailableChildren() {
		return availableChildren.clone();
	}

	@Override
	public boolean isConstant() {
		return constant;
	}

	@Override
	public String toString() {
		return "DbgpProperty (Name: " + name + "; Full name: " + fullName //$NON-NLS-1$ //$NON-NLS-2$
				+ "; Type: " + type + "; Value: " + value + " Address: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ address + ")"; //$NON-NLS-1$
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public int getPage() {
		return page;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public String getAddress() {
		return address;
	}
}
