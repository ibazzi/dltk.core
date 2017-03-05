/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.core.tests.builder;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;
import org.eclipse.dltk.core.builder.IBuildParticipantFactory;
import org.eclipse.dltk.internal.core.builder.BuildParticipantManager;
import org.eclipse.dltk.internal.core.builder.BuildParticipantManager.BuildParticipantFactoryValue;
import org.eclipse.dltk.utils.TextUtils;
import org.junit.Test;

public class BuildParticipantManagerTests {

	private static class TestBuildParticipantFactory implements
			IBuildParticipantFactory {

		final String key;

		public TestBuildParticipantFactory(String key) {
			this.key = key;
		}

		@Override
		public IBuildParticipant createBuildParticipant(IScriptProject project)
				throws CoreException {
			return new TestBuildParticipant(key);
		}

	}

	private static class TestBuildParticipant implements IBuildParticipant {

		final String key;

		public TestBuildParticipant(String key) {
			this.key = key;
		}

		@Override
		public void build(IBuildContext context) throws CoreException {
			// NOP
		}

	}

	private BuildParticipantFactoryValue createDescriptor(String id,
			String requirements) {
		final BuildParticipantFactoryValue descriptor = new BuildParticipantFactoryValue(
				new TestBuildParticipantFactory(id), id, id);
		final String[] ids = TextUtils.split(requirements, ',');
		if (ids != null && ids.length != 0) {
			for (int i = 0; i < ids.length; ++i) {
				descriptor.requirements.add(ids[i]);
			}
		}
		return descriptor;
	}

	@Test
	public void testSimple() {
		BuildParticipantFactoryValue[] descriptors = new BuildParticipantFactoryValue[] {
				createDescriptor("A", null), createDescriptor("B", null),
				createDescriptor("C", null) };
		final IBuildParticipant[] participants = BuildParticipantManager
				.createParticipants(null, descriptors).participants;
		assertEquals(3, participants.length);
		assertEquals("A", ((TestBuildParticipant) participants[0]).key);
		assertEquals("B", ((TestBuildParticipant) participants[1]).key);
		assertEquals("C", ((TestBuildParticipant) participants[2]).key);
	}

	@Test
	public void testDependency() {
		BuildParticipantFactoryValue[] descriptors = new BuildParticipantFactoryValue[] {
				createDescriptor("A", "B,C"), createDescriptor("B", "C"),
				createDescriptor("C", null) };
		final IBuildParticipant[] participants = BuildParticipantManager
				.createParticipants(null, descriptors).participants;
		assertEquals(3, participants.length);
		assertEquals("C", ((TestBuildParticipant) participants[0]).key);
		assertEquals("B", ((TestBuildParticipant) participants[1]).key);
		assertEquals("A", ((TestBuildParticipant) participants[2]).key);
	}

	@Test
	public void testMissingDependency() {
		BuildParticipantFactoryValue[] descriptors = new BuildParticipantFactoryValue[] {
				createDescriptor("A", "D"), createDescriptor("B", "C"),
				createDescriptor("C", null) };
		final IBuildParticipant[] participants = BuildParticipantManager
				.createParticipants(null, descriptors).participants;
		assertEquals(2, participants.length);
		assertEquals("C", ((TestBuildParticipant) participants[0]).key);
		assertEquals("B", ((TestBuildParticipant) participants[1]).key);
	}

}
