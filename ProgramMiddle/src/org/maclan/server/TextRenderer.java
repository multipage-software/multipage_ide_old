/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan.server;

import java.io.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.maclan.*;
import org.multipage.gui.Utility;
import org.multipage.util.*;

/**
 * @author
 *
 */
public class TextRenderer {
	
	/**
	 * Target.
	 */
	public static String serializedTarget = "###";

	/**
	 * List file name.
	 */
	private static final String listFileName = "list";

	/**
	 * Set default data.
	 */
	public static void setDefaultData() {
		
	}

	/**
	 * Load data.
	 * @param inputStream
	 */
	public static void serializeData(ObjectInputStream inputStream)
		throws IOException {
		
		serializedTarget = inputStream.readUTF();
	}

	/**
	 * Save data.
	 * @param outputStream
	 */
	public static void serializeData(ObjectOutputStream outputStream)
		throws IOException {
		
		outputStream.writeUTF(serializedTarget);
	}

	/**
	 * Middle layer reference.
	 */
	private MiddleLight middle;
	
	/**
	 * Target.
	 */
	private String target;
	
	/**
	 * Resources folder.
	 */
	private String resourcesFolder = "";
	
	/**
	 * Flag.
	 */
	private boolean skipErrorFiles = true;

	/**
	 * Page file names.
	 */
	private LinkedList<String> pageFileNames;

	/**
	 * Extracted page text length.
	 */
	private int extractedLength;

	/**
	 * Error flag.
	 */
	private boolean isError;

	/**
	 * Common resource file names flag.
	 */
	private boolean commonResourceFileNames = false;

	/**
	 * Constructor.
	 * @param properties 
	 * @throws Exception 
	 */
	public TextRenderer(Properties properties) throws Exception {
		
		// Create new light middle layer and login to the database.
		middle = MiddleUtility.newMiddleLightInstance();
		middle.enableCache(true);
		
		MiddleResult result = middle.login(properties);
		if (result.isNotOK()) {
			isError = true;
			throw new Exception(result.getMessage());
		}
		
		isError = false;
	}
	
	/**
	 * Constructor.
	 * @param middle
	 */
	public TextRenderer(MiddleLight middle) {
		
		this.middle = middle;
		
		isError = false;
	}
	
	/**
	 * Dispose object.
	 */
	public void dispose() {
		
		// Logout from the database.
		middle.logout(MiddleResult.OK);
	}

	/**
	 * Check whether target exists.
	 * @throws Exception
	 */
	private void checkTarget()
		throws Exception {
		
		File folder = new File(target);
		if (!folder.exists()) {
			throw new Exception(String.format(
					Resources.getString("server.messageRenderingTargetDoesntExist"), target));
		}
	}

