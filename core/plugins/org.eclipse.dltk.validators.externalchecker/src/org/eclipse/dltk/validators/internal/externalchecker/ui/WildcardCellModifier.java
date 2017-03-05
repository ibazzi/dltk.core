package org.eclipse.dltk.validators.internal.externalchecker.ui;

import org.eclipse.dltk.validators.internal.externalchecker.core.CustomWildcard;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

public class WildcardCellModifier implements ICellModifier {

	ExternalCheckerRulesBlock dialog;

	public WildcardCellModifier(ExternalCheckerRulesBlock d) {
		super();
		this.dialog = d;
	}

	@Override
	public boolean canModify(Object element, String property) {
		return true;
	}

	@Override
	public Object getValue(Object element, String property) {
		int index = dialog.getColumnNames().indexOf(property);

		Object result = null;
		CustomWildcard wcard = (CustomWildcard) element;
		switch (index) {
		case 0:
			result = wcard.getLetter();
			break;
		case 1:
			result = wcard.getSpattern();
			break;
		case 2:
			result = wcard.getDescription();
			break;

		default:
			break;
		}

		return result;

	}

	@Override
	public void modify(Object element, String property, Object value) {
		int index = dialog.getColumnNames().indexOf(property);
		TableItem item = (TableItem) element;
		CustomWildcard task = (CustomWildcard) item.getData();
		String valueString;

		switch (index) {
		case 0:
			valueString = ((String) value).trim();
			task.setLetter(valueString);
			dialog.getWlist().wcardChanged(task);
			break;

		case 1:
			valueString = ((String) value).trim();
			task.setSpattern(valueString);
			dialog.getWlist().wcardChanged(task);

			break;
		}
		dialog.getWlist().wcardChanged(task);
	}

}
