/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.ui.text.completion;

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.core.CompletionContext;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.Flags;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.ILocalVariable;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.corext.util.Strings;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.ScriptElementImageDescriptor;
import org.eclipse.dltk.ui.ScriptElementImageProvider;
import org.eclipse.dltk.ui.ScriptElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

/**
 * Provides labels forscriptcontent assist proposals. The functionality is
 * similar to the one provided by {@link org.eclipse.dltk.ui.ModelElementLabels}
 * , but based on signatures and {@link CompletionProposal}s.
 * 
 */
public class CompletionProposalLabelProvider {

	private static final String QUALIFIER_SEPARATOR = ScriptElementLabels.CONCAT_STRING;
	private static final String RETURN_TYPE_SEPARATOR = ScriptElementLabels.DECL_STRING;
	private static final String VAR_TYPE_SEPARATOR = ScriptElementLabels.DECL_STRING;

	/**
	 * The completion context.
	 */
	// private CompletionContext fContext;
	/**
	 * Creates a new label provider.
	 */
	public CompletionProposalLabelProvider() {
	}

	/**
	 * Creates and returns a parameter list of the given method proposal
	 * suitable for display. The list does not include parentheses. The lower
	 * bound of parameter types is returned.
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 *    &quot;void method(int i, Strings)&quot; -&gt; &quot;int i, String s&quot;
	 *    &quot;? extends Number method(java.lang.String s, ? super Number n)&quot; -&gt; &quot;String s, Number n&quot;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param methodProposal
	 *            the method proposal to create the parameter list for. Must be
	 *            of kind {@link CompletionProposal#METHOD_REF}.
	 * @return the list of comma-separated parameters suitable for display
	 */
	public String createParameterList(CompletionProposal methodProposal) {
		Assert.isTrue(methodProposal.getKind() == CompletionProposal.METHOD_REF);
		return Strings
				.markScriptElementLabelLTR(
						appendParameterList(new StyledString(), methodProposal))
				.toString();
	}

	/**
	 * Appends the parameter list to <code>buffer</code>. See
	 * <code>createUnboundedParameterList</code> for details.
	 * 
	 * @param buffer
	 *            the buffer to append to
	 * @param methodProposal
	 *            the method proposal
	 * @return the modified <code>buffer</code>
	 */
	protected StyledString appendParameterList(StyledString buffer,
			CompletionProposal methodProposal) {
		String[] parameterNames = methodProposal.findParameterNames(null);
		String[] parameterTypes = null;
		if (parameterNames != null) {
			final Integer paramLimit = (Integer) methodProposal
					.getAttribute(ScriptCompletionProposalCollector.ATTR_PARAM_LIMIT);
			if (paramLimit != null) {
				for (int i = 0; i < parameterNames.length; i++) {
					if (i >= paramLimit.intValue()) {
						break;
					}
					if (i > 0) {
						buffer.append(',');
						buffer.append(' ');
					}
					buffer.append(parameterNames[i]);
				}
				return buffer;
			}
		}
		return appendParameterSignature(buffer, parameterTypes, parameterNames);
	}

	/**
	 * Creates a display string of a parameter list (without the parentheses)
	 * for the given parameter types and names.
	 * 
	 * @param parameterTypes
	 *            the parameter types
	 * @param parameterNames
	 *            the parameter names
	 * @return the display string of the parameter list defined by the passed
	 *         arguments
	 */
	protected StyledString appendParameterSignature(StyledString buffer,
			String[] parameterTypes, String[] parameterNames) {
		if (parameterNames != null) {
			for (int i = 0; i < parameterNames.length; i++) {
				if (i > 0) {
					buffer.append(',');
					buffer.append(' ');
				}
				buffer.append(parameterNames[i]);
			}
		}
		return buffer;
	}

