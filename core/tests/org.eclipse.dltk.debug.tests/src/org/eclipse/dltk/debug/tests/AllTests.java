/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.debug.tests;

import org.eclipse.dltk.debug.dbgp.tests.DbgpBase64Tests;
import org.eclipse.dltk.debug.dbgp.tests.DbgpPropertyCommandsTests;
import org.eclipse.dltk.debug.dbgp.tests.DbgpRequestTests;
import org.eclipse.dltk.debug.dbgp.tests.DbgpStackCommandsTests;
import org.eclipse.dltk.debug.dbgp.tests.DbgpStackLevelTests;
import org.eclipse.dltk.debug.dbgp.tests.DbgpStatusTests;
import org.eclipse.dltk.debug.dbgp.tests.service.DbgpServiceTests;
import org.eclipse.dltk.debug.tests.breakpoints.BreakpointTests;
import org.eclipse.dltk.internal.debug.tests.VariableNameComparatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ DbgpRequestTests.class, DbgpStackLevelTests.class, DbgpPropertyCommandsTests.class,
		DbgpStackCommandsTests.class, DbgpBase64Tests.class, DbgpStatusTests.class,
		DbgpServiceTests.class, BreakpointTests.class, VariableNameComparatorTest.class })
public class AllTests {

}
