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
package org.eclipse.dltk.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.dltk.debug.core.eval.IScriptEvaluationCommand;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.core.model.IScriptType;
import org.eclipse.dltk.debug.core.model.IScriptValue;

public class ScriptValueProxy implements IScriptValue {

	private final IScriptValue origin;

	public ScriptValueProxy(IScriptValue origin) {
		this.origin = origin;
	}

	@Override
	public IScriptEvaluationCommand createEvaluationCommand(
			String messageTemplate, IScriptThread thread) {
		return origin.createEvaluationCommand(messageTemplate, thread);
	}

	@Override
	public String getDetailsString() {
		return origin.getDetailsString();
	}

	@Override
	public String getEvalName() {
		return origin.getEvalName();
	}

	@Override
	public String getInstanceId() {
		return origin.getInstanceId();
	}

	@Override
	public String getMemoryAddress() {
		return origin.getMemoryAddress();
	}

	@Override
	public String getRawValue() {
		return origin.getRawValue();
	}

	@Override
	public IScriptType getType() {
		return origin.getType();
	}

	@Override
	public IVariable getVariable(int offset) throws DebugException {
		return origin.getVariable(offset);
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return origin.getReferenceTypeName();
	}

	@Override
	public String getValueString() throws DebugException {
		return origin.getValueString();
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return origin.getVariables();
	}

	@Override
	public boolean hasVariables() throws DebugException {
		return origin.hasVariables();
	}

	@Override
	public boolean isAllocated() throws DebugException {
		return origin.isAllocated();
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return origin.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		return origin.getLaunch();
	}

	@Override
	public String getModelIdentifier() {
		return origin.getModelIdentifier();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return origin.getAdapter(adapter);
	}

}
