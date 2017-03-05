package org.eclipse.dltk.ui.browsing.ext;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

class ExtendedClasesLabelProvider implements ILabelProvider {
	private final ILabelProvider labelProvider;

	/**
	 * @param extendedClassesView
	 */
	ExtendedClasesLabelProvider(ILabelProvider extendedClassesView) {
		labelProvider = extendedClassesView;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof MixedClass) {
			MixedClass cl = (MixedClass) element;
			if (cl.getElements().size() > 0) {
				return labelProvider.getImage(cl.getElements().get(0));
			}
		}
		return labelProvider.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IModelElement) {
			return ((IModelElement) element).getElementName();
		} else if (element instanceof MixedClass) {
			MixedClass cl = (MixedClass) element;
			// if (cl.getElements().size() > 1) {
			// return cl.getName() + "("
			// + Integer.toString(cl.getElements().size()) + ")";
			// } else {
			return cl.getName();
			// }
		} else if (element != null) {
			return element.toString();
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}
}