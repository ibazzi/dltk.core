package org.eclipse.dltk.debug.ui;

import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.TextConsole;

public abstract class ScriptDebugConsoleTracker
		implements IPatternMatchListener {
	protected TextConsole console;

	public ScriptDebugConsoleTracker() {
		super();
	}

	@Override
	public void connect(TextConsole console) {
		this.console = console;
	}

	@Override
	public void disconnect() {
		console = null;
	}

	protected TextConsole getConsole() {
		return console;
	}

	@Override
	public int getCompilerFlags() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLineQualifier() {
		return null;
	}
}
