/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.testing.model;

import org.eclipse.dltk.internal.testing.model.TestElement.Status;
import org.eclipse.dltk.testing.DLTKTestingPlugin;
import org.eclipse.dltk.testing.TestRunListener;
import org.eclipse.dltk.testing.model.ITestCaseElement;




/**
 * Notifier for the callback listener API {@link TestRunListener}.
 */
public class TestRunListenerAdapter implements ITestSessionListener {

	private final TestRunSession fSession;

	public TestRunListenerAdapter(TestRunSession session) {
		fSession= session;
	}
	
	private Object[] getListeners() {
		return DLTKTestingPlugin.getDefault().getNewTestRunListeners().getListeners();
	}
	
	private void fireSessionStarted() {
		Object[] listeners= getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((TestRunListener) listeners[i]).sessionStarted(fSession);
		}
	}
	
	private void fireSessionFinished() {
		Object[] listeners= getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((TestRunListener) listeners[i]).sessionFinished(fSession);
		}
	}
	
	private void fireTestCaseStarted(ITestCaseElement testCaseElement) {
		Object[] listeners= getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((TestRunListener) listeners[i]).testCaseStarted(testCaseElement);
		}
	}
	
	private void fireTestCaseFinished(ITestCaseElement testCaseElement) {
		Object[] listeners= getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((TestRunListener) listeners[i]).testCaseFinished(testCaseElement);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#sessionStarted()
	 */
	@Override
	public void sessionStarted() {
		// wait until all test are added
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#sessionEnded(long)
	 */
	@Override
	public void sessionEnded(long elapsedTime) {
		fireSessionFinished();
		fSession.swapOut();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#sessionStopped(long)
	 */
	@Override
	public void sessionStopped(long elapsedTime) {
		fireSessionFinished();
		fSession.swapOut();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#sessionTerminated()
	 */
	@Override
	public void sessionTerminated() {
		fSession.swapOut();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#testAdded(org.eclipse.jdt.internal.junit.model.TestElement)
	 */
	@Override
	public void testAdded(TestElement testElement) {
		// do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#runningBegins()
	 */
	@Override
	public void runningBegins() {
		fireSessionStarted();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#testStarted(org.eclipse.jdt.internal.junit.model.TestCaseElement)
	 */
	@Override
	public void testStarted(TestCaseElement testCaseElement) {
		fireTestCaseStarted(testCaseElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#testEnded(org.eclipse.jdt.internal.junit.model.TestCaseElement)
	 */
	@Override
	public void testEnded(TestCaseElement testCaseElement) {
		fireTestCaseFinished(testCaseElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#testFailed(org.eclipse.jdt.internal.junit.model.TestElement, org.eclipse.jdt.internal.junit.model.TestElement.Status, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void testFailed(TestElement testElement, Status status, String trace, String expected, String actual, int code) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.junit.model.ITestSessionListener#testReran(org.eclipse.jdt.internal.junit.model.TestCaseElement, org.eclipse.jdt.internal.junit.model.TestElement.Status, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void testReran(TestCaseElement testCaseElement, Status status, String trace, String expectedResult, String actualResult) {
		// ignore
	}
	
	@Override
	public boolean acceptsSwapToDisk() {
		return true;
	}
}
