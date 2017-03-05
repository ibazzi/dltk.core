/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.validators.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;

public abstract class AbstractValidatorType implements IValidatorType {
	protected final Map<String, IValidator> validators = new HashMap<>();

	@Override
	public IValidator[] getValidators() {
		return validators.values().toArray(new IValidator[validators.size()]);
	}

	@Override
	public IValidator findValidator(String id) {
		return this.validators.get(id);
	}

	@Override
	public void addValidator(IValidator validator) {
		Assert.isLegal(!isBuiltin(), "could not add to the built-in validator type"); //$NON-NLS-1$
		if (validator.getValidatorType() != this) {
			throw new IllegalArgumentException("Wrong validator type"); //$NON-NLS-1$
		}
		validators.put(validator.getID(), validator);
	}

	@Override
	public void disposeValidator(String id) {
		Assert.isTrue(!isBuiltin());
		final IValidator validator = validators.remove(id);
		if (validator != null) {
			ValidatorRuntime.fireValidatorRemoved(validator);
		}
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public IValidator[] getAllValidators(IProject project) {
		return getValidators();
	}

}
