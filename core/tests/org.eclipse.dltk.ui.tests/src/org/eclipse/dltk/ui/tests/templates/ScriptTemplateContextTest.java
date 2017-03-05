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
package org.eclipse.dltk.ui.tests.templates;

import static org.junit.Assert.assertEquals;

import org.eclipse.dltk.ui.templates.IScriptTemplateIndenter;
import org.eclipse.dltk.ui.templates.NopScriptTemplateIndenter;
import org.eclipse.dltk.ui.templates.TabExpandScriptTemplateIndenter;
import org.junit.Test;

public class ScriptTemplateContextTest {
	@Test
	public void testCalulateIndent() {
		assertEquals("", TestTemplateContext.calculateIndent(""));
		assertEquals("", TestTemplateContext.calculateIndent("if"));
		assertEquals("\t", TestTemplateContext.calculateIndent("\t" + "if"));
		assertEquals("\t\t", TestTemplateContext.calculateIndent("\t\t" + "if"));
		assertEquals("  ", TestTemplateContext.calculateIndent("  " + "if"));
		assertEquals("\t  ", TestTemplateContext.calculateIndent("\t  " + "if"));
	}

	private String tabExpandIndent(String line) {
		final IScriptTemplateIndenter indenter = new TabExpandScriptTemplateIndenter(
				4);
		final StringBuffer sb = new StringBuffer();
		indenter.indentLine(sb, "", line);
		return sb.toString();
	}
	@Test
	public void testTabExpandIndenter() {
		assertEquals("if", tabExpandIndent("if"));
		assertEquals("    if", tabExpandIndent("\t" + "if"));
		assertEquals("        if", tabExpandIndent("\t\t" + "if"));
	}

	private String nopIndent(String line) {
		final IScriptTemplateIndenter indenter = new NopScriptTemplateIndenter();
		final StringBuffer sb = new StringBuffer();
		indenter.indentLine(sb, "", line);
		return sb.toString();
	}
	@Test
	public void testNopIndenter() {
		assertEquals("if", nopIndent("if"));
		assertEquals("\t" + "if", nopIndent("\t" + "if"));
		assertEquals("\t\t" + "if", nopIndent("\t\t" + "if"));
	}
}
