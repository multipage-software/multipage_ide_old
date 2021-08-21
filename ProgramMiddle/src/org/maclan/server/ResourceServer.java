/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.*;

import org.maclan.*;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class ResourceServer {

	/**
	 * Load resource
	 * @param middle 
	 * @param request
	 * @param response
	 */
	public static boolean loadResource(MiddleLight middle,
			Request request, Response response) {
		
		MiddleResult result;
		Long resourceId = null;
		String mimeType = null;
		
		// Get resource ID.
		String resourceIdText = request.getParameter("res_id");
		
		// If there area no parameters, return false value.
		if (resourceIdText == null) {
			return false;
		}
		
		if (resourceId == null && resourceIdText != null) {
				
			long resourceIdAux = Long.parseLong(resourceIdText);
			Obj<String> mimeTypeAux = new Obj<String>();
			
			// Load resource MIME type.
			result = middle.loadResourceMimeType(resourceIdAux, mimeTypeAux);
			if (result.isOK()) {
				resourceId = resourceIdAux;
				mimeType = mimeTypeAux.ref;
			}
		}
		
		if (resourceId != null) {
			
			// Set response MIME type.
			response.setContentType(mimeType);
			
			// Set file name.
			String fileName = request.getParameter("filename");
			if (fileName != null) {
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
			}

			// Get resource saving method.
			Obj<Boolean> savedAsText = new Obj<Boolean>();
			result = middle.loadResourceSavingMethod(resourceId, savedAsText);
			if (result.isOK()) {
				
				if (savedAsText.ref) {
					// Set coding.
					response.setCharacterEncoding("UTF-8");
					// Load from text.
					middle.loadResourceTextToStream(resourceId, response.getOutputStream());
				}
				else {
					// Load from BLOB.
					middle.loadResourceBlobToStream(resourceId, response.getOutputStream());
				}
			}
		}
		else {
			ServerUtilities.output(response.getOutputStream(), 
						Resources.getString("server.messageCannotFindResource"));
		}
		
		return true;
	}

	/**
	 * Load resource.
	 * @param middle
	 * @param resourceId
	 * @param outputStream
	 */
	public static MiddleResult loadResource(MiddleLight middle, long resourceId,
			FileOutputStream outputStream) {
		
		// Get resource saving method.
		Obj<Boolean> savedAsText = new Obj<Boolean>();
		MiddleResult result = middle.loadResourceSavingMethod(resourceId, savedAsText);
		if (result.isOK()) {
			
			if (savedAsText.ref) {
				// Load from text.
				result = middle.loadResourceTextToStream(resourceId, outputStream);
			}
			else {
				// Load from BLOB.
				result = middle.loadResourceBlobToStream(resourceId, outputStream);
			}
		}

		return result;
	}
}
