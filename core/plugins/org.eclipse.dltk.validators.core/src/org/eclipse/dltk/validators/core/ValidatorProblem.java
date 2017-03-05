package org.eclipse.dltk.validators.core;

import java.util.HashMap;
import java.util.Map;

public class ValidatorProblem implements IValidatorProblem {

	private Map<String, Object> attributes = new HashMap<>();

	private String fileName;

	private int lineNo;
	private String message;

	private IValidatorProblem.Type probType;

	public ValidatorProblem(String fileName, String message, int lineNo, Type probType) {
		this.fileName = fileName;
		this.message = message;
		this.lineNo = lineNo;
		this.probType = probType;
	}

	@Override
	public void addAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getLineNumber() {
		return lineNo;
	}

	@Override
	public boolean isError() {
		return (probType == IValidatorProblem.Type.ERROR) ? true : false;
	}

	@Override
	public boolean isWarning() {
		return (probType == IValidatorProblem.Type.WARN) ? true : false;
	}
}
