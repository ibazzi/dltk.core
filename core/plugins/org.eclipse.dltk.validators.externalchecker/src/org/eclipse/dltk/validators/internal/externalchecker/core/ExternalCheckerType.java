package org.eclipse.dltk.validators.internal.externalchecker.core;

import org.eclipse.dltk.validators.core.AbstractValidatorType;
import org.eclipse.dltk.validators.core.IResourceValidator;
import org.eclipse.dltk.validators.core.ISourceModuleValidator;
import org.eclipse.dltk.validators.core.IValidator;
import org.eclipse.dltk.validators.core.ValidatorRuntime;

public class ExternalCheckerType extends AbstractValidatorType {

	public static final String ID = "org.eclipse.dltk.validators.core.externalChecker"; //$NON-NLS-1$

	@Override
	public IValidator createValidator(String id) {
		return new ExternalChecker(id, getName(), this);
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return Messages.ExternalCheckerType_externalChecker;
	}

	@Override
	public String getNature() {
		return ValidatorRuntime.ANY_NATURE;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public boolean supports(Class validatorType) {
		return ISourceModuleValidator.class.equals(validatorType) || IResourceValidator.class.equals(validatorType);
	}

}
