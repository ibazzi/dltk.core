package org.eclipse.dltk.validators.internal.externalchecker.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.dltk.validators.internal.externalchecker.core.CustomWildcard;

public class CustomWildcardsList {

	private Vector<CustomWildcard> wcards = new Vector<>();
	private Set<IWildcardListViewer> changeListeners = new HashSet<>();

	public void addWcard() {
		CustomWildcard r = new CustomWildcard("x", Messages.CustomWildcardsList_enterPattern, //$NON-NLS-1$
				Messages.CustomWildcardsList_enterDescription);
		wcards.add(r);
		Iterator<IWildcardListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().addWildcard(r);
		}
	}

	public void removeChangeListener(IWildcardListViewer viewer) {
		changeListeners.remove(viewer);
	}

	public void addChangeListener(IWildcardListViewer viewer) {
		changeListeners.add(viewer);
	}

	public Vector<CustomWildcard> getWcards() {
		return wcards;
	}

	public void wcardChanged(CustomWildcard r) {
		Iterator<IWildcardListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			iterator.next().updateWildcard(r);
	}

	public void addWcard(CustomWildcard r) {
		wcards.add(r);
		Iterator<IWildcardListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().addWildcard(r);
		}
	}

	public void removeWcard(CustomWildcard task) {
		wcards.remove(task);
		Iterator<IWildcardListViewer> iterator = changeListeners.iterator();
		while (iterator.hasNext())
			iterator.next().removeWildcard(task);
	}

	public void removeAll() {
		for (int i = 0; i < this.wcards.size(); i++) {
			CustomWildcard task = wcards.get(i);
			Iterator<IWildcardListViewer> iterator = changeListeners.iterator();
			while (iterator.hasNext())
				iterator.next().removeWildcard(task);
		}
		wcards.clear();

	}

	public CustomWildcard[] getWildcards() {
		return this.wcards.toArray(new CustomWildcard[this.wcards.size()]);
	}

}
