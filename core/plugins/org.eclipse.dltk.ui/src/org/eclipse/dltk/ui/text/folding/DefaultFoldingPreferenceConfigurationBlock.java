package org.eclipse.dltk.ui.text.folding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.preferences.FieldValidators.MinimumNumberValidator;
import org.eclipse.dltk.ui.preferences.ImprovedAbstractConfigurationBlock;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.dltk.ui.preferences.PreferencesMessages;
import org.eclipse.dltk.ui.util.PixelConverter;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 */
public class DefaultFoldingPreferenceConfigurationBlock extends
		ImprovedAbstractConfigurationBlock {

	private static int DEFAULT_MIN_LINES = 2;

	private IFoldingPreferenceBlock documentationBlock;
	private IFoldingPreferenceBlock sourceCodeBlock;

	public DefaultFoldingPreferenceConfigurationBlock(
			OverlayPreferenceStore store, PreferencePage page) {
		super(store, page);

		documentationBlock = createDocumentationBlock(store, page);
		sourceCodeBlock = createSourceCodeBlock(store, page);
	}

	private Composite foldingOptionsComposite;

	@Override
	public Control createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent
				.getFont(), 1, 1, GridData.FILL_BOTH);

		Button enableFolding = SWTFactory.createCheckButton(composite,
				PreferencesMessages.FoldingConfigurationBlock_enable);

		foldingOptionsComposite = SWTFactory.createComposite(composite,
				composite.getFont(), 1, 1, GridData.FILL_BOTH);
		((GridLayout) foldingOptionsComposite.getLayout()).marginHeight = 0;
		((GridLayout) foldingOptionsComposite.getLayout()).marginWidth = 0;

		createMinLines(foldingOptionsComposite);

		TabFolder tabFolder = new TabFolder(foldingOptionsComposite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem comments = new TabItem(tabFolder, SWT.NONE);
		comments
				.setText(PreferencesMessages.FoldingConfigurationBlock_docTabName);
		comments.setControl(documentationBlock.createControl((tabFolder)));

		if (sourceCodeBlock != null) {
			TabItem blocks = new TabItem(tabFolder, SWT.NONE);
			blocks
					.setText(PreferencesMessages.FoldingConfigurationBlock_srcTabName);
			blocks.setControl(sourceCodeBlock.createControl(tabFolder));
		}

		enableFolding.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					enableFoldingControls();
				} else {
					disableFoldingControls();
				}
			}
		});

		bindControl(enableFolding, PreferenceConstants.EDITOR_FOLDING_ENABLED);

		createAdditionalTabs(tabFolder);

		return composite;
	}

	private ControlEnableState fBlockEnableState;

	@Override
	public void dispose() {
		documentationBlock.dispose();
		if (sourceCodeBlock != null) {
			sourceCodeBlock.dispose();
		}
	}

	protected void createAdditionalTabs(TabFolder tabFolder) {
		// empty implementation
	}

	protected IFoldingPreferenceBlock createDocumentationBlock(
			OverlayPreferenceStore store, PreferencePage page) {
		return new DocumentationFoldingPreferenceBlock(store, page);
	}

	@Override
	protected List<OverlayKey> createOverlayKeys() {
		ArrayList<OverlayKey> keys = new ArrayList<OverlayKey>();

		keys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_FOLDING_ENABLED));

		keys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.INT,
				PreferenceConstants.EDITOR_FOLDING_LINES_LIMIT));

		return keys;
	}

	protected IFoldingPreferenceBlock createSourceCodeBlock(
			OverlayPreferenceStore store, PreferencePage page) {
		return new SourceCodeFoldingPreferenceBlock(store, page);
	}

	protected int defaultMinLines() {
		return DEFAULT_MIN_LINES;
	}

	@Override
	protected void initializeFields() {
		super.initializeFields();
		documentationBlock.initialize();

		if (sourceCodeBlock != null) {
			sourceCodeBlock.initialize();
		}
		if (getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED)) {
			enableFoldingControls();
		} else {
			disableFoldingControls();
		}
	}

	private void createMinLines(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent, parent
				.getFont(), 2, 1, GridData.FILL);
		((GridLayout) composite.getLayout()).marginWidth = 0;

		int minLines = defaultMinLines();

		SWTFactory
				.createLabel(
						composite,
						NLS
								.bind(
										PreferencesMessages.FoldingConfigurationBlock_minLinesToEnableFolding,
										Integer.valueOf(minLines)), 0, 1);

		Text textBox = SWTFactory.createText(composite, SWT.BORDER, 1,
				Util.EMPTY_STRING);
		textBox.setTextLimit(4);
		((GridData) textBox.getLayoutData()).widthHint = new PixelConverter(
				composite).convertWidthInCharsToPixels(4 + 1);

		bindControl(textBox, PreferenceConstants.EDITOR_FOLDING_LINES_LIMIT,
				new MinimumNumberValidator(minLines));
	}

	/**
	 * @since 2.0
	 */
	protected void enableFoldingControls() {
		if (fBlockEnableState != null) {
			fBlockEnableState.restore();
			fBlockEnableState = null;
		}
	}

	/**
	 * @since 2.0
	 */
	protected void disableFoldingControls() {
		if (fBlockEnableState == null) {
			fBlockEnableState = ControlEnableState
					.disable(foldingOptionsComposite);
		}
	}
}
