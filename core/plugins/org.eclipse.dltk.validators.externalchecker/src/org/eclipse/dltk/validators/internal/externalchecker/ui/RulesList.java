package org.eclipse.dltk.validators.internal.externalchecker.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.dltk.validators.internal.externalchecker.core.Rule;

public class RulesList {

	private Vector<Rule> rules = new Vector<>();
	private Set<IRulesListViewer> changeListeners = new HashSet<>();
	private String[] types = { Messages.RulesList_error, Messages.RulesList_warning };

	public void addRule() {
		Rule r = new Rule("%f:%n:%m", Messages.RulesList_error); //$NON-NLS-1$
		rules.add(r);
		Iterator<IRulesListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().addRule(r);
		}
	}

	public void removeChangeListener(IRulesListViewer viewer) {
		changeListeners.remove(viewer);
	}

	public void addChangeListener(IRulesListViewer viewer) {
		changeListeners.add(viewer);
	}

	public Vector<Rule> getRules() {
		return rules;
	}

	public void ruleChanged(Rule r) {
		Iterator<IRulesListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			iterator.next().updateRule(r);
	}

	public void addRule(Rule r) {
		rules.add(r);
		Iterator<IRulesListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().addRule(r);
		}
	}

	public void removeRule(Rule task) {
		rules.remove(task);
		Iterator<IRulesListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			iterator.next().removeRule(task);
	}

	public String[] getTypes() {
		return types;
	}
}
