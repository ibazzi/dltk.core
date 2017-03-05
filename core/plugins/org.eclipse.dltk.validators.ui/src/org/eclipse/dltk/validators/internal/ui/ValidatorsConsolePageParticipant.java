/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.validators.internal.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class ValidatorsConsolePageParticipant
		implements IConsolePageParticipant {

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		Assert.isLegal(console instanceof ValidatorConsole);
		Assert.isLegal(ValidatorConsole.TYPE.equals(console.getType()));
		IActionBars bars = page.getSite().getActionBars();
		IToolBarManager toolbarManager = bars.getToolBarManager();
		toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				new CloseValidatorsConsoleAction((ValidatorConsole) console));
		toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				new RemoveAllValidatorConsolesAction());
		bars.getMenuManager().add(new ShowCommandLineValidatorsConsoleAction(
				page, (ValidatorConsole) console));
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}
}
