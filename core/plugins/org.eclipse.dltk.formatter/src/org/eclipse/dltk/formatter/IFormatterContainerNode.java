/*******************************************************************************
 * Copyright (c) 2008, 2016 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.formatter;

import java.util.List;

public interface IFormatterContainerNode extends IFormatterNode {

	void addChild(IFormatterNode child);

	@Override
	boolean isEmpty();

	List<IFormatterNode> getBody();

	List<IFormatterNode> getChildren();

}
