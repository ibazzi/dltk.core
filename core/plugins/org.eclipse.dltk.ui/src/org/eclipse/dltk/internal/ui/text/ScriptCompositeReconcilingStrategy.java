/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.ScriptUtils;
import org.eclipse.dltk.internal.ui.text.spelling.ScriptSpellingReconcileStrategy;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.text.spelling.SpellCheckDelegate;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ScriptCompositeReconcilingStrategy extends
		CompositeReconcilingStrategy {
	private ITextEditor fEditor;
	private ScriptReconcilingStrategy fScriptStrategy;

	/**
	 * Creates a new script reconciling strategy.
	 *
	 * @param editor
	 *            the editor of the strategy's reconciler
	 * @param documentPartitioning
	 *            the document partitioning this strategy uses for configuration
	 */
	public ScriptCompositeReconcilingStrategy(ITextEditor editor,
			String documentPartitioning, SpellCheckDelegate spellCheckDelegate) {
		fEditor = editor;
		fScriptStrategy = new ScriptReconcilingStrategy(editor);
		final List<IReconcilingStrategy> strategies = new ArrayList<IReconcilingStrategy>();
		strategies.add(fScriptStrategy);
		if (spellCheckDelegate != null) {
			final IDLTKLanguageToolkit toolkit = ScriptUtils
					.getLanguageToolkit(editor);
			if (toolkit != null) {
				final IContentType contentType = Platform
						.getContentTypeManager().getContentType(
								toolkit.getLanguageContentType());
				if (contentType != null) {
					strategies.add(new ScriptSpellingReconcileStrategy(editor,
							documentPartitioning, contentType,
							spellCheckDelegate));
				}
			}
		}
		setReconcilingStrategies(strategies
				.toArray(new IReconcilingStrategy[strategies.size()]));
	}

	private IProblemRequestorExtension getProblemRequestorExtension() {
		if (fEditor == null) {
			return null;
		}

		IDocumentProvider p = fEditor.getDocumentProvider();
		if (p == null) {
			p = DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider();
		}
		IAnnotationModel m = p.getAnnotationModel(fEditor.getEditorInput());
		if (m instanceof IProblemRequestorExtension)
			return (IProblemRequestorExtension) m;
		return null;
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.reconcile(dirtyRegion, subRegion);
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.reconcile(dirtyRegion, subRegion);
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.reconcile(partition);
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.reconcile(partition);
		}
	}

	@Override
	public void initialReconcile() {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.initialReconcile();
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.initialReconcile();
		}
	}

	/**
	 * Tells this strategy whether to inform its listeners.
	 *
	 * @param notify
	 *            <code>true</code> if listeners should be notified
	 */
	public void notifyListeners(boolean notify) {
		fScriptStrategy.notifyListeners(notify);
	}

	/**
	 * Called before reconciling is started.
	 *
	 *
	 */
	public void aboutToBeReconciled() {
		fScriptStrategy.aboutToBeReconciled();
	}
}
