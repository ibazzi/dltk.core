/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.corext.callhierarchy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.ICalleeProcessor;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.search.IDLTKSearchScope;

class CalleeMethodWrapper extends MethodWrapper {
	private Comparator<MethodWrapper> fMethodWrapperComparator = (m1, m2) -> {
		CallLocation callLocation1 = m1.getMethodCall().getFirstCallLocation();
		CallLocation callLocation2 = m2.getMethodCall().getFirstCallLocation();

		if ((callLocation1 != null) && (callLocation2 != null)) {
			if (callLocation1.getStart() == callLocation2.getStart()) {
				return callLocation1.getEnd() - callLocation2.getEnd();
			}

			return callLocation1.getStart() - callLocation2.getStart();
		}

		return 0;
	};

	/**
	 * Constructor for CalleeMethodWrapper.
	 */
	public CalleeMethodWrapper(MethodWrapper parent, MethodCall methodCall) {
		super(parent, methodCall);
	}

	/*
	 * Returns the calls sorted after the call location
	 */
	@Override
	public MethodWrapper[] getCalls(IProgressMonitor progressMonitor) {
		MethodWrapper[] result = super.getCalls(progressMonitor);
		Arrays.sort(result, fMethodWrapperComparator);

		return result;
	}

	@Override
	protected String getTaskName() {
		return CallHierarchyMessages.CalleeMethodWrapper_taskname;
	}

	@Override
	protected MethodWrapper createMethodWrapper(MethodCall methodCall) {
		return new CalleeMethodWrapper(this, methodCall);
	}

	/**
	 * Find callees called from the current method.
	 *
	 */
	@Override
	protected Map findChildren(IProgressMonitor progressMonitor) {
		if (getMember().exists()
				&& getMember().getElementType() == IModelElement.METHOD) {
			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
					.getLanguageToolkit(getMember());
			if (toolkit != null) {
				IDLTKSearchScope scope = CallHierarchy.getDefault()
						.getSearchScope(toolkit);
				ICalleeProcessor processor = DLTKLanguageManager
						.createCalleeProcessor(toolkit.getNatureId(),
								(IMethod) getMember(), progressMonitor, scope);

				if (processor != null) {
					Map result = processor.doOperation();
					if (result != null) {
						CallSearchResultCollector collector = new CallSearchResultCollector();
						Set keys = result.keySet();
						Iterator i = keys.iterator();
						while (i.hasNext()) {
							SimpleReference e = (SimpleReference) i.next();
							IMethod[] calls = (IMethod[]) result.get(e);
							for (int j = 0; j < calls.length; ++j) {
								collector.addMember(getMember(), calls[j],
										e.sourceStart(), e.sourceEnd());
							}
						}
						return collector.getCallers();
					}
				}
			}
		}

		if (DLTKCore.DEBUG) {
			System.err.println(
					"TODO:CalleeMethodWrap findChildren not implemented..."); //$NON-NLS-1$
		}
		return new HashMap(0);
	}
}
