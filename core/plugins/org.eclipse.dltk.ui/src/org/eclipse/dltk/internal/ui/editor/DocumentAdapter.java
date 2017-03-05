/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.internal.ui.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.BufferChangedEvent;
import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.IBufferChangedListener;
import org.eclipse.dltk.core.IOpenable;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.swt.widgets.Display;



/**
 * Adapts <code>IDocument</code> to <code>IBuffer</code>. Uses the
 * same algorithm as the text widget to determine the buffer's line delimiter.
 * All text inserted into the buffer is converted to this line delimiter.
 * This class is <code>public</code> for test purposes only.
 */
public class DocumentAdapter implements IBuffer, IDocumentListener {

		/**
		 * Internal implementation of a NULL instanceof IBuffer.
		 */
		static private class NullBuffer implements IBuffer {
			@Override
			public void addBufferChangedListener(IBufferChangedListener listener) {}
			@Override
			public void append(char[] text) {}
			@Override
			public void append(String text) {}
			@Override
			public void close() {}
			@Override
			public char getChar(int position) { return 0; }
			@Override
			public char[] getCharacters() { return null; }
			@Override
			public String getContents() { return null; }
			@Override
			public int getLength() { return 0; }
			@Override
			public IOpenable getOwner() { return null; }
			@Override
			public String getText(int offset, int length) { return null; }
			@Override
			public IResource getUnderlyingResource() { return null; }
			@Override
			public boolean hasUnsavedChanges() { return false; }
			@Override
			public boolean isClosed() { return false; }
			@Override
			public boolean isReadOnly() { return true; }
			@Override
			public void removeBufferChangedListener(IBufferChangedListener listener) {}
			@Override
			public void replace(int position, int length, char[] text) {}
			@Override
			public void replace(int position, int length, String text) {}
			@Override
			public void save(IProgressMonitor progress, boolean force) throws ModelException {}
			@Override
			public void setContents(char[] contents) {}
			@Override
			public void setContents(String contents) {}
		}


		/** NULL implementing <code>IBuffer</code> */
		public final static IBuffer NULL= new NullBuffer();

		/*
	 *
		 */
		private IPath fPath;


		/**
		 *  Executes a document set content call in the ui thread.
		 */
		protected class DocumentSetCommand implements Runnable {

			private String fContents;

			@Override
			public void run() {
				fDocument.set(fContents);
			}

			public void set(String contents) {
				fContents= contents;
				//Display.getDefault().syncExec(this);
				DocumentAdapter.run(this);
			}
		}

		/**
		 * Executes a document replace call in the ui thread.
		 */
		protected class DocumentReplaceCommand implements Runnable {

			private int fOffset;
			private int fLength;
			private String fText;

			@Override
			public void run() {
				try {
					fDocument.replace(fOffset, fLength, fText);
				} catch (BadLocationException x) {
					// ignore
				}
			}

			public void replace(int offset, int length, String text) {
				fOffset= offset;
				fLength= length;
				fText= text;
				Display.getDefault().syncExec(this);
			}
		}

	private static final boolean DEBUG_LINE_DELIMITERS= true;

	private IOpenable fOwner;
	private IFile fFile;
	private ITextFileBuffer fTextFileBuffer;
	private IDocument fDocument;

	private DocumentSetCommand fSetCmd= new DocumentSetCommand();
	private DocumentReplaceCommand fReplaceCmd= new DocumentReplaceCommand();

	private Set<String> fLegalLineDelimiters;

	private List<IBufferChangedListener> fBufferListeners = new ArrayList<IBufferChangedListener>(
			3);
	private IStatus fStatus;

	/** @since 4.0 */
	private LocationKind fLocationKind;

	/** @since 4.0 */
	private IFileStore fFileStore;

	/**
	 * Constructs a new document adapter.
	 */
	public DocumentAdapter(IOpenable owner, IFile file) {

		fOwner= owner;
		fFile= file;
		fPath= fFile.getFullPath();
		fLocationKind = LocationKind.IFILE;

		initialize();
	}
	/**
	 * Constructs a new document adapter.
	 *
	 *
	 */
	public DocumentAdapter(IOpenable owner, IPath path) {
		Assert.isLegal(path != null);

		fOwner= owner;
		fPath= path;
		fLocationKind = LocationKind.NORMALIZE;

		initialize();
	}

