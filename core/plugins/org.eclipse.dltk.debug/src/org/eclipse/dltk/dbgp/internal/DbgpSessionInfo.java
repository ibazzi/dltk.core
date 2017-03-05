/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.dbgp.internal;

import java.net.URI;

import org.eclipse.dltk.dbgp.IDbgpSessionInfo;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;

public class DbgpSessionInfo implements IDbgpSessionInfo {
	private final String appId;

	private final String ideKey;

	private final String session;

	private final String threadId;

	private final String parentId;

	private final String language;

	private final URI fileUri;

	private DbgpException error;

	public DbgpSessionInfo(String appId, String ideKey, String session,
			String threadId, String parentId, String language, URI fileUri,
			DbgpException error) {
		super();
		this.appId = appId;
		this.ideKey = ideKey;
		this.session = session;
		this.threadId = threadId;
		this.parentId = parentId;
		this.language = language;
		this.fileUri = fileUri;
		this.error = error;
	}

	@Override
	public String getApplicationId() {
		return appId;
	}

	@Override
	public URI getFileUri() {
		return fileUri;
	}

	@Override
	public String getIdeKey() {
		return ideKey;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public String getParentAppId() {
		return parentId;
	}

	@Override
	public String getSession() {
		return session;
	}

	@Override
	public String getThreadId() {
		return threadId;
	}

	@Override
	public DbgpException getError() {
		return error;
	}

}
