package org.eclipse.dltk.internal.debug.ui.interpreters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.core.internal.environment.LocalEnvironment;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.ui.preferences.ComboViewerBlock;
import org.eclipse.dltk.ui.preferences.ImprovedAbstractConfigurationBlock;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.dltk.ui.util.SWTFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Preference block that can be used to select an installed interpreter for
 * 'internal' editor, etc use.
 */
public abstract class InternalScriptInterpreterPreferenceBlock
		extends ImprovedAbstractConfigurationBlock {

	private ComboViewerBlock viewer;

	public InternalScriptInterpreterPreferenceBlock(
			OverlayPreferenceStore store, PreferencePage page) {
		super(store, page);
	}

	@Override
	public final Control createControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent,
				parent.getFont(), 1, 1, GridData.FILL);

		Group group = SWTFactory.createGroup(composite, getSelectorGroupLabel(),
				1, 1, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(group, getSelectorNameLabel(), 1);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer = new ComboViewerBlock(group, gd) {
			@Override
			protected String getObjectName(Object element) {
				return ((IInterpreterInstall) element).getName();
			}

			@Override
			protected void selectedObjectChanged(Object element) {
				setString(getPreferenceKey(), getObjectId(element));
			}

			@Override
			protected Object getDefaultObject() {
				return getDefaultSelectedInterpreter();
			}

			@Override
			protected String getObjectId(Object element) {
				return ScriptRuntime.getCompositeIdFromInterpreter(
						(IInterpreterInstall) element);
			}

			@Override
			protected String getSavedObjectId() {
				return getString(getPreferenceKey());
			}

			@Override
			protected Object getObjectById(String id) {
				return ScriptRuntime.getInterpreterFromCompositeId(id);
			}
		};

		viewer.initialize(getInterpreterInstalls());

		return composite;
	}

	/**
	 * Returns the language's nature id.
	 */
	protected abstract String getNatureId();

	/**
	 * Returns the preference key that will be used to store the interpreter
	 * preference.
	 */
	protected abstract String getPreferenceKey();

	/**
	 * Returns the label that will be used for the selector group.
	 */
	protected abstract String getSelectorGroupLabel();

	/**
	 * Returns the label that will be used for the selector name.
	 */
	protected abstract String getSelectorNameLabel();

	/**
	 * Returns the {@link IInterpreterInstall} that will be auto-selected if an
	 * interpreter id is not found in the preference store.
	 */
	protected Object getDefaultSelectedInterpreter() {
		return ScriptRuntime.getDefaultInterpreterInstall(getNatureId(),
				LocalEnvironment.getInstance());
	}

	@Override
	protected List<OverlayKey> createOverlayKeys() {
		ArrayList<OverlayKey> keys = new ArrayList<>(1);

		keys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING, getPreferenceKey()));

		return keys;
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		viewer.performDefaults();
	}

	private IInterpreterInstall[] getInterpreterInstalls() {
		List<IInterpreterInstall> interpreters = new ArrayList<>();
		IInterpreterInstallType[] types = ScriptRuntime
				.getInterpreterInstallTypes(getNatureId());
		for (int i = 0; i < types.length; i++) {
			IInterpreterInstall[] installs = types[i].getInterpreterInstalls();
			for (int j = 0; j < installs.length; j++) {
				interpreters.add(installs[j]);
			}
		}

		return interpreters
				.toArray(new IInterpreterInstall[interpreters.size()]);
	}
}
