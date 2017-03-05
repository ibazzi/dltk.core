/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal.commands;

import java.net.URI;
import java.util.Map;

import org.eclipse.dltk.dbgp.IDbgpCommunicator;
import org.eclipse.dltk.dbgp.IDbgpFeature;
import org.eclipse.dltk.dbgp.IDbgpProperty;
import org.eclipse.dltk.dbgp.IDbgpStackLevel;
import org.eclipse.dltk.dbgp.IDbgpStatus;
import org.eclipse.dltk.dbgp.breakpoints.DbgpBreakpointConfig;
import org.eclipse.dltk.dbgp.breakpoints.IDbgpBreakpoint;
import org.eclipse.dltk.dbgp.commands.IDbgpBreakpointCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpContextCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpContinuationCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpDataTypeCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpFeatureCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpPropertyCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpSourceCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpStackCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpStatusCommands;
import org.eclipse.dltk.dbgp.commands.IDbgpStreamCommands;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;

public class DbgpCoreCommands implements IDbgpCoreCommands {

	private final IDbgpFeatureCommands featureCommands;

	private final IDbgpStatusCommands statusCommands;

	private final IDbgpBreakpointCommands breakpointCommands;

	private final IDbgpSourceCommands sourceCommands;

	private final IDbgpContextCommands contextCommands;

	private final IDbgpStackCommands stackCommands;

	private final IDbgpContinuationCommands continuationCommands;

	private final IDbgpStreamCommands streamCommands;

	private final IDbgpDataTypeCommands dataTypeCommands;

	private final IDbgpPropertyCommands propertyCommands;

	public DbgpCoreCommands(IDbgpCommunicator communicator) {
		this.featureCommands = new DbgpFeatureCommands(communicator);
		this.statusCommands = new DbgpStatusCommands(communicator);
		this.breakpointCommands = new DbgpBreakpointCommands(communicator);
		this.sourceCommands = new DbgpSourceCommands(communicator);
		this.contextCommands = new DbgpContextCommands(communicator);
		this.stackCommands = new DbgpStackCommands(communicator);
		this.continuationCommands = new DbgpContinuationCommands(communicator);
		this.streamCommands = new DbgpStreamCommands(communicator);
		this.propertyCommands = new DbgpPropertyCommands(communicator);
		this.dataTypeCommands = new DbgpDataTypeCommands(communicator);
	}

	@Override
	public IDbgpFeature getFeature(String featureName) throws DbgpException {
		return featureCommands.getFeature(featureName);
	}

	@Override
	public boolean setFeature(String featureName, String featureValue)
			throws DbgpException {
		return featureCommands.setFeature(featureName, featureValue);
	}

	@Override
	public IDbgpBreakpoint getBreakpoint(String id) throws DbgpException {
		return breakpointCommands.getBreakpoint(id);
	}

	@Override
	public IDbgpBreakpoint[] getBreakpoints() throws DbgpException {
		return breakpointCommands.getBreakpoints();
	}

	@Override
	public void removeBreakpoint(String id) throws DbgpException {
		breakpointCommands.removeBreakpoint(id);
	}

	@Override
	public String setCallBreakpoint(URI uri, String function,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setCallBreakpoint(uri, function, info);
	}

	@Override
	public String setConditionalBreakpoint(URI uri, DbgpBreakpointConfig info)
			throws DbgpException {
		return breakpointCommands.setConditionalBreakpoint(uri, info);
	}

	@Override
	public String setConditionalBreakpoint(URI uri, int lineNumber,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setConditionalBreakpoint(uri, lineNumber,
				info);
	}

	@Override
	public String setExceptionBreakpoint(String exception,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setExceptionBreakpoint(exception, info);
	}

	@Override
	public String setLineBreakpoint(URI uri, int lineNumber,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setLineBreakpoint(uri, lineNumber, info);
	}

	@Override
	public String setReturnBreakpoint(URI uri, String function,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setReturnBreakpoint(uri, function, info);
	}

