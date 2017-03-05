package org.eclipse.dltk.debug.core.model;

public class AtomicScriptType implements IScriptType {
	private String name;

	public AtomicScriptType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public boolean isString() {
		return false;
	}

	@Override
	public String formatDetails(IScriptValue value) {
		return formatValue(value);
	}

	@Override
	public String formatValue(IScriptValue value) {
		return value.getRawValue();
	}

	protected void appendInstanceId(IScriptValue value, StringBuffer buffer) {
		String id = value.getInstanceId();
		if (id != null) {
			buffer.append(" ("); //$NON-NLS-1$
			buffer.append(ScriptModelMessages.variableInstanceId);
			buffer.append("="); //$NON-NLS-1$
			buffer.append(id);
			buffer.append(")"); //$NON-NLS-1$
		}
	}
}
