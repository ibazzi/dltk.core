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
package org.eclipse.dltk.core.tests.mixin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.index.EntryResult;
import org.eclipse.dltk.core.search.index.MixinIndex;
import org.eclipse.dltk.core.search.indexing.IIndexConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link MixinIndex} class.
 */
public class MixinIndexTests {

	private File file;
	private MixinIndex index;

	@Before
	public void setUp() throws Exception {
		file = File.createTempFile("mixin", ".index");
		index = createIndex(true);
	}

	private MixinIndex createIndex(boolean isNew) throws IOException {
		return new MixinIndex(file.getPath(), file.getPath(), !isNew);
	}

	@After
	public void tearDown() throws Exception {
		index = null;
		file.delete();
	}

	@Test
	public void testQuery() throws IOException {
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "A");
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "B");
		EntryResult[] result = index.query(
				new char[][] { IIndexConstants.MIXIN }, "key1".toCharArray(),
				SearchPattern.R_EXACT_MATCH);
		assertEquals(1, result.length);
		String[] docNames = result[0].getDocumentNames(index);
		assertEquals(2, docNames.length);
		Arrays.sort(docNames);
		assertEquals("A", docNames[0]);
		assertEquals("B", docNames[1]);
	}
	
	@Test
	public void testQueryDocumentNames() throws IOException {
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "A");
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "B");
		String[] docNames = index.queryDocumentNames(null);
		assertEquals(2, docNames.length);
		Arrays.sort(docNames);
		assertEquals("A", docNames[0]);
		assertEquals("B", docNames[1]);
		docNames = index.queryDocumentNames(Util.EMPTY_STRING);
		assertEquals(2, docNames.length);
		docNames = index.queryDocumentNames("Z");
		assertEquals(0, docNames.length);
	}
	
	@Test
	public void testRemove() throws IOException {
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "A");
		index.addIndexEntry(IIndexConstants.MIXIN, "key1".toCharArray(), "B");
		index.remove("A");
		String[] docNames = index.queryDocumentNames(null);
		assertEquals(1, docNames.length);
		assertEquals("B", docNames[0]);
		EntryResult[] result = index.query(
				new char[][] { IIndexConstants.MIXIN }, "key1".toCharArray(),
				SearchPattern.R_EXACT_MATCH);
		assertEquals(1, result.length);
		docNames = result[0].getDocumentNames(index);
		assertEquals(1, docNames.length);
		assertEquals("B", docNames[0]);
	}
	
	@Test
	public void testDocumentsWithoutKeys() throws IOException {
		index.addDocumentName("A");
		assertTrue(index.hasChanged());
		index.save();
		index = createIndex(false);
		String[] docNames = index.queryDocumentNames(null);
		assertEquals(1, docNames.length);
		assertEquals("A", docNames[0]);
	}
	
	@Test
	public void testDocumentsWithoutKeysHasChanged() throws IOException {
		index.addDocumentName("A");
		assertTrue(index.hasChanged());
		index.save();
		assertFalse(index.hasChanged());
		index.addDocumentName("A");
		assertFalse(index.hasChanged());
		index.addDocumentName("B");
		assertTrue(index.hasChanged());
	}

}
