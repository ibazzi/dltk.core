/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.dltk.dbgp.IDbgpProperty;
import org.eclipse.dltk.dbgp.commands.IDbgpPropertyCommands;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.core.ScriptDebugManager;
import org.eclipse.dltk.debug.core.eval.IScriptEvaluationCommand;
import org.eclipse.dltk.debug.core.eval.IScriptEvaluationEngine;
import org.eclipse.dltk.debug.core.model.AtomicScriptType;
import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.core.model.IScriptType;
import org.eclipse.dltk.debug.core.model.IScriptTypeFactory;
import org.eclipse.dltk.debug.core.model.IScriptValue;
import org.eclipse.dltk.internal.debug.core.eval.ScriptEvaluationCommand;
import org.eclipse.osgi.util.NLS;

public class ScriptValue extends ScriptDebugElement
		implements IScriptValue, IIndexedValue {

	static final IVariable[] NO_VARIABLES = new IVariable[0];

	private final IScriptType type;
	final IVariable[] variables;
	private IScriptStackFrame frame;
	private int pageSize;
	private String name;
	private String fullname;
	private String value;
	private String details;
	private boolean hasChildren;
	private String key;
	private String rawValue;
	private String address;

	public static IScriptValue createValue(IScriptStackFrame frame,
			IDbgpProperty property) {
		IScriptType type = createType(frame.getDebugTarget(), property);
		return new ScriptValue(frame, property, type);
	}

	private static IScriptType createType(IDebugTarget target,
			IDbgpProperty property) {
		IScriptType type = null;
		final String rawType = property.getType();

		final IScriptTypeFactory factory = ScriptDebugManager.getInstance()
				.getTypeFactoryByDebugModel(target.getModelIdentifier());
		if (factory != null) {
			type = factory.buildType(rawType);
		} else {
			type = new AtomicScriptType(rawType);
		}
		return type;
	}

	protected ScriptValue(IScriptStackFrame frame, IDbgpProperty property,
			IScriptType type) {
		this.frame = frame;
		this.type = type;

		this.key = property.getKey();
		this.name = property.getName();
		this.fullname = property.getEvalName();
		this.rawValue = property.getValue();
		this.value = null;
		this.hasChildren = property.hasChildren();
		this.pageSize = property.getPageSize();
		this.address = property.getAddress();

		final int childrenCount = property.getChildrenCount();
		if (childrenCount > 0) {
			this.variables = new IVariable[childrenCount];
			fillVariables(property.getPage(), property);
		} else {
			this.variables = NO_VARIABLES;
		}
	}

	private void loadPage(int page) throws DbgpException {
		IDbgpPropertyCommands commands = frame.getScriptThread()
				.getDbgpSession().getCoreCommands();
		IDbgpProperty pageProperty = commands.getProperty(page, fullname,
				frame.getLevel());
		fillVariables(page, pageProperty);
		final int endIndex = Math.min((page + 1) * pageSize, variables.length);
		for (int i = page * pageSize; i < endIndex; ++i) {
			if (variables[i] == null) {
				variables[i] = new UnknownVariable(frame, this, i);
			}
		}
	}

	private void fillVariables(int page, IDbgpProperty pageProperty) {
		int offset = getPageOffset(page);
		IDbgpProperty[] properties = pageProperty.getAvailableChildren();
		final int size = Math.min(properties.length, variables.length - offset);
		if (size != properties.length) {
			DLTKDebugPlugin.logWarning(
					NLS.bind(Messages.AvailableChildrenExceedsVariableLength,
							name),
					null);
		}
		if (size > 0) {
			for (int i = 0; i < size; ++i) {
				IDbgpProperty p = properties[i];
				variables[offset + i] = new ScriptVariable(frame, p.getName(),
						p);
			}
			Arrays.sort(this.variables, offset, offset + size,
					ScriptDebugManager.getInstance()
							.getVariableNameComparatorByDebugModel(
									getDebugTarget().getModelIdentifier()));
		}
		Assert.isLegal(pageSize > 0 || properties.length == variables.length);
	}

	private int getPageOffset(int page) {
		if (pageSize <= 0)
			pageSize = frame.getScriptThread().getPropertyPageSize();

		if (pageSize <= 0)
			return 0;
		return page * pageSize;
	}

	private int getPageForOffset(int offset) {
		Assert.isLegal(pageSize > 0);
		return offset / pageSize;
	}

	@Override
	public String getReferenceTypeName() {
		return getType().getName();
	}

	@Override
	public String getValueString() {
		if (value == null || value.length() == 0) {
			value = type.formatValue(this);
		}
		return value;
	}

	@Override
	public String getDetailsString() {
		if (details == null || details.length() == 0) {
			details = type.formatDetails(this);
		}

		return details;
	}

	@Override
	public String getRawValue() {
		return rawValue;
	}

	@Override
	public String getEvalName() {
		return fullname;
	}

	@Override
	public boolean hasVariables() {
		return hasChildren;
	}

	@Override
	public boolean isAllocated() {
		return true;
	}

	@Override
	public String toString() {
		return getValueString();
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return frame.getDebugTarget();
	}

	@Override
	public String getInstanceId() {
		return key;
	}

	@Override
	public IScriptType getType() {
		return type;
	}

	@Override
	public IScriptEvaluationCommand createEvaluationCommand(
			String messageTemplate, IScriptThread thread) {
		IScriptEvaluationEngine engine = thread.getEvaluationEngine();

		String pattern = "(%variable%)"; //$NON-NLS-1$
		String evalName = getEvalName();
		if (messageTemplate.indexOf(pattern) != -1) {
			String snippet = replacePattern(messageTemplate, pattern, evalName);
			return new ScriptEvaluationCommand(engine, snippet, frame);
		}
		DLTKDebugPlugin.logWarning(NLS.bind(
				Messages.ScriptValue_detailFormatterRequiredToContainIdentifier,
				pattern), null);
		return new ScriptEvaluationCommand(engine, evalName, frame);
	}

	private static String replacePattern(String messageTemplate, String pattern,
			String evalName) {
		String result = messageTemplate;
		while (result.indexOf(pattern) != -1) {
			int pos = result.indexOf(pattern);
			result = result.substring(0, pos) + evalName
					+ result.substring(pos + pattern.length(), result.length());
		}
		return result;
	}

	@Override
	public int getInitialOffset() {
		return 0;
	}

	@Override
	public int getSize() {
		return variables.length;
	}

	@Override
	public IVariable getVariable(int offset) throws DebugException {
		try {
			if (variables[offset] == null) {
				loadPage(getPageForOffset(offset));
			}
			return variables[offset];
		} catch (DbgpException e) {
			throw wrapDbgpException(
					NLS.bind(Messages.ScriptValue_unableToLoadChildrenOf, name),
					e);
		}
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		return getVariables(0, getSize());
	}

	@Override
	public IVariable[] getVariables(int offset, int length)
			throws DebugException {
		IVariable[] variables = new IVariable[length];
		for (int i = 0; i < length; i++) {
			variables[i] = getVariable(offset + i);
		}
		return variables;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IIndexedValue.class && type.isCollection()) {
			return (T) this;
		}
		return super.getAdapter(adapter);
	}

	public String getName() {
		return name;
	}

	@Override
	public String getMemoryAddress() {
		return address;
	}

	/**
	 * Tests that some of the children are already created.
	 *
	 * @return
	 */
	protected boolean hasChildrenValuesLoaded() {
		for (int i = 0; i < variables.length; ++i) {
			if (variables[i] != null) {
				return true;
			}
		}
		return false;
	}
}
