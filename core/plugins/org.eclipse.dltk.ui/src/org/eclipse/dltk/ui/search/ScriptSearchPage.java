/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM Corporation and others. - initial API and Implementation
 *     xored software, Inc. - Ported to DLTK from JDT
 *     Alon Peled <alon@zend.com> - Fix of bug bug 235137
 *******************************************************************************/
package org.eclipse.dltk.ui.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.ScriptUtils;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.internal.ui.actions.SelectionConverter;
import org.eclipse.dltk.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.dltk.internal.ui.search.DLTKSearchQuery;
import org.eclipse.dltk.internal.ui.search.DLTKSearchScopeFactory;
import org.eclipse.dltk.internal.ui.search.PatternStrings;
import org.eclipse.dltk.internal.ui.search.SearchMessages;
import org.eclipse.dltk.internal.ui.search.SearchUtil;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class ScriptSearchPage extends DialogPage implements
		ISearchPage, IDLTKSearchConstants {

	private static class SearchPatternData {
		private int searchFor;
		private int limitTo;
		private String pattern;
		private boolean isCaseSensitive;
		private IModelElement modelElement;
		private boolean includeInterpreterEnvironment;
		private int scope;
		private IWorkingSet[] workingSets;

		public SearchPatternData(int searchFor, int limitTo,
				boolean isCaseSensitive, String pattern, IModelElement element,
				boolean includeInterpreterEnvironment) {
			this(searchFor, limitTo, pattern, isCaseSensitive, element,
					ISearchPageContainer.WORKSPACE_SCOPE, null,
					includeInterpreterEnvironment);
		}

		public SearchPatternData(int searchFor, int limitTo, String pattern,
				boolean isCaseSensitive, IModelElement element, int scope,
				IWorkingSet[] workingSets, boolean includeInterpreterEnvironment) {
			this.searchFor = searchFor;
			this.limitTo = limitTo;
			this.pattern = pattern;
			this.isCaseSensitive = isCaseSensitive;
			this.scope = scope;
			this.workingSets = workingSets;
			this.includeInterpreterEnvironment = includeInterpreterEnvironment;

			setModelElement(element);
		}

		public void setModelElement(IModelElement modelElement) {
			this.modelElement = modelElement;
		}

		public boolean isCaseSensitive() {
			return isCaseSensitive;
		}

		public IModelElement getModelElement() {
			return modelElement;
		}

		public int getLimitTo() {
			return limitTo;
		}

		public String getPattern() {
			return pattern;
		}

		public int getScope() {
			return scope;
		}

		public int getSearchFor() {
			return searchFor;
		}

		public IWorkingSet[] getWorkingSets() {
			return workingSets;
		}

		public boolean includesInterpreterEnvironment() {
			return includeInterpreterEnvironment;
		}

		public void store(IDialogSettings settings) {
			settings.put("searchFor", searchFor); //$NON-NLS-1$
			settings.put("scope", scope); //$NON-NLS-1$
			settings.put("pattern", pattern); //$NON-NLS-1$
			settings.put("limitTo", limitTo); //$NON-NLS-1$
			settings.put(
					"modelElement", modelElement != null ? modelElement.getHandleIdentifier() : ""); //$NON-NLS-1$ //$NON-NLS-2$
			settings.put("isCaseSensitive", isCaseSensitive); //$NON-NLS-1$
			if (workingSets != null) {
				String[] wsIds = new String[workingSets.length];
				for (int i = 0; i < workingSets.length; i++) {
					wsIds[i] = workingSets[i].getName();
				}
				settings.put("workingSets", wsIds); //$NON-NLS-1$
			} else {
				settings.put("workingSets", new String[0]); //$NON-NLS-1$
			}
			settings.put(
					"includeInterpreterEnvironment", includeInterpreterEnvironment); //$NON-NLS-1$
		}

		public static SearchPatternData create(IDialogSettings settings) {
			String pattern = settings.get("pattern"); //$NON-NLS-1$
			if (pattern.length() == 0) {
				return null;
			}
			IModelElement elem = null;
			String handleId = settings.get("modelElement"); //$NON-NLS-1$
			if (handleId != null && handleId.length() > 0) {
				IModelElement restored = DLTKCore.create(handleId);
				if (restored != null && isSearchableType(restored)
						&& restored.exists()) {
					elem = restored;
				}
			}
			String[] wsIds = settings.getArray("workingSets"); //$NON-NLS-1$
			IWorkingSet[] workingSets = null;
			if (wsIds != null && wsIds.length > 0) {
				IWorkingSetManager workingSetManager = PlatformUI
						.getWorkbench().getWorkingSetManager();
				workingSets = new IWorkingSet[wsIds.length];
				for (int i = 0; workingSets != null && i < wsIds.length; i++) {
					workingSets[i] = workingSetManager.getWorkingSet(wsIds[i]);
					if (workingSets[i] == null) {
						workingSets = null;
					}
				}
			}

			try {
				int searchFor = settings.getInt("searchFor"); //$NON-NLS-1$
				int scope = settings.getInt("scope"); //$NON-NLS-1$
				int limitTo = settings.getInt("limitTo"); //$NON-NLS-1$
				boolean isCaseSensitive = settings
						.getBoolean("isCaseSensitive"); //$NON-NLS-1$

				boolean includeInterpreterEnvironment;
				if (settings.get("includeInterpreterEnvironment") != null) { //$NON-NLS-1$
					includeInterpreterEnvironment = settings
							.getBoolean("includeInterpreterEnvironment"); //$NON-NLS-1$
				} else {
					includeInterpreterEnvironment = forceIncludeInterpreterEnvironment(limitTo);
				}
				return new SearchPatternData(searchFor, limitTo, pattern,
						isCaseSensitive, elem, scope, workingSets,
						includeInterpreterEnvironment);
			} catch (NumberFormatException e) {
				return null;
			}
		}

	}

	public static final String PARTICIPANT_EXTENSION_POINT = "org.eclipse.dltk.ui.queryParticipants"; //$NON-NLS-1$

	/**
	 * At the moment DLTK provides only abstract base class for search page, so
	 * this identifier doesn't have any sense.
	 */
	@Deprecated
	public static final String EXTENSION_POINT_ID = "org.eclipse.dltk.ui.DLTKSearchPage"; //$NON-NLS-1$

	private static final int HISTORY_SIZE = 12;

	// Dialog store id constants
	private final static String PAGE_NAME = "DLTKSearchPage"; //$NON-NLS-1$
	private final static String STORE_CASE_SENSITIVE = "CASE_SENSITIVE"; //$NON-NLS-1$
	private final static String STORE_HISTORY = "HISTORY"; //$NON-NLS-1$
	private final static String STORE_HISTORY_SIZE = "HISTORY_SIZE"; //$NON-NLS-1$

	private final List<SearchPatternData> fPreviousSearchPatterns;

	private SearchPatternData fInitialData;
	private IModelElement fModelElement;
	private boolean fFirstTime = true;
	private IDialogSettings fDialogSettings;
	private boolean fIsCaseSensitive;

	private Combo fPattern;
	private ISearchPageContainer fContainer;
	private Button fCaseSensitive;

	private Button[] fSearchFor;
	private String[] fSearchForText = {
			SearchMessages.SearchPage_searchFor_type,
			SearchMessages.SearchPage_searchFor_method,
			// SearchMessages.SearchPage_searchFor_package,
			// SearchMessages.SearchPage_searchFor_constructor,
			SearchMessages.SearchPage_searchFor_field };

	private Button[] fLimitTo;
	private String[] fLimitToText = {
			SearchMessages.SearchPage_limitTo_declarations,
			// SearchMessages.SearchPage_limitTo_implementors,
			SearchMessages.SearchPage_limitTo_references,
			SearchMessages.SearchPage_limitTo_allOccurrences
	// SearchMessages.SearchPage_limitTo_readReferences,
	// SearchMessages.SearchPage_limitTo_writeReferences
	};

	private Button fIncludeInterpreterEnvironmentCheckbox;

	public ScriptSearchPage() {
		fPreviousSearchPatterns = new ArrayList<SearchPatternData>();
	}

	@Override
	public boolean performAction() {
		return performNewSearch();
	}

	private boolean performNewSearch() {
		SearchPatternData data = getPatternData();

		// Setup search scope
		IDLTKSearchScope scope = null;
		String scopeDescription = ""; //$NON-NLS-1$

		boolean includeInterpreterEnvironment = data
				.includesInterpreterEnvironment();
		DLTKSearchScopeFactory factory = DLTKSearchScopeFactory.getInstance();

		switch (getContainer().getSelectedScope()) {
		case ISearchPageContainer.WORKSPACE_SCOPE:
			scopeDescription = factory
					.getWorkspaceScopeDescription(includeInterpreterEnvironment);
			scope = factory.createWorkspaceScope(includeInterpreterEnvironment,
					getLanguageToolkit());
			break;
		case ISearchPageContainer.SELECTION_SCOPE:
			IModelElement[] modelElements = factory
					.getModelElements(getContainer().getSelection());
			scope = factory.createSearchScope(modelElements,
					includeInterpreterEnvironment, getLanguageToolkit());
			scopeDescription = factory.getSelectionScopeDescription(
					modelElements, includeInterpreterEnvironment);
			break;
		case ISearchPageContainer.SELECTED_PROJECTS_SCOPE: {
			ArrayList<String> res = new ArrayList<String>();
			String[] projectNames = getContainer().getSelectedProjectNames();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for (int i = 0; i < projectNames.length; i++) {
				getEnclosingProjects(res, projectNames[i], root);
			}
			scope = factory.createProjectSearchScope(
					res.toArray(new String[res.size()]),
					includeInterpreterEnvironment, getLanguageToolkit());
			scopeDescription = factory.getProjectScopeDescription(projectNames,
					includeInterpreterEnvironment);
			break;
		}
		case ISearchPageContainer.WORKING_SET_SCOPE: {
			IWorkingSet[] workingSets = getContainer().getSelectedWorkingSets();
			// should not happen - just to be sure
			if (workingSets == null || workingSets.length < 1)
				return false;
			scopeDescription = factory.getWorkingSetScopeDescription(
					workingSets, includeInterpreterEnvironment);
			scope = factory.createSearchScope(workingSets,
					includeInterpreterEnvironment, getLanguageToolkit());
			SearchUtil.updateLRUWorkingSets(workingSets);
		}
		}

		QuerySpecification querySpec = null;
		if (data.getModelElement() != null
				&& getPattern().equals(fInitialData.getPattern())) {
			// if (data.getLimitTo() == IDLTKSearchConstants.REFERENCES)
			// SearchUtil.warnIfBinaryConstant(data.getModelElement(),
			// getShell());
			querySpec = new ElementQuerySpecification(data.getModelElement(),
					data.getLimitTo(), scope, scopeDescription);
		} else {
			querySpec = new PatternQuerySpecification(data.getPattern(),
					data.getSearchFor(), data.isCaseSensitive(),
					data.getLimitTo(), scope, scopeDescription);
			data.setModelElement(null);
		}

		DLTKSearchQuery textSearchJob = new DLTKSearchQuery(querySpec);
		NewSearchUI.runQueryInBackground(textSearchJob);
		return true;
	}

	/**
	 * @param res
	 * @param projectNames
	 * @param root
	 * @param i
	 */
	private void getEnclosingProjects(ArrayList<String> res,
			String projectName, IWorkspaceRoot root) {
		if (res.contains(projectName))
			return;
		IScriptProject project = DLTKCore.create(root.getProject(projectName));
		if (project.exists()) {
			res.add(project.getProject().getName());
			try {
				IBuildpathEntry[] resolvedBuildpath = project
						.getResolvedBuildpath(true);
				for (IBuildpathEntry buildpathEntry : resolvedBuildpath) {
					if (buildpathEntry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
						getEnclosingProjects(res, buildpathEntry.getPath()
								.lastSegment(), root);
					}
				}
			} catch (ModelException e) {
				e.printStackTrace();
			}
		}
	}

	private int getLimitTo() {
		for (int i = 0; i < fLimitTo.length; i++) {
			if (fLimitTo[i].getSelection())
				return i;
		}
		return -1;
	}

	private void setLimitTo(int searchFor, int limitTo) {
		/*
		 * if (!(searchFor == TYPE) && limitTo == IMPLEMENTORS ) { limitTo =
		 * REFERENCES; }
		 *
		 * if (!(searchFor == FIELD) && (limitTo == READ_ACCESSES || limitTo ==
		 * WRITE_ACCESSES)) { limitTo = REFERENCES; }
		 */
		for (int i = 0; i < fLimitTo.length; i++) {
			fLimitTo[i].setSelection(limitTo == i);
		}

		fLimitTo[DECLARATIONS].setEnabled(true);
		// fLimitTo[IMPLEMENTORS].setEnabled( searchFor == TYPE);
		fLimitTo[REFERENCES].setEnabled(true);
		fLimitTo[ALL_OCCURRENCES].setEnabled(true);
		// fLimitTo[READ_ACCESSES].setEnabled(searchFor == FIELD);
		// fLimitTo[WRITE_ACCESSES].setEnabled(searchFor == FIELD);

	}

	private String[] getPreviousSearchPatterns() {
		// Search results are not persistent
		int patternCount = fPreviousSearchPatterns.size();
		String[] patterns = new String[patternCount];
		for (int i = 0; i < patternCount; i++)
			patterns[i] = fPreviousSearchPatterns.get(i).getPattern();
		return patterns;
	}

	private int getSearchFor() {
		for (int i = 0; i < fSearchFor.length; i++) {
			if (fSearchFor[i].getSelection())
				return i;
		}

		return -1;
	}

	private String getPattern() {
		return fPattern.getText();
	}

	private SearchPatternData findInPrevious(String pattern) {
		for (Iterator<SearchPatternData> iter = fPreviousSearchPatterns
				.iterator(); iter.hasNext();) {
			SearchPatternData element = iter.next();
			if (pattern.equals(element.getPattern())) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Return search pattern data and update previous searches. An existing
	 * entry will be updated.
	 */
	private SearchPatternData getPatternData() {
		String pattern = getPattern();
		SearchPatternData match = findInPrevious(pattern);
		if (match != null) {
			fPreviousSearchPatterns.remove(match);
		}
		match = new SearchPatternData(getSearchFor(), getLimitTo(), pattern,
				fCaseSensitive.getSelection(), fModelElement, getContainer()
						.getSelectedScope(), getContainer()
						.getSelectedWorkingSets(),
				fIncludeInterpreterEnvironmentCheckbox.getSelection());

		fPreviousSearchPatterns.add(0, match); // insert on top
		return match;
	}

	/*
	 * Implements method from IDialogPage
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible && fPattern != null) {
			if (fFirstTime) {
				fFirstTime = false;
				// Set item and text here to prevent page from resizing
				fPattern.setItems(getPreviousSearchPatterns());
				initSelections();
			}
			fPattern.setFocus();
		}
		updateOKStatus();
		super.setVisible(visible);
	}

	public boolean isValid() {
		return true;
	}

	// ---- Widget creation ------------------------------------------------

	/**
	 * Creates the page's content.
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		readConfiguration();

		Composite result = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		result.setLayout(layout);

		Control expressionComposite = createExpression(result);
		expressionComposite.setLayoutData(new GridData(GridData.FILL,
				GridData.CENTER, true, false, 2, 1));

		Label separator = new Label(result, SWT.NONE);
		separator.setVisible(false);
		GridData data = new GridData(GridData.FILL, GridData.FILL, false,
				false, 2, 1);
		data.heightHint = convertHeightInCharsToPixels(1) / 3;
		separator.setLayoutData(data);

		Control searchFor = createSearchFor(result);
		searchFor.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false, 1, 1));

		Control limitTo = createLimitTo(result);
		limitTo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				false, 1, 1));

		fIncludeInterpreterEnvironmentCheckbox = new Button(result, SWT.CHECK);
		fIncludeInterpreterEnvironmentCheckbox
				.setText(SearchMessages.SearchPage_searchInterpreterEnvironment_label);
		fIncludeInterpreterEnvironmentCheckbox.setLayoutData(new GridData(
				SWT.FILL, SWT.CENTER, false, false, 2, 1));

		// createParticipants(result);

		SelectionAdapter modelElementInitializer = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// Skip events from non selected buttons
				if (event.widget instanceof Button) {
					if (!((Button) event.widget).getSelection()) {
						return;
					}
				}
				if (getSearchFor() == fInitialData.getSearchFor())
					fModelElement = fInitialData.getModelElement();
				else
					fModelElement = null;
				setLimitTo(getSearchFor(), getLimitTo());
				doPatternModified();
			}
		};

		fSearchFor[TYPE].addSelectionListener(modelElementInitializer);
		fSearchFor[METHOD].addSelectionListener(modelElementInitializer);
		fSearchFor[FIELD].addSelectionListener(modelElementInitializer);
		// fSearchFor[CONSTRUCTOR].addSelectionListener(modelElementInitializer);
		// fSearchFor[PACKAGE].addSelectionListener(modelElementInitializer);

		setControl(result);

		Dialog.applyDialogFont(result);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(result,
		// IJavaHelpContextIds.JAVA_SEARCH_PAGE);
		if (DLTKCore.DEBUG) {
			System.out.println("TODO: Add help support here..."); //$NON-NLS-1$
		}
	}

	private Control createExpression(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		result.setLayout(layout);

		// Pattern text + info
		Label label = new Label(result, SWT.LEFT);
		label.setText(SearchMessages.SearchPage_expression_label);
		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false,
				false, 2, 1));

		// Pattern combo
		fPattern = new Combo(result, SWT.SINGLE | SWT.BORDER);
		fPattern.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handlePatternSelected();
				updateOKStatus();
			}
		});
		fPattern.addModifyListener(e -> {
			doPatternModified();
			updateOKStatus();

		});
		TextFieldNavigationHandler.install(fPattern);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false,
				1, 1);
		data.widthHint = convertWidthInCharsToPixels(50);
		fPattern.setLayoutData(data);

		// Ignore case checkbox
		fCaseSensitive = new Button(result, SWT.CHECK);
		fCaseSensitive
				.setText(SearchMessages.SearchPage_expression_caseSensitive);
		fCaseSensitive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fIsCaseSensitive = fCaseSensitive.getSelection();
			}
		});
		fCaseSensitive.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				false, false, 1, 1));

		return result;
	}

	final void updateOKStatus() {
		boolean isValid = isValidSearchPattern();
		getContainer().setPerformActionEnabled(isValid);
	}

	private boolean isValidSearchPattern() {
		if (getPattern().length() == 0) {
			return false;
		}
		if (fModelElement != null) {
			return true;
		}
		return SearchPattern
				.createPattern(getPattern(), getSearchFor(), getLimitTo(),
						SearchPattern.R_EXACT_MATCH, getLanguageToolkit()) != null;
	}

	@Override
	public void dispose() {
		writeConfiguration();
		super.dispose();
	}

	private void doPatternModified() {
		if (fInitialData != null
				&& getPattern().equals(fInitialData.getPattern())
				&& fInitialData.getModelElement() != null
				&& fInitialData.getSearchFor() == getSearchFor()) {
			fCaseSensitive.setEnabled(false);
			fCaseSensitive.setSelection(true);
			fModelElement = fInitialData.getModelElement();
		} else {
			fCaseSensitive.setEnabled(true);
			fCaseSensitive.setSelection(fIsCaseSensitive);
			fModelElement = null;
		}
	}

	private void handlePatternSelected() {
		int selectionIndex = fPattern.getSelectionIndex();
		if (selectionIndex < 0
				|| selectionIndex >= fPreviousSearchPatterns.size())
			return;

		SearchPatternData initialData = fPreviousSearchPatterns
				.get(selectionIndex);

		setSearchFor(initialData.getSearchFor());
		setLimitTo(initialData.getSearchFor(), initialData.getLimitTo());

		fPattern.setText(initialData.getPattern());
		fIsCaseSensitive = initialData.isCaseSensitive();
		fModelElement = initialData.getModelElement();
		fCaseSensitive.setEnabled(fModelElement == null);
		fCaseSensitive.setSelection(initialData.isCaseSensitive());

		if (initialData.getWorkingSets() != null)
			getContainer().setSelectedWorkingSets(initialData.getWorkingSets());
		else
			getContainer().setSelectedScope(initialData.getScope());

		fInitialData = initialData;
	}

	private void setSearchFor(int searchFor) {
		for (int i = 0; i < fSearchFor.length; i++) {
			fSearchFor[i].setSelection(searchFor == i);
		}
	}

	private Control createSearchFor(Composite parent) {
		Group result = new Group(parent, SWT.NONE);
		result.setText(SearchMessages.SearchPage_searchFor_label);
		result.setLayout(new GridLayout(2, true));

		fSearchFor = new Button[fSearchForText.length];
		for (int i = 0; i < fSearchForText.length; i++) {
			Button button = new Button(result, SWT.RADIO);
			button.setText(fSearchForText[i]);
			button.setSelection(i == TYPE);
			button.setLayoutData(new GridData());
			fSearchFor[i] = button;
		}

		// Fill with dummy radio buttons
		Label filler = new Label(result, SWT.NONE);
		filler.setVisible(false);
		filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,
				1));

		return result;
	}

	private Control createLimitTo(Composite parent) {
		Group result = new Group(parent, SWT.NONE);
		result.setText(SearchMessages.SearchPage_limitTo_label);
		result.setLayout(new GridLayout(2, true));

		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget instanceof Button) {
					if (((Button) e.widget).getSelection() == false) {
						return;
					}
				}
				updateUseInterpreterEnvironment();
			}
		};

		fLimitTo = new Button[fLimitToText.length];
		for (int i = 0; i < fLimitToText.length; i++) {
			Button button = new Button(result, SWT.RADIO);
			button.setText(fLimitToText[i]);
			fLimitTo[i] = button;
			button.setSelection(i == REFERENCES);
			button.addSelectionListener(listener);
			button.setLayoutData(new GridData());
		}

		return result;
	}

	private void initSelections() {
		ISelection sel = getContainer().getSelection();
		SearchPatternData initData = null;

		if (sel instanceof IStructuredSelection) {
			initData = tryStructuredSelection((IStructuredSelection) sel);
		} else if (sel instanceof ITextSelection) {
			IEditorPart activePart = getActiveEditor();
			if (activePart != null) {
				if (ScriptUtils.checkNature(getLanguageToolkit().getNatureId(),
						activePart, true)) {
					try {
						IModelElement[] elements = SelectionConverter
								.codeResolve(activePart);
						if (elements != null && elements.length > 0) {
							initData = determineInitValuesFrom(elements[0]);
						}
					} catch (ModelException e) {
						// ignore
					}
				} else {
					initData = getDefaultInitValues();
				}
			}
			if (initData == null) {
				initData = trySimpleTextSelection((ITextSelection) sel);
			}
		}
		if (initData == null) {
			initData = getDefaultInitValues();
		}

		fInitialData = initData;
		fModelElement = initData.getModelElement();
		fCaseSensitive.setSelection(initData.isCaseSensitive());
		fCaseSensitive.setEnabled(fModelElement == null);

		setSearchFor(initData.getSearchFor());
		setLimitTo(initData.getSearchFor(), initData.getLimitTo());

		fPattern.setText(initData.getPattern());

		boolean forceIncludeInterpreterEnvironment = forceIncludeInterpreterEnvironment(getLimitTo());
		fIncludeInterpreterEnvironmentCheckbox
				.setEnabled(!forceIncludeInterpreterEnvironment);
		fIncludeInterpreterEnvironmentCheckbox
				.setSelection(forceIncludeInterpreterEnvironment
						|| initData.includesInterpreterEnvironment());
	}

	private void updateUseInterpreterEnvironment() {
		boolean forceIncludeInterpreterEnvironment = forceIncludeInterpreterEnvironment(getLimitTo());
		fIncludeInterpreterEnvironmentCheckbox
				.setEnabled(!forceIncludeInterpreterEnvironment);
		boolean isSelected = true;
		if (!forceIncludeInterpreterEnvironment) {
			isSelected = fIncludeInterpreterEnvironmentCheckbox.getSelection();
		} else {
			isSelected = true;
		}
		fIncludeInterpreterEnvironmentCheckbox.setSelection(isSelected);
	}

	private static boolean forceIncludeInterpreterEnvironment(int limitTo) {
		return limitTo == DECLARATIONS; /* || limitTo == IMPLEMENTORS; */
	}

	private SearchPatternData tryStructuredSelection(
			IStructuredSelection selection) {
		if (selection == null || selection.size() > 1)
			return null;

		Object o = selection.getFirstElement();
		SearchPatternData res = null;
		if (o instanceof IModelElement) {
			res = determineInitValuesFrom((IModelElement) o);
		}
		// else if (o instanceof LogicalPackage) {
		// LogicalPackage lp= (LogicalPackage)o;
		// return new SearchPatternData(PACKAGE, REFERENCES, fIsCaseSensitive,
		// lp.getElementName(), null, false);
		// }
		else if (o instanceof IAdaptable) {
			IModelElement element = ((IAdaptable) o)
					.getAdapter(IModelElement.class);
			if (element != null) {
				res = determineInitValuesFrom(element);
			}
		}
		if (res == null && o instanceof IAdaptable) {
			IWorkbenchAdapter adapter = ((IAdaptable) o)
					.getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				return new SearchPatternData(TYPE, REFERENCES,
						fIsCaseSensitive, adapter.getLabel(o), null, false);
			}
		}
		return res;
	}

	final static boolean isSearchableType(IModelElement element) {
		switch (element.getElementType()) {
		case IModelElement.SCRIPT_FOLDER:
		case IModelElement.PACKAGE_DECLARATION:
			// case IModelElement.IMPORT_DECLARATION:
		case IModelElement.TYPE:
		case IModelElement.FIELD:
		case IModelElement.METHOD:
			return true;
		}
		return false;
	}

	private SearchPatternData determineInitValuesFrom(IModelElement element) {
		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
				.getLanguageToolkit(element);
		if (toolkit != null
				&& !toolkit.getNatureId().equals(
						getLanguageToolkit().getNatureId())) {
			return null;
		}
		DLTKSearchScopeFactory factory = DLTKSearchScopeFactory.getInstance();
		boolean isInsideInterpreterEnvironment = factory
				.isInsideInterpreter(element);

		switch (element.getElementType()) {
		case IModelElement.SCRIPT_FOLDER:
			// case IModelElement.PACKAGE_DECLARATION:
			// return new SearchPatternData(PACKAGE, REFERENCES, true,
			// element.getElementName(), element,
			// isInsideInterpreterEnvironment);
			// case IModelElement.IMPORT_DECLARATION: {
			// IImportDeclaration declaration= (IImportDeclaration) element;
			// if (declaration.isOnDemand()) {
			// String name=
			// Signature.getQualifier(declaration.getElementName());
			// return new SearchPatternData(PACKAGE, DECLARATIONS, true, name,
			// element, true);
			// }
			// return new SearchPatternData(TYPE, DECLARATIONS, true,
			// element.getElementName(), element, true);
			// }
			break;
		case IModelElement.TYPE:
			return new SearchPatternData(TYPE, REFERENCES, true,
					PatternStrings.getTypeSignature((IType) element), element,
					isInsideInterpreterEnvironment);
		case IModelElement.SOURCE_MODULE: {
			if (DLTKCore.DEBUG) {
				System.out
						.println("TODO: DLTKSearchPage: Add init values for source module support."); //$NON-NLS-1$
			}
			// IType mainType= ((ISourceModule) element).
			// if (mainType != null) {
			// return new SearchPatternData(TYPE, REFERENCES, true,
			// PatternStrings.getTypeSignature(mainType), mainType,
			// isInsideInterpreterEnvironment);
			// }
			break;
		}
		case IModelElement.FIELD:
			return new SearchPatternData(FIELD, REFERENCES, true,
					PatternStrings.getFieldSignature((IField) element),
					element, isInsideInterpreterEnvironment);
		case IModelElement.METHOD:
			IMethod method = (IMethod) element;
			int searchFor = /* method.isConstructor() ? CONSTRUCTOR : */METHOD;
			return new SearchPatternData(searchFor, REFERENCES, true,
					PatternStrings.getMethodSignature(method), element,
					isInsideInterpreterEnvironment);
		}
		return null;
	}

	public static boolean isLineDelimiterChar(char ch) {
		return ch == '\n' || ch == '\r';
	}

	private SearchPatternData trySimpleTextSelection(ITextSelection selection) {
		String selectedText = selection.getText();
		if (selectedText != null && selectedText.length() > 0) {
			int i = 0;
			while (i < selectedText.length()
					&& !isLineDelimiterChar(selectedText.charAt(i))) {
				i++;
			}
			if (i > 0) {
				return new SearchPatternData(TYPE, REFERENCES,
						fIsCaseSensitive, selectedText.substring(0, i), null,
						true);
			}
		}
		return null;
	}

	private SearchPatternData getDefaultInitValues() {
		if (!fPreviousSearchPatterns.isEmpty()) {
			return fPreviousSearchPatterns.get(0);
		}
		return new SearchPatternData(TYPE, REFERENCES, fIsCaseSensitive,
				"", null, false); //$NON-NLS-1$
	}

	@Override
	public void setContainer(ISearchPageContainer container) {
		fContainer = container;
	}

	/**
	 * Returns the search page's container.
	 */
	private ISearchPageContainer getContainer() {
		return fContainer;
	}

	private IEditorPart getActiveEditor() {
		IWorkbenchPage activePage = DLTKUIPlugin.getActivePage();
		if (activePage != null) {
			return activePage.getActiveEditor();
		}
		return null;
	}

	// --------------- Configuration handling --------------

	/**
	 * Returns the page settings for this Script search page.
	 *
	 * @return the page settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		final String pageName = getLanguageToolkit().getLanguageName()
				+ PAGE_NAME;
		IDialogSettings settings = DLTKUIPlugin.getDefault()
				.getDialogSettings();
		fDialogSettings = settings.getSection(pageName);
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(pageName);
		return fDialogSettings;
	}

	/**
	 * Initializes itself from the stored page settings.
	 */
	private void readConfiguration() {
		IDialogSettings s = getDialogSettings();
		fIsCaseSensitive = s.getBoolean(STORE_CASE_SENSITIVE);

		try {
			int historySize = s.getInt(STORE_HISTORY_SIZE);
			for (int i = 0; i < historySize; i++) {
				IDialogSettings histSettings = s.getSection(STORE_HISTORY + i);
				if (histSettings != null) {
					SearchPatternData data = SearchPatternData
							.create(histSettings);
					if (data != null) {
						fPreviousSearchPatterns.add(data);
					}
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s = getDialogSettings();
		s.put(STORE_CASE_SENSITIVE, fIsCaseSensitive);

		int historySize = Math
				.min(fPreviousSearchPatterns.size(), HISTORY_SIZE);
		s.put(STORE_HISTORY_SIZE, historySize);
		for (int i = 0; i < historySize; i++) {
			IDialogSettings histSettings = s.addNewSection(STORE_HISTORY + i);
			SearchPatternData data = fPreviousSearchPatterns.get(i);
			data.store(histSettings);
		}
	}

	protected abstract IDLTKLanguageToolkit getLanguageToolkit();
}
