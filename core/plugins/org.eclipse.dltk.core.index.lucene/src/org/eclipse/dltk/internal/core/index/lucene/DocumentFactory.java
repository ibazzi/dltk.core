/*******************************************************************************
 * Copyright (c) 2016 Zend Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Zend Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.internal.core.index.lucene;

import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_DOC;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_ELEMENT_NAME;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_METADATA;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_PARENT;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_PATH;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.BDV_QUALIFIER;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.F_CC_NAME;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.F_ELEMENT_NAME_LC;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.F_PARENT;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.F_PATH;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.F_QUALIFIER;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_FLAGS;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_LENGTH;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_NAME_LENGTH;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_NAME_OFFSET;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_OFFSET;
import static org.eclipse.dltk.internal.core.index.lucene.IndexFields.NDV_TIMESTAMP;

import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.util.BytesRef;
import org.eclipse.dltk.core.index2.IIndexingRequestor.DeclarationInfo;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;

/**
 * <p>
 * Factory for creating different types of Lucene documents.
 * </p>
 * <p>
 * To boost the performance of documents search and related data retrieval,
 * numeric and binary document values are being used in pair with non-stored
 * fields. It basically means that non-stored fields are used for document
 * search purposes while numeric and binary document values are used to retrieve
 * the related data for particular search matches.
 * </p>
 * 
 * @author Bartlomiej Laczkowski
 */
public final class DocumentFactory {

	/**
	 * Creates and returns a document for provided reference info.
	 * 
	 * @param source
	 * @param info
	 * @return a document for provided reference info
	 */
	public static Document createForReference(String source,
			ReferenceInfo info) {
		Document doc = new Document();
		// Fields for search (no store, doc values will be used instead)
		addStringEntry(doc, F_PATH, source, false);
		addStringEntry(doc, F_PARENT, info.parent, false);
		addStringEntry(doc, F_QUALIFIER, info.qualifier, false);
		addStringLCEntry(doc, F_ELEMENT_NAME_LC, info.elementName, false);
		// Add numeric doc values
		addLongEntry(doc, NDV_OFFSET, info.offset);
		addLongEntry(doc, NDV_LENGTH, info.length);
		// Add text as binary doc values
		addBinaryEntry(doc, BDV_PATH, source);
		addBinaryEntry(doc, BDV_ELEMENT_NAME, info.elementName);
		addBinaryEntry(doc, BDV_QUALIFIER, info.qualifier);
		addBinaryEntry(doc, BDV_METADATA, info.metadata);
		return doc;
	}

	/**
	 * Creates and returns a document for provided declaration info.
	 * 
	 * @param source
	 * @param info
	 * @return a document for provided declaration info
	 */
	public static Document createForDeclaration(String source,
			DeclarationInfo info) {
		Document doc = new Document();
		// Fields for search (no store, doc values will be used instead)
		addStringEntry(doc, F_PATH, source, false);
		addStringEntry(doc, F_PARENT, info.parent, false);
		addStringEntry(doc, F_QUALIFIER, info.qualifier, false);
		addStringLCEntry(doc, F_ELEMENT_NAME_LC, info.elementName, false);
		addCCNameEntry(doc, info.elementName);
		// Add numeric doc values
		addLongEntry(doc, NDV_OFFSET, info.offset);
		addLongEntry(doc, NDV_LENGTH, info.length);
		addLongEntry(doc, NDV_NAME_OFFSET, info.nameOffset);
		addLongEntry(doc, NDV_NAME_LENGTH, info.nameLength);
		addLongEntry(doc, NDV_FLAGS, info.flags);
		// Add text as binary doc values
		addBinaryEntry(doc, BDV_PATH, source);
		addBinaryEntry(doc, BDV_ELEMENT_NAME, info.elementName);
		addBinaryEntry(doc, BDV_PARENT, info.parent);
		addBinaryEntry(doc, BDV_QUALIFIER, info.qualifier);
		addBinaryEntry(doc, BDV_METADATA, info.metadata);
		addBinaryEntry(doc, BDV_DOC, info.doc);
		return doc;
	}

	/**
	 * Creates and returns a document for source file time stamp.
	 * 
	 * @param source
	 * @param timestamp
	 * @return a document for source file time stamp
	 */
	public static Document createForTimestamp(String source, long timestamp) {
		Document doc = new Document();
		addStringEntry(doc, F_PATH, source, true);
		addLongEntry(doc, NDV_TIMESTAMP, timestamp);
		return doc;
	}

	private static void addLongEntry(Document doc, String category,
			long value) {
		doc.add(new NumericDocValuesField(category, value));
	}

	private static void addStringEntry(Document doc, String category,
			String value, boolean store) {
		if (value == null) {
			return;
		}
		doc.add(new StringField(category, value,
				store ? Field.Store.YES : Field.Store.NO));
	}

	private static void addStringLCEntry(Document doc, String category,
			String value, boolean store) {
		addStringEntry(doc, category, value.toLowerCase(), store);
	}

	private static void addCCNameEntry(Document doc, String name) {
		addStringEntry(doc, F_CC_NAME, Utils.getCamelCaseName(name), false);
	}

	private static void addBinaryEntry(Document doc, String category,
			String value) {
		if (value == null) {
			return;
		}
		doc.add(new BinaryDocValuesField(category, new BytesRef(value)));
	}

}
