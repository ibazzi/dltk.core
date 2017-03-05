package org.eclipse.dltk.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * Represents an 'array' script type
 */
public class ArrayScriptType extends CollectionScriptType {

	private static String ARRAY = "array"; //$NON-NLS-1$

	public ArrayScriptType() {
		super(ARRAY);
	}

	@Override
	protected String buildDetailString(IVariable variable)
			throws DebugException {
		String name = variable.getName();
		if (name != null && name.length() > 0) {
			int counter = 0;
			if (name.startsWith("-"))
				counter++;
			while (counter < name.length()) {
				if (!Character.isDigit(name.charAt(counter++))) {
					return name + "=" + super.buildDetailString(variable);
				}
			}
		}
		return super.buildDetailString(variable);
	}
}
