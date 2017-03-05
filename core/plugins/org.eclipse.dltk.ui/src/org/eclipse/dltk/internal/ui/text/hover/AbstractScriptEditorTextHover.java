/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.text.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.ICodeAssist;
import org.eclipse.dltk.core.ICodeSelection;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.ui.editor.EditorUtility;
import org.eclipse.dltk.internal.ui.text.ScriptWordFinder;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.IWorkingCopyManager;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.completion.HTMLPrinter;
import org.eclipse.dltk.ui.text.hover.IScriptEditorTextHover;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;
import org.osgi.framework.Bundle;

/**
 * Abstract class for providing hover information for Script elements.
 */
public abstract class AbstractScriptEditorTextHover
		implements IScriptEditorTextHover, ITextHoverExtension {

	/**
	 * The style sheet (css).
	 */
	private static String fgCSSStyles;
	private IEditorPart fEditor;
	private IPreferenceStore fStore;

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		fStore = store;
	}

	/**
	 * @return the fStore
	 */
	protected IPreferenceStore getPreferenceStore() {
		return fStore;
	}

	@Override
	public void setEditor(IEditorPart editor) {
		fEditor = editor;
	}

	protected IEditorPart getEditor() {
		return fEditor;
	}

	protected ICodeAssist getCodeAssist() {
		if (fEditor != null) {
			IEditorInput input = fEditor.getEditorInput();

			IWorkingCopyManager manager = DLTKUIPlugin.getDefault()
					.getWorkingCopyManager();
			return manager.getWorkingCopy(input, false);
		}

		return null;
	}

	@Override
	public IRegion getHoverRegion(final ITextViewer textViewer, int offset) {
		return ScriptWordFinder.findWord(textViewer.getDocument(), offset);
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		String nature = null;
		IModelElement inputModelElement = getEditorInputModelElement();
		if (inputModelElement == null)
			return null;
		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
				.getLanguageToolkit(inputModelElement);
		if (toolkit == null) {
			return null;
		}
		nature = toolkit.getNatureId();
		if (nature == null) {
			return null;
		}

		ICodeAssist resolve = getCodeAssist();
		if (resolve != null) {
			try {
				String content = null;
				try {
					content = textViewer.getDocument().get(
							hoverRegion.getOffset(), hoverRegion.getLength());
				} catch (BadLocationException e) {
				}

				final ICodeSelection result = resolve.codeSelectAll(
						hoverRegion.getOffset(), hoverRegion.getLength());

				if (result == null) {
					if (content != null) {
						return getHoverInfo(nature, content);
					}
					return null;
				}
				return getHoverInfo(nature, result);

			} catch (ModelException x) {
				return null;
			}
		}
		return null;
	}

	protected ISourceModule getEditorInputModelElement() {
		return EditorUtility.getEditorInputModelElement(this.fEditor, false);
	}

	@Deprecated
	protected String getHoverInfo(String nature,
			IModelElement[] modelElements) {
		return null;
	}

	/**
	 * Provides hover information for the given elements.
	 *
	 * @param elements
	 *            the Script elements for which to provide hover information
	 * @return the hover information string
	 *
	 */
	protected String getHoverInfo(String nature, Object[] elements) {
		final List<IModelElement> modelElements = new ArrayList<IModelElement>();
		for (Object element : elements) {
			if (element instanceof IModelElement) {
				modelElements.add((IModelElement) element);
			}
		}
		return getHoverInfo(nature,
				modelElements.toArray(new IModelElement[modelElements.size()]));
	}

	/**
	 * Provides hover information for the given elements.
	 *
	 * @param selection
	 *            the Script elements for which to provide hover information
	 * @return the hover information string
	 */
	protected String getHoverInfo(String nature, ICodeSelection selection) {
		return getHoverInfo(nature, selection.toArray());
	}

	/**
	 * Provides hover information for the keyword.
	 *
	 * @param content
	 *            text of the keyword
	 * @return the hover information string
	 *
	 */
	protected String getHoverInfo(String nature, String content) {
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return parent -> new DefaultInformationControl(parent,
				EditorsUI.getTooltipAffordanceString());
	}

	/**
	 * Returns the tool tip affordance string.
	 *
	 * @return the affordance string or <code>null</code> if disabled or no key
	 *         binding is defined
	 *
	 */
	// protected String getTooltipAffordanceString() {
	// if (this.getPreferenceStore() == null) {
	// return "{0}";
	// }
	// if (fBindingService == null
	// || !getPreferenceStore().getBoolean(
	// PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
	// return null;
	//
	// String keySequence = fBindingService
	// .getBestActiveBindingFormattedFor(IScriptEditorActionDefinitionIds.
	// SHOW_DOCUMENTATION);
	// if (keySequence == null)
	// return null;
	//
	// return Messages.format(
	// ScriptHoverMessages.ScriptTextHover_makeStickyHint,
	// keySequence == null ? "" : keySequence); //$NON-NLS-1$
	// }
	/**
	 * Returns the style sheet.
	 *
	 *
	 */
	protected static String getStyleSheet() {
		if (fgCSSStyles == null) {
			Bundle bundle = Platform.getBundle(DLTKUIPlugin.getPluginId());
			URL url = bundle.getEntry("/DocumentationHoverStyleSheet.css"); //$NON-NLS-1$
			if (url != null) {
				try {
					url = FileLocator.toFileURL(url);
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(url.openStream()));
					StringBuffer buffer = new StringBuffer(200);
					String line = reader.readLine();
					while (line != null) {
						buffer.append(line);
						buffer.append('\n');
						line = reader.readLine();
					}
					fgCSSStyles = buffer.toString();
				} catch (IOException ex) {
					DLTKUIPlugin.log(ex);
				}
			}
		}
		String css = fgCSSStyles;
		if (css != null) {
			FontData fontData = JFaceResources.getFontRegistry().getFontData(
					PreferenceConstants.APPEARANCE_DOCUMENTATION_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}
}