	/**
	 * Creates a display label for the given method proposal. The display label
	 * consists of:
	 * <ul>
	 * <li>the method name</li>
	 * <li>the parameter list (see
	 * {@link #createParameterList(CompletionProposal)})</li>
	 * <li>the raw simple name of the declaring type</li>
	 * </ul>
	 * <p>
	 * Examples: For the <code>get(int)</code> method of a variable of type
	 * <code>List<? extends Number></code>, the following display name is
	 * returned: <code>get(int index)  Number - List</code>.<br>
	 * For the <code>add(E)</code> method of a variable of type returned:
	 * <code>add(Number o)  void - List</code>.<br>
	 * <code>List<? super Number></code>, the following display name is
	 * </p>
	 * 
	 * @param methodProposal
	 *            the method proposal to display
	 * @return the display label for the given method proposal
	 */
	protected StyledString createMethodProposalLabel(
			CompletionProposal methodProposal) {
		StyledString buffer = new StyledString();

		// method name
		buffer.append(methodProposal.getName());

		// parameters
		buffer.append('(');
		appendParameterList(buffer, methodProposal);
		buffer.append(')');
		IModelElement element = methodProposal.getModelElement();
		if (element != null && element.getElementType() == IModelElement.METHOD
				&& element.exists()) {
			final IMethod method = (IMethod) element;
			try {
				if (!method.isConstructor()) {
					String type = method.getType();
					if (type != null) {
						buffer.append(getReturnTypeSeparator()).append(type);
					}
					IType declaringType = method.getDeclaringType();
					if (declaringType != null) {
						buffer.append(getQualifierSeparator()).append(
								declaringType.getElementName());
					}
				}
			} catch (ModelException e) {
				// ignore
			}
		}

		return buffer;
	}

	protected StyledString createOverrideMethodProposalLabel(
			CompletionProposal methodProposal) {
		StyledString nameBuffer = new StyledString();

		// method name
		nameBuffer.append(methodProposal.getName());

		// parameters
		nameBuffer.append('(');
		appendParameterList(nameBuffer, methodProposal);
		nameBuffer.append(')');

		// nameBuffer.append(RETURN_TYPE_SEPARATOR);

		// // return type
		// // TODO remove SignatureUtil.fix83600 call when bugs are fixed
		// char[] returnType = createTypeDisplayName(SignatureUtil
		// .getUpperBound(Signature.getReturnType(SignatureUtil
		// .fix83600(methodProposal.getSignature()))));
		// nameBuffer.append(returnType);
		//
		// // declaring type
		// nameBuffer.append(QUALIFIER_SEPARATOR,
		// StyledString.QUALIFIER_STYLER);
		//
		// String declaringType = extractDeclaringTypeFQN(methodProposal);
		// declaringType = Signature.getSimpleName(declaringType);
		// nameBuffer.append(Messages.format(
		// JavaTextMessages.ResultCollector_overridingmethod,
		// BasicElementLabels.getJavaElementName(declaringType)),
		// StyledString.QUALIFIER_STYLER);

		return nameBuffer;
	}

	/**
	 * Creates a display label for a given type proposal. The display label
	 * consists of:
	 * <ul>
	 * <li>the simple type name (erased when the context is in javadoc)</li>
	 * <li>the package name</li>
	 * </ul>
	 * <p>
	 * Examples: A proposal for the generic type
	 * <code>java.util.List&lt;E&gt;</code>, the display label is:
	 * <code>List<E> - java.util</code>.
	 * </p>
	 * 
	 * @param typeProposal
	 *            the method proposal to display
	 * @return the display label for the given type proposal
	 */
	public StyledString createTypeProposalLabel(
			CompletionProposal typeProposal) {
		return createTypeProposalLabel(typeProposal.getName().toCharArray());
	}

	protected StyledString createTypeProposalLabel(char[] fullName) {
		int qIndex = findSimpleNameStart(fullName);

		StyledString buf = new StyledString();
		buf.append(new String(fullName, qIndex, fullName.length - qIndex));
		if (qIndex > 0) {
			buf.append(ScriptElementLabels.CONCAT_STRING,
					StyledString.QUALIFIER_STYLER);
			buf.append(new String(fullName, 0, qIndex - 1),
					StyledString.QUALIFIER_STYLER);
		}
		return Strings.markScriptElementLabelLTR(buf);
	}

	protected StyledString createSimpleLabelWithType(
			CompletionProposal proposal) {
		StyledString buf = new StyledString();
		buf.append(proposal.getName());
		IModelElement element = proposal.getModelElement();
		if (element != null
				&& element.getElementType() == IModelElement.LOCAL_VARIABLE
				&& element.exists()) {
			final ILocalVariable var = (ILocalVariable) element;
			String type = var.getType();
			if (type != null) {
				buf.append(VAR_TYPE_SEPARATOR);
				buf.append(type);
			}
		}
		return Strings.markScriptElementLabelLTR(buf);
	}

