/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de bug 37333, 26653
 *     Johan Walles: walles@mailblocks.com bug 68737
 *******************************************************************************/
package org.eclipse.dltk.internal.testing.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.internal.testing.model.TestElement;
import org.eclipse.dltk.testing.ITestRunnerUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A pane that shows a stack trace of a failed test.
 */
public class FailureTrace implements IMenuListener {
	private static final int MAX_LABEL_LENGTH = 256;

	private Table fTable;
	private TestRunnerViewPart fTestRunner;
	private String fInputTrace;
	private final Clipboard fClipboard;
	private TestElement fFailure;
	private CompareResultsAction fCompareAction;
	private EnableStackFilterAction fStackTraceFilterAction;
	private final FailureTableDisplay fFailureTableDisplay;

	public FailureTrace(Composite parent, Clipboard clipboard,
			TestRunnerViewPart testRunner, ToolBar toolBar) {
		Assert.isNotNull(clipboard);

		fTestRunner = testRunner;
		fClipboard = clipboard;
		// fill the failure trace viewer toolbar
		ToolBarManager failureToolBarmanager = new ToolBarManager(toolBar);
		fStackTraceFilterAction = new EnableStackFilterAction(this);
		failureToolBarmanager.add(fStackTraceFilterAction);
		fCompareAction = new CompareResultsAction(this);
		fCompareAction.setEnabled(false);
		failureToolBarmanager.add(fCompareAction);
		failureToolBarmanager.update(true);

		fTable = new Table(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

		OpenStrategy handler = new OpenStrategy(fTable);
		handler.addOpenListener(e -> {
			if (fTable.getSelectionIndex() == 0
					&& fFailure.isComparisonFailure()) {
				fCompareAction.run();
			}
			if (fTable.getSelection().length != 0) {
				IAction a = createOpenEditorAction(getSelectedText());
				if (a != null)
					a.run();
			}
		});

		initMenu();

		fFailureTableDisplay = new FailureTableDisplay(fTable);
	}

	private void initMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(fTable);
		fTable.setMenu(menu);
	}

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		if (fTable.getSelectionCount() > 0) {
			IAction a = createOpenEditorAction(getSelectedText());
			if (a != null)
				manager.add(a);
			manager.add(
					new DLTKTestingCopyAction(FailureTrace.this, fClipboard));
		}
		// fix for bug 68058
		if (fFailure != null && fFailure.isComparisonFailure())
			manager.add(fCompareAction);
	}

	public String getTrace() {
		return fInputTrace;
	}

	private String getSelectedText() {
		return fTable.getSelection()[0].getText();
	}

	private IAction createOpenEditorAction(String traceLine) {
		final ITestRunnerUI runnerUI = fTestRunner.getTestRunnerUI();
		return runnerUI.createOpenEditorAction(traceLine);
	}

	ITestRunnerUI getTestRunnerUI() {
		return fTestRunner.getTestRunnerUI();
	}

	/**
	 * Returns the composite used to present the trace
	 *
	 * @return The composite
	 */
	Composite getComposite() {
		return fTable;
	}

	/**
	 * Refresh the table from the trace.
	 */
	public void refresh() {
		updateTable(fInputTrace);
	}

	/**
	 * Shows a TestFailure
	 *
	 * @param test
	 *            the failed test
	 */
	public void showFailure(TestElement test) {
		fFailure = test;
		String trace = ""; //$NON-NLS-1$
		updateEnablement(test);
		if (test != null)
			trace = test.getTrace();
		if (fInputTrace == trace)
			return;
		fInputTrace = trace;
		updateTable(trace);
	}

	public void updateEnablement(TestElement test) {
		boolean enableCompare = test != null && test.isComparisonFailure();
		fCompareAction.setEnabled(enableCompare);
		if (enableCompare) {
			fCompareAction.updateOpenDialog(test);
		}
	}

	private void updateTable(String trace) {
		if (trace == null || trace.trim().equals("")) { //$NON-NLS-1$
			clear();
			return;
		}
		trace = trace.trim();
		fTable.setRedraw(false);
		fTable.removeAll();
		new TextualTrace(trace, fTestRunner.getTestRunnerUI())
				.display(fFailureTableDisplay, MAX_LABEL_LENGTH);
		fTable.setRedraw(true);
	}

	/**
	 * Shows other information than a stack trace.
	 *
	 * @param text
	 *            the informational message to be shown
	 */
	public void setInformation(String text) {
		clear();
		TableItem tableItem = fFailureTableDisplay.newTableItem();
		tableItem.setText(text);
	}

	/**
	 * Prepares the trace view for the new test session.
	 */
	public void reset() {
		clear();
		final ITestRunnerUI runnerUI = fTestRunner.getTestRunnerUI();
		if (runnerUI.canFilterStack()) {
			fStackTraceFilterAction.setEnabled(true);
			fStackTraceFilterAction.setChecked(runnerUI.isFilterStack());
		} else {
			fStackTraceFilterAction.setEnabled(false);
			fStackTraceFilterAction.setChecked(false);
		}
	}

	/**
	 * Clears the non-stack trace info
	 */
	public void clear() {
		fTable.removeAll();
		fInputTrace = null;
	}

	public TestElement getFailedTest() {
		return fFailure;
	}

	public Shell getShell() {
		return fTable.getShell();
	}

	public FailureTableDisplay getFailureTableDisplay() {
		return fFailureTableDisplay;
	}
}
