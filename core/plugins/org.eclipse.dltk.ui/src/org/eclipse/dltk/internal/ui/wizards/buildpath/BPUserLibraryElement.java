/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.wizards.buildpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IBuildpathContainer;
import org.eclipse.dltk.core.IBuildpathContainerExtension3;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IScriptProject;

public class BPUserLibraryElement {

	private class UpdatedBuildpathContainer
			implements IBuildpathContainer, IBuildpathContainerExtension3 {
		@Override
		public IBuildpathEntry[] getBuildpathEntries() {
			BPListElement[] children = getChildren();
			IBuildpathEntry[] entries = new IBuildpathEntry[children.length];
			for (int i = 0; i < entries.length; i++) {
				entries[i] = children[i].getBuildpathEntry();
			}
			return entries;
		}

		@Override
		public String getDescription() {
			return getName();
		}

		@Override
		public int getKind() {
			return isSystemLibrary() ? IBuildpathContainer.K_SYSTEM
					: K_APPLICATION;
		}

		@Override
		public IPath getPath() {
			return BPUserLibraryElement.this.getPath();
		}

		@Override
		public Map<String, String> getAttributes() {
			return BPUserLibraryElement.this.getAttributes();
		}
	}

	private String fName;
	private List<BPListElement> fChildren;
	private boolean fIsSystemLibrary;
	private Map<String, String> fAttributes;

	public BPUserLibraryElement(String name, IBuildpathContainer container,
			IScriptProject project) {
		this(name, container, project, null);
	}

	public BPUserLibraryElement(String name, IBuildpathContainer container,
			IScriptProject project, Map<String, String> attributes) {
		fName = name;
		fChildren = new ArrayList<BPListElement>();
		if (container != null) {
			IBuildpathEntry[] entries = container.getBuildpathEntries();
			BPListElement[] res = new BPListElement[entries.length];
			for (int i = 0; i < res.length; i++) {
				IBuildpathEntry curr = entries[i];
				BPListElement elem = BPListElement.createFromExisting(this,
						curr, project);
				// elem.setAttribute(CPListElement.SOURCEATTACHMENT,
				// curr.getSourceAttachmentPath());
				// elem.setAttribute(CPListElement.JAVADOC,
				// ScriptUI.getLibraryJavadocLocation(curr.getPath()));
				fChildren.add(elem);
			}
			fIsSystemLibrary = container.getKind() == IBuildpathContainer.K_SYSTEM;
		} else {
			fIsSystemLibrary = false;
		}
		if (attributes != null)
			fAttributes = new HashMap<String, String>(attributes);
	}

	public BPUserLibraryElement(String name, boolean isSystemLibrary,
			BPListElement[] children) {
		this(name, isSystemLibrary, children, null);
	}

	public BPUserLibraryElement(String name, boolean isSystemLibrary,
			BPListElement[] children, Map<String, String> attributes) {
		fName = name;
		fChildren = new ArrayList<BPListElement>();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				BPListElement child = children[i];
				child.setParentContainer(this);
				fChildren.add(children[i]);
			}
		}
		fIsSystemLibrary = isSystemLibrary;
		if (attributes != null)
			fAttributes = new HashMap<String, String>(attributes);
	}

	public BPListElement[] getChildren() {
		return fChildren.toArray(new BPListElement[fChildren
				.size()]);
	}

	public String getName() {
		return fName;
	}

	public IPath getPath() {
		return new Path(DLTKCore.USER_LIBRARY_CONTAINER_ID).append(fName);
	}

	public boolean isSystemLibrary() {
		return fIsSystemLibrary;
	}

	public Map<String, String> getAttributes() {
		return fAttributes != null
				? Collections.<String, String> unmodifiableMap(fAttributes)
				: Collections.<String, String> emptyMap();
	}

	public String getAttribute(String name) {
		if (fAttributes == null) {
			return null;
		}
		return fAttributes.get(name);
	}

	public void add(BPListElement element) {
		if (!fChildren.contains(element)) {
			fChildren.add(element);
		}
	}

	private List<BPListElement> moveUp(List<BPListElement> elements,
			List<BPListElement> move) {
		int nElements = elements.size();
		List<BPListElement> res = new ArrayList<BPListElement>(nElements);
		BPListElement floating = null;
		for (int i = 0; i < nElements; i++) {
			BPListElement curr = elements.get(i);
			if (move.contains(curr)) {
				res.add(curr);
			} else {
				if (floating != null) {
					res.add(floating);
				}
				floating = curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		return res;
	}

	public void moveUp(List<BPListElement> toMoveUp) {
		if (toMoveUp.size() > 0) {
			fChildren = moveUp(fChildren, toMoveUp);
		}
	}

	public void moveDown(List<BPListElement> toMoveDown) {
		if (toMoveDown.size() > 0) {
			Collections.reverse(fChildren);
			fChildren = moveUp(fChildren, toMoveDown);
			Collections.reverse(fChildren);
		}
	}

	public void remove(BPListElement element) {
		fChildren.remove(element);
	}

	public void replace(BPListElement existingElement, BPListElement element) {
		if (fChildren.contains(element)) {
			fChildren.remove(existingElement);
		} else {
			int index = fChildren.indexOf(existingElement);
			if (index != -1) {
				fChildren.set(index, element);
			} else {
				fChildren.add(element);
			}
			copyAttribute(existingElement, element, BPListElement.ACCESSRULES);
		}
	}

	private void copyAttribute(BPListElement source, BPListElement target,
			String attributeName) {
		Object value = source.getAttribute(attributeName);
		if (value != null) {
			target.setAttribute(attributeName, value);
		}
	}

	public IBuildpathContainer getUpdatedContainer() {
		return new UpdatedBuildpathContainer();
	}

	public boolean hasChanges(IBuildpathContainer oldContainer,
			IScriptProject project) {
		if (oldContainer == null
				|| (oldContainer.getKind() == IBuildpathContainer.K_SYSTEM) != fIsSystemLibrary) {
			return true;
		}
		IBuildpathEntry[] oldEntries = oldContainer.getBuildpathEntries();
		if (fChildren.size() != oldEntries.length) {
			return true;
		}
		for (int i = 0; i < oldEntries.length; i++) {
			BPListElement child = fChildren.get(i);
			if (!child.getBuildpathEntry().equals(oldEntries[i])) {
				return true;
			}
		}
		return false;
	}

}