	protected StyledString createFieldProposalLabel(
			CompletionProposal proposal) {
		StyledString buf = new StyledString();
		buf.append(proposal.getName());
		IModelElement element = proposal.getModelElement();
		if (element != null && element.getElementType() == IModelElement.FIELD
				&& element.exists()) {
			final IField field = (IField) element;
			try {
				String type = field.getType();
				if (type != null) {
					buf.append(VAR_TYPE_SEPARATOR);
					buf.append(type);
				}
			} catch (ModelException e) {
				// ignore
			}
		}
		return Strings.markScriptElementLabelLTR(buf);
	}

	public StyledString createSimpleLabel(CompletionProposal proposal) {
		return Strings.markScriptElementLabelLTR(
				new StyledString(String.valueOf(proposal.getName())));
	}

	public StyledString createKeywordLabel(CompletionProposal proposal) {
		return Strings.markScriptElementLabelLTR(
				new StyledString(String.valueOf(proposal.getName())));
	}

	/**
	 * Creates the display label for a given <code>CompletionProposal</code>.
	 * 
	 * @param proposal
	 *            the completion proposal to create the display label for
	 * @return the display label for <code>proposal</code>
	 */
	public String createLabel(CompletionProposal proposal) {
		return createStyledLabel(proposal).getString();
	}

	/**
	 * Creates a display label with styles for a given
	 * <code>CompletionProposal</code>.
	 * 
	 * @param proposal
	 *            the completion proposal to create the display label for
	 * @return the display label for <code>proposal</code>
	 * 
	 * @since 3.4
	 */
	public StyledString createStyledLabel(CompletionProposal proposal) {
		switch (proposal.getKind()) {
		case CompletionProposal.METHOD_NAME_REFERENCE:
		case CompletionProposal.METHOD_REF:
		case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			return createMethodProposalLabel(proposal);
		case CompletionProposal.METHOD_DECLARATION:
			return createOverrideMethodProposalLabel(proposal);
		case CompletionProposal.TYPE_REF:
			return createTypeProposalLabel(proposal);
			// case CompletionProposal.JAVADOC_TYPE_REF:
			// return createJavadocTypeProposalLabel(proposal);
			// case CompletionProposal.JAVADOC_FIELD_REF:
			// case CompletionProposal.JAVADOC_VALUE_REF:
			// case CompletionProposal.JAVADOC_BLOCK_TAG:
			// case CompletionProposal.JAVADOC_INLINE_TAG:
			// case CompletionProposal.JAVADOC_PARAM_REF:
			// return createJavadocSimpleProposalLabel(proposal);
			// case CompletionProposal.JAVADOC_METHOD_REF:
			// return createJavadocMethodProposalLabel(proposal);
		case CompletionProposal.FIELD_REF:
			return createFieldProposalLabel(proposal);
		case CompletionProposal.LOCAL_VARIABLE_REF:
		case CompletionProposal.VARIABLE_DECLARATION:
			return createSimpleLabelWithType(proposal);
		case CompletionProposal.KEYWORD:
			return createKeywordLabel(proposal);
		case CompletionProposal.PACKAGE_REF:
		case CompletionProposal.LABEL_REF:
			return createSimpleLabel(proposal);
		default:
			Assert.isTrue(false);
			return null;
		}
	}

	/**
	 * Creates and returns a decorated image descriptor for a completion
	 * proposal.
	 * 
	 * @param proposal
	 *            the proposal for which to create an image descriptor
	 * @return the created image descriptor, or <code>null</code> if no image is
	 *         available
	 */
	public ImageDescriptor createImageDescriptor(CompletionProposal proposal) {
		ImageDescriptor descriptor;
		switch (proposal.getKind()) {
		case CompletionProposal.METHOD_DECLARATION:
		case CompletionProposal.METHOD_NAME_REFERENCE:
		case CompletionProposal.METHOD_REF:
		case CompletionProposal.POTENTIAL_METHOD_DECLARATION:
			return createMethodImageDescriptor(proposal);
		case CompletionProposal.TYPE_REF:
			descriptor = DLTKPluginImages.DESC_OBJS_CLASSALT;
			break;
		case CompletionProposal.FIELD_REF:
			descriptor = DLTKPluginImages.DESC_FIELD_DEFAULT;
			break;
		case CompletionProposal.LOCAL_VARIABLE_REF:
		case CompletionProposal.VARIABLE_DECLARATION:
			descriptor = DLTKPluginImages.DESC_OBJS_LOCAL_VARIABLE;
			break;
		case CompletionProposal.PACKAGE_REF:
			descriptor = DLTKPluginImages.DESC_OBJS_PACKAGE;
			break;
		case CompletionProposal.KEYWORD:
			descriptor = DLTKPluginImages.DESC_OBJS_KEYWORD;
			break;
		case CompletionProposal.LABEL_REF:
			descriptor = null;
			break;
		// case CompletionProposal.JAVADOC_METHOD_REF:
		// case CompletionProposal.JAVADOC_TYPE_REF:
		// case CompletionProposal.JAVADOC_FIELD_REF:
		// case CompletionProposal.JAVADOC_VALUE_REF:
		// case CompletionProposal.JAVADOC_BLOCK_TAG:
		// case CompletionProposal.JAVADOC_INLINE_TAG:
		// case CompletionProposal.JAVADOC_PARAM_REF:
		// descriptor = JavaPluginImages.DESC_OBJS_JAVADOCTAG;
		// break;
		default:
			descriptor = null;
			Assert.isTrue(false);
		}

		if (descriptor == null)
			return null;
		return decorateImageDescriptor(descriptor, proposal);
	}