	/**
	 * Render areas to HTML pages.
	 * @param areas
	 * @param languages 
	 * @param versions 
	 * @param encoding 
	 * @param showTextIds 
	 * @param generateList 
	 * @param generateIndex 
	 * @param target 
	 * @param renderRelatedAreas 
	 * @param renderingFolder 
	 * @param swingWorkerHelper 
	 * @throws Exception 
	 */
	public void render(LinkedList<Area> areas,
			LinkedList<Language> languages, LinkedList<VersionObj> versions, String encoding,
			boolean showTextIds, boolean generateList, boolean generateIndex,
			int extractedLength, String target,
			LinkedList<String> pageFileNames,
			Obj<Boolean> renderRelatedAreas, SwingWorkerHelper swingWorkerHelper)
			throws Exception {

		// Set target.
		this.target = target;
		
		this.pageFileNames = pageFileNames;
		
		this.extractedLength = extractedLength;
		
		// Check target.
		checkTarget();
		
		// Prepare list of file names.
		LinkedList<RenderedPage> listFiles = null;
		HashMap<String, RenderClass> renderClasses = null;
		HashMap<String, HashMap<Long, IndexEntry>> indexedWords = null;
		
		if (generateList) {
			listFiles = new LinkedList<RenderedPage>();
			renderClasses = new HashMap<String, RenderClass>();
			
			if (generateIndex) {
				indexedWords = new HashMap<String, HashMap<Long, IndexEntry>>();
			}
		}
		
		try {

			// Flags and resources.
			HashMap<Long, RenderedFlag> flags = new HashMap<Long, RenderedFlag>();
			Map<Long, LinkedList<RenderedResource>> resources = new HashMap<Long, LinkedList<RenderedResource>>();
			
			// Skip rendering list.
			LinkedList<SkipRendering> skipRenderingList = new LinkedList<SkipRendering>();
			
			// Do loop for all languages.
			for (Language language : languages) {
				
				// Reset list.
				if (listFiles != null) {
					
					listFiles.clear();
					renderClasses.clear();
					
					// Reset index.
					if (indexedWords != null) {
						indexedWords.clear();
					}
				}
				
				// Get list file name.
				String fileName = getListFileName(language);
				
				// Create empty area versions to render and already rendered area versions sets.
				LinkedList<AreaVersion> areaVersionsToRender = new LinkedList<AreaVersion>();
				HashSet<AreaVersion> renderedAreaVersions = new HashSet<AreaVersion>();
				
				// Set area versions to render.
				for (Area area : areas) {
					for (VersionObj version : versions) {
						
						areaVersionsToRender.add(new AreaVersion(area, version));
					}
				}
				
				double delta = 100.0 / areaVersionsToRender.size();
				Double progress = delta;
				
				// Render area versions.
				while (!areaVersionsToRender.isEmpty()) {

					// If there is only one area in this step blink the progress bar.
					if (swingWorkerHelper != null && areaVersionsToRender.size() == 1) {
						swingWorkerHelper.setProgressBar(0);
						Thread.sleep(100);
					}
					
					// If progress exceeds the maximum recompute it.
					if (progress.intValue() > 100) {
						
						delta = 100.0 / areaVersionsToRender.size();
						progress = delta;
					}
					
					// Set progress bar.
					if (swingWorkerHelper != null) {
						swingWorkerHelper.setProgressBar(progress.intValue());
					}
					
					// Get area version to render.
					AreaVersion areaVersion = areaVersionsToRender.removeFirst();
					
					// Add area version to rendered list.
					renderedAreaVersions.add(areaVersion);
					
					// Get area and version to render.
					Area areaToRender = areaVersion.getArea();
					VersionObj versionToRender = areaVersion.getVersion();
					
					// Process cancellation.
					if (swingWorkerHelper != null) {
						if (swingWorkerHelper.isScheduledCancel()) {
							throw new CancellationException();
						}
					}
					
					long areaId = areaToRender.getId();
					long versionId = versionToRender.getId();
					
					// If to skip rendering.
					if (SkipRendering.contains(skipRenderingList, areaId, versionId)) {
						continue;
					}
					
					// Skip next languages flag.
					Obj<Boolean> skipNextLanguages = new Obj<Boolean>(false);
					
					HashSet<AreaVersion> relatedAreaVersions = new HashSet<AreaVersion>();
					
					// Render area.
					renderArea(areaToRender, language, languages, versionToRender, encoding, showTextIds, flags, resources,
							listFiles, indexedWords, fileName, renderClasses, skipNextLanguages, relatedAreaVersions,
							swingWorkerHelper);
					
					// Process related areas.
					if (renderRelatedAreas.ref) {
						
						for (AreaVersion relatedAreaVersion : relatedAreaVersions) {
							
							// If the related area is already rendered, do nothing.
							if (renderedAreaVersions.contains(relatedAreaVersion)) {
								continue;
							}
							
							// Insert related area version into the area versions to render list if not already inserted.
							boolean include = true;
							for (AreaVersion areaVersionToRender : areaVersionsToRender) {
								if (areaVersionToRender.equals(relatedAreaVersion)) {
									include = false;
									break;
								}
							}
							if (include) {
								areaVersionsToRender.add(relatedAreaVersion);
							}
						}
					}
					
					// If to skip next languages add item to the list.
					if (skipNextLanguages.ref) {
						skipRenderingList.add(new SkipRendering(areaId, versionId));
					}
					
					// Update progress.
					progress += delta;
				}
			
				// Save list of pages.
				if (listFiles != null) {
					saveFileNamesList(listFiles, renderClasses, indexedWords, fileName, swingWorkerHelper);
				}
				
				// Clear middle cache because of the language change.
				middle.clearCache();
			}
			
			// Reset progress bar.
			int count = flags.size() + resources.size();
			double delta = 100.0 / count;
			Double progress = delta;
			
			// Render flags.
			for (RenderedFlag flag : flags.values()) {
				
				if (swingWorkerHelper != null) {
					swingWorkerHelper.setProgressBar(progress.intValue());
					if (swingWorkerHelper.isScheduledCancel()) {
						throw new CancellationException();
					}
				}
				
				
				renderFlag(flag, swingWorkerHelper);
				progress += delta;

			}
			
			// Render resources.
			for (LinkedList<RenderedResource> resourcesList : resources.values()) {
				
				if (swingWorkerHelper != null) {
					swingWorkerHelper.setProgressBar(progress.intValue());
					if (swingWorkerHelper.isScheduledCancel()) {
						throw new CancellationException();
					}
				}
				
				for (RenderedResource resource : resourcesList) {

					renderResource(resource, swingWorkerHelper);
				}
				progress += delta;
			}
		}
		catch (Exception e) {
			
			throw e;
		}
	}

	/**
	 * Get liast file name.
	 * @param language
	 * @return
	 */
	private String getListFileName(Language language) {
		
		return listFileName + "_" + language.id + ".list";
	}
	
