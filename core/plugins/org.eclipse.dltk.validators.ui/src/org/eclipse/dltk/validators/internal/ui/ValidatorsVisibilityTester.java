package org.eclipse.dltk.validators.internal.ui;

import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.ui.actions.IActionFilterTester;
import org.eclipse.dltk.validators.core.IResourceValidator;
import org.eclipse.dltk.validators.core.ISourceModuleValidator;
import org.eclipse.dltk.validators.core.IValidator;
import org.eclipse.dltk.validators.core.IValidatorType;
import org.eclipse.dltk.validators.core.ValidatorRuntime;

public class ValidatorsVisibilityTester implements IActionFilterTester {

	public ValidatorsVisibilityTester() {
	}

	@Override
	public boolean test(Object target, String name, String value) {
		if (target instanceof IModelElement) {
			final IDLTKLanguageToolkit toolkit = DLTKLanguageManager
					.getLanguageToolkit((IModelElement) target);
			if (toolkit != null) {
				final IValidatorType[] types = ValidatorRuntime
						.getValidatorTypes(toolkit.getNatureId());
				if (types != null && types.length > 0) {
					for (int i = 0, size = types.length; i < size; ++i) {
						final IValidatorType type = types[i];
						if (!isSupported(type)) {
							continue;
						}
						final IValidator[] validators = type.getValidators();
						if (validators != null && validators.length != 0) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean isSupported(final IValidatorType type) {
		return type.supports(ISourceModuleValidator.class)
				|| type.supports(IResourceValidator.class);
	}
}