	/**
	 * Constructs a new document adapter.
	 *
	 * @param owner
	 *            the owner of this buffer
	 * @param fileStore
	 *            the file store of the file that backs the buffer
	 * @param path
	 *            the path of the file that backs the buffer
	 * @since 4.0
	 */
	public DocumentAdapter(IOpenable owner, IFileStore fileStore, IPath path) {
		Assert.isLegal(fileStore != null);
		Assert.isLegal(path != null);
		fOwner = owner;
		fFileStore = fileStore;
		fPath = path;
		fLocationKind = LocationKind.NORMALIZE;

		initialize();
	}

	private void initialize() {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		try {
			if (fFileStore != null) {
				manager.connectFileStore(fFileStore, new NullProgressMonitor());
				fTextFileBuffer = manager
						.getFileStoreTextFileBuffer(fFileStore);
			} else {
				manager.connect(fPath, fLocationKind, new NullProgressMonitor());
				fTextFileBuffer = manager.getTextFileBuffer(fPath,
						fLocationKind);
			}
			fDocument= fTextFileBuffer.getDocument();
		} catch (CoreException x) {
			fStatus= x.getStatus();
			fDocument= manager.createEmptyDocument(fPath, LocationKind.NORMALIZE);
			if (fDocument instanceof ISynchronizable)
				((ISynchronizable)fDocument).setLockObject(new Object());
		}
		fDocument.addPrenotifiedDocumentListener(this);
	}

	/**
	 * Returns the status of this document adapter.
	 */
	public IStatus getStatus() {
		if (fStatus != null)
			return fStatus;
		if (fTextFileBuffer != null)
			return fTextFileBuffer.getStatus();
		return null;
	}

	/**
	 * Returns the adapted document.
	 *
	 * @return the adapted document
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/*
	 * @see IBuffer#addBufferChangedListener(IBufferChangedListener)
	 */
	@Override
	public void addBufferChangedListener(IBufferChangedListener listener) {
		Assert.isNotNull(listener);
		if (!fBufferListeners.contains(listener))
			fBufferListeners.add(listener);
	}

	/*
	 * @see IBuffer#removeBufferChangedListener(IBufferChangedListener)
	 */
	@Override
	public void removeBufferChangedListener(IBufferChangedListener listener) {
		Assert.isNotNull(listener);
		fBufferListeners.remove(listener);
	}

	@Override
	public void append(char[] text) {
		append(new String(text));
	}

	@Override
	public void append(String text) {
		if (DEBUG_LINE_DELIMITERS) {
			validateLineDelimiters(text);
		}
		fReplaceCmd.replace(fDocument.getLength(), 0, text);
	}

	@Override
	public void close() {

		if (isClosed())
			return;

		IDocument d= fDocument;
		fDocument= null;
		d.removePrenotifiedDocumentListener(this);

		if (fTextFileBuffer != null) {
			ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
			try {
				if (fFileStore != null)
					manager.disconnectFileStore(fFileStore,
							new NullProgressMonitor());
				else
					manager.disconnect(fPath, fLocationKind,
							new NullProgressMonitor());
			} catch (CoreException x) {
				// ignore
			}
			fTextFileBuffer= null;
		}

		fireBufferChanged(new BufferChangedEvent(this, 0, 0, null));
		fBufferListeners.clear();
	}

