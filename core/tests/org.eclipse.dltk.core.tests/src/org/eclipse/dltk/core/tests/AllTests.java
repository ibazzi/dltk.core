/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.core.tests;

import org.eclipse.dltk.core.tests.builder.BuildParticipantManagerTests;
import org.eclipse.dltk.core.tests.buildpath.BuildpathTests;
import org.eclipse.dltk.core.tests.buildpath.SetContainerEventsTest;
import org.eclipse.dltk.core.tests.cache.CacheTests;
import org.eclipse.dltk.core.tests.cache.SourceModuleInfoCacheTest;
import org.eclipse.dltk.core.tests.compiler.CompilerCharOperationTests;
import org.eclipse.dltk.core.tests.compiler.CompilerUtilTests;
import org.eclipse.dltk.core.tests.ddp.CoreDDPTests;
import org.eclipse.dltk.core.tests.launching.EnvironmentResolverTests;
import org.eclipse.dltk.core.tests.launching.InterpreterConfigTests;
import org.eclipse.dltk.core.tests.mixin.MixinIndexTests;
import org.eclipse.dltk.core.tests.mixin.MixinModelTests;
import org.eclipse.dltk.core.tests.model.BufferTests;
import org.eclipse.dltk.core.tests.model.ExternalFragmentTests;
import org.eclipse.dltk.core.tests.model.ModelMembersTests;
import org.eclipse.dltk.core.tests.model.NamespaceTests;
import org.eclipse.dltk.core.tests.model.WorkingCopyTests;
import org.eclipse.dltk.core.tests.parser.SourceParserTests;
import org.eclipse.dltk.core.tests.search.Bug387751Test;
import org.eclipse.dltk.core.tests.util.CharacterStackTests;
import org.eclipse.dltk.core.tests.utils.CharOperationTests;
import org.eclipse.dltk.core.tests.utils.IntListTests;
import org.eclipse.dltk.core.tests.utils.InternalCoreUtilTest;
import org.eclipse.dltk.core.tests.utils.TextUtilsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ BuildParticipantManagerTests.class, BuildpathTests.class,
		UserLibraryTests.class, CacheTests.class, CompilerUtilTests.class,
		CompilerCharOperationTests.class, IntListTests.class,
		CoreDDPTests.class, EnvironmentResolverTests.class,
		InterpreterConfigTests.class, MixinIndexTests.class,
		MixinModelTests.class, BufferTests.class, ModelMembersTests.class,
		NamespaceTests.class, WorkingCopyTests.class,
		ExternalFragmentTests.class, SourceParserTests.class,
		CharacterStackTests.class, CharOperationTests.class,
		InternalCoreUtilTest.class, TextUtilsTest.class, Bug387751Test.class,
		SourceModuleInfoCacheTest.class, SetContainerEventsTest.class })
public class AllTests {
}
