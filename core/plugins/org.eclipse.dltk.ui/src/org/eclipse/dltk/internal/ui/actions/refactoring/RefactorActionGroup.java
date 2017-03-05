/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.actions.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.internal.ui.actions.ActionMessages;
import org.eclipse.dltk.internal.ui.actions.ActionUtil;
import org.eclipse.dltk.internal.ui.actions.SelectionConverter;
import org.eclipse.dltk.internal.ui.editor.ModelTextSelection;
import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.IContextMenuConstants;
import org.eclipse.dltk.ui.actions.DLTKActionConstants;
import org.eclipse.dltk.ui.actions.IScriptEditorActionDefinitionIds;
import org.eclipse.dltk.ui.actions.SelectionDispatchAction;
import org.eclipse.dltk.utils.LazyExtensionManager;
import org.eclipse.dltk.utils.LazyExtensionManager.Descriptor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.QuickMenuCreator;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.Page;

/**
 * Action group that adds refactor actions (for example 'Rename', 'Move') to a
 * context menu and the global menu bar.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 *
 */
public class RefactorActionGroup extends ActionGroup {

	private static final String PERF_REFACTOR_ACTION_GROUP = "org.eclipse.dltk.ui/perf/explorer/RefactorActionGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the refactor sub menu (value
	 * <code>org.eclipse.dltk.ui.refactoring.menu</code>).
	 *
	 *
	 */
	public static final String MENU_ID = "org.eclipse.dltk.ui.refactoring.menu"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the reorg group of the refactor sub menu (value
	 * <code>reorgGroup</code>).
	 *
	 *
	 */
	public static final String GROUP_REORG = "reorgGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the type group of the refactor sub menu (value
	 * <code>typeGroup</code>).
	 *
	 *
	 */
	public static final String GROUP_TYPE = "typeGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the coding group of the refactor sub menu (value
	 * <code>codingGroup</code>).
	 *
	 *
	 */
	public static final String GROUP_CODING = "codingGroup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the coding group 2 of the refactor sub menu (value
	 * <code>codingGroup2</code>).
	 *
	 *
	 */
	public static final String GROUP_CODING2 = "codingGroup2"; //$NON-NLS-1$

	/**
	 * Pop-up menu: id of the reorg group 2 of the refactor sub menu (value
	 * <code>reorgGroup2</code>).
	 *
	 *
	 */
	private static final String GROUP_REORG2 = "reorgGroup2"; //$NON-NLS-1$ //TODO(3.3):
																// make public

	/**
	 * Pop-up menu: id of the type group 2 of the refactor sub menu (value
	 * <code>typeGroup2</code>).
	 *
	 *
	 */
	private static final String GROUP_TYPE2 = "typeGroup2"; //$NON-NLS-1$ //TODO(3.3):
															// make public

	private IWorkbenchSite fSite;
	private ScriptEditor fEditor;
	private String fGroupName = IContextMenuConstants.GROUP_REORGANIZE;

	protected SelectionDispatchAction fMoveAction;
	protected SelectionDispatchAction fRenameAction;
	// protected SelectionDispatchAction fModifyParametersAction;
	// protected SelectionDispatchAction fConvertAnonymousToNestedAction;
	// protected SelectionDispatchAction fConvertNestedToTopAction;

	// protected SelectionDispatchAction fPullUpAction;
	// protected SelectionDispatchAction fPushDownAction;
	// protected SelectionDispatchAction fExtractInterfaceAction;
	// protected SelectionDispatchAction fExtractSupertypeAction;
	// protected SelectionDispatchAction fChangeTypeAction;
	// protected SelectionDispatchAction fUseSupertypeAction;
	// protected SelectionDispatchAction fInferTypeArgumentsAction;

	// protected SelectionDispatchAction fInlineAction;
	// private SelectionDispatchAction fReplaceInvocationsAction;
	// protected SelectionDispatchAction fIntroduceIndirectionAction;
	// protected SelectionDispatchAction fExtractMethodAction;
	// protected SelectionDispatchAction fExtractTempAction;
	// protected SelectionDispatchAction fExtractConstantAction;
	// protected SelectionDispatchAction fIntroduceParameterAction;
	// protected SelectionDispatchAction fIntroduceFactoryAction;
	// protected SelectionDispatchAction fConvertLocalToFieldAction;
	// protected SelectionDispatchAction fSelfEncapsulateField;

	protected UndoRedoActionGroup fUndoRedoActionGroup;

	private final List<SelectionDispatchAction> fActions = new ArrayList<SelectionDispatchAction>();
	private final List<ContributedRefactoringAction> fContributedActions = new ArrayList<ContributedRefactoringAction>();

	private static final String QUICK_MENU_ID = "org.eclipse.dltk.ui.edit.text.script.refactor.quickMenu"; //$NON-NLS-1$

	private IHandlerActivation fQuickAccessHandlerActivation;
	private IHandlerService fHandlerService;

	private static class NoActionAvailable extends Action {
		public NoActionAvailable() {
			setEnabled(true);
			setText(ActionMessages.RefactorActionGroup_noRefactoringAvailable);
		}
	}

	private Action fNoActionAvailable = new NoActionAvailable();

	/**
	 * Creates a new <code>RefactorActionGroup</code>. The group requires that
	 * the selection provided by the part's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param part
	 *            the view part that owns this action group
	 * @param toolkit
	 *            toolkit for language-specific refactorings
	 */
	public RefactorActionGroup(IViewPart part, IDLTKLanguageToolkit toolkit) {
		this(part.getSite(), toolkit);

		IUndoContext workspaceContext = ResourcesPlugin.getWorkspace()
				.getAdapter(IUndoContext.class);
		fUndoRedoActionGroup = new UndoRedoActionGroup(part.getViewSite(),
				workspaceContext, true);
		installQuickAccessAction();
	}

	@Deprecated
	public RefactorActionGroup(IViewPart part) {
		this(part, null);
	}

	/**
	 * Creates a new <code>RefactorActionGroup</code>. The action requires that
	 * the selection provided by the page's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param page
	 *            the page that owns this action group
	 * @param toolkit
	 *            toolkit for language-specific refactorings
	 *
	 */
	public RefactorActionGroup(Page page, IDLTKLanguageToolkit toolkit) {
		this(page.getSite(), toolkit);
		installQuickAccessAction();
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 *
	 * @param editor
	 *            the compilation unit editor
	 * @param groupName
	 *            the group name to add the actions to
	 */
	public RefactorActionGroup(ScriptEditor editor, String groupName) {

		final PerformanceStats stats = PerformanceStats
				.getStats(PERF_REFACTOR_ACTION_GROUP, this);
		stats.startRun();

		fSite = editor.getEditorSite();
		fEditor = editor;

		fGroupName = groupName;
		ISelection selection = editor.getSelectionProvider().getSelection();

		fRenameAction = new RenameAction(editor);
		initAction(fRenameAction, selection,
				IScriptEditorActionDefinitionIds.RENAME_ELEMENT);
		editor.setAction("RenameElement", fRenameAction); //$NON-NLS-1$
		//
		fMoveAction = new MoveAction(editor);
		initAction(fMoveAction, selection,
				IScriptEditorActionDefinitionIds.MOVE_ELEMENT);
		editor.setAction("MoveElement", fMoveAction); //$NON-NLS-1$
		final Descriptor<IEditorActionDelegate>[] delegates = new LazyExtensionManager<IEditorActionDelegate>(
				DLTKUIPlugin.PLUGIN_ID + ".refactoring") {
			@Override
			protected boolean isValidElement(IConfigurationElement element) {
				return "action".equals(element.getName())
						&& fEditor.getLanguageToolkit().getNatureId()
								.equals(element.getAttribute("nature"));
			}
		}.getDescriptors();
		for (Descriptor<IEditorActionDelegate> descriptor : delegates) {
			final IEditorActionDelegate delegate = descriptor.get();
			if (delegate != null) {
				final ContributedRefactoringAction action = new ContributedRefactoringAction(
						fEditor, delegate);
				String id = descriptor.getAttribute("id");
				action.setId(id);
				action.setText(descriptor.getAttribute("label"));
				initAction(action, selection, id);
				editor.setAction(id, action);
				fContributedActions.add(action);
			}
		}
		//
		// fModifyParametersAction= new ModifyParametersAction(editor);
		// initAction(fModifyParametersAction, selection,
		// IScriptEditorActionDefinitionIds.MODIFY_METHOD_PARAMETERS);
		// editor.setAction("ModifyParameters", fModifyParametersAction);
		// //$NON-NLS-1$
		//
		// fConvertAnonymousToNestedAction= new
		// ConvertAnonymousToNestedAction(editor);
		// initUpdatingAction(fConvertAnonymousToNestedAction, provider,
		// selection,
		// IScriptEditorActionDefinitionIds.CONVERT_ANONYMOUS_TO_NESTED);
		// editor.setAction("ConvertAnonymousToNested",
		// fConvertAnonymousToNestedAction); //$NON-NLS-1$
		//
		// fConvertNestedToTopAction= new ConvertNestedToTopAction(editor);
		// initAction(fConvertNestedToTopAction, selection,
		// IScriptEditorActionDefinitionIds.MOVE_INNER_TO_TOP);
		// editor.setAction("MoveInnerToTop", fConvertNestedToTopAction);
		// //$NON-NLS-1$
		//
		// fPullUpAction= new PullUpAction(editor);
		// initAction(fPullUpAction, selection,
		// IScriptEditorActionDefinitionIds.PULL_UP);
		// editor.setAction("PullUp", fPullUpAction); //$NON-NLS-1$
		//
		// fPushDownAction= new PushDownAction(editor);
		// initAction(fPushDownAction, selection,
		// IScriptEditorActionDefinitionIds.PUSH_DOWN);
		// editor.setAction("PushDown", fPushDownAction); //$NON-NLS-1$
		//
		// fExtractSupertypeAction= new ExtractSuperTypeAction(editor);
		// initAction(fExtractSupertypeAction, selection,
		// ExtractSuperTypeAction.EXTRACT_SUPERTYPE);
		// editor.setAction("ExtractSupertype", fExtractSupertypeAction);
		// //$NON-NLS-1$
		//
		// fExtractInterfaceAction= new ExtractInterfaceAction(editor);
		// initAction(fExtractInterfaceAction, selection,
		// IScriptEditorActionDefinitionIds.EXTRACT_INTERFACE);
		// editor.setAction("ExtractInterface", fExtractInterfaceAction);
		// //$NON-NLS-1$
		//
		// fChangeTypeAction= new ChangeTypeAction(editor);
		// initUpdatingAction(fChangeTypeAction, provider, selection,
		// IScriptEditorActionDefinitionIds.CHANGE_TYPE);
		// editor.setAction("ChangeType", fChangeTypeAction); //$NON-NLS-1$
		//
		// fUseSupertypeAction= new UseSupertypeAction(editor);
		// initAction(fUseSupertypeAction, selection,
		// IScriptEditorActionDefinitionIds.USE_SUPERTYPE);
		// editor.setAction("UseSupertype", fUseSupertypeAction); //$NON-NLS-1$
		//
		// fInferTypeArgumentsAction= new InferTypeArgumentsAction(editor);
		// initAction(fInferTypeArgumentsAction, selection,
		// IScriptEditorActionDefinitionIds.INFER_TYPE_ARGUMENTS_ACTION);
		// editor.setAction("InferTypeArguments", fInferTypeArgumentsAction);
		// //$NON-NLS-1$
		//
		// fInlineAction= new InlineAction(editor);
		// initAction(fInlineAction, selection,
		// IScriptEditorActionDefinitionIds.INLINE);
		// editor.setAction("Inline", fInlineAction); //$NON-NLS-1$
		//
		// fExtractMethodAction= new ExtractMethodAction(editor);
		// initUpdatingAction(fExtractMethodAction, provider, selection,
		// IScriptEditorActionDefinitionIds.EXTRACT_METHOD);
		// editor.setAction("ExtractMethod", fExtractMethodAction);
		// //$NON-NLS-1$
		//
		// fExtractTempAction= new ExtractTempAction(editor);
		// initUpdatingAction(fExtractTempAction, provider, selection,
		// IScriptEditorActionDefinitionIds.EXTRACT_LOCAL_VARIABLE);
		// editor.setAction("ExtractLocalVariable", fExtractTempAction);
		// //$NON-NLS-1$
		//
		// fExtractConstantAction= new ExtractConstantAction(editor);
		// initUpdatingAction(fExtractConstantAction, provider, selection,
		// IScriptEditorActionDefinitionIds.EXTRACT_CONSTANT);
		// editor.setAction("ExtractConstant", fExtractConstantAction);
		// //$NON-NLS-1$
		//
		// // fReplaceInvocationsAction= new ReplaceInvocationsAction(editor);
		// // initUpdatingAction(fReplaceInvocationsAction, provider, selection,
		// IScriptEditorActionDefinitionIds.REPLACE_INVOCATIONS);
		//// editor.setAction("ReplaceInvocations", fReplaceInvocationsAction);
		// //$NON-NLS-1$
		//
		// fIntroduceIndirectionAction= new IntroduceIndirectionAction(editor);
		// initUpdatingAction(fIntroduceIndirectionAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INTRODUCE_INDIRECTION);
		// editor.setAction("IntroduceIndirection",
		// fIntroduceIndirectionAction); //$NON-NLS-1$
		//
		// fIntroduceParameterAction= new IntroduceParameterAction(editor);
		// initUpdatingAction(fIntroduceParameterAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INTRODUCE_PARAMETER);
		// editor.setAction("IntroduceParameter", fIntroduceParameterAction);
		// //$NON-NLS-1$
		//
		// fIntroduceFactoryAction= new IntroduceFactoryAction(editor);
		// initUpdatingAction(fIntroduceFactoryAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INTRODUCE_FACTORY);
		// editor.setAction("IntroduceFactory", fIntroduceFactoryAction);
		// //$NON-NLS-1$
		//
		// fConvertLocalToFieldAction= new ConvertLocalToFieldAction(editor);
		// initUpdatingAction(fConvertLocalToFieldAction, provider, selection,
		// IScriptEditorActionDefinitionIds.PROMOTE_LOCAL_VARIABLE);
		// editor.setAction("PromoteTemp", fConvertLocalToFieldAction);
		// //$NON-NLS-1$
		//
		// fSelfEncapsulateField= new SelfEncapsulateFieldAction(editor);
		// initAction(fSelfEncapsulateField, selection,
		// IScriptEditorActionDefinitionIds.SELF_ENCAPSULATE_FIELD);
		// editor.setAction("SelfEncapsulateField", fSelfEncapsulateField);
		// //$NON-NLS-1$
		//
		// fQuickAccessAction= new RefactorQuickAccessAction(editor);
		// fKeyBindingService= editor.getEditorSite().getKeyBindingService();
		// fKeyBindingService.registerAction(fQuickAccessAction);

		installQuickAccessAction();

		stats.endRun();
	}

	private RefactorActionGroup(IWorkbenchSite site,
			final IDLTKLanguageToolkit toolkit) {

		final PerformanceStats stats = PerformanceStats
				.getStats(PERF_REFACTOR_ACTION_GROUP, this);
		stats.startRun();

		fSite = site;
		ISelectionProvider provider = fSite.getSelectionProvider();
		ISelection selection = provider.getSelection();

		fMoveAction = new MoveAction(site);
		initUpdatingAction(fMoveAction, provider, selection,
				IScriptEditorActionDefinitionIds.MOVE_ELEMENT);
		//
		fRenameAction = new RenameAction(site);
		initUpdatingAction(fRenameAction, provider, selection,
				IScriptEditorActionDefinitionIds.RENAME_ELEMENT);
		if (toolkit != null) {
			/*
			 * final Descriptor<IEditorActionDelegate>[] delegates = new
			 * LazyExtensionManager<IEditorActionDelegate>(
			 * DLTKUIPlugin.PLUGIN_ID + ".refactoring") { protected boolean
			 * isValidElement(IConfigurationElement element) { return
			 * "action".equals(element.getName()) &&
			 * toolkit.getNatureId().equals( element.getAttribute("nature")); }
			 * }.getDescriptors(); for (Descriptor<IEditorActionDelegate>
			 * descriptor : delegates) { final IEditorActionDelegate delegate =
			 * descriptor.get(); if (delegate != null) { final
			 * ContributedRefactoringAction action = new
			 * ContributedRefactoringAction( site, delegate); String id =
			 * descriptor.getAttribute("id"); action.setId(id);
			 * action.setText(descriptor.getAttribute("label"));
			 * initUpdatingAction(action, provider, selection, id);
			 * fContributedActions.add(action); } }
			 */
		}
		//
		// fModifyParametersAction= new ModifyParametersAction(fSite);
		// initUpdatingAction(fModifyParametersAction, provider, selection,
		// IScriptEditorActionDefinitionIds.MODIFY_METHOD_PARAMETERS);
		//
		// fPullUpAction= new PullUpAction(fSite);
		// initUpdatingAction(fPullUpAction, provider, selection,
		// IScriptEditorActionDefinitionIds.PULL_UP);
		//
		// fPushDownAction= new PushDownAction(fSite);
		// initUpdatingAction(fPushDownAction, provider, selection,
		// IScriptEditorActionDefinitionIds.PUSH_DOWN);
		//
		// fSelfEncapsulateField= new SelfEncapsulateFieldAction(fSite);
		// initUpdatingAction(fSelfEncapsulateField, provider, selection,
		// IScriptEditorActionDefinitionIds.SELF_ENCAPSULATE_FIELD);
		//
		// fExtractSupertypeAction= new ExtractSuperTypeAction(fSite);
		// initUpdatingAction(fExtractSupertypeAction, provider, selection,
		// ExtractSuperTypeAction.EXTRACT_SUPERTYPE);
		//
		// fExtractInterfaceAction= new ExtractInterfaceAction(fSite);
		// initUpdatingAction(fExtractInterfaceAction, provider, selection,
		// IScriptEditorActionDefinitionIds.EXTRACT_INTERFACE);
		//
		// fChangeTypeAction= new ChangeTypeAction(fSite);
		// initUpdatingAction(fChangeTypeAction, provider, selection,
		// IScriptEditorActionDefinitionIds.CHANGE_TYPE);
		//
		// fConvertNestedToTopAction= new ConvertNestedToTopAction(fSite);
		// initUpdatingAction(fConvertNestedToTopAction, provider, selection,
		// IScriptEditorActionDefinitionIds.MOVE_INNER_TO_TOP);
		//
		// fUseSupertypeAction= new UseSupertypeAction(fSite);
		// initUpdatingAction(fUseSupertypeAction, provider, selection,
		// IScriptEditorActionDefinitionIds.USE_SUPERTYPE);
		//
		// fInferTypeArgumentsAction= new InferTypeArgumentsAction(fSite);
		// initUpdatingAction(fInferTypeArgumentsAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INFER_TYPE_ARGUMENTS_ACTION);
		//
		// fInlineAction= new InlineAction(fSite);
		// initUpdatingAction(fInlineAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INLINE);
		//
		// // fReplaceInvocationsAction= new ReplaceInvocationsAction(fSite);
		// // initUpdatingAction(fReplaceInvocationsAction, provider, selection,
		// IScriptEditorActionDefinitionIds.REPLACE_INVOCATIONS);
		//
		// fIntroduceIndirectionAction= new IntroduceIndirectionAction(fSite);
		// initUpdatingAction(fIntroduceIndirectionAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INTRODUCE_INDIRECTION);
		//
		// fIntroduceFactoryAction= new IntroduceFactoryAction(fSite);
		// initUpdatingAction(fIntroduceFactoryAction, provider, selection,
		// IScriptEditorActionDefinitionIds.INTRODUCE_FACTORY);
		//
		// fConvertAnonymousToNestedAction= new
		// ConvertAnonymousToNestedAction(fSite);
		// initUpdatingAction(fConvertAnonymousToNestedAction, provider,
		// selection,
		// IScriptEditorActionDefinitionIds.CONVERT_ANONYMOUS_TO_NESTED);
		//
		// fKeyBindingService= keyBindingService;
		// if (fKeyBindingService != null) {
		// fQuickAccessAction= new RefactorQuickAccessAction(null);
		// fKeyBindingService.registerAction(fQuickAccessAction);
		// }

		stats.endRun();
	}

	private void installQuickAccessAction() {
		fHandlerService = fSite.getService(IHandlerService.class);
		if (fHandlerService != null) {
			final QuickMenuCreator creator = new QuickMenuCreator() {
				@Override
				protected void fillMenu(IMenuManager menu) {
					fillQuickMenu(menu);
				}
			};
			IHandler handler = new AbstractHandler() {
				@Override
				public Object execute(ExecutionEvent event)
						throws ExecutionException {
					creator.createMenu();
					return null;
				}
			};
			fQuickAccessHandlerActivation = fHandlerService
					.activateHandler(QUICK_MENU_ID, handler);
		}
	}

	private void initAction(SelectionDispatchAction action,
			ISelection selection, String actionDefinitionId) {
		initUpdatingAction(action, null, selection, actionDefinitionId);
	}

	/**
	 * Sets actionDefinitionId, updates enablement, adds to fActions, and adds
	 * selection changed listener if provider is not <code>null</code>.
	 *
	 * @param action
	 * @param provider
	 *            can be <code>null</code>
	 * @param selection
	 * @param actionDefinitionId
	 */
	private void initUpdatingAction(SelectionDispatchAction action,
			ISelectionProvider provider, ISelection selection,
			String actionDefinitionId) {
		action.setActionDefinitionId(actionDefinitionId);
		action.update(selection);
		if (provider != null)
			provider.addSelectionChangedListener(action);
		fActions.add(action);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.SELF_ENCAPSULATE_FIELD,
		// fSelfEncapsulateField);
		actionBars.setGlobalActionHandler(DLTKActionConstants.MOVE,
				fMoveAction);
		actionBars.setGlobalActionHandler(DLTKActionConstants.RENAME,
				fRenameAction);
		for (ContributedRefactoringAction action : fContributedActions)
			actionBars.setGlobalActionHandler(action.getId(), action);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.MODIFY_PARAMETERS,
		// fModifyParametersAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.PULL_UP,
		// fPullUpAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.PUSH_DOWN,
		// fPushDownAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.EXTRACT_TEMP,
		// fExtractTempAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.EXTRACT_CONSTANT,
		// fExtractConstantAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.INTRODUCE_PARAMETER,
		// fIntroduceParameterAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.INTRODUCE_FACTORY,
		// fIntroduceFactoryAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.EXTRACT_METHOD,
		// fExtractMethodAction);
		// //
		// actionBars.setGlobalActionHandler(DLTKActionConstants.REPLACE_INVOCATIONS,
		// fReplaceInvocationsAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.INTRODUCE_INDIRECTION,
		// fIntroduceIndirectionAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.INLINE,
		// fInlineAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.EXTRACT_INTERFACE,
		// fExtractInterfaceAction);
		// actionBars.setGlobalActionHandler(ExtractSuperTypeAction.EXTRACT_SUPERTYPES,
		// fExtractSupertypeAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.CHANGE_TYPE,
		// fChangeTypeAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.CONVERT_NESTED_TO_TOP,
		// fConvertNestedToTopAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.USE_SUPERTYPE,
		// fUseSupertypeAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.INFER_TYPE_ARGUMENTS,
		// fInferTypeArgumentsAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.CONVERT_LOCAL_TO_FIELD,
		// fConvertLocalToFieldAction);
		// actionBars.setGlobalActionHandler(DLTKActionConstants.CONVERT_ANONYMOUS_TO_NESTED,
		// fConvertAnonymousToNestedAction);
		if (fUndoRedoActionGroup != null) {
			fUndoRedoActionGroup.fillActionBars(actionBars);
		}
	}

	/**
	 * Retargets the File actions with the corresponding refactoring actions.
	 *
	 * @param actionBars
	 *            the action bar to register the move and rename action with
	 */
	public void retargetFileMenuActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(),
				fRenameAction);
		actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(),
				fMoveAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		addRefactorSubmenu(menu);
	}

	@Override
	public void dispose() {
		ISelectionProvider provider = fSite.getSelectionProvider();
		// disposeAction(fSelfEncapsulateField, provider);
		disposeAction(fMoveAction, provider);
		disposeAction(fRenameAction, provider);
		for (ContributedRefactoringAction action : fContributedActions) {
			disposeAction(action, provider);
		}
		// disposeAction(fModifyParametersAction, provider);
		// disposeAction(fPullUpAction, provider);
		// disposeAction(fPushDownAction, provider);
		// disposeAction(fExtractTempAction, provider);
		// disposeAction(fExtractConstantAction, provider);
		// disposeAction(fIntroduceParameterAction, provider);
		// disposeAction(fIntroduceFactoryAction, provider);
		// disposeAction(fExtractMethodAction, provider);
		// disposeAction(fIntroduceIndirectionAction, provider);
		// disposeAction(fInlineAction, provider);
		// disposeAction(fReplaceInvocationsAction, provider);
		// disposeAction(fExtractInterfaceAction, provider);
		// disposeAction(fExtractSupertypeAction, provider);
		// disposeAction(fChangeTypeAction, provider);
		// disposeAction(fConvertNestedToTopAction, provider);
		// disposeAction(fUseSupertypeAction, provider);
		// disposeAction(fInferTypeArgumentsAction, provider);
		// disposeAction(fConvertLocalToFieldAction, provider);
		// disposeAction(fConvertAnonymousToNestedAction, provider);
		if (fQuickAccessHandlerActivation != null && fHandlerService != null) {
			fHandlerService.deactivateHandler(fQuickAccessHandlerActivation);
		}
		if (fUndoRedoActionGroup != null) {
			fUndoRedoActionGroup.dispose();
		}
		super.dispose();
	}

	private void disposeAction(ISelectionChangedListener action,
			ISelectionProvider provider) {
		if (action != null)
			provider.removeSelectionChangedListener(action);
	}

	private void addRefactorSubmenu(IMenuManager menu) {
		String menuText = ActionMessages.RefactorMenu_label;
		// if (fQuickAccessAction != null) {
		// menuText= fQuickAccessAction.addShortcut(menuText);
		// }
		MenuManager refactorSubmenu = new MenuManager(menuText, MENU_ID);
		refactorSubmenu.setActionDefinitionId(QUICK_MENU_ID);
		if (fEditor != null) {
			IModelElement element = SelectionConverter.getInput(fEditor);
			if (element != null && ActionUtil.isOnBuildPath(element)) {
				refactorSubmenu
						.addMenuListener(manager -> refactorMenuShown(manager));
				refactorSubmenu.add(fNoActionAvailable);
				menu.appendToGroup(fGroupName, refactorSubmenu);
			}
		} else {
			ISelection selection = fSite.getSelectionProvider().getSelection();
			for (SelectionDispatchAction action : fActions) {
				action.update(selection);
			}
			if (fillRefactorMenu(refactorSubmenu) > 0) {
				menu.appendToGroup(fGroupName, refactorSubmenu);
			}
		}
	}

	private int fillRefactorMenu(IMenuManager refactorSubmenu) {
		int added = 0;
		refactorSubmenu.add(new Separator(GROUP_REORG));
		added += addAction(refactorSubmenu, fRenameAction);
		added += addAction(refactorSubmenu, fMoveAction);
		refactorSubmenu.add(new Separator(GROUP_CODING));
		for (IAction action : fContributedActions) {
			added += addAction(refactorSubmenu, action);
		}
		// added+= addAction(refactorSubmenu, fModifyParametersAction);
		// added+= addAction(refactorSubmenu, fExtractMethodAction);
		// added+= addAction(refactorSubmenu, fExtractTempAction);
		// added+= addAction(refactorSubmenu, fExtractConstantAction);
		// added+= addAction(refactorSubmenu, fInlineAction);
		refactorSubmenu.add(new Separator(GROUP_REORG2));
		// added+= addAction(refactorSubmenu, fConvertAnonymousToNestedAction);
		// added+= addAction(refactorSubmenu, fConvertNestedToTopAction);
		// added+= addAction(refactorSubmenu, fConvertLocalToFieldAction);
		refactorSubmenu.add(new Separator(GROUP_TYPE));
		// added+= addAction(refactorSubmenu, fExtractInterfaceAction);
		// added+= addAction(refactorSubmenu, fExtractSupertypeAction);
		// added+= addAction(refactorSubmenu, fUseSupertypeAction);
		// added+= addAction(refactorSubmenu, fPullUpAction);
		// added+= addAction(refactorSubmenu, fPushDownAction);
		refactorSubmenu.add(new Separator(GROUP_CODING2));
		// added+= addAction(refactorSubmenu, fIntroduceIndirectionAction);
		// added+= addAction(refactorSubmenu, fIntroduceFactoryAction);
		// added+= addAction(refactorSubmenu, fIntroduceParameterAction);
		// added+= addAction(refactorSubmenu, fSelfEncapsulateField);
		// added+= addAction(refactorSubmenu, fReplaceInvocationsAction);
		refactorSubmenu.add(new Separator(GROUP_TYPE2));
		// added+= addAction(refactorSubmenu, fChangeTypeAction);
		// added+= addAction(refactorSubmenu, fInferTypeArgumentsAction);
		return added;
	}

	private int addAction(IMenuManager menu, IAction action) {
		if (action != null && action.isEnabled()) {
			menu.add(action);
			return 1;
		}
		return 1;
	}

	private void refactorMenuShown(final IMenuManager refactorSubmenu) {
		// we know that we have an MenuManager since we created it in
		// addRefactorSubmenu.
		Menu menu = ((MenuManager) refactorSubmenu).getMenu();
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuHidden(MenuEvent e) {
				refactorMenuHidden(refactorSubmenu);
			}
		});
		ITextSelection textSelection = (ITextSelection) fEditor
				.getSelectionProvider().getSelection();
		ModelTextSelection javaSelection = new ModelTextSelection(
				getEditorInput(), getDocument(), textSelection.getOffset(),
				textSelection.getLength());

		for (SelectionDispatchAction action : fActions) {
			action.update(javaSelection);
		}
		refactorSubmenu.removeAll();
		if (fillRefactorMenu(refactorSubmenu) == 0)
			refactorSubmenu.add(fNoActionAvailable);
	}

	private void refactorMenuHidden(IMenuManager manager) {
		ITextSelection textSelection = (ITextSelection) fEditor
				.getSelectionProvider().getSelection();
		for (SelectionDispatchAction action : fActions) {
			action.update(textSelection);
		}
	}

	private IModelElement getEditorInput() {
		return DLTKUIPlugin.getDefault().getWorkingCopyManager()
				.getWorkingCopy(fEditor.getEditorInput());
	}

	private IDocument getDocument() {
		return DLTKUIPlugin.getDefault().getSourceModuleDocumentProvider()
				.getDocument(fEditor.getEditorInput());
	}

	private void fillQuickMenu(IMenuManager menu) {
		if (fEditor != null) {
			IModelElement element = SelectionConverter.getInput(fEditor);
			if (element == null || !ActionUtil.isOnBuildPath(element)) {
				menu.add(fNoActionAvailable);
				return;
			}
			ITextSelection textSelection = (ITextSelection) fEditor
					.getSelectionProvider().getSelection();
			ModelTextSelection scriptSelection = new ModelTextSelection(
					getEditorInput(), getDocument(), textSelection.getOffset(),
					textSelection.getLength());
			for (SelectionDispatchAction action : fActions)
				action.update(scriptSelection);
			fillRefactorMenu(menu);
			for (SelectionDispatchAction action : fActions)
				action.update(textSelection);
		} else {
			ISelection selection = fSite.getSelectionProvider().getSelection();
			for (SelectionDispatchAction action : fActions)
				action.update(selection);
			fillRefactorMenu(menu);
		}
	}
}
