package org.eclipse.dltk.ui.templates;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class CodeTemplateSourceViewerConfigurationAdapter extends
		SourceViewerConfiguration {
	private SourceViewerConfiguration fConfig;
	private IContentAssistProcessor fContentAssistProcessor;

	public CodeTemplateSourceViewerConfigurationAdapter(
			SourceViewerConfiguration config,
			IContentAssistProcessor contentAssistProcessor) {
		fConfig = config;
		fContentAssistProcessor = contentAssistProcessor;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.enableAutoActivation(true);
		assistant.enableAutoInsert(true);
		assistant.setContentAssistProcessor(fContentAssistProcessor,
				IDocument.DEFAULT_CONTENT_TYPE);
		return assistant;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return fConfig.getAnnotationHover(sourceViewer);
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(
			ISourceViewer sourceViewer, String contentType) {
		return fConfig.getAutoEditStrategies(sourceViewer, contentType);
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return fConfig.getConfiguredContentTypes(sourceViewer);
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return fConfig.getConfiguredDocumentPartitioning(sourceViewer);
	}

	@Override
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer,
			String contentType) {
		return fConfig.getConfiguredTextHoverStateMasks(sourceViewer,
				contentType);
	}

	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		return fConfig.getContentFormatter(sourceViewer);
	}

	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		return fConfig.getDefaultPrefixes(sourceViewer, contentType);
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		return fConfig.getDoubleClickStrategy(sourceViewer, contentType);
	}

	@Override
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return fConfig.getHyperlinkDetectors(sourceViewer);
	}

	@Override
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		return fConfig.getHyperlinkPresenter(sourceViewer);
	}

	@Override
	public int getHyperlinkStateMask(ISourceViewer sourceViewer) {
		return fConfig.getHyperlinkStateMask(sourceViewer);
	}

	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		return fConfig.getIndentPrefixes(sourceViewer, contentType);
	}

	@Override
	public IInformationControlCreator getInformationControlCreator(
			ISourceViewer sourceViewer) {
		return fConfig.getInformationControlCreator(sourceViewer);
	}

	@Override
	public IInformationPresenter getInformationPresenter(
			ISourceViewer sourceViewer) {
		return fConfig.getInformationPresenter(sourceViewer);
	}

	@Override
	public IAnnotationHover getOverviewRulerAnnotationHover(
			ISourceViewer sourceViewer) {
		return fConfig.getOverviewRulerAnnotationHover(sourceViewer);
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		return fConfig.getPresentationReconciler(sourceViewer);
	}

	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(
			ISourceViewer sourceViewer) {
		return fConfig.getQuickAssistAssistant(sourceViewer);
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return fConfig.getReconciler(sourceViewer);
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return fConfig.getTabWidth(sourceViewer);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType, int stateMask) {
		return fConfig.getTextHover(sourceViewer, contentType, stateMask);
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		return fConfig.getTextHover(sourceViewer, contentType);
	}

	@Override
	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
		return fConfig.getUndoManager(sourceViewer);
	}

}
