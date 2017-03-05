package org.eclipse.dltk.validators.internal.externalchecker.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.validators.core.IValidator;
import org.eclipse.dltk.validators.core.IValidatorProblem;
import org.eclipse.dltk.validators.core.ValidatorRuntime;
import org.eclipse.dltk.validators.internal.externalchecker.core.CustomWildcard;
import org.eclipse.dltk.validators.internal.externalchecker.core.ExternalChecker;
import org.eclipse.dltk.validators.internal.externalchecker.core.ExternalCheckerWildcardManager;
import org.eclipse.dltk.validators.internal.externalchecker.core.Rule;
import org.eclipse.dltk.validators.internal.externalchecker.core.WildcardException;
import org.eclipse.dltk.validators.internal.externalchecker.core.WildcardMatcher;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class ExternalCheckerConsoleTracker implements IPatternMatchListener {

	protected TextConsole console;
	private List<Rule> rules = new ArrayList<>();

	public ExternalCheckerConsoleTracker() {
		super();

		IValidator[] validators = ValidatorRuntime.getAllValidators();
		for (int i = 0; i < validators.length; i++) {
			if (validators[i] instanceof ExternalChecker) {
				ExternalChecker checker = (ExternalChecker) validators[i];
				if (checker.isAutomatic()) {
					for (int j = 0; j < checker.getNRules(); j++) {
						rules.add(checker.getRule(j));
					}
				}
			}
		}
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

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			IOConsole cons = (IOConsole) event.getSource();
			IDocument doc = cons.getDocument();
			int offset = event.getOffset();
			int length = event.getLength();
			String text = doc.get(offset, length);

			List<CustomWildcard> wlist = ExternalCheckerWildcardManager.loadCustomWildcards();
			WildcardMatcher wmatcher = new WildcardMatcher(wlist);

			for (int i = 0; i < rules.size(); i++) {
				Rule rule = rules.get(i);
				try {
					IValidatorProblem problem = wmatcher.match(rule, text);
					if (problem != null) {
						IHyperlink link = new ExternalCheckerSyntaxHyperlink(console, problem);
						console.addHyperlink(link, offset, text.length());
						break;
					}
				} catch (WildcardException x) {
				}
			}
			// offset = offset + text.length() + 1;
		} catch (BadLocationException e) {
		}
	}

	@Override
	public String getPattern() {
		return ".+"; //$NON-NLS-1$
	}
}
