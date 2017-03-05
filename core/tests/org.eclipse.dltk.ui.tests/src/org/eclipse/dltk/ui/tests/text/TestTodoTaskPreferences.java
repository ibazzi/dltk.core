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
package org.eclipse.dltk.ui.tests.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.compiler.task.ITodoTaskPreferences;
import org.eclipse.dltk.compiler.task.TodoTask;

class TestTodoTaskPreferences implements ITodoTaskPreferences {

	private final String[] tags;
	private final boolean caseSensitive;

	/**
	 * @param tags
	 * @param caseSensitive
	 */
	public TestTodoTaskPreferences(String[] tags, boolean caseSensitive) {
		this.tags = new String[tags.length];
		System.arraycopy(tags, 0, this.tags, 0, tags.length);
		this.caseSensitive = caseSensitive;
	}

	@Override
	public String[] getTagNames() {
		return tags;
	}

	@Override
	public List<TodoTask> getTaskTags() {
		final List<TodoTask> taskTags = new ArrayList<>();
		for (int i = 0; i < tags.length; ++i) {
			taskTags.add(new TodoTask(tags[i], TodoTask.PRIORITY_NORMAL));
		}
		return taskTags;
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
