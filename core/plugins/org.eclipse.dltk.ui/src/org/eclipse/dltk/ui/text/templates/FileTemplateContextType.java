/*******************************************************************************
 * Copyright (c) 2007, 2017 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.ui.text.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateVariableType;
import org.eclipse.osgi.util.NLS;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * A generic template context type for file resources based on content-type.
 */
public class FileTemplateContextType extends TemplateContextType {

	/* resolver types */
	public static final String FILENAME = "file_name"; //$NON-NLS-1$
	public static final String FILEBASE = "file_base"; //$NON-NLS-1$
	public static final String FILELOCATION = "file_loc"; //$NON-NLS-1$
	public static final String FILEPATH = "file_path"; //$NON-NLS-1$
	public static final String PROJECTNAME = "project_name"; //$NON-NLS-1$

	/**
	 * Resolver that resolves to the variable defined in the context.
	 */
	static class FileTemplateVariableResolver extends
			SimpleTemplateVariableResolver {
		public FileTemplateVariableResolver(String type, String description) {
			super(type, description);
		}

		@Override
		protected String resolve(TemplateContext context) {
			String value = context.getVariable(getType());
			return value != null ? value : Util.EMPTY_STRING;
		}
	}

	/**
	 * This date variable evaluates to the current date in a specific format.
	 */
	static class DateVariableResolver extends SimpleTemplateVariableResolver {
		private String fFormat;

		public DateVariableResolver() {
			super(
					"date", TemplateMessages.FileTemplateContextType_variable_description_date); //$NON-NLS-1$
		}

		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			fFormat = null;
			TemplateVariableType type = variable.getVariableType();
			List params = type.getParams();
			if (params.size() == 1) {
				fFormat = params.get(0).toString();
			}
			super.resolve(variable, context);
		}

		@Override
		protected String resolve(TemplateContext context) {
			DateFormat f;
			if (fFormat == null) {
				f = DateFormat.getDateInstance();
			} else {
				f = new SimpleDateFormat(fFormat);
			}
			return f.format(new java.util.Date());
		}
	}

	/**
	 * Resolver that resolves to the value of a core variable.
	 */
	static class CoreVariableResolver extends SimpleTemplateVariableResolver {
		private String fVariableName;
		private String[] fArguments;

		public CoreVariableResolver(String type) {
			super(
					type,
					TemplateMessages.FileTemplateContextType__variable_description_eclipse);
		}

		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			fVariableName = variable.getName();
			TemplateVariableType type = variable.getVariableType();
			List params = type.getParams();
			fArguments = (String[]) params.toArray(new String[params.size()]);
			super.resolve(variable, context);
		}

		@Override
		protected String resolve(TemplateContext context) {
			StringBuffer expr = new StringBuffer("${"); //$NON-NLS-1$
			expr.append(fVariableName);
			for (int i = 0; i < fArguments.length; i++) {
				expr.append(':').append(fArguments[i]);
			}
			expr.append('}');
			IStringVariableManager mgr = VariablesPlugin.getDefault()
					.getStringVariableManager();
			try {
				return mgr.performStringSubstitution(expr.toString(), false);
			} catch (CoreException exc) {
				return expr.toString();
			}
		}

	}

	public FileTemplateContextType() {
		// global
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new DateVariableResolver());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		//addResolver(new CoreVariableResolver("eclipse")); //$NON-NLS-1$
		addResourceVariables();
	}

	protected void addResourceVariables() {
		addResolver(new FileTemplateVariableResolver(
				FILENAME,
				TemplateMessages.FileTemplateContextType_variable_description_filename));
		addResolver(new FileTemplateVariableResolver(
				FILEBASE,
				TemplateMessages.FileTemplateContextType_variable_description_filebase));
		addResolver(new FileTemplateVariableResolver(
				FILELOCATION,
				TemplateMessages.FileTemplateContextType_variable_description_fileloc));
		addResolver(new FileTemplateVariableResolver(
				FILEPATH,
				TemplateMessages.FileTemplateContextType_variable_description_filepath));
		addResolver(new FileTemplateVariableResolver(
				PROJECTNAME,
				TemplateMessages.FileTemplateContextType_variable_description_projectname));
	}

	@Override
	protected TemplateVariableResolver getResolver(String type) {
		// compatibility with editor template variables
		if ("file".equals(type)) { //$NON-NLS-1$
			type = FILENAME;
		} else if ("project".equals(type) || "enclosing_project".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
			type = PROJECTNAME;
		}
		return super.getResolver(type);
	}

	@Override
	protected void validateVariables(TemplateVariable[] variables)
			throws TemplateException {
		ArrayList required = new ArrayList(5);
		for (int i = 0; i < variables.length; i++) {
			String type = variables[i].getType();
			if (getResolver(type) == null) {
				throw new TemplateException(
						NLS
								.bind(
										TemplateMessages.FileTemplateContextType_validate_unknownvariable,
										type));
			}
			required.remove(type);
		}
		if (!required.isEmpty()) {
			String missing = (String) required.get(0);
			throw new TemplateException(
					NLS
							.bind(
									TemplateMessages.FileTemplateContextType_validate_missingvariable,
									missing));
		}
		super.validateVariables(variables);
	}

	@Override
	public void resolve(TemplateVariable variable, TemplateContext context) {
		String type = variable.getType();
		TemplateVariableResolver resolver = getResolver(type);
		if (resolver == null) {
			resolver = new FileTemplateVariableResolver(type, ""); //$NON-NLS-1$
		}
		resolver.resolve(variable, context);
	}

}
