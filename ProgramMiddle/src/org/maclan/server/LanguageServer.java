/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.*;
import java.util.HashMap;

import org.maclan.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class LanguageServer {

	/**
	 * Process single tags.
	 * @param text
	 * @param language
	 * @param isRendering 
	 * @param server 
	 * @param renderingFlags 
	 * @return
	 */
	protected static String processSingleSubTags(String text, Language language,
			boolean isRendering, AreaServer server, HashMap<Long, RenderedFlag> renderingFlags)
		throws Exception {

		// If rendering flags are not null.
		if (renderingFlags != null) {
			
			// Get area assembled path.
			String absolutePath = server.getAreaAssembledPath(server.getRequestedArea().getId(), server.getCurrentVersionId());
			
			if (ServerUtilities.existsTag(text, "LANG_FLAG")) {
				RenderedFlag.addToSet(renderingFlags, language.id, absolutePath);
			}
		}
		
		// Replace flags.
		String format = isRendering ? "<img src=\"flag%d.png\" title=\"%s\">" : "<img src=\"?flag_id=%d\" title=\"%s\">";
		String imageTag = String.format(format, language.id, language.description);
		
		text = ServerUtilities.replaceTags(text, "LANG_FLAG", imageTag);

		// Replace descriptions.
		text = ServerUtilities.replaceTags(text, "LANG_DESCRIPTION", language.description);
		
		// Replace aliases.
		text = ServerUtilities.replaceTags(text, "LANG_ALIAS", language.alias);
		
		// Replace identifiers.
		text = ServerUtilities.replaceTags(text, "LANG_ID", String.valueOf(language.id));
		
		return text;
	}

	/**
	 * Load language flag.
	 * @param middle
	 * @param request
	 * @param response
	 * @return
	 */
	public static boolean loadFlag(MiddleLight middle,
			Request request, Response response) {
		
		MiddleResult result;
		long languageId;
		
		// Get parameter.
		String languageIdText = request.getParameter("flag_id");
		
		if (languageIdText != null) {
			languageId = Integer.parseInt(languageIdText);
		}
		else {
			return false;
		}
		
		
		// Load language flag.
		Obj<byte []> iconBytes = new Obj<byte[]>();
		
		result = middle.loadLanguageFlag(languageId, iconBytes);
		if (result.isNotOK()) {
			return true;
		}
		
		// Output icon data.
		try {
			// Get output stream.
			OutputStream outputStream = response.getOutputStream();
			
			// Write data.
			outputStream.write(iconBytes.ref);
		}
		catch (IOException e) {

		}
		
		return true;
	}

	/**
	 * Render flag.
	 * @param middle
	 * @param languageId
	 * @param outputStream
	 * @throws Exception
	 */
	public static void loadFlag(MiddleLight middle, long languageId,
			FileOutputStream outputStream) throws Exception {
		
		// Load language flag.
		Obj<byte []> iconBytes = new Obj<byte[]>();
		
		MiddleResult result = middle.loadLanguageFlag(languageId, iconBytes);
		if (result.isNotOK()) {
			throw new Exception(result.getMessage());
		}
		
		// Write data.
		outputStream.write(iconBytes.ref);
	}

	/**
	 * Set current language.
	 * @param middle
	 * @param request
	 */
	synchronized public static void setCurrentLanguage(MiddleLight middle,
			Request request) {

		MiddleResult result;
		
		// Get language ID.
		String languageIdText = request.getParameter("lang_id");
		if (languageIdText != null) {
			
			try {
				long languageId = Long.valueOf(languageIdText);
				// Set language.
				result = middle.setLanguage(languageId);
				if (result.isOK()) {
					return;
				}
			}
			catch (NumberFormatException e) {
			}
		}
		
		// Get start language.
		middle.setStartLanguageCurrent();
	}
}
