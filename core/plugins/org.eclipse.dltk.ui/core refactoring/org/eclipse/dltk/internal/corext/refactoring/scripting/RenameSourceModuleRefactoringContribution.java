/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *

 *******************************************************************************/
package org.eclipse.dltk.internal.corext.refactoring.scripting;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.internal.corext.refactoring.ScriptRefactoringContribution;
import org.eclipse.dltk.internal.corext.refactoring.rename.RenameSourceModuleProcessor;
import org.eclipse.dltk.internal.corext.refactoring.rename.ScriptRenameRefactoring;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;


/**
 * Refactoring contribution for the rename compilation unit refactoring.
 *
 *
 */
public final class RenameSourceModuleRefactoringContribution extends ScriptRefactoringContribution {

	@Override
	public Refactoring createRefactoring(final RefactoringDescriptor descriptor) throws CoreException {
		return new ScriptRenameRefactoring(new RenameSourceModuleProcessor(null));
	}
}
