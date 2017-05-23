/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.core.search.matching;

import org.eclipse.dltk.compiler.CharOperation;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.indexing.IIndexConstants;

public class FieldPattern extends VariablePattern implements IIndexConstants {
	// declaring type
	public char[] declaringQualification;
	public char[] declaringSimpleName;
	// type
	protected char[] typeQualification;
	protected char[] typeSimpleName;
	protected static char[][] REF_CATEGORIES = { REF };
	protected static char[][] REF_AND_DECL_CATEGORIES = { REF, FIELD_DECL };
	protected static char[][] DECL_CATEGORIES = { FIELD_DECL };

	public static char[] createIndexKey(String fieldName) {
		return fieldName.toCharArray();
	}

	public FieldPattern(boolean findDeclarations, boolean readAccess,
			boolean writeAccess, char[] name, char[] declaringQualification,
			char[] declaringSimpleName, char[] typeQualification,
			char[] typeSimpleName, int matchRule, IDLTKLanguageToolkit toolkit) {
		super(FIELD_PATTERN, findDeclarations, readAccess, writeAccess, name,
				matchRule, toolkit);
		this.declaringQualification = isCaseSensitive() ? declaringQualification
				: CharOperation.toLowerCase(declaringQualification);
		this.declaringSimpleName = isCaseSensitive() ? declaringSimpleName
				: CharOperation.toLowerCase(declaringSimpleName);
		this.typeQualification = isCaseSensitive() ? typeQualification
				: CharOperation.toLowerCase(typeQualification);
		this.typeSimpleName = (isCaseSensitive() || isCamelCase()) ? typeSimpleName
				: CharOperation.toLowerCase(typeSimpleName);
	}

	/*
	 * Instanciate a field pattern with additional information for generics
	 * search
	 */
	public FieldPattern(boolean findDeclarations, boolean readAccess,
			boolean writeAccess, char[] name, char[] declaringQualification,
			char[] declaringSimpleName, char[] typeQualification,
			char[] typeSimpleName, String typeSignature, int matchRule,
			IDLTKLanguageToolkit toolkit) {
		this(findDeclarations, readAccess, writeAccess, name,
				declaringQualification, declaringSimpleName, typeQualification,
				typeSimpleName, matchRule, toolkit);
	}

	@Override
	public void decodeIndexKey(char[] key) {
		this.name = key;
	}

	@Override
	public SearchPattern getBlankPattern() {
		return new FieldPattern(false, false, false, null, null, null, null,
				null, R_EXACT_MATCH | R_CASE_SENSITIVE, getToolkit());
	}

	@Override
	public char[] getIndexKey() {
		return this.name;
	}

	@Override
	public char[][] getIndexCategories() {
		if (this.findReferences)
			return this.findDeclarations || this.writeAccess ? REF_AND_DECL_CATEGORIES
					: REF_CATEGORIES;
		if (this.findDeclarations)
			return DECL_CATEGORIES;
		return CharOperation.NO_CHAR_CHAR;
	}

	@Override
	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		return true; // index key is not encoded so query results all match
	}

	@Override
	protected boolean mustResolve() {
		if (this.declaringSimpleName != null
				|| this.declaringQualification != null)
			return true;
		if (this.typeSimpleName != null || this.typeQualification != null)
			return true;
		return super.mustResolve();
	}

	@Override
	protected StringBuffer print(StringBuffer output) {
		if (this.findDeclarations) {
			output.append(this.findReferences ? "FieldCombinedPattern: " //$NON-NLS-1$
					: "FieldDeclarationPattern: "); //$NON-NLS-1$
		} else {
			output.append("FieldReferencePattern: "); //$NON-NLS-1$
		}
		if (declaringQualification != null)
			output.append(declaringQualification).append('.');
		if (declaringSimpleName != null)
			output.append(declaringSimpleName).append('.');
		else if (declaringQualification != null)
			output.append("*."); //$NON-NLS-1$
		if (name == null) {
			output.append("*"); //$NON-NLS-1$
		} else {
			output.append(name);
		}
		if (typeQualification != null)
			output.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
		else if (typeSimpleName != null)
			output.append(" --> "); //$NON-NLS-1$
		if (typeSimpleName != null)
			output.append(typeSimpleName);
		else if (typeQualification != null)
			output.append("*"); //$NON-NLS-1$
		return super.print(output);
	}
}