	@Override
	public String setWatchBreakpoint(URI uri, int line,
			DbgpBreakpointConfig info) throws DbgpException {
		return breakpointCommands.setWatchBreakpoint(uri, line, info);
	}

	@Override
	public void updateBreakpoint(String id, DbgpBreakpointConfig config)
			throws DbgpException {
		breakpointCommands.updateBreakpoint(id, config);
	}

	@Override
	public IDbgpStatus detach() throws DbgpException {
		return continuationCommands.detach();
	}

	@Override
	public IDbgpStatus run() throws DbgpException {
		return continuationCommands.run();
	}

	@Override
	public IDbgpStatus stepInto() throws DbgpException {
		return continuationCommands.stepInto();
	}

	@Override
	public IDbgpStatus stepOut() throws DbgpException {
		return continuationCommands.stepOut();
	}

	@Override
	public IDbgpStatus stepOver() throws DbgpException {
		return continuationCommands.stepOver();
	}

	@Override
	public IDbgpStatus stop() throws DbgpException {
		return continuationCommands.stop();
	}

	@Override
	public Map getTypeMap() throws DbgpException {
		return dataTypeCommands.getTypeMap();
	}

	@Override
	public String getSource(URI uri) throws DbgpException {
		return sourceCommands.getSource(uri);
	}

	@Override
	public String getSource(URI uri, int beginLine) throws DbgpException {
		return sourceCommands.getSource(uri, beginLine);
	}

	@Override
	public String getSource(URI uri, int beginLine, int endLine)
			throws DbgpException {
		return sourceCommands.getSource(uri, beginLine, endLine);
	}

	@Override
	public IDbgpStatus getStatus() throws DbgpException {
		return statusCommands.getStatus();
	}

	@Override
	public IDbgpStackLevel getStackLevel(int stackDepth) throws DbgpException {
		return stackCommands.getStackLevel(stackDepth);
	}

	@Override
	public IDbgpStackLevel[] getStackLevels() throws DbgpException {
		return stackCommands.getStackLevels();
	}

	@Override
	public int getStackDepth() throws DbgpException {
		return stackCommands.getStackDepth();
	}

	@Override
	public Map getContextNames(int stackDepth) throws DbgpException {
		return contextCommands.getContextNames(stackDepth);
	}

	@Override
	public IDbgpProperty[] getContextProperties(int stackDepth)
			throws DbgpException {
		return contextCommands.getContextProperties(stackDepth);
	}

	@Override
	public IDbgpProperty[] getContextProperties(int stackDepth, int contextId)
			throws DbgpException {
		return contextCommands.getContextProperties(stackDepth, contextId);
	}

	@Override
	public boolean configureStderr(int value) throws DbgpException {
		return streamCommands.configureStderr(value);
	}

	@Override
	public boolean configureStdout(int value) throws DbgpException {
		return streamCommands.configureStdout(value);
	}

	@Override
	public IDbgpProperty getProperty(String name) throws DbgpException {
		return propertyCommands.getProperty(name);
	}

	@Override
	public IDbgpProperty getProperty(String name, int stackDepth)
			throws DbgpException {
		return propertyCommands.getProperty(name, stackDepth);
	}

	@Override
	public IDbgpProperty getProperty(String name, int stackDepth, int contextId)
			throws DbgpException {
		return propertyCommands.getProperty(name, stackDepth, contextId);
	}

	@Override
	public boolean setProperty(IDbgpProperty property) throws DbgpException {
		return propertyCommands.setProperty(property);
	}

	@Override
	public boolean setProperty(String name, int stackDepth, String value)
			throws DbgpException {
		return propertyCommands.setProperty(name, stackDepth, value);
	}

	@Override
	public IDbgpProperty getPropertyByKey(String name, String key)
			throws DbgpException {
		return propertyCommands.getPropertyByKey(name, key);
	}

	@Override
	public IDbgpProperty getProperty(int page, String name, int stackDepth)
			throws DbgpException {
		return propertyCommands.getProperty(page, name, stackDepth);
	}

}
