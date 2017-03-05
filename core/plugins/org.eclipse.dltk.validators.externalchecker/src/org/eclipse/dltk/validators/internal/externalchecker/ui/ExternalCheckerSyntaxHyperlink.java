package org.eclipse.dltk.validators.internal.externalchecker.ui;

import org.eclipse.dltk.validators.core.IValidatorProblem;
import org.eclipse.ui.console.TextConsole;

public class ExternalCheckerSyntaxHyperlink extends ExternalCheckerGenericHyperlink {

	private IValidatorProblem problem;

	public ExternalCheckerSyntaxHyperlink(TextConsole console, IValidatorProblem problem) {
		super(console);
		this.problem = problem;
	}

	@Override
	protected String getFileName() {
		return problem.getFileName();
	}

	@Override
	protected int getLineNumber() {
		return problem.getLineNumber();
	}
}
