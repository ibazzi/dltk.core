/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.callhierarchy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.dltk.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;


public class DeferredMethodWrapper extends MethodWrapperWorkbenchAdapter implements IDeferredWorkbenchAdapter {
    private final CallHierarchyContentProvider fProvider;

    /**
     * A simple job scheduling rule for serializing jobs that shouldn't be run
     * concurrently.
     */
    private class BatchSimilarSchedulingRule implements ISchedulingRule {
        public String id;

        public BatchSimilarSchedulingRule(String id) {
            this.id = id;
        }

        @Override
		public boolean isConflicting(ISchedulingRule rule) {
            if (rule instanceof BatchSimilarSchedulingRule) {
                return ((BatchSimilarSchedulingRule) rule).id.equals(id);
            }
            return false;
        }

        @Override
		public boolean contains(ISchedulingRule rule) {
            return this == rule;
        }
    }

    DeferredMethodWrapper(CallHierarchyContentProvider provider, MethodWrapper methodWrapper) {
    	super(methodWrapper);
        this.fProvider = provider;
    }

    private Object getCalls(IProgressMonitor monitor) {
        return getMethodWrapper().getCalls(monitor);
    }

    @Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
        try {
            fProvider.startFetching();
            DeferredMethodWrapper methodWrapper = (DeferredMethodWrapper) object;
            collector.add((Object[]) methodWrapper.getCalls(monitor), monitor);
            collector.done();
        } catch (OperationCanceledException e) {
            collector.add(new Object[] { TreeTermination.SEARCH_CANCELED }, monitor);
        } catch (Exception e) {
            DLTKUIPlugin.log(e);
        } finally {
            fProvider.doneFetching();
        }
    }

    @Override
	public boolean isContainer() {
        return true;
    }

    @Override
	public ISchedulingRule getRule(Object o) {
        return new BatchSimilarSchedulingRule("org.eclipse.dltk.ui.callhierarchy.methodwrapper"); //$NON-NLS-1$
    }

    @Override
	public Object[] getChildren(Object o) {
        return this.fProvider.fetchChildren(((DeferredMethodWrapper) o).getMethodWrapper());
    }

    /**
     * Returns an object which is an instance of the given class associated
     * with this object. Returns <code>null</code> if no such object can be
     * found.
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IDeferredWorkbenchAdapter.class)
            return this;
        return null;
    }
}
