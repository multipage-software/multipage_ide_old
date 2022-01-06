/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.io.IOException;

import javax.swing.UIManager;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class GeneralGui {

	/**
	 * Resource location.
	 */
	private static final String resourcesLocation = "org.multipage.gui.properties.messages";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * Get state serializer
	 * @return
	 */
	public static StateSerializer getSerializer() {
		
		return serializer;
	}
	
	/**
	 * Initialize program basic layer.
	 * @param serializer 
	 */
	public static boolean initialize(String language, String country,
			StateSerializer serializer) {
		
		// Remember the serializer
		GeneralGui.serializer = serializer;
		
		// Set local identifiers.
		Resources.setLanguageAndCountry(language, country);
		
		// Load resources file.
		if (!Resources.loadResource(resourcesLocation)) {
			return false;
		}

		// Localize dialogs.
		localizeOptionDialogs();
		
		// Add state serializer.
		if (serializer != null) {
			serializer.add(new SerializeStateAdapter() {
				// On read state.
				@Override
				protected void onReadState(StateInputStream inputStream)
						throws IOException, ClassNotFoundException {
					// Serialize program dictionary.
					seriliazeData(inputStream);
				}
				// On write state.
				@Override
				protected void onWriteState(StateOutputStream outputStream)
						throws IOException {
					// Serialize program dictionary.
					serializeData(outputStream);
				}
				// On set default state.
				@Override
				protected void onSetDefaultState() {
					// Set default data.
					setDefaultData();
				}
			});
		}
		
		return true;
	}

	/**
	 * Localize option dialogs.
	 */
	private static void localizeOptionDialogs() {
		
		String text = Resources.getString("org.multipage.gui.textYes");
		if (text != null) {
			UIManager.put("OptionPane.yesButtonText", text);
		}
		
		text = Resources.getString("org.multipage.gui.textNo");
		if (text != null) {
			UIManager.put("OptionPane.noButtonText", text);
		}
		
		text = Resources.getString("org.multipage.gui.textCancel");
		if (text != null) {
			UIManager.put("OptionPane.cancelButtonText", text);
		}
		
		text = Resources.getString("org.multipage.gui.textTitle");
		if (text != null) {
			UIManager.put("OptionPane.titleButtonText", text);
		}
	}

	/**
	 * Set default data.
	 */
	protected static void setDefaultData() {

		// Utility default data.
		Utility.setDefaultData();
		// Text editor default data.
		TextEditorPane.setDefaultData();
		DateTimeDialog.setDefaultData();
		CssFontPanel.setDefaultData();
		CssBorderPanel.setDefaultData();
		CssOutlinesPanel.setDefaultData();
		CssBoxShadowPanel.setDefaultData();
		CssBackgroundImagesPanel.setDefaultData();
		CssNumberPanel.setDefaultData();
		CssBorderRadiusPanel.setDefaultData();
		CssTextShadowPanel.setDefaultData();
		CssBorderImagePanel.setDefaultData();
		SelectFontNameDialog.setDefaultData();
		CssClipPanel.setDefaultData();
		CssFlexPanel.setDefaultData();
		CssSpacingPanel.setDefaultData();
		CssCountersPanel.setDefaultData();
		CssListStylePanel.setDefaultData();
		CssKeyframesPanel.setDefaultData();
		CssKeyFrameItemDialog.setDefaultData();
		CssFindPropertyDialog.setDefaultData();
		CssAnimationPanel.setDefaultData();
		CssPerspectiveOriginPanel.setDefaultData();
		CssTransformPanel.setDefaultData();
		CssTransformMatrixDialog.setDefaultData();
		CssTransformOriginPanel.setDefaultData();
		CssTransitionPanel.setDefaultData();
		CssCursorPanel.setDefaultData();
		CssQuotesPanel.setDefaultData();
		CssTextLinePanel.setDefaultData();
		CssResourcePanel.setDefaultData();
		AnchorDialog.setDefaultData();
		HelpDialog.setDefaultData();
		CssResourcesUrlsPanel.setDefaultData();
	}
	
	/**
	 * Serialize module data.
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void seriliazeData(StateInputStream inputStream)
		throws IOException, ClassNotFoundException {

		// Load utility data.
		Utility.serializeData(inputStream);
		// Load text editor data.
		TextEditorPane.serializeData(inputStream);
		DateTimeDialog.serializeData(inputStream);
		CssFontPanel.serializeData(inputStream);
		CssBorderPanel.serializeData(inputStream);
		CssOutlinesPanel.serializeData(inputStream);
		CssBoxShadowPanel.serializeData(inputStream);
		CssBackgroundImagesPanel.serializeData(inputStream);
		CssNumberPanel.serializeData(inputStream);
		CssBorderRadiusPanel.serializeData(inputStream);
		CssTextShadowPanel.serializeData(inputStream);
		CssBorderImagePanel.serializeData(inputStream);
		SelectFontNameDialog.serializeData(inputStream);
		CssClipPanel.serializeData(inputStream);
		CssFlexPanel.serializeData(inputStream);
		CssSpacingPanel.serializeData(inputStream);
		CssCountersPanel.serializeData(inputStream);
		CssListStylePanel.serializeData(inputStream);
		CssKeyframesPanel.serializeData(inputStream);
		CssKeyFrameItemDialog.serializeData(inputStream);
		CssFindPropertyDialog.serializeData(inputStream);
		CssAnimationPanel.serializeData(inputStream);
		CssPerspectiveOriginPanel.serializeData(inputStream);
		CssTransformPanel.serializeData(inputStream);
		CssTransformMatrixDialog.serializeData(inputStream);
		CssTransformOriginPanel.serializeData(inputStream);
		CssTransitionPanel.serializeData(inputStream);
		CssCursorPanel.serializeData(inputStream);
		CssQuotesPanel.serializeData(inputStream);
		CssTextLinePanel.serializeData(inputStream);
		CssResourcePanel.serializeData(inputStream);
		AnchorDialog.serializeData(inputStream);
		HelpDialog.serializeData(inputStream);
		CssResourcesUrlsPanel.serializeData(inputStream);
	}

	/**
	 * Serialize module data.
	 * @param outputStream
	 * @throws IOException
	 */
	public static void serializeData(StateOutputStream outputStream)
		throws IOException {

		// Save utility data.
		Utility.serializeData(outputStream);
		// Save text editor data.
		TextEditorPane.serializeData(outputStream);
		DateTimeDialog.serializeData(outputStream);
		CssFontPanel.serializeData(outputStream);
		CssBorderPanel.serializeData(outputStream);
		CssOutlinesPanel.serializeData(outputStream);
		CssBoxShadowPanel.serializeData(outputStream);
		CssBackgroundImagesPanel.serializeData(outputStream);
		CssNumberPanel.serializeData(outputStream);
		CssBorderRadiusPanel.serializeData(outputStream);
		CssTextShadowPanel.serializeData(outputStream);
		CssBorderImagePanel.serializeData(outputStream);
		SelectFontNameDialog.serializeData(outputStream);
		CssClipPanel.serializeData(outputStream);
		CssFlexPanel.serializeData(outputStream);
		CssSpacingPanel.serializeData(outputStream);
		CssCountersPanel.serializeData(outputStream);
		CssListStylePanel.serializeData(outputStream);
		CssKeyframesPanel.serializeData(outputStream);
		CssKeyFrameItemDialog.serializeData(outputStream);
		CssFindPropertyDialog.serializeData(outputStream);
		CssAnimationPanel.serializeData(outputStream);
		CssPerspectiveOriginPanel.serializeData(outputStream);
		CssTransformPanel.serializeData(outputStream);
		CssTransformMatrixDialog.serializeData(outputStream);
		CssTransformOriginPanel.serializeData(outputStream);
		CssTransitionPanel.serializeData(outputStream);
		CssCursorPanel.serializeData(outputStream);
		CssQuotesPanel.serializeData(outputStream);
		CssTextLinePanel.serializeData(outputStream);
		CssResourcePanel.serializeData(outputStream);
		AnchorDialog.serializeData(outputStream);
		HelpDialog.serializeData(outputStream);
		CssResourcesUrlsPanel.serializeData(outputStream);
	}
}
