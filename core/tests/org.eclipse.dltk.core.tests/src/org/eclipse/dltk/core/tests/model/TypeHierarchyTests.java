/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.core.tests.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.WorkingCopyOwner;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.core.hierarchy.TypeHierarchy;

public class TypeHierarchyTests extends ModifyingResourceTests {
	private static boolean DEBUG = false;
	private static final String[] TEST_NATURE = new String[] { "org.eclipse.dltk.core.tests.testnature" };

	private ISourceModule cu = null;
	private ISourceModule copy = null;

	public class TestWorkingCopyOwner extends WorkingCopyOwner {
		@Override
		public IBuffer createBuffer(ISourceModule workingCopy) {
			return new TestBuffer(workingCopy);
		}
	}

	private final Set<IType> expectedExploredClasses = new HashSet<IType>();
	private final Set<IType> expectedCyclicClasses = new HashSet<IType>();
	private final FakeTypeHierarchy typeHierarchy = new FakeTypeHierarchy();
	private boolean useCacheSuperclass = true;

	public TypeHierarchyTests(String name) {
		super(ModelTestsPlugin.PLUGIN_NAME, name);
	}

	public static Suite suite() {
		return new Suite(TypeHierarchyTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		typeHierarchy.initialize(1);
		expectedExploredClasses.clear();
		expectedCyclicClasses.clear();
		useCacheSuperclass = true;
		try {

			this.createScriptProject("P", TEST_NATURE, new String[] {
					"src"
			} );
			this.createFolder("P/src/x/y");
			this.createFile("P/src/x/y/Z.txt", "");
			this.cu = this.getSourceModule("P/src/x/y/Z.txt");
			this.copy = cu.getWorkingCopy(null);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.copy != null)
			this.copy.discardWorkingCopy();
		AbstractModelTests.deleteProject("P");
		super.tearDown();
	}

	static class FakeTypeHierarchy extends TypeHierarchy {
		@Override
		public void addSubtype(IType type, IType subtype) {
			super.addSubtype(type, subtype);
		}

		@Override
		protected void cacheSuperclass(IType type, IType superclass) {
			super.cacheSuperclass(type, superclass);
		}

		@Override
		public void resetClassPaths() {
			super.resetClassPaths();
		}

		public Set<IType> getExploredClasses() {
			return this.exploredClasses;
		}

		public Set<IType> getCyclicClasses() {
			return this.cyclicClasses;
		}

		@Override
		protected void initialize(int size) {
			super.initialize(size);
		}
	}

	private Map<String, SourceType> createFakeTypes(char begin, char end, ModelElement modelElement) {
		Map<String, SourceType> types = new HashMap<String, SourceType>();
		for (char i = begin; i <= end; i++) {
			String name = Character.toString(i);
			types.put(name, new SourceType(modelElement, name));
		}
		return types;
	}

	private void populate(SourceType parent, boolean isParentOnCyclicPath, SourceType child,
			boolean isChildOnCyclicPath) {
		assertNotNull(parent);
		assertNotNull(child);

		if (useCacheSuperclass) {
			typeHierarchy.cacheSuperclass(parent, child);
		} else {
			typeHierarchy.addSubtype(child, parent);
		}
		expectedExploredClasses.add(parent);
		expectedExploredClasses.add(child);
		if (isParentOnCyclicPath) {
			expectedCyclicClasses.add(parent);
		}
		if (isChildOnCyclicPath) {
			expectedCyclicClasses.add(child);
		}

		explorePartialGraphThroughSubclasses(parent, false);
		explorePartialGraphThroughSubclasses(child, false);

		explorePartialGraphThroughSuperclasses(parent, false);
		explorePartialGraphThroughSuperclasses(child, false);
	}

	private void print(Set<IType> classPaths) {
		Set<IType> sortedClassPaths = new TreeSet<IType>((arg0, arg1) -> {
			if (arg0 == arg1 || arg0.equals(arg1)) {
				return 0;
			}
			return arg0.getElementName().compareTo(arg1.getElementName());
		});
		sortedClassPaths.addAll(classPaths);
		System.out.println(sortedClassPaths);
	}

	private void printCurrentState() {
		if (!DEBUG) {
			return;
		}
		print(typeHierarchy.getExploredClasses());
		print(expectedExploredClasses);
		System.out.println();
		print(typeHierarchy.getCyclicClasses());
		print(expectedCyclicClasses);
		System.out.println();
	}

	private void exploreAllGraphThroughSubclasses(SourceType type, boolean doReset) {
		assertNotNull(type);
		if (doReset) {
			typeHierarchy.resetClassPaths();
		}
		typeHierarchy.getSubclasses(type);
		printCurrentState();
		assertEquals(typeHierarchy.getExploredClasses(), expectedExploredClasses);
		assertEquals(typeHierarchy.getCyclicClasses(), expectedCyclicClasses);
	}

	private void exploreAllGraphThroughSuperclasses(SourceType type, boolean doReset) {
		assertNotNull(type);
		if (doReset) {
			typeHierarchy.resetClassPaths();
		}
		typeHierarchy.getSuperclass(type);
		printCurrentState();
		assertEquals(typeHierarchy.getExploredClasses(), expectedExploredClasses);
		assertEquals(typeHierarchy.getCyclicClasses(), expectedCyclicClasses);
	}

	private void explorePartialGraphThroughSubclasses(SourceType type, boolean doReset) {
		assertNotNull(type);
		if (doReset) {
			typeHierarchy.resetClassPaths();
		}
		typeHierarchy.getSubclasses(type);
		printCurrentState();
		assertTrue(!expectedExploredClasses.contains(type) || !typeHierarchy.getExploredClasses().isEmpty());
		assertTrue(typeHierarchy.getExploredClasses().containsAll(typeHierarchy.getCyclicClasses()));
		assertTrue(expectedExploredClasses.containsAll(typeHierarchy.getExploredClasses()));
		assertTrue(expectedExploredClasses.containsAll(typeHierarchy.getCyclicClasses()));
		assertTrue(expectedCyclicClasses.containsAll(typeHierarchy.getCyclicClasses()));
	}

	private void explorePartialGraphThroughSuperclasses(SourceType type, boolean doReset) {
		assertNotNull(type);
		if (doReset) {
			typeHierarchy.resetClassPaths();
		}
		typeHierarchy.getSuperclass(type);
		printCurrentState();
		assertTrue(!expectedExploredClasses.contains(type) || !typeHierarchy.getExploredClasses().isEmpty());
		assertTrue(typeHierarchy.getExploredClasses().containsAll(typeHierarchy.getCyclicClasses()));
		assertTrue(expectedExploredClasses.containsAll(typeHierarchy.getExploredClasses()));
		assertTrue(expectedExploredClasses.containsAll(typeHierarchy.getCyclicClasses()));
		assertTrue(expectedCyclicClasses.containsAll(typeHierarchy.getCyclicClasses()));
	}

	public void testCyclicHierarchy001() throws Exception {
		assertNotNull(cu);
		IModelElement p = cu.getParent();
		assertTrue(p instanceof ModelElement);

		typeHierarchy.resetClassPaths();
		Map<String, SourceType> types = createFakeTypes('A', 'U', (ModelElement) p);

		//* Class hierarchy representation, "(c)" means that the relevant class in on a cyclic path
		//*
		//*                                                                      +------+
		//*                                                                      |  L   |
		//*                                                                      +------+
		//*                                                                        ^
		//*                                                                        |                      +-------------------------------------+
		//*                                                                        |                      v                                     |
		//*     +------+     +------+     +------+     +------+     +------+     +------+     +---+     +------+     +---+     +---+     +---+  |
		//*  +> | B(c) | <-- | A(c) | --> | H(c) | --> |      | --> | J(c) | --> | K(c) | --> | O | --> | P(c) | --> | R | --> | S | --> | U |  |
		//*  |  +------+     +------+     +------+     |      |     +------+     +------+     +---+     +------+     +---+     +---+     +---+  |
		//*  |    |            ^                       |      |                    |                      |            |                   ^    |
		//*  |    |            +---------------------- | I(c) | <------------------+                      |            |                   |    |
		//*  |    v                                    |      |                                           v            v                   |    |
		//*  |  +------+                               |      |                                         +------+     +---+                 |    |
		//*  |  | C(c) | <------------------+       +- |      | --------------+                         | Q(c) |     | T | ----------------+    |
		//*  |  +------+                    |       |  +------+               |                         +------+     +---+                      |
		//*  |    |                         |       |                         |                           |                                     |
		//*  |    |                         |       |    +------------+       |                           |                                     |
		//*  |    v                         |       |    v            |       |                           |                                     |
		//*  |  +------+     +------+     +------+  |  +------+     +------+  |                           |                                     |
		//*  +- | D(c) | --> | E(c) | --> | F(c) |  +> | M(c) | --> | N(c) |  |                           +-------------------------------------+
		//*     +------+     +------+     +------+     +------+     +------+  |
		//*                    |            ^                                 |
		//*                    |            +---------------------------------+
		//*                    v
		//*                  +------+
		//*                  |  G   |
		//*                  +------+
		//*
		populate(types.get("A"), true, types.get("B"), true);
		populate(types.get("B"), true, types.get("C"), true);
		populate(types.get("C"), true, types.get("D"), true);
		populate(types.get("D"), true, types.get("E"), true);
		populate(types.get("E"), true, types.get("F"), true);
		populate(types.get("E"), true, types.get("G"), false);
		populate(types.get("F"), true, types.get("C"), true);
		populate(types.get("D"), true, types.get("B"), true);
		populate(types.get("A"), true, types.get("H"), true);
		populate(types.get("H"), true, types.get("I"), true);
		populate(types.get("I"), true, types.get("F"), true);
		populate(types.get("I"), true, types.get("A"), true);
		populate(types.get("I"), true, types.get("J"), true);
		populate(types.get("J"), true, types.get("K"), true);
		populate(types.get("K"), true, types.get("I"), true);
		populate(types.get("K"), true, types.get("L"), false);
		populate(types.get("K"), true, types.get("O"), false);
		populate(types.get("O"), false, types.get("P"), true);
		populate(types.get("P"), true, types.get("Q"), true);
		populate(types.get("Q"), true, types.get("P"), true);
		populate(types.get("P"), true, types.get("R"), false);
		populate(types.get("I"), true, types.get("M"), true);
		populate(types.get("M"), true, types.get("N"), true);
		populate(types.get("N"), true, types.get("M"), true);
		populate(types.get("R"), false, types.get("S"), false);
		populate(types.get("R"), false, types.get("T"), false);
		populate(types.get("S"), false, types.get("U"), false);
		populate(types.get("T"), false, types.get("U"), false);
		assertTrue(expectedExploredClasses.containsAll(expectedCyclicClasses));

		exploreAllGraphThroughSuperclasses(types.get("A"), false);
		exploreAllGraphThroughSuperclasses(types.get("A"), true);
		exploreAllGraphThroughSuperclasses(types.get("K"), true);
		explorePartialGraphThroughSuperclasses(types.get("O"), true);
		exploreAllGraphThroughSuperclasses(types.get("K"), false);
		explorePartialGraphThroughSuperclasses(types.get("D"), true);

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSuperclasses(types.get(typeName), true);
		}

		exploreAllGraphThroughSubclasses(types.get("B"), true);
		exploreAllGraphThroughSubclasses(types.get("O"), true);

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSubclasses(types.get(typeName), true);
		}
	}

	public void testCyclicHierarchy002() throws Exception {
		useCacheSuperclass = false;
		testCyclicHierarchy001();
	}

	public void testCyclicHierarchy003() throws Exception {
		assertNotNull(cu);
		IModelElement p = cu.getParent();
		assertTrue(p instanceof ModelElement);

		typeHierarchy.resetClassPaths();
		Map<String, SourceType> types = createFakeTypes('A', 'Z', (ModelElement) p);

		//* Class hierarchy representation, "(c)" means that the relevant class in on a cyclic path
		//*
		//*     +------+     +---+     +---+
		//*  +> | W(c) | --> | V | --> | H |
		//*  |  +------+     +---+     +---+
		//*  |    |
		//*  |    |
		//*  |    v
		//*  |  +------+
		//*  +- | X(c) |
		//*     +------+
		//*
		populate(types.get("W"), true, types.get("X"), true);
		populate(types.get("W"), true, types.get("V"), false);
		populate(types.get("V"), false, types.get("H"), false);
		populate(types.get("X"), true, types.get("W"), true);
		assertTrue(expectedExploredClasses.containsAll(expectedCyclicClasses));

		explorePartialGraphThroughSuperclasses(types.get("H"), true);

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSuperclasses(types.get(typeName), true);
		}

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSubclasses(types.get(typeName), true);
		}
	}

	public void testCyclicHierarchy004() throws Exception {
		useCacheSuperclass = false;
		testCyclicHierarchy003();
	}

	public void testNonCyclicHierarchy005() throws Exception {
		assertNotNull(cu);
		IModelElement p = cu.getParent();
		assertTrue(p instanceof ModelElement);

		typeHierarchy.resetClassPaths();
		Map<String, SourceType> types = createFakeTypes('A', 'F', (ModelElement) p);

		//* Class hierarchy representation, "(c)" means that the relevant class in on a cyclic path
		//*
		//*                     +---+
		//*                     | F |
		//*                     +---+
		//*                       ^
		//*                       |
		//*                       |
		//* +---+     +---+     +---+     +---+
		//* | A | --> | B | --> | D | --> | E |
		//* +---+     +---+     +---+     +---+
		//*   |                   ^
		//*   |                   |
		//*   v                   |
		//* +---+                 |
		//* | C | ----------------+
		//* +---+
		//*
		populate(types.get("A"), false, types.get("B"), false);
		populate(types.get("A"), false, types.get("C"), false);
		populate(types.get("B"), false, types.get("D"), false);
		populate(types.get("C"), false, types.get("D"), false);
		populate(types.get("D"), false, types.get("E"), false);
		populate(types.get("D"), false, types.get("F"), false);
		assertTrue(expectedExploredClasses.containsAll(expectedCyclicClasses));

		exploreAllGraphThroughSuperclasses(types.get("A"), false);

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSuperclasses(types.get(typeName), true);
		}

		for (String typeName : types.keySet()) {
			explorePartialGraphThroughSubclasses(types.get(typeName), true);
		}
	}

	public void testCyclicHierarchy006() throws Exception {
		useCacheSuperclass = false;
		testNonCyclicHierarchy005();
	}

	public void testEmptyHierarchy007() throws Exception {
		assertNotNull(cu);
		IModelElement p = cu.getParent();
		assertTrue(p instanceof ModelElement);

		typeHierarchy.resetClassPaths();

		SourceType type = new SourceType((ModelElement) p, "A");
		if (useCacheSuperclass) {
			typeHierarchy.cacheSuperclass(type, null);
		} else {
			typeHierarchy.addSubtype(null, type);
		}

		assertTrue(typeHierarchy.getSuperclass(type).length == 0);
		assertTrue(typeHierarchy.getSubclasses(type).length == 0);

		assertTrue(typeHierarchy.getExploredClasses().isEmpty());
		assertTrue(typeHierarchy.getCyclicClasses().isEmpty());
	}

	public void testEmptyHierarchy008() throws Exception {
		useCacheSuperclass = false;
		testEmptyHierarchy007();
	}

	public void testFullyCyclicHierarchy009() throws Exception {
		assertNotNull(cu);
		IModelElement p = cu.getParent();
		assertTrue(p instanceof ModelElement);

		typeHierarchy.resetClassPaths();
		Map<String, SourceType> types = createFakeTypes('A', 'H', (ModelElement) p);

		//* Class hierarchy representation, "(c)" means that the relevant class in on a cyclic path
		//*
		//*                +---------------------------------------------------+
		//*                v                                                   |
		//* +------+     +------+     +------+     +------+     +------+     +------+
		//* | A(c) | --> | B(c) | --> | C(c) | --> | G(c) | --> | H(c) | --> | F(c) |
		//* +------+     +------+     +------+     +------+     +------+     +------+
		//*   ^                         |
		//*   |                         |
		//*   |                         v
		//*   |                       +------+     +------+
		//*   |                       | D(c) | --> | E(c) |
		//*   |                       +------+     +------+
		//*   |                                      |
		//*   +--------------------------------------+
		//*
		populate(types.get("A"), true, types.get("B"), true);
		populate(types.get("B"), true, types.get("C"), true);
		populate(types.get("C"), true, types.get("D"), true);
		populate(types.get("D"), true, types.get("E"), true);
		populate(types.get("E"), true, types.get("A"), true);
		populate(types.get("F"), true, types.get("B"), true);
		populate(types.get("C"), true, types.get("G"), true);
		populate(types.get("G"), true, types.get("H"), true);
		populate(types.get("H"), true, types.get("F"), true);
		assertTrue(expectedExploredClasses.containsAll(expectedCyclicClasses));
		assertEquals(expectedExploredClasses, expectedCyclicClasses);

		for (String typeName : types.keySet()) {
			exploreAllGraphThroughSuperclasses(types.get(typeName), true);
		}

		for (String typeName : types.keySet()) {
			exploreAllGraphThroughSubclasses(types.get(typeName), true);
		}
	}

	public void testFullyCyclicHierarchy010() throws Exception {
		useCacheSuperclass = false;
		testFullyCyclicHierarchy009();
	}
}