	/**
	 * Save list of file names.
	 * @param listFiles
	 * @param renderClasses 
	 * @param indexedWords 
	 * @param language 
	 * @param swingWorkerHelper 
	 */
	private void saveFileNamesList(LinkedList<RenderedPage> listFiles,
			HashMap<String, RenderClass> renderClasses, 
			HashMap<String, HashMap<Long, IndexEntry>> indexedWords, String fileName,
			SwingWorkerHelper<Object> swingWorkerHelper) {

		File file = new File(target, fileName);
		FileOutputStream fileOutputStream = null;
		OutputStreamWriter outputWriter = null;
		PrintWriter writer = null;
		
		try {
			
			fileOutputStream = new FileOutputStream(file);
			outputWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
			writer = new PrintWriter(outputWriter);
			
			writer.println("{\"files\":[");
			boolean isFirst = true;
			
			// Save files.
			for (RenderedPage page : listFiles) {
				
				if (!isFirst) {
					writer.print(',');
				}
				else {
					writer.print(' ');
				}
				writer.print("{\"f\":\"" + page.fileName + '\"');
				
				if (page.renderClassName != null) {
					writer.print(",\"c\":\"" + page.renderClassName + '\"');
				}
				
				if (indexedWords == null) {
					writer.println('}');
				}
				else {
					writer.println(",\"i\":" + page.id + ",\"e\":\"" + page.pageExtract +"\"}");
				}
				isFirst = false;
			}
			
			// Save render classes.
			if (renderClasses != null && !renderClasses.isEmpty()) {
				
				writer.println("],");
				writer.println("\"classes\":[");
				
				isFirst = true;
				
				Set<Entry<String, RenderClass>> entries = renderClasses.entrySet();
				for (Entry<String, RenderClass> entry : entries) {
					
					if (!isFirst) {
						writer.print(',');
					}
					else {
						writer.print(' ');
					}
					
					writer.println("[\"" + entry.getKey() + "\",\"" + entry.getValue().getText() + "\"]");
					
					isFirst = false;
				}
			}
			
			// Save index.
			if (indexedWords == null) {
				writer.print("]}");
			}
			else {
				writer.println("],");
				writer.println("\"index\":[");
				
				isFirst = true;
				
				// Sort words.
				Set<String> wordsSet = indexedWords.keySet();
				ArrayList<String> wordsArray = new ArrayList<String>(wordsSet);
				Collections.sort(wordsArray);
				
				// Output words.
				for (String word : wordsArray) {
					
					if (!isFirst) {
						writer.print(',');
					}
					else {
						writer.print(' ');
					}
					
					// Output files list.
					HashMap<Long, IndexEntry> indexEntries = indexedWords.get(word);
					String indexesText = "";
					
					boolean isFirst2 = true;
					
					for (Long fileId : indexEntries.keySet()) {
						
						if (!isFirst2) {
							indexesText += ",";
						}
						
						IndexEntry indexEntry = indexEntries.get(fileId);
						indexesText += "[" + fileId + "," + indexEntry.occurence + "]";

						isFirst2 = false;
					}
					
					writer.println("{\"w\":\"" + word + "\",\"i\":[" + indexesText + "]}");
					
					isFirst = false;
				}
				
				writer.print("]}");
			}
		}
		catch (Exception e) {
			
			if (swingWorkerHelper != null) {
				swingWorkerHelper.addMessage(
						Resources.getString("server.messageCannotCreateListFile") + 
						": " + e.getMessage());
			}
		}
		finally {

			if (writer != null) {
				writer.close();
			}
			if (outputWriter != null) {
				try {
					outputWriter.close();
				} catch (IOException e) {
				}
			}
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Render resource.
	 * @param resource
	 * @param swingWorkerHelper 
	 * @throws Exception 
	 */
	private void renderResource(RenderedResource resource, SwingWorkerHelper<Object> swingWorkerHelper)
			throws Exception {
		
		// Open file stream.
		String resourceFileName;
		if (resource.getFileName() == null) {
			
			resourceFileName = "res" + resource.getId();
			
			String extension = resource.getExtension();
			if (extension != null) {
				resourceFileName += "." + extension;
			}
		}
		else {
			resourceFileName = resource.getFileName();
		}
		
		// Get resource paths.
		LinkedList<String> renderedPaths = resource.getRenderedPaths();
		
		// Do loop for all paths.
		for (String renderedPath : renderedPaths) {
			
			// Compile path name.
			String renderedPathName = null;
			if (renderedPath == null) {
				renderedPathName = target;
			}
			else {
				renderedPathName = target + File.separator + renderedPath;
			}

			// Create file object.
			File resourceFile = new File(renderedPathName, resourceFileName);
			
			// Create path.
			File parentFile = resourceFile.getParentFile();
			if (parentFile != null) {
				parentFile.mkdirs();
			}
			
			// Create output stream.
			FileOutputStream outputStream = new FileOutputStream(resourceFile);
			
			MiddleResult result = ResourceServer.loadResource(middle, resource.getId(), outputStream);
			if (result.isNotOK()) {
				
				String messageStart = String.format(
						Resources.getString("server.textResourceError"), resource.toString());
				
				if (swingWorkerHelper != null) {
					swingWorkerHelper.addMessage(messageStart + " " + result.getMessage());
				}
			}
			
			// Close file stream.
			outputStream.close();
		}
	}

	/**
	 * Render flag.
	 * @param flag
	 * @param swingWorkerHelper 
	 */
	private void renderFlag(RenderedFlag flag, SwingWorkerHelper<Object> swingWorkerHelper)
		throws Exception {
		
		long languageId = flag.getLanguageId();
		
		// Do loop for all paths.
		for (String absolutePath : flag.getAbsolutePaths()) {

			// Compile path name.
			String pathName = null;
			if (absolutePath.isEmpty()) {
				pathName = target;
			}
			else {
				pathName = target + File.separator + absolutePath;
			}
			
			// Open file stream.
			String flagFileName = "flag" + languageId + ".png";
			File flagFile = new File(pathName, flagFileName);
			
			// Create directories.
			File parentFile = flagFile.getParentFile();
			if (parentFile != null) {
				parentFile.mkdirs();
			}
			
			FileOutputStream outputStream = new FileOutputStream(flagFile);
			
			boolean error = false;
			
			try {
				LanguageServer.loadFlag(middle, languageId, outputStream);
			}
			catch (Exception e) {
				error = true;
				
				String messageStart = String.format(
						Resources.getString("server.textLanguageFlagError"), flagFileName);
				
				if (swingWorkerHelper != null) {
					swingWorkerHelper.addMessage(messageStart + " " + e.getMessage());
				}
			}
			
			// Close file stream.
			outputStream.close();
			
			if (error) {
				flagFile.delete();
			}
		}
	}

	/**
	 * Render single area.
	 * @param loginProperties 
	 * @param area
	 * @param languages 
	 * @param version 
	 * @param showTextIds 
	 * @param flags 
	 * @param resources 
	 * @param listFiles 
	 * @param indexedWords 
	 * @param listFileName 
	 * @param skipNextLanguages 
	 * @param relatedAreaVersions 
	 * @param swingWorkerHelper 
	 * @throws Exception 
	 */
	private void renderArea(final Area area, final Language language, final LinkedList<Language> languages, final VersionObj version,
			final String encoding, boolean showTextIds, HashMap<Long, RenderedFlag> flags, Map<Long, LinkedList<RenderedResource>> resources,
			final LinkedList<RenderedPage> listFiles, final HashMap<String, HashMap<Long, IndexEntry>> indexedWords,
			final String listFileName, final Map<String, RenderClass> renderClasses, final Obj<Boolean> skipNextLanguages,
			HashSet<AreaVersion> relatedAreaVersions, final SwingWorkerHelper<Object> swingWorkerHelper)
					throws Exception {

		// If the area is not visible, exit the method.
		if (!area.isVisible()) {
			return;
		}
		
		// If the area is a constructor area, exit the method.
		if (area.isConstructorArea()) {
			return;
		}
		
		// Create request parameters.
		Hashtable<String, String[]> parameters = new Hashtable<String, String[]>();
		String [] areaIdText = {((Long) area.getId()).toString()};
		parameters.put("area_id", areaIdText);
		
		String [] languageIdText = {((Long) language.id).toString()};
		parameters.put("lang_id", languageIdText);
		
		String [] versionIdText = {((Long) version.getId()).toString()};
		parameters.put("ver_id", versionIdText);
		
		// Create request object.
		Request request = new Request(null, parameters);
		
		final Obj<FileOutputStream> outputStream  = new Obj<FileOutputStream>();
		final Obj<File> renderedFile = new Obj<File>();
		
		// Create area server.
		final AreaServer areaServer = new AreaServer();
		
		// Create private response object.
		Response response = new Response(null, new ResponseAdapter() {
			
			String fileExtension = "htm";
			String contentType = "";

			@Override
			public void setContentType(String contentType) {
				
				this.contentType = contentType;
			}

			@Override
			public void setContentExtension(String extension) {

				// Set file extension.
				if (extension != null && version.isDefault()) {
					fileExtension = extension;
				}
			}

			@Override
			public OutputStream getOutputStream() {
				
				if (outputStream.ref == null) {
					
					// Create output stream.
					try {
						// Get rendered file name.
						String renderedFileName = areaServer.getRenderedAreaFileName(area.getId(), language.id, version.getId());
						
						// Get area assembled path.
						String areaAbsolutePath = areaServer.getAreaAssembledPath(area.getId(), version.getId());
						
						// Check file name. It doesn't have to be list file filename.
						if (listFiles != null) {
							if (listFileName.equals(renderedFileName)) {
								if (swingWorkerHelper != null) {
									swingWorkerHelper.addMessage(
											String.format(Resources.getString("server.messageRenderedFileEqualsToListFileName"),
													listFileName));
								}
							}
						}
						
						// Compile rendered path name.
						String renderedPathName = null;
						
						if (!areaAbsolutePath.isEmpty()) {
							renderedPathName = target + File.separator + areaAbsolutePath;
						}
						else {
							renderedPathName = target;
						}
						
						// Create output stream.
						renderedFile.ref = new File(renderedPathName, renderedFileName);
						File parentFile = renderedFile.ref.getParentFile();
						if (parentFile != null) {
							parentFile.mkdirs();
						}
						outputStream.ref = new FileOutputStream(renderedFile.ref);
						
						// Add file name to the list.
						if (listFiles != null && (fileExtension.equalsIgnoreCase("htm")
								               || fileExtension.equalsIgnoreCase("html"))) {
							
							listFiles.add(new RenderedPage(renderedFileName, area.getId()));
						}
						
						if (pageFileNames != null) {
							
							// Save page file name and relative path.
							String relativePath = Utility.getRelativePath(target, renderedPathName);
							String fileNameToSave = relativePath.isEmpty()
									? renderedFileName : relativePath + File.separator + renderedFileName;
							
							// Check if page file already exists.
							if (Utility.isStringInList(fileNameToSave, pageFileNames)) {
								if (swingWorkerHelper != null) {
									swingWorkerHelper.addMessage(
											String.format(Resources.getString("server.messageRenderedFileAlreadyExists"),
													fileNameToSave));
								}
							}
							else {
								pageFileNames.add(fileNameToSave);
							}
						}
					}
					catch (Exception e) {
					}
				}
				// Get output stream.
				return outputStream.ref;
			}
			@Override
			public Writer getWriter(OutputStream outputStream) {
				// Set HTML encoding tag.
				try {
					Writer writer = new OutputStreamWriter(outputStream, encoding);
					return writer;
				}
				catch (UnsupportedEncodingException e) {
					return null;
				}
				catch (Exception e2) {
					return null;
				}
			}
			@Override
			public String postProcessText(String text, RenderClass renderClass)
				throws Exception {
				
				if (listFiles != null) {
					// Find rendered page.
					long fileId = area.getId();
					RenderedPage renderedPage = null;
					for (RenderedPage item : listFiles) {
						
						if (item.id == fileId) {
							
							renderedPage = item;
							
							String renderClassName = null;
							
							if (renderClass != null) {
								renderClassName = renderClass.getName();
								
								if (renderClasses != null && renderClassName != null) {
									renderClasses.put(renderClassName, renderClass);
								}
							}
	
							renderedPage.renderClassName = renderClassName;
							break;
						}
					}
					
					// Create indexes.
					if (indexedWords != null) {
						computeIndexesAndExtract(indexedWords, renderedPage, text, fileId);
					}
				}
				
				if (isMimeHtml(contentType)) {
					
					// Get current tabulator character.
					String tabulator = "\t";
					AreaServer areaServer = response.getAreaServer();
					
					if (areaServer != null) {
						String serverTabulator = areaServer.getTabulator();
						if (serverTabulator != null) {
							tabulator = serverTabulator;
						}
					}
					
					if (areaServer.useMetaCharset()) {
						return addMetaCharset(text, encoding, tabulator);
					}
				}
				return text;
			}
			
			@Override
			public void setOutputNotLocalized() {
				
				// Set "skip next languages" flag.
				skipNextLanguages.ref = true;
			}
		});

		// Create blocks stack.
		BlockDescriptorsStack blocks = new BlockDescriptorsStack();
		// Create analysis object.
		Analysis analysis = new Analysis();
		
		// Set language.
		middle.setLanguage(language.id);
		
		// Create area server listener.
		AreaServerListener areaServerListener = new AreaServerListener() {
			// Check if the language is rendered.
			@Override
			public boolean isRendered(long languageId) {
				
				for (Language renderedLanguage : languages) {
					if (renderedLanguage.id == languageId) {
						return true;
					}
				}
				return false;
			}
			// Get rendering target.
			@Override
			public String getRenderingTarget() {
				
				return target;
			}
			// On area server error.
			@Override
			public void onError(String message) {

				if (swingWorkerHelper != null) {
					swingWorkerHelper.addMessage(message);
				}
			}
		};
		
		areaServer.initServerState();
		areaServer.setListener(areaServerListener);
		areaServer.setRendering(true);
		areaServer.setRenderingFlags(flags);
		areaServer.setRenderingResources(resources);
		areaServer.setShowLocalizedTextIds(showTextIds);
		areaServer.setRelatedAreaVersions(relatedAreaVersions);
		areaServer.setCommonResourceFileNames(commonResourceFileNames);
		areaServer.setResourcesRenderFolder(resourcesFolder);
		
		areaServer.loadAreaPage(middle, blocks, analysis, request, response);

		// Close the output stream.
		if (outputStream.ref != null) {
			outputStream.ref.close();
		}
		
		// If area not loaded, remove the file.
		if (skipErrorFiles && response.isError() && renderedFile.ref != null) {
			renderedFile.ref.delete();
		}
		
		isError = response.isError();
	}

	/**
	 * Returns true value if MIME type is HTML.
	 * @param contentType
	 * @return
	 */
	protected boolean isMimeHtml(String mimeType) {
		
		return mimeType.equalsIgnoreCase("text/html");
	}

	/**
	 * Add character set in the meta tag after the head tag.
	 * @param text
	 * @param coding
	 * @param tabulator 
	 * @return
	 */
	protected String addMetaCharset(String text, String coding, String tabulator) {
		
		String metaTag = String.format(
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\">",
				coding);
		
		// Try to find <head> tag.
		Pattern headPattern = Pattern.compile("<\\s*head\\s*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = headPattern.matcher(text);
		
		// If not found, put it at the beginning.
		if (!matcher.find()) {
			return metaTag + "\r\n" + text;
		}
		
		String outputText = text.substring(0, matcher.end()) + "\r\n" + tabulator + metaTag + text.substring(matcher.end());
		return outputText;
	}

	/**
	 * @param skipErrorFiles the skipErrorFiles to set
	 */
	public void setSkipErrorFiles(boolean skipErrorFiles) {
		this.skipErrorFiles = skipErrorFiles;
	}

	/**
	 * Compute indexes.
	 * @param indexedWords
	 * @param renderedPage 
	 * @param htmlText
	 * @param renderClass 
	 * @param fileId 
	 */
	protected void computeIndexesAndExtract(HashMap<String, HashMap<Long, IndexEntry>> indexedWords,
			RenderedPage renderedPage, String htmlText, long fileId)
				throws Exception {
		
		String pageExtract = "";
		// Try to get extract.
		if (renderedPage != null) {
			
			final String [] tagsToExtract = {"h1", "h2", "h3", "h4", "h5", "h6", "title"};
			
			for (String tagName : tagsToExtract) {
				pageExtract = getHtmlTagTextContent(htmlText, tagName);
				if (!pageExtract.isEmpty()) {
					break;
				}
			}
		}
		
		// Extract page body.
		htmlText = extractPageBody(htmlText);

		// Convert HTML to plain text.
		htmlText = convertHtmlToPlainText(htmlText);

		// Try to get extract.
		if (renderedPage != null) {
			if (pageExtract.isEmpty()) {
				pageExtract = htmlText;
			}
			
			// Abbreviate extract.
			if (extractedLength < pageExtract.length()) {
				pageExtract = pageExtract.substring(0, extractedLength) + "...";
			}
		}
		
		// Adjust page extract.
		pageExtract = pageExtract.trim();
		pageExtract = pageExtract.replace('\"', ' ');
		pageExtract = pageExtract.replace('\'', ' ');
		pageExtract = pageExtract.replace('\\', ' ');
		
		if (renderedPage != null) {
			renderedPage.setPageExtract(pageExtract);
		}
		
		// Covert to upper case.
		htmlText = htmlText.toUpperCase();
		
		// Remove diacritics.
		htmlText = removeDiacritics(htmlText);
		
		// Split words.[\^$.|?*+(){}
		String [] words = htmlText.split("[\\s\\.\\?\\\\\\[!\\+-/<>\\{\\}\\(\\),;:\"'\\*]{1,}");
		
		// Do loop for all words.
		for (String word : words) {
			
			word = word.trim();
			if (word.isEmpty()) {
				continue;
			}
			
			// Get existing word.
			HashMap<Long, IndexEntry> indexEntries = indexedWords.get(word);
			if (indexEntries == null) {
				
				indexEntries = new HashMap<Long, IndexEntry>();
				indexedWords.put(word, indexEntries);
			}
				
			// Get file entry.
			IndexEntry indexEntry = indexEntries.get(fileId);
			if (indexEntry == null) {
			
				// Create new entry.
				indexEntry = new IndexEntry(1);
				indexEntries.put(fileId, indexEntry);
			}
			else {
				indexEntry.incrementOccurance();
			}
		}
	}

	/**
	 * Get first HTML tag content.
	 * @param htmlText
	 * @param tagName
	 * @return
	 */
	private String getHtmlTagTextContent(String htmlText, String tagName) {
		
		String tagContentText = "";
		
		Pattern tagStartPattern = Pattern.compile("<\\s*" + tagName + "[^>]*>", Pattern.CASE_INSENSITIVE);
		Matcher tagStartMatcher = tagStartPattern.matcher(htmlText);
		if (tagStartMatcher.find()) {
		
			int startIndex = tagStartMatcher.end();
			int endIndex = htmlText.length();
			
			Pattern tagEndPattern = Pattern.compile("<\\s*/\\s*" + tagName + "\\s*>", Pattern.CASE_INSENSITIVE);
			Matcher tagEndMatcher = tagEndPattern.matcher(htmlText);
			if (tagEndMatcher.find(startIndex)) {
				
				endIndex = tagEndMatcher.start();

			}
			
			tagContentText = htmlText.substring(startIndex, endIndex);
			tagContentText = convertHtmlToPlainText(tagContentText);
		}

		return tagContentText;
	}

	/**
	 * Extract text body.
	 * @param htmlText
	 * @return
	 */
	private String extractPageBody(String htmlText) {
		
		int bodyStartIndex = 0;
		
		Pattern bodyStartPattern = Pattern.compile("<\\s*body[^>]*>", Pattern.CASE_INSENSITIVE);
		Matcher bodyStartMatcher = bodyStartPattern.matcher(htmlText);
		
		if (bodyStartMatcher.find()) {
			bodyStartIndex = bodyStartMatcher.end();
		}
		
		int bodyEndIndex = htmlText.length();
		
		Pattern bodyEndPattern = Pattern.compile("<\\s*/\\s*body\\s*>", Pattern.CASE_INSENSITIVE);
		Matcher bodyEndMatcher = bodyEndPattern.matcher(htmlText);
		
		Integer lastFound = null;
		while (bodyEndMatcher.find()) {
			lastFound = bodyEndMatcher.start();
		}
		
		if (lastFound != null) {
			bodyEndIndex = lastFound;
		}
		
		String htmlBodyText = htmlText.substring(bodyStartIndex, bodyEndIndex);
		
		return htmlBodyText;
	}

	/**
	 * Covert HTML to plain text.
	 * @param htmlText
	 * @return
	 */
	private String convertHtmlToPlainText(String htmlText) {
		
		// Remove scripts.
		htmlText = removeScripts(htmlText);
		
		// Remove markups.
		htmlText = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE).matcher(htmlText).replaceAll("");
		
		// Replace special entities.
		htmlText = replaceSpecialEntities(htmlText);
		
		// Remove multiple spaces.
		htmlText = Pattern.compile("\\s{1,}", Pattern.CASE_INSENSITIVE).matcher(htmlText).replaceAll(" ");
		
		return htmlText.trim();
	}

	/**
	 * Remove scripts.
	 * @param htmlText
	 * @return
	 */
	private String removeScripts(String htmlText) {
		
		Pattern scriptStartPattern = Pattern.compile("<\\s*script[^>]*>", Pattern.CASE_INSENSITIVE);
		Pattern scriptEndPattern = Pattern.compile("<\\s*/\\s*script\\s*>", Pattern.CASE_INSENSITIVE);

		while (true) {
			
			// Find script start.
			Matcher scriptStartMatch = scriptStartPattern.matcher(htmlText);
			if (!scriptStartMatch.find()) {
				break;
			}
			
			int startIndex = scriptStartMatch.start();
			int endIndex;
			
			int length = htmlText.length();
			
			// Find script end.
			Matcher scriptEndMatch = scriptEndPattern.matcher(htmlText);
			if (scriptEndMatch.find()) {
				endIndex = scriptEndMatch.end();
			}
			else {
				endIndex = length;
			}
			
			// Extract script.
			htmlText = htmlText.substring(0, startIndex) + htmlText.substring(endIndex, length);
		}
				
		return htmlText;
	}

	/**
	 * Replace special entities.
	 * @param htmlText
	 * @return
	 */
	private String replaceSpecialEntities(String htmlText) {

	    String [][] table = 
			    	   {{"&nbsp;", " "},
			    		{"&amp;", "&"},
			    		{"&lt;", "<"},
			    		{"&gt;", ">"}};
	    
	    for (String [] item : table ) {
	    	
	    	htmlText = Pattern.compile(item[0], Pattern.CASE_INSENSITIVE).matcher(htmlText).replaceAll(item[1]);
	    }
		return htmlText;
	}
	
	/**
	 * Remove diacritics.
	 * @param text
	 * @return
	 */
	private String removeDiacritics(String text) {
		
        String [][] table =
            {{"A", "[\\u0041\\u24B6\\uFF21\\u00C0\\u00C1\\u00C2\\u1EA6\\u1EA4\\u1EAA\\u1EA8\\u00C3\\u0100\\u0102\\u1EB0\\u1EAE\\u1EB4\\u1EB2\\u0226\\u01E0\\u00C4\\u01DE\\u1EA2\\u00C5\\u01FA\\u01CD\\u0200\\u0202\\u1EA0\\u1EAC\\u1EB6\\u1E00\\u0104\\u023A\\u2C6F]"},
            {"AA","[\\uA732]"},
            {"AE","[\\u00C6\\u01FC\\u01E2]"},
            {"AO","[\\uA734]"},
            {"AU","[\\uA736]"},
            {"AV","[\\uA738\\uA73A]"},
            {"AY","[\\uA73C]"},
            {"B", "[\\u0042\\u24B7\\uFF22\\u1E02\\u1E04\\u1E06\\u0243\\u0182\\u0181]"},
            {"C", "[\\u0043\\u24B8\\uFF23\\u0106\\u0108\\u010A\\u010C\\u00C7\\u1E08\\u0187\\u023B\\uA73E]"},
            {"D", "[\\u0044\\u24B9\\uFF24\\u1E0A\\u010E\\u1E0C\\u1E10\\u1E12\\u1E0E\\u0110\\u018B\\u018A\\u0189\\uA779]"},
            {"DZ","[\\u01F1\\u01C4]"},
            {"Dz","[\\u01F2\\u01C5]"},
            {"E", "[\\u0045\\u24BA\\uFF25\\u00C8\\u00C9\\u00CA\\u1EC0\\u1EBE\\u1EC4\\u1EC2\\u1EBC\\u0112\\u1E14\\u1E16\\u0114\\u0116\\u00CB\\u1EBA\\u011A\\u0204\\u0206\\u1EB8\\u1EC6\\u0228\\u1E1C\\u0118\\u1E18\\u1E1A\\u0190\\u018E]"},
            {"F", "[\\u0046\\u24BB\\uFF26\\u1E1E\\u0191\\uA77B]"},
            {"G", "[\\u0047\\u24BC\\uFF27\\u01F4\\u011C\\u1E20\\u011E\\u0120\\u01E6\\u0122\\u01E4\\u0193\\uA7A0\\uA77D\\uA77E]"},
            {"H", "[\\u0048\\u24BD\\uFF28\\u0124\\u1E22\\u1E26\\u021E\\u1E24\\u1E28\\u1E2A\\u0126\\u2C67\\u2C75\\uA78D]"},
            {"I", "[\\u0049\\u24BE\\uFF29\\u00CC\\u00CD\\u00CE\\u0128\\u012A\\u012C\\u0130\\u00CF\\u1E2E\\u1EC8\\u01CF\\u0208\\u020A\\u1ECA\\u012E\\u1E2C\\u0197]"},
            {"J", "[\\u004A\\u24BF\\uFF2A\\u0134\\u0248]"},
            {"K", "[\\u004B\\u24C0\\uFF2B\\u1E30\\u01E8\\u1E32\\u0136\\u1E34\\u0198\\u2C69\\uA740\\uA742\\uA744\\uA7A2]"},
            {"L", "[\\u004C\\u24C1\\uFF2C\\u013F\\u0139\\u013D\\u1E36\\u1E38\\u013B\\u1E3C\\u1E3A\\u0141\\u023D\\u2C62\\u2C60\\uA748\\uA746\\uA780]"},
            {"LJ","[\\u01C7]"},
            {"Lj","[\\u01C8]"},
            {"M", "[\\u004D\\u24C2\\uFF2D\\u1E3E\\u1E40\\u1E42\\u2C6E\\u019C]"},
            {"N", "[\\u004E\\u24C3\\uFF2E\\u01F8\\u0143\\u00D1\\u1E44\\u0147\\u1E46\\u0145\\u1E4A\\u1E48\\u0220\\u019D\\uA790\\uA7A4]"},
            {"NJ","[\\u01CA]"},
            {"Nj","[\\u01CB]"},
            {"O", "[\\u004F\\u24C4\\uFF2F\\u00D2\\u00D3\\u00D4\\u1ED2\\u1ED0\\u1ED6\\u1ED4\\u00D5\\u1E4C\\u022C\\u1E4E\\u014C\\u1E50\\u1E52\\u014E\\u022E\\u0230\\u00D6\\u022A\\u1ECE\\u0150\\u01D1\\u020C\\u020E\\u01A0\\u1EDC\\u1EDA\\u1EE0\\u1EDE\\u1EE2\\u1ECC\\u1ED8\\u01EA\\u01EC\\u00D8\\u01FE\\u0186\\u019F\\uA74A\\uA74C]"},
            {"OI","[\\u01A2]"},
            {"OO","[\\uA74E]"},
            {"OU","[\\u0222]"},
            {"P", "[\\u0050\\u24C5\\uFF30\\u1E54\\u1E56\\u01A4\\u2C63\\uA750\\uA752\\uA754]"},
            {"Q", "[\\u0051\\u24C6\\uFF31\\uA756\\uA758\\u024A]"},
            {"R", "[\\u0052\\u24C7\\uFF32\\u0154\\u1E58\\u0158\\u0210\\u0212\\u1E5A\\u1E5C\\u0156\\u1E5E\\u024C\\u2C64\\uA75A\\uA7A6\\uA782]"},
            {"S", "[\\u0053\\u24C8\\uFF33\\u1E9E\\u015A\\u1E64\\u015C\\u1E60\\u0160\\u1E66\\u1E62\\u1E68\\u0218\\u015E\\u2C7E\\uA7A8\\uA784]"},
            {"T", "[\\u0054\\u24C9\\uFF34\\u1E6A\\u0164\\u1E6C\\u021A\\u0162\\u1E70\\u1E6E\\u0166\\u01AC\\u01AE\\u023E\\uA786]"},
            {"TZ","[\\uA728]"},
            {"U", "[\\u0055\\u24CA\\uFF35\\u00D9\\u00DA\\u00DB\\u0168\\u1E78\\u016A\\u1E7A\\u016C\\u00DC\\u01DB\\u01D7\\u01D5\\u01D9\\u1EE6\\u016E\\u0170\\u01D3\\u0214\\u0216\\u01AF\\u1EEA\\u1EE8\\u1EEE\\u1EEC\\u1EF0\\u1EE4\\u1E72\\u0172\\u1E76\\u1E74\\u0244]"},
            {"V", "[\\u0056\\u24CB\\uFF36\\u1E7C\\u1E7E\\u01B2\\uA75E\\u0245]"},
            {"VY","[\\uA760]"},
            {"W", "[\\u0057\\u24CC\\uFF37\\u1E80\\u1E82\\u0174\\u1E86\\u1E84\\u1E88\\u2C72]"},
            {"X", "[\\u0058\\u24CD\\uFF38\\u1E8A\\u1E8C]"},
            {"Y", "[\\u0059\\u24CE\\uFF39\\u1EF2\\u00DD\\u0176\\u1EF8\\u0232\\u1E8E\\u0178\\u1EF6\\u1EF4\\u01B3\\u024E\\u1EFE]"},
            {"Z", "[\\u005A\\u24CF\\uFF3A\\u0179\\u1E90\\u017B\\u017D\\u1E92\\u1E94\\u01B5\\u0224\\u2C7F\\u2C6B\\uA762]"}};

        
        for (String [] item : table) {
        	
        	text = text.replaceAll(item[1], item[0]);
        }
		return text;
	}

	/**
	 * @return the isError
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * Render area or related areas to application directory on server.
	 * @param server
	 * @param area
	 * @param renderRelatedAreas
	 */
	public static void renderToServer(AreaServer server, Area area, boolean renderRelatedAreas, String encoding)
		throws Exception {
		
		// Render HTML pages.
		TextRenderer renderer = null;
		
		try {
			// Create renderer.
			renderer = new TextRenderer(server.state.middle);
			
			// Set renderer parameters.
			
			String applicationDirectory = server.state.request.getServerRootPath();
			renderer.setSkipErrorFiles(false);
			Obj<Boolean> renderRelatedAreasObj = new Obj<Boolean>(renderRelatedAreas);
			
			LinkedList<Area> areas = new LinkedList<Area>();
			areas.add(area);
			LinkedList<Language> languages = new LinkedList<Language>();
			languages.add(server.getCurrentLanguage());
			LinkedList<VersionObj> versions = new LinkedList<VersionObj>();
			versions.add(server.getVersion(server.getCurrentVersionId()));
			LinkedList<String> fileNames = new LinkedList<String>();
			
			if (encoding == null) {
				encoding = "UTF-8";
			}
			
			// Render area to application directory.
			renderer.render(areas, languages, versions, encoding, server.isShowLocalizedTextIds(), false,
					false, 0, applicationDirectory, fileNames, renderRelatedAreasObj, null);
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Set common resource file names flag.
	 * @param flag
	 */
	public void setCommonResourceFileNamesFlag(boolean flag) {
		
		this.commonResourceFileNames  = flag;
	}
	
	/**
	 * Set resources folder.
	 * @param resourcesFolder
	 */
	public void setResourcesFolder(String resourcesFolder) {
		
		if (resourcesFolder == null) {
			resourcesFolder = "";
		}
		this.resourcesFolder = resourcesFolder;
	}
}

/**
 * Rendered page class.
 * @author
 *
 */
class RenderedPage {

	/**
	 * File name.
	 */
	String fileName;
	
	/**
	 * Identifier.
	 */
	long id;
	
	/**
	 * Page extract.
	 */
	String pageExtract;
	
	/**
	 * Render class.
	 */
	String renderClassName;

	/**
	 * Constructor.
	 */
	RenderedPage(String fileName, long id) {
		
		this.fileName = fileName;
		this.id = id;
	}

	/**
	 * Set page extract.
	 * @param pageExtract
	 */
	public void setPageExtract(String pageExtract) {
		
		this.pageExtract = pageExtract;
	}
}

/**
 * Word index entry.
 * @author
 *
 */
class IndexEntry {

	/**
	 * Occurrence.
	 */
	int occurence;
	
	/**
	 * Constructor.
	 */
	IndexEntry(int occurence) {
		
		this.occurence = occurence;
	}

	/**
	 * Increment occurance.
	 */
	public void incrementOccurance() {
		
		occurence++;
	}
}


/**
 * Skip rendering class.
 * @author
 *
 */
class SkipRendering {
	
	/**
	 * Fields.
	 */
	long areaId;
	long versionId;
	
	/**
	 * Constructor.
	 * @param areaId
	 * @param versionId
	 */
	SkipRendering(long areaId, long versionId) {
		
		this.areaId = areaId;
		this.versionId = versionId;
	}

	/**
	 * Returns true if the list constains given information.
	 * @param list
	 * @param areaId
	 * @param versionId
	 * @return
	 */
	static boolean contains(LinkedList<SkipRendering> list,
			long areaId, long versionId) {
		
		// Do loop for all items.
		for (SkipRendering item : list) {
			
			if (item.areaId == areaId && item.versionId == versionId) {
				return true;
			}
		}
		
		return false;
	}
}
