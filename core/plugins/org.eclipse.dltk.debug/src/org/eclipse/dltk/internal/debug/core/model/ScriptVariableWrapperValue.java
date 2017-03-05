/*******************************************************************************
 * Copyright (c) 2008, 2017 xored software, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.dltk.debug.core.eval.IScriptEvaluationCommand;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.core.model.IScriptType;
import org.eclipse.dltk.debug.core.model.IScriptValue;

final class ScriptVariableWrapperValue implements IScriptValue {

	private final ScriptVariableWrapper owner;

	ScriptVariableWrapperValue(ScriptVariableWrapper scriptVariableWrapper) {
		this.owner = scriptVariableWrapper;
	}

	@Override
	public String getReferenceTypeName() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getRawValue() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getValueString() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return this.owner.getChildren();
	}

	@Override
	public boolean hasVariables() {
		return this.owner.hasChildren();
	}

	@Override
	public boolean isAllocated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return owner.target;
	}

	@Override
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	@Override
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public IScriptEvaluationCommand createEvaluationCommand(
			String messageTemplate, IScriptThread thread) {
		return null;
	}

	@Override
	public String getEvalName() {
		return null;
	}

	@Override
	public String getInstanceId() {
		return null;
	}

	@Override
	public IScriptType getType() {
		return this.owner.getType();
	}

	@Override
	public IVariable getVariable(int offset) {
		return null;
	}

	@Override
	public String getMemoryAddress() {
		return null;
	}

	@Override
	public String getDetailsString() {
		return getValueString();
	}
}
