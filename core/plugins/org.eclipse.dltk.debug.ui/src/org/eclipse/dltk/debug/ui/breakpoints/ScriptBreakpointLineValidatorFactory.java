package org.eclipse.dltk.debug.ui.breakpoints;

public class ScriptBreakpointLineValidatorFactory {
	public static final IScriptBreakpointLineValidator NON_EMPTY_VALIDATOR = (
			line, number) -> line.trim().length() > 0;

	public static IScriptBreakpointLineValidator createNonEmptyNoCommentValidator(
			final String commentPrefix) {
		return (line, number) -> {
			final String trimmedLine = line.trim();
			return trimmedLine.length() > 0
					&& trimmedLine.indexOf(commentPrefix) != 0;
		};
	}
}
