/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.ui.actions;

import java.util.Iterator;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.internal.ui.callhierarchy.SearchUtil;
import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
import org.eclipse.dltk.internal.ui.search.SearchMessages;
import org.eclipse.dltk.ui.IContextMenuConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Action group that adds the search for declarations actions to a context menu
 * and the global menu bar.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeclarationsSearchGroup extends ActionGroup {

	private static final String MENU_TEXT = SearchMessages.group_declarations;

	private IWorkbenchSite fSite;

	private AbstractDecoratedTextEditor fEditor;

	private IActionBars fActionBars;

	private String fGroupId;

	private FindDeclarationsAction fFindDeclarationsAction;

	private FindDeclarationsInProjectAction fFindDeclarationsInProjectAction;

	private FindDeclarationsInWorkingSetAction fFindDeclarationsInWorkingSetAction;

	private FindDeclarationsInHierarchyAction fFindDeclarationsInHierarchyAction;

	private final IDLTKLanguageToolkit toolkit;

	private ISelectionProvider fSelectionProvider;

	/**
	 * Creates a new <code>DeclarationsSearchGroup</code>. The group requires
	 * that the selection provided by the site's selection provider is of type
	 * <code>
	 * IStructuredSelection</code>.
	 *
	 * @param site
	 *            the workbench site that owns this action group
	 */
	public DeclarationsSearchGroup(IWorkbenchSite site, IDLTKLanguageToolkit tk) {
		fSite = site;
		this.toolkit = tk;
		fGroupId = IContextMenuConstants.GROUP_SEARCH;

		fFindDeclarationsAction = new FindDeclarationsAction(toolkit, site);
		fFindDeclarationsAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_WORKSPACE);

		fFindDeclarationsInProjectAction = new FindDeclarationsInProjectAction(
				toolkit, site);
		fFindDeclarationsInProjectAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_PROJECTS);

		fFindDeclarationsInHierarchyAction = new FindDeclarationsInHierarchyAction(
				toolkit, site);
		fFindDeclarationsInHierarchyAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_HIERARCHY);

		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(
				toolkit, site);
		fFindDeclarationsInWorkingSetAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_WORKING_SET);

		// register the actions as selection listeners
		fSelectionProvider = fSite.getSelectionProvider();
		assert fSelectionProvider != null;

		registerActions();
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 *
	 * DLTK < 5.3 binary compatibility
	 *
	 * @param editor
	 *            the Script editor
	 */
	public DeclarationsSearchGroup(ScriptEditor editor,
			IDLTKLanguageToolkit tk) {
		this((AbstractDecoratedTextEditor) editor, tk);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 *
	 * @param editor
	 *            the Script editor
	 * @since 5.3
	 */
	public DeclarationsSearchGroup(AbstractDecoratedTextEditor editor,
			IDLTKLanguageToolkit tk) {
		this.toolkit = tk;
		fEditor = editor;
		fSite = fEditor.getSite();
		fGroupId = ITextEditorActionConstants.GROUP_FIND;

		fFindDeclarationsAction = new FindDeclarationsAction(toolkit, fEditor);
		fFindDeclarationsAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_WORKSPACE);
		fEditor.setAction("SearchDeclarationsInWorkspace", fFindDeclarationsAction); //$NON-NLS-1$

		fFindDeclarationsInProjectAction = new FindDeclarationsInProjectAction(
				toolkit, fEditor);
		fFindDeclarationsInProjectAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_PROJECTS);
		fEditor.setAction("SearchDeclarationsInProjects", fFindDeclarationsInProjectAction); //$NON-NLS-1$

		fFindDeclarationsInHierarchyAction = new FindDeclarationsInHierarchyAction(
				toolkit, fEditor);
		fFindDeclarationsInHierarchyAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_HIERARCHY);
		fEditor.setAction("SearchDeclarationsInHierarchy", fFindDeclarationsInHierarchyAction); //$NON-NLS-1$

		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(
				toolkit, fEditor);
		fFindDeclarationsInWorkingSetAction.setActionDefinitionId(IScriptEditorActionDefinitionIds.SEARCH_DECLARATIONS_IN_WORKING_SET);
		fEditor.setAction("SearchDeclarationsInWorkingSet", fFindDeclarationsInWorkingSetAction); //$NON-NLS-1$
	}

	private void registerAction(SelectionDispatchAction action, ISelectionProvider provider, ISelection selection) {
		action.update(selection);
		provider.addSelectionChangedListener(action);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		fActionBars = actionBars;
		updateGlobalActionHandlers();
	}

	private void addAction(IAction action, IMenuManager manager) {
		if (action.isEnabled()) {
			manager.add(action);
		}
	}

	private void addWorkingSetAction(IWorkingSet[] workingSets, IMenuManager manager) {
		FindAction action;
		if (fEditor != null)
			action = new WorkingSetFindAction(fEditor,
					new FindDeclarationsInWorkingSetAction(toolkit, fEditor,
							workingSets), SearchUtil.toString(workingSets));
		else
			action = new WorkingSetFindAction(fSite,
					new FindDeclarationsInWorkingSetAction(toolkit, fSite,
							workingSets), SearchUtil.toString(workingSets));
		action.update(getContext().getSelection());
		addAction(action, manager);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		IMenuManager javaSearchMM = new MenuManager(MENU_TEXT, IContextMenuConstants.GROUP_SEARCH);
		addAction(fFindDeclarationsAction, javaSearchMM);
		addAction(fFindDeclarationsInProjectAction, javaSearchMM);
		addAction(fFindDeclarationsInHierarchyAction, javaSearchMM);

		javaSearchMM.add(new Separator());

		Iterator iter = SearchUtil.getLRUWorkingSets().sortedIterator();
		while (iter.hasNext()) {
			addWorkingSetAction((IWorkingSet[]) iter.next(), javaSearchMM);
		}
		addAction(fFindDeclarationsInWorkingSetAction, javaSearchMM);

		if (!javaSearchMM.isEmpty())
			manager.appendToGroup(fGroupId, javaSearchMM);
	}

	@Override
	public void dispose() {
		unregisterActions();

		fSelectionProvider = null;
		fFindDeclarationsAction = null;
		fFindDeclarationsInProjectAction = null;
		fFindDeclarationsInHierarchyAction = null;
		fFindDeclarationsInWorkingSetAction = null;
		updateGlobalActionHandlers();
		super.dispose();
	}

	private void updateGlobalActionHandlers() {
		if (fActionBars != null) {
			fActionBars.setGlobalActionHandler(DLTKActionConstants.FIND_DECLARATIONS_IN_WORKSPACE, fFindDeclarationsAction);
			fActionBars.setGlobalActionHandler(DLTKActionConstants.FIND_DECLARATIONS_IN_PROJECT, fFindDeclarationsInProjectAction);
			fActionBars.setGlobalActionHandler(DLTKActionConstants.FIND_DECLARATIONS_IN_HIERARCHY, fFindDeclarationsInHierarchyAction);
			fActionBars.setGlobalActionHandler(DLTKActionConstants.FIND_DECLARATIONS_IN_WORKING_SET, fFindDeclarationsInWorkingSetAction);
		}
	}

	private void disposeAction(ISelectionChangedListener action, ISelectionProvider provider) {
		if (action != null)
			provider.removeSelectionChangedListener(action);
	}

	private void registerActions() {
		if (fEditor != null) {
			return;
		}
		ISelection selection = fSelectionProvider.getSelection();

		registerAction(fFindDeclarationsAction, fSelectionProvider, selection);
		registerAction(fFindDeclarationsInProjectAction, fSelectionProvider, selection);
		registerAction(fFindDeclarationsInHierarchyAction, fSelectionProvider, selection);
		registerAction(fFindDeclarationsInWorkingSetAction, fSelectionProvider, selection);
	}

	private void unregisterActions() {
		if (fEditor != null) {
			return;
		}
		disposeAction(fFindDeclarationsAction, fSelectionProvider);
		disposeAction(fFindDeclarationsInProjectAction, fSelectionProvider);
		disposeAction(fFindDeclarationsInHierarchyAction, fSelectionProvider);
		disposeAction(fFindDeclarationsInWorkingSetAction, fSelectionProvider);
	}

	/**
	 * Proxy to {@link SelectionDispatchAction#setSpecialSelectionProvider(ISelectionProvider)}
	 *
	 * @since 5.3
	 * @param provider
	 */
	public void setSpecialSelectionProvider(ISelectionProvider provider) {
		assert fEditor != null || provider != null;
		unregisterActions();
		fSelectionProvider = provider;
		registerActions();

		fFindDeclarationsAction.setSpecialSelectionProvider(provider);
		fFindDeclarationsInProjectAction.setSpecialSelectionProvider(provider);
		fFindDeclarationsInWorkingSetAction.setSpecialSelectionProvider(provider);
		fFindDeclarationsInHierarchyAction.setSpecialSelectionProvider(provider);
	}
}
