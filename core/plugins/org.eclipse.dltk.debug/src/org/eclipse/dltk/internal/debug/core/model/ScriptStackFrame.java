/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.debug.core.model;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.dbgp.IDbgpProperty;
import org.eclipse.dltk.dbgp.IDbgpStackLevel;
import org.eclipse.dltk.dbgp.commands.IDbgpContextCommands;
import org.eclipse.dltk.dbgp.exceptions.DbgpDebuggingEngineException;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.core.IScriptVariableContainer;
import org.eclipse.dltk.debug.core.ScriptDebugManager;
import org.eclipse.dltk.debug.core.model.IRefreshableScriptVariable;
import org.eclipse.dltk.debug.core.model.IScriptStack;
import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.core.model.IScriptVariable;
import org.eclipse.dltk.debug.core.model.ISourceOffsetLookup;
import org.eclipse.osgi.util.NLS;

public class ScriptStackFrame extends ScriptDebugElement
		implements IScriptStackFrame {

	private final IScriptThread thread;
	private IDbgpStackLevel level;
	private final IScriptStack stack;

	private ScriptVariableContainer variables = null;
	private boolean needRefreshVariables = false;

	protected static IScriptVariable[] readVariables(
			ScriptStackFrame parentFrame, int contextId,
			IDbgpContextCommands commands) throws DbgpException {

		try {
			IDbgpProperty[] properties = commands
					.getContextProperties(parentFrame.getLevel(), contextId);

			IScriptVariable[] variables = new IScriptVariable[properties.length];

			// Workaround for bug 215215
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=215215
			// Remove this code when Tcl active state debugger fixed
			Set duplicates = findDuplicateNames(properties);

			for (int i = 0; i < properties.length; ++i) {
				IDbgpProperty property = properties[i];
				String name = property.getName();
				if (duplicates.contains(name)) {
					name = property.getEvalName();
				}
				variables[i] = new ScriptVariable(parentFrame, name, property);
			}

			return variables;
		} catch (DbgpDebuggingEngineException e) {
			if (DLTKCore.DEBUG) {
				e.printStackTrace();
			}
			return new IScriptVariable[0];
		}
	}

	private static Set findDuplicateNames(IDbgpProperty[] properties) {
		final Set duplicates = new HashSet();
		final Set alreadyExsisting = new HashSet();
		for (int i = 0; i < properties.length; ++i) {
			final IDbgpProperty property = properties[i];
			final String name = property.getName();
			if (!alreadyExsisting.add(name)) {
				duplicates.add(name);
			}
		}
		return duplicates;
	}

	/**
	 * Return null in case suspend more is no more active during calculation of
	 * variables.
	 *
	 * @return
	 * @throws DbgpException
	 */
	protected ScriptVariableContainer readAllVariables() throws DbgpException {
		final IDbgpContextCommands commands = thread.getDbgpSession()
				.getCoreCommands();

		// TODO: Until more sequence approach will be implemented
		if (!thread.isSuspended()) {
			return null;
		}

		final ScriptVariableContainer result = new ScriptVariableContainer();

		final Map names = commands.getContextNames(getLevel());
		if (thread.retrieveLocalVariables()
				&& names.containsKey(
						Integer.valueOf(IDbgpContextCommands.LOCAL_CONTEXT_ID))
				&& thread.isSuspended()) {
			result.locals = readVariables(this,
					IDbgpContextCommands.LOCAL_CONTEXT_ID, commands);
		}
		if (thread.retrieveGlobalVariables()
				&& names.containsKey(
						Integer.valueOf(IDbgpContextCommands.GLOBAL_CONTEXT_ID))
				&& thread.isSuspended()) {
			result.globals = readVariables(this,
					IDbgpContextCommands.GLOBAL_CONTEXT_ID, commands);
		}
		if (thread.retrieveClassVariables()
				&& names.containsKey(
						Integer.valueOf(IDbgpContextCommands.CLASS_CONTEXT_ID))
				&& thread.isSuspended()) {
			result.classes = readVariables(this,
					IDbgpContextCommands.CLASS_CONTEXT_ID, commands);
		}
		// TODO: Until more sequence approach will be implemented
		if (!thread.isSuspended()) {
			return null;
		}
		return result;
	}

	private static class ScriptVariableContainer {
		IVariable[] locals = null;
		IVariable[] globals = null;
		IVariable[] classes = null;
		ScriptVariableWrapper globalsWrapper = null;
		ScriptVariableWrapper classesWrapper = null;

		ScriptVariableContainer sort(IDebugTarget target) {
			final Comparator variableComparator = ScriptDebugManager
					.getInstance().getVariableNameComparatorByDebugModel(
							target.getModelIdentifier());
			if (locals != null) {
				Arrays.sort(locals, variableComparator);
			}
			if (globals != null) {
				Arrays.sort(globals, variableComparator);
			}
			if (classes != null) {
				Arrays.sort(classes, variableComparator);
			}
			return this;
		}

		private int size() {
			int size = 0;
			if (locals != null) {
				size += locals.length;
			}
			if (globals != null) {
				++size;
			}
			if (classes != null) {
				++size;
			}
			return size;
		}

		IScriptVariable[] toArray(IDebugTarget target) {
			final int size = size();
			final IScriptVariable[] result = new IScriptVariable[size];
			if (size != 0) {
				int index = 0;
				if (globals != null) {
					if (globalsWrapper == null) {
						globalsWrapper = new ScriptVariableWrapper(target,
								Messages.ScriptStackFrame_globalVariables,
								globals,
								IScriptVariableContainer.ContainerKind.Global);
					} else {
						globalsWrapper.refreshValue(globals);
					}
					result[index++] = globalsWrapper;
				}
				if (classes != null) {
					if (classesWrapper == null) {
						classesWrapper = new ScriptVariableWrapper(target,
								Messages.ScriptStackFrame_classVariables,
								classes,
								IScriptVariableContainer.ContainerKind.Class);
					} else {
						classesWrapper.refreshValue(classes);
					}
					result[index++] = classesWrapper;
				}
				if (locals != null) {
					System.arraycopy(locals, 0, result, index, locals.length);
					index += locals.length;
				}
			}
			return result;
		}

		/**
		 * @return
		 */
		public boolean hasVariables() {
			return locals != null && locals.length != 0 || classes != null
					|| globals != null;
		}

		/**
		 * @param varName
		 * @return
		 * @throws DebugException
		 */
		public IVariable findVariable(String varName) throws DebugException {
			if (locals != null) {
				final IVariable variable = findVariable(varName, locals);
				if (variable != null) {
					return variable;
				}
			}
			if (globals != null) {
				final IVariable variable = findVariable(varName, globals);
				if (variable != null) {
					return variable;
				}
			}
			return null;
		}

		private static IVariable findVariable(String varName, IVariable[] vars)
				throws DebugException {
			for (int i = 0; i < vars.length; i++) {
				final IVariable var = vars[i];
				if (var.getName().equals(varName)) {
					return var;
				}
			}
			return null;
		}
	}

	public ScriptStackFrame(IScriptStack stack, IDbgpStackLevel stackLevel) {
		this.stack = stack;
		this.thread = stack.getThread();
		this.level = stackLevel;
	}

	public synchronized void updateVariables() {
		this.variables = null;
	}

	@Override
	public IScriptStack getStack() {
		return stack;
	}

	/**
	 * @return
	 * @deprecated use #getSourceURI()
	 */
	@Deprecated
	public URI getFileName() {
		return level.getFileURI();
	}

	private static final int MULTI_LINE_COUNT = 2;

	@Override
	public int getCharStart() throws DebugException {
		final int beginLine = level.getBeginLine();
		if (beginLine > 0) {
			final int endLine = level.getEndLine();
			if (endLine > 0 && endLine >= beginLine) {
				final ISourceOffsetLookup offsetLookup = DLTKDebugPlugin
						.getSourceOffsetLookup();
				if (offsetLookup != null) {
					return offsetLookup.calculateOffset(this, beginLine,
							level.getBeginColumn(), false);
				}
			}
		}
		return -1;
	}

	@Override
	public int getCharEnd() throws DebugException {
		final int endLine = level.getEndLine();
		if (endLine > 0) {
			final int beginLine = level.getBeginLine();
			if (beginLine > 0 && endLine >= beginLine) {
				final ISourceOffsetLookup offsetLookup = DLTKDebugPlugin
						.getSourceOffsetLookup();
				if (offsetLookup != null) {
					if (endLine < beginLine + MULTI_LINE_COUNT) {
						final int offset = offsetLookup.calculateOffset(this,
								endLine, level.getEndColumn(), true);
						if (offset >= 0) {
							return offset + 1;
						}
					} else {
						final int offset = offsetLookup.calculateOffset(this,
								beginLine, -1, true);
						if (offset >= 0) {
							return offset + 1;
						}
					}
				}
			}
		}
		return -1;
	}

	@Override
	public int getLineNumber() {
		return level.getLineNumber();
	}

	@Override
	public int getMethodOffset() {
		return level.getMethodOffset();
	}

	@Override
	public String getMethodName() {
		return level.getMethodName();
	}

	@Override
	public int getBeginLine() {
		return level.getBeginLine();
	}

	@Override
	public int getBeginColumn() {
		return level.getBeginColumn();
	}

	@Override
	public int getEndLine() {
		return level.getEndLine();
	}

	@Override
	public int getEndColumn() {
		return level.getEndColumn();
	}

	@Override
	public String getWhere() {
		return level.getWhere().trim();
	}

	@Override
	public String getName() throws DebugException {
		String name = level.getWhere().trim();

		if (name == null || name.length() == 0) {
			name = toString();
		}

		name += " (" + level.getFileURI().getPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

		return name;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return new IRegisterGroup[0];
	}

	@Override
	public IThread getThread() {
		return thread;
	}

	@Override
	public synchronized boolean hasVariables() throws DebugException {
		checkVariablesAvailable();
		if (variables == null) {
			return false;
		}
		return variables.hasVariables();
	}

	private synchronized void checkVariablesAvailable() throws DebugException {
		if (!thread.isSuspended()) {
			// Do not do variables lookup if not suspended, since it could
			// become in running/stepping state until we get here.
			return;
		}
		try {
			if (variables == null) {
				variables = readAllVariables();
				if (variables != null) {
					variables.sort(getDebugTarget());
				}
			} else if (needRefreshVariables) {
				try {
					refreshVariables();
				} finally {
					needRefreshVariables = false;
				}
			}
		} catch (DbgpException e) {
			variables = new ScriptVariableContainer();
			final Status status = new Status(IStatus.ERROR,
					DLTKDebugPlugin.PLUGIN_ID,
					Messages.ScriptStackFrame_unableToLoadVariables, e);
			DLTKDebugPlugin.log(status);
			throw new DebugException(status);
		}
	}

	/**
	 * @throws DebugException
	 * @throws DbgpException
	 */
	private void refreshVariables() throws DebugException, DbgpException {
		final ScriptVariableContainer newVars = readAllVariables();
		if (newVars == null) {
			// No need to refresh, refresh will happen in next
			variables = null;
			return;
		}
		newVars.sort(getDebugTarget());
		variables.locals = refreshVariables(newVars.locals, variables.locals);
		variables.globals = refreshVariables(newVars.globals,
				variables.globals);
		variables.classes = refreshVariables(newVars.classes,
				variables.classes);
	}

	/**
	 * @param newVars
	 * @param oldVars
	 * @return
	 * @throws DebugException
	 */
	static IVariable[] refreshVariables(IVariable[] newVars,
			IVariable[] oldVars) throws DebugException {
		if (oldVars != null) {
			final Map map = new HashMap();
			for (int i = 0; i < oldVars.length; ++i) {
				final IVariable variable = oldVars[i];
				if (variable instanceof IRefreshableScriptVariable) {
					map.put(variable.getName(), variable);
				}
			}
			if (newVars != null) {
				for (int i = 0; i < newVars.length; ++i) {
					final IVariable variable = newVars[i];
					final IRefreshableScriptVariable old;
					old = (IRefreshableScriptVariable) map
							.get(variable.getName());
					if (old != null) {
						newVars[i] = old.refreshVariable(variable);
					}
				}
			}
		}
		return newVars;
	}

	@Override
	public synchronized IVariable[] getVariables() throws DebugException {
		checkVariablesAvailable();
		if (variables != null) {
			return variables.toArray(getDebugTarget());
		}
		return new IVariable[0];
	}

	// IStep
	@Override
	public boolean canStepInto() {
		return thread.canStepInto();
	}

	@Override
	public boolean canStepOver() {
		return thread.canStepOver();
	}

	@Override
	public boolean canStepReturn() {
		return thread.canStepReturn();
	}

	@Override
	public boolean isStepping() {
		return thread.isStepping();
	}

	@Override
	public void stepInto() throws DebugException {
		thread.stepInto();
	}

	@Override
	public void stepOver() throws DebugException {
		thread.stepOver();
	}

	@Override
	public void stepReturn() throws DebugException {
		thread.stepReturn();
	}

	// ISuspenResume
	@Override
	public boolean canResume() {
		return thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return thread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		thread.suspend();
	}

	// ITerminate
	@Override
	public boolean canTerminate() {
		return thread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return thread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		thread.terminate();
	}

	// IDebugElement
	@Override
	public IDebugTarget getDebugTarget() {
		return thread.getDebugTarget();
	}

	@Override
	public synchronized IScriptVariable findVariable(String varName)
			throws DebugException {
		checkVariablesAvailable();
		if (variables != null) {
			return (IScriptVariable) variables.findVariable(varName);
		}
		return null;
	}

	@Override
	public int getLevel() {
		return level.getLevel();
	}

	@Override
	public String toString() {
		return NLS.bind(Messages.ScriptStackFrame_stackFrame,
				Integer.valueOf(level.getLevel()));
	}

	@Override
	public String getSourceLine() {
		return level.getWhere();
	}

	@Override
	public URI getSourceURI() {
		return level.getFileURI();
	}

	@Override
	public IScriptThread getScriptThread() {
		return (IScriptThread) getThread();
	}

	/**
	 * @param frame
	 * @param depth
	 * @return
	 */
	public ScriptStackFrame bind(IDbgpStackLevel newLevel) {
		if (level.isSameMethod(newLevel)) {
			level = newLevel;
			needRefreshVariables = true;
			return this;
		}
		return new ScriptStackFrame(stack, newLevel);
	}
}