	@Override
	public char getChar(int position) {
		try {
			return fDocument.getChar(position);
		} catch (BadLocationException x) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	@Override
	public char[] getCharacters() {
		String content= getContents();
		return content == null ? null : content.toCharArray();
	}

	@Override
	public String getContents() {
		return fDocument.get();
	}

	@Override
	public int getLength() {
		return fDocument.getLength();
	}

	@Override
	public IOpenable getOwner() {
		return fOwner;
	}

	@Override
	public String getText(int offset, int length) {
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException x) {
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	@Override
	public IResource getUnderlyingResource() {
		return fFile;
	}

	@Override
	public boolean hasUnsavedChanges() {
		return fTextFileBuffer != null ? fTextFileBuffer.isDirty() : false;
	}

	@Override
	public boolean isClosed() {
		return fDocument == null;
	}

	@Override
	public boolean isReadOnly() {
		IResource resource= getUnderlyingResource();
		return resource == null ? true
				: resource.getResourceAttributes().isReadOnly();
	}

	@Override
	public void replace(int position, int length, char[] text) {
		replace(position, length, new String(text));
	}

	@Override
	public void replace(int position, int length, String text) {
		if (DEBUG_LINE_DELIMITERS) {
			validateLineDelimiters(text);
		}
		fReplaceCmd.replace(position, length, text);
	}

	@Override
	public void save(IProgressMonitor progress, boolean force) throws ModelException {
		try {
			if (fTextFileBuffer != null)
				fTextFileBuffer.commit(progress, force);
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public void setContents(char[] contents) {
		setContents(new String(contents));
	}

	@Override
	public void setContents(String contents) {
		int oldLength= fDocument.getLength();

		if (contents == null) {

			if (oldLength != 0)
				fSetCmd.set(""); //$NON-NLS-1$

		} else {

			// set only if different
			if (DEBUG_LINE_DELIMITERS) {
				validateLineDelimiters(contents);
			}

			if (!contents.equals(fDocument.get()))
				fSetCmd.set(contents);
		}
	}


	private void validateLineDelimiters(String contents) {

		if (fLegalLineDelimiters == null) {
			// collect all line delimiters in the document
			HashSet<String> existingDelimiters = new HashSet<String>();

			for (int i= fDocument.getNumberOfLines() - 1; i >= 0; i-- ) {
				try {
					String curr= fDocument.getLineDelimiter(i);
					if (curr != null) {
						existingDelimiters.add(curr);
					}
				} catch (BadLocationException e) {
					DLTKUIPlugin.log(e);
				}
			}
			if (existingDelimiters.isEmpty()) {
				return; // first insertion of a line delimiter: no test
			}
			fLegalLineDelimiters= existingDelimiters;

		}

		DefaultLineTracker tracker= new DefaultLineTracker();
		tracker.set(contents);

		int lines= tracker.getNumberOfLines();
		if (lines <= 1)
			return;

		for (int i= 0; i < lines; i++) {
			try {
				String curr= tracker.getLineDelimiter(i);
				if (curr != null && !fLegalLineDelimiters.contains(curr)) {
					StringBuffer buf = new StringBuffer(
							"WARNING: DocumentAdapter added new line delimiter to code: "); //$NON-NLS-1$
					for (int k= 0; k < curr.length(); k++) {
						if (k > 0)
							buf.append(' ');
						buf.append((int) curr.charAt(k));
					}
					IStatus status = new Status(IStatus.WARNING,
							DLTKUIPlugin.PLUGIN_ID, IStatus.OK, buf.toString(),
							new Throwable());
					DLTKUIPlugin.log(status);
				}
			} catch (BadLocationException e) {
				DLTKUIPlugin.log(e);
			}
		}
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// there is nothing to do here
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		fireBufferChanged(new BufferChangedEvent(this, event.getOffset(), event.getLength(), event.getText()));
	}

	private void fireBufferChanged(BufferChangedEvent event) {
		if (fBufferListeners != null && fBufferListeners.size() > 0) {
			Iterator<IBufferChangedListener> e = new ArrayList<IBufferChangedListener>(
					fBufferListeners).iterator();
			while (e.hasNext())
				e.next().bufferChanged(event);
		}
	}

	/**
	 * Run the given runnable in the UI thread.
	 *
	 * @param runnable the runnable
	 * @since 3.3
	 */
	private static final void run(Runnable runnable) {
		Display currentDisplay= Display.getCurrent();
		if (currentDisplay != null)
			runnable.run();
		else
			Display.getDefault().syncExec(runnable);
	}

}
