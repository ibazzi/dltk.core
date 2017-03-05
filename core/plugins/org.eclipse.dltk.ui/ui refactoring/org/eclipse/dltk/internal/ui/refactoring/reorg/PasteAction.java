/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.refactoring.reorg;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.ExternalProjectFragment;
import org.eclipse.dltk.internal.core.ExternalScriptFolder;
import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.dltk.internal.corext.refactoring.reorg.ModelElementTransfer;
import org.eclipse.dltk.internal.corext.refactoring.reorg.ParentChecker;
import org.eclipse.dltk.internal.corext.refactoring.reorg.ReorgUtils;
import org.eclipse.dltk.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.dltk.internal.ui.workingsets.WorkingSetIDs;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.actions.SelectionDispatchAction;
import org.eclipse.dltk.ui.util.ExceptionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.CopyProjectOperation;
import org.eclipse.ui.part.ResourceTransfer;


public class PasteAction extends SelectionDispatchAction{

	private final Clipboard fClipboard;

	public PasteAction(IWorkbenchSite site, Clipboard clipboard) {
		super(site);
		Assert.isNotNull(clipboard);
		fClipboard= clipboard;

		setText(ReorgMessages.PasteAction_4);
		setDescription(ReorgMessages.PasteAction_5);

		ISharedImages workbenchImages= DLTKUIPlugin.getDefault().getWorkbench().getSharedImages();
		setDisabledImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setHoverImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

		if (DLTKCore.DEBUG) {
			System.err.println("Add help support here..."); //$NON-NLS-1$
		}
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IScriptHelpContextIds.PASTE_ACTION);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		// Moved condition checking to run (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=78450)
	}

	private Paster[] createEnabledPasters(TransferData[] availableDataTypes) throws ModelException {
		Paster paster;
		Shell shell = getShell();
		List<Paster> result = new ArrayList<Paster>(2);
		paster= new ProjectPaster(shell, fClipboard);
		if (paster.canEnable(availableDataTypes))
			result.add(paster);

		paster= new ModelElementAndResourcePaster(shell, fClipboard);
		if (paster.canEnable(availableDataTypes))
			result.add(paster);

		paster= new FilePaster(shell, fClipboard);
		if (paster.canEnable(availableDataTypes))
			result.add(paster);

		paster= new WorkingSetPaster(shell, fClipboard);
		if (paster.canEnable(availableDataTypes))
			result.add(paster);

		return result.toArray(new Paster[result.size()]);
	}

	private static Object getContents(final Clipboard clipboard, final Transfer transfer, Shell shell) {
		//see bug 33028 for explanation why we need this
		final Object[] result= new Object[1];
		shell.getDisplay()
				.syncExec(() -> result[0] = clipboard.getContents(transfer));
		return result[0];
	}

	private static boolean isAvailable(Transfer transfer, TransferData[] availableDataTypes) {
		for (int i= 0; i < availableDataTypes.length; i++) {
			if (transfer.isSupportedType(availableDataTypes[i])) return true;
		}
		return false;
	}

	@Override
	public void run(IStructuredSelection selection) {
		try {
			TransferData[] availableTypes= fClipboard.getAvailableTypes();
			List elements= selection.toList();
			IResource[] resources= ReorgUtils.getResources(elements);
			IModelElement[] modelElements= ReorgUtils.getModelElements(elements);
			IWorkingSet[] workingSets= ReorgUtils.getWorkingSets(elements);
			Paster[] pasters= createEnabledPasters(availableTypes);
			for (int i= 0; i < pasters.length; i++) {
				if (pasters[i].canPasteOn(modelElements, resources, workingSets)) {
					pasters[i].paste(modelElements, resources, workingSets, availableTypes);
					return;// one is enough
				}
			}
			MessageDialog.openError(DLTKUIPlugin.getActiveWorkbenchShell(), RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_disabled);
		} catch (ModelException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		} catch (InterruptedException e) {
			// OK
		}
	}

	private abstract static class Paster{
		private final Shell fShell;
		private final Clipboard fClipboard2;
		protected Paster(Shell shell, Clipboard clipboard){
			fShell= shell;
			fClipboard2= clipboard;
		}
		protected final Shell getShell() {
			return fShell;
		}
		protected final Clipboard getClipboard() {
			return fClipboard2;
		}

		protected final IResource[] getClipboardResources(TransferData[] availableDataTypes) {
			Transfer transfer= ResourceTransfer.getInstance();
			if (isAvailable(transfer, availableDataTypes)) {
				return (IResource[])getContents(fClipboard2, transfer, getShell());
			}
			return null;
		}

		protected final IModelElement[] getClipboardScriptElements(TransferData[] availableDataTypes) {
			Transfer transfer= ModelElementTransfer.getInstance();
			if (isAvailable(transfer, availableDataTypes)) {
				return (IModelElement[])getContents(fClipboard2, transfer, getShell());
			}
			return null;
		}

		public abstract void paste(IModelElement[] selectedScriptElements, IResource[] selectedResources, IWorkingSet[] selectedWorkingSets, TransferData[] availableTypes) throws ModelException, InterruptedException, InvocationTargetException;
		public abstract boolean canEnable(TransferData[] availableTypes)  throws ModelException;
		public abstract boolean canPasteOn(IModelElement[] selectedScriptElements, IResource[] selectedResources, IWorkingSet[] selectedWorkingSets)  throws ModelException;
	}


	private static class WorkingSetPaster extends Paster {
		protected WorkingSetPaster(Shell shell, Clipboard clipboard) {
			super(shell, clipboard);
		}
		@Override
		public void paste(IModelElement[] selectedScriptElements, IResource[] selectedResources, IWorkingSet[] selectedWorkingSets, TransferData[] availableTypes) throws ModelException, InterruptedException, InvocationTargetException {
			IWorkingSet workingSet= selectedWorkingSets[0];
			Set<IAdaptable> elements = new HashSet<IAdaptable>(
					Arrays.asList(workingSet.getElements()));
			IModelElement[] modelElements= getClipboardScriptElements(availableTypes);
			if (modelElements != null) {
				for (int i= 0; i < modelElements.length; i++) {
					if (!ReorgUtils.containsElementOrParent(elements, modelElements[i]))
						elements.add(modelElements[i]);
				}
			}
			IResource[] resources= getClipboardResources(availableTypes);
			if (resources != null) {
				List realScriptElements= new ArrayList();
				List realResource= new ArrayList();
				ReorgUtils.splitIntoModelElementsAndResources(resources, realScriptElements, realResource);
				for (Iterator iter= realScriptElements.iterator(); iter.hasNext();) {
					IModelElement element= (IModelElement)iter.next();
					if (!ReorgUtils.containsElementOrParent(elements, element))
						elements.add(element);
				}
				for (Iterator iter= realResource.iterator(); iter.hasNext();) {
					IResource element= (IResource)iter.next();
					if (!ReorgUtils.containsElementOrParent(elements, element))
						elements.add(element);
				}
			}
			workingSet.setElements(
					elements.toArray(new IAdaptable[elements.size()]));
		}
		@Override
		public boolean canEnable(TransferData[] availableTypes) throws ModelException {
			return isAvailable(ResourceTransfer.getInstance(), availableTypes) ||
				isAvailable(ModelElementTransfer.getInstance(), availableTypes);
		}
		@Override
		public boolean canPasteOn(IModelElement[] selectedScriptElements, IResource[] selectedResources, IWorkingSet[] selectedWorkingSets) throws ModelException {
			if (selectedResources.length != 0 || selectedScriptElements.length != 0 || selectedWorkingSets.length != 1)
				return false;
			IWorkingSet ws= selectedWorkingSets[0];
			return !WorkingSetIDs.OTHERS.equals(ws.getId());
		}
	}

    private static class ProjectPaster extends Paster{

    	protected ProjectPaster(Shell shell, Clipboard clipboard) {
			super(shell, clipboard);
		}

		@Override
		public boolean canEnable(TransferData[] availableDataTypes) {
			boolean resourceTransfer= isAvailable(ResourceTransfer.getInstance(), availableDataTypes);
			boolean modelElementTransfer= isAvailable(ModelElementTransfer.getInstance(), availableDataTypes);
			if (! modelElementTransfer)
				return canPasteSimpleProjects(availableDataTypes);
			if (! resourceTransfer)
				return canPasteScriptProjects(availableDataTypes);
			return canPasteScriptProjects(availableDataTypes) && canPasteSimpleProjects(availableDataTypes);
    	}

		@Override
		public void paste(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets, TransferData[] availableTypes) {
			pasteProjects(availableTypes);
		}

		private void pasteProjects(TransferData[] availableTypes) {
			pasteProjects(getProjectsToPaste(availableTypes));
		}

		private void pasteProjects(IProject[] projects){
			Shell shell= getShell();
			for (int i = 0; i < projects.length; i++) {
				new CopyProjectOperation(shell).copyProject(projects[i]);
			}
		}
		private IProject[] getProjectsToPaste(TransferData[] availableTypes) {
			IResource[] resources= getClipboardResources(availableTypes);
			IModelElement[] modelElements= getClipboardScriptElements(availableTypes);
			Set result= new HashSet();
			if (resources != null)
				result.addAll(Arrays.asList(resources));
			if (modelElements != null)
				result.addAll(Arrays.asList(ReorgUtils.getNotNulls(ReorgUtils.getResources(modelElements))));
			Assert.isTrue(result.size() > 0);
			return (IProject[]) result.toArray(new IProject[result.size()]);
		}

		@Override
		public boolean canPasteOn(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets) {
			return selectedWorkingSets.length == 0; // Can't paste on working sets here
		}

		private boolean canPasteScriptProjects(TransferData[] availableDataTypes) {
			IModelElement[] modelElements= getClipboardScriptElements(availableDataTypes);
			return 	modelElements != null &&
					modelElements.length != 0 &&
					! ReorgUtils.hasElementsNotOfType(modelElements, IModelElement.SCRIPT_PROJECT);
		}

		private boolean canPasteSimpleProjects(TransferData[] availableDataTypes) {
			IResource[] resources= getClipboardResources(availableDataTypes);
			if (resources == null || resources.length == 0) return false;
			for (int i= 0; i < resources.length; i++) {
				if (resources[i].getType() != IResource.PROJECT || ! ((IProject)resources[i]).isOpen())
					return false;
			}
			return true;
		}
    }

    private static class FilePaster extends Paster{
		protected FilePaster(Shell shell, Clipboard clipboard) {
			super(shell, clipboard);
		}

		@Override
		public void paste(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets, TransferData[] availableTypes) throws ModelException {
			String[] fileData= getClipboardFiles(availableTypes);
			if (fileData == null)
				return;

			IContainer container= getAsContainer(getTarget(modelElements, resources));
			if (container == null)
				return;

			new CopyFilesAndFoldersOperation(getShell()).copyFiles(fileData, container);
		}

		private Object getTarget(IModelElement[] modelElements, IResource[] resources) {
			if (modelElements.length + resources.length == 1){
				if (modelElements.length == 1)
					return modelElements[0];
				else
					return resources[0];
			} else
				return getCommonParent(modelElements, resources);
		}

		@Override
		public boolean canPasteOn(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets) throws ModelException {
			Object target= getTarget(modelElements, resources);
			return target != null && canPasteFilesOn(getAsContainer(target)) && selectedWorkingSets.length == 0;
		}

		@Override
		public boolean canEnable(TransferData[] availableDataTypes) throws ModelException {
			return isAvailable(FileTransfer.getInstance(), availableDataTypes);
		}

		private boolean canPasteFilesOn(Object target) {
			boolean isScriptFolder= target instanceof IProjectFragment;
			boolean isScriptProject= target instanceof IScriptProject;
			boolean isProjectFragment= target instanceof IProjectFragment;
			boolean isContainer= target instanceof IContainer;

			if( target instanceof ExternalProjectFragment || target instanceof ExternalScriptFolder || target instanceof ExternalSourceModule )  {
				return false;
			}

			if (!(isScriptFolder || isScriptProject || isProjectFragment || isContainer))
				return false;

			if (isContainer) {
				return true;
			} else {
				IModelElement element= (IModelElement)target;
				return !element.isReadOnly();
			}
		}

		private IContainer getAsContainer(Object target) throws ModelException{
			if (target == null)
				return null;
			if (target instanceof IContainer)
				return (IContainer)target;
			if (target instanceof IFile)
				return ((IFile)target).getParent();
			return getAsContainer(((IModelElement)target).getCorrespondingResource());
		}

		private String[] getClipboardFiles(TransferData[] availableDataTypes) {
			Transfer transfer= FileTransfer.getInstance();
			if (isAvailable(transfer, availableDataTypes)) {
				return (String[])getContents(getClipboard(), transfer, getShell());
			}
			return null;
		}
		private Object getCommonParent(IModelElement[] modelElements, IResource[] resources) {
			return new ParentChecker(resources, modelElements).getCommonParent();
		}
    }
    private static class ModelElementAndResourcePaster extends Paster {

		protected ModelElementAndResourcePaster(Shell shell, Clipboard clipboard) {
			super(shell, clipboard);
		}

		private TransferData[] fAvailableTypes;

		@Override
		public void paste(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets, TransferData[] availableTypes) throws ModelException, InterruptedException, InvocationTargetException{
			IResource[] clipboardResources= getClipboardResources(availableTypes);
			if (clipboardResources == null)
				clipboardResources= new IResource[0];
			IModelElement[] clipboardScriptElements= getClipboardScriptElements(availableTypes);
			if (clipboardScriptElements == null)
				clipboardScriptElements= new IModelElement[0];

			Object destination= getTarget(modelElements, resources);
			if (destination instanceof IModelElement) {
				ReorgCopyStarter.create(clipboardScriptElements, clipboardResources, (IModelElement)destination).run(getShell());
			}
			else if (destination instanceof IResource) {
				ReorgCopyStarter.create(clipboardScriptElements, clipboardResources, (IResource)destination).run(getShell());
			}
		}

		private Object getTarget(IModelElement[] modelElements, IResource[] resources) {
			if (modelElements.length + resources.length == 1){
				if (modelElements.length == 1)
					return modelElements[0];
				else
					return resources[0];
			} else
				return getCommonParent(modelElements, resources);
		}

		private Object getCommonParent(IModelElement[] modelElements, IResource[] resources) {
			return new ParentChecker(resources, modelElements).getCommonParent();
		}

		@Override
		public boolean canPasteOn(IModelElement[] modelElements, IResource[] resources, IWorkingSet[] selectedWorkingSets) throws ModelException {
			if (selectedWorkingSets.length != 0)
				return false;
			IResource[] clipboardResources= getClipboardResources(fAvailableTypes);
			if (clipboardResources == null)
				clipboardResources= new IResource[0];
			IModelElement[] clipboardScriptElements= getClipboardScriptElements(fAvailableTypes);
			if (clipboardScriptElements == null)
				clipboardScriptElements= new IModelElement[0];
			Object destination= getTarget(modelElements, resources);
			if (destination instanceof IModelElement) {
				return ReorgCopyStarter.create(clipboardScriptElements, clipboardResources, (IModelElement)destination) != null;
			}
			if (destination instanceof IResource) {
				return ReorgCopyStarter.create(clipboardScriptElements, clipboardResources, (IResource)destination) != null;
			}
			return false;
		}

		@Override
		public boolean canEnable(TransferData[] availableTypes) {
			fAvailableTypes= availableTypes;
			return isAvailable(ModelElementTransfer.getInstance(), availableTypes) || isAvailable(ResourceTransfer.getInstance(), availableTypes);
		}
    }

}