	public ImageDescriptor createMethodImageDescriptor(
			CompletionProposal proposal) {
		return decorateImageDescriptor(
				ScriptElementImageProvider.getMethodImageDescriptor(proposal
						.getFlags()), proposal);
	}

	public ImageDescriptor createTypeImageDescriptor(CompletionProposal proposal) {
		// boolean isInterfaceOrAnnotation= Flags.isInterface(flags) ||
		// Flags.isAnnotation(flags);
		return decorateImageDescriptor(
				ScriptElementImageProvider.getTypeImageDescriptor(
						proposal.getFlags(), false), proposal);
	}

	protected ImageDescriptor createFieldImageDescriptor(
			CompletionProposal proposal) {
		return decorateImageDescriptor(
				ScriptElementImageProvider.getFieldImageDescriptor(proposal
						.getFlags()), proposal);
	}

	protected ImageDescriptor createLocalImageDescriptor(
			CompletionProposal proposal) {
		return decorateImageDescriptor(
				DLTKPluginImages.DESC_OBJS_LOCAL_VARIABLE, proposal);
	}

	protected ImageDescriptor createPackageImageDescriptor(
			CompletionProposal proposal) {
		return decorateImageDescriptor(DLTKPluginImages.DESC_OBJS_PACKAGE,
				proposal);
	}

	/**
	 * Returns a version of <code>descriptor</code> decorated according to the
	 * passed <code>modifier</code> flags.
	 * 
	 * @param descriptor
	 *            the image descriptor to decorate
	 * @param proposal
	 *            the proposal
	 * @return an image descriptor for a method proposal
	 * @see Flags
	 */
	protected ImageDescriptor decorateImageDescriptor(
			ImageDescriptor descriptor, CompletionProposal proposal) {
		int adornmentFlags = ScriptElementImageProvider.computeAdornmentFlags(
				proposal.getModelElement(),
				ScriptElementImageProvider.SMALL_ICONS
						| ScriptElementImageProvider.OVERLAY_ICONS);

		if (proposal.isConstructor()) {
			adornmentFlags |= ScriptElementImageDescriptor.CONSTRUCTOR;
		}

		if (adornmentFlags == 0) {
			return descriptor;
		}

		return new ScriptElementImageDescriptor(descriptor, adornmentFlags,
				ScriptElementImageProvider.SMALL_SIZE);
	}

	private int findSimpleNameStart(char[] array) {
		int lastDot = 0;
		for (int i = 0, len = array.length; i < len; i++) {
			char ch = array[i];
			if (ch == '<') {
				return lastDot;
			} else if (ch == '.') {
				lastDot = i + 1;
			}
		}
		return lastDot;
	}

	/**
	 * Sets the completion context.
	 * 
	 * @param context
	 *            the completion context
	 * 
	 */
	void setContext(CompletionContext context) {
		// fContext = context;
	}

	/**
	 * Returns an user-readable string for separating the return type (e.g.
	 * " : ").
	 * 
	 * @since 5.1
	 */
	protected String getReturnTypeSeparator() {
		return ScriptTextMessages.CompletionProposalLabelProvider_returnTypeSeparator;
	}

	/**
	 * Returns an user-readable string for separating post qualified names (e.g.
	 * " - ").
	 * 
	 * @since 5.1
	 */
	protected String getQualifierSeparator() {
		return ScriptTextMessages.CompletionProposalLabelProvider_qualifierSeparator;
	}
}
