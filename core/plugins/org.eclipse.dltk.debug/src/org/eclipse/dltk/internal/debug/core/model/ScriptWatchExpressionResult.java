package org.eclipse.dltk.internal.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.dltk.debug.core.eval.IScriptEvaluationResult;

public class ScriptWatchExpressionResult implements IWatchExpressionResult {

	private final IScriptEvaluationResult result;

	public ScriptWatchExpressionResult(IScriptEvaluationResult result) {
		this.result = result;
	}

	@Override
	public String[] getErrorMessages() {
		return result.getErrorMessages();
	}

	@Override
	public DebugException getException() {
		return result.getException();
	}

	@Override
	public String getExpressionText() {
		return result.getSnippet();
	}

	@Override
	public IValue getValue() {
		return result.getValue();
	}

	@Override
	public boolean hasErrors() {
		return result.hasErrors();
	}
}
