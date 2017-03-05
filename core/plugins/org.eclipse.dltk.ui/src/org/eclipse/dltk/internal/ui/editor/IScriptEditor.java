package org.eclipse.dltk.internal.ui.editor;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptLanguageProvider;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.jface.action.IAction;

/**
 * @since 2.0
 */
public interface IScriptEditor extends IScriptLanguageProvider {

	public ISourceReference computeHighlightRangeSourceReference();

	public void synchronizeOutlinePage(ISourceReference element,
			boolean checkIfOutlinePageActive);

	@Override
	public IDLTKLanguageToolkit getLanguageToolkit();

	public IAction getAction(String undo);

	public void outlinePageClosed();

	public IModelElement getElementAt(int offset);

	public void setSelection(IModelElement element);

}
