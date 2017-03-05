/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal.commands;

import org.eclipse.dltk.dbgp.DbgpBaseCommands;
import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.IDbgpCommunicator;
import org.eclipse.dltk.dbgp.IDbgpProperty;
import org.eclipse.dltk.dbgp.commands.IDbgpExtendedCommands;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;
import org.eclipse.dltk.dbgp.internal.utils.DbgpXmlEntityParser;
import org.eclipse.dltk.dbgp.internal.utils.DbgpXmlParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DbgpExtendedCommands extends DbgpBaseCommands
		implements IDbgpExtendedCommands {

	private IDbgpProperty parseResponse(Element response) {
		if (DbgpXmlParser.parseSuccess(response)) {
			NodeList list = response
					.getElementsByTagName(DbgpXmlEntityParser.TAG_PROPERTY);
			return DbgpXmlEntityParser.parseProperty((Element) list.item(0));
		}
		return null;
	}

	public DbgpExtendedCommands(IDbgpCommunicator communicator)
			throws DbgpException {
		super(communicator);
	}

	@Override
	public boolean makeBreak() throws DbgpException {
		return DbgpXmlParser
				.parseSuccess(communicate(createAsyncRequest(BREAK_COMMAND)));
	}

	@Override
	public boolean configureStdin(int value) throws DbgpException {
		DbgpRequest request = createRequest(STDIN_COMMAND);
		request.addOption("-c", value); //$NON-NLS-1$
		return DbgpXmlParser.parseSuccess(communicate(request));
	}

	@Override
	public boolean sendStdin(String data) throws DbgpException {
		DbgpRequest request = createRequest(STDIN_COMMAND);
		request.setData(data);
		return DbgpXmlParser.parseSuccess(communicate(request));
	}

	@Override
	public IDbgpProperty evaluate(String snippet) throws DbgpException {
		DbgpRequest request = createRequest(EVAL_COMMAND);
		request.setData(snippet);
		return parseResponse(communicate(request));
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public IDbgpProperty evaluate(String snippet, int depth)
			throws DbgpException {
		return evaluate(snippet);
	}

	@Override
	public IDbgpProperty expression(String expression) throws DbgpException {
		DbgpRequest request = createRequest(EXPR_COMMAND);
		request.setData(expression);
		return parseResponse(communicate(request));
	}

	@Override
	public IDbgpProperty execute(String code) throws DbgpException {
		DbgpRequest request = createRequest(EXEC_COMMAND);
		request.setData(code);
		return parseResponse(communicate(request));
	}
}
