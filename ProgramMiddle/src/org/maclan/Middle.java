/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.awt.image.*;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

import org.multipage.util.*;

/**
 * 
 * @author
 *
 */
public interface Middle extends MiddleLight {
	
	/**
	 * Set model.
	 * @param model
	 */
	public void setModel(AreasModel model);
	
	/**
	 * Create database if it doesn't exist.
	 * @param loginProperties
	 * @param selectDatabaseCallback = can be null
	 * @param isNewDatabase
	 */
	public MiddleResult attachOrCreateNewBasicArea(Properties loginProperties,
			Function<LinkedList<String>, String> selectDatabaseCallback, Obj<Boolean> isNewDatabase);

	/**
	 * Update language text.
	 * @param textId
	 * @param languageId
	 * @param text
	 * @return
	 */
	public MiddleResult updateLanguageText(long languageId, long textId, String text);

	/**
	 * Update language text.
	 * @param login
	 * @param textId
	 * @param languageId
	 * @param text
	 * @return
	 */
	public MiddleResult updateLanguageText(Properties login, long languageId,
			long textId, String text);
	
	/**
	 * Remove language text.
	 * @param textId
	 * @param languageId
	 * @return
	 */
	public MiddleResult removeLanguageText(long languageId, long textId);

	/**
	 * Delete current language text.
	 * @param textId
	 * @return
	 */
	public MiddleResult removeCurrentLanguageText(long textId);

	/**
	 * Delete language text.
	 * @param login
	 * @param textId
	 * @param languageId 
	 * @return
	 */
	public MiddleResult removeLanguageText(Properties login, long languageId, long textId);

	/**
	 * Loads namespace tree.
	 */
	public MiddleResult loadNamespaces(Properties loginProperties, NamespacesModel model);

	/**
	 * Remove namespace tree.
	 */
	public MiddleResult removeNamespaceTree(Properties properties,
			Namespace namespace, final NamespacesModel model);

	/**
	 * Check login.
	 */
	public MiddleResult checkLogin(Properties properties);
	
	/**
	 * Inserts namespace node.
	 * @param model 
	 */
	public MiddleResult insertNamespace(Properties loginProperties,
			Namespace namespace);
	
	/**
	 * Update namespace description.
	 */
	public MiddleResult updateNamespaceDescritpion(
			Properties properties,
			Namespace namespace);

	/**
	 * Load areas and programs.
	 * @param loadHiddenSlots 
	 */
	public MiddleResult loadAreasModel(Properties properties,
			AreasModel model, boolean loadHiddenSlots);
	
	/**
	 * Load areas extended.
	 * @param statement
	 * @param set
	 * @param model
	 * @throws SQLException
	 */
	public void loadAreasExtended(PreparedStatement statement, ResultSet set,
			AreasModel model) throws SQLException;
	
	/**
	 * Insert new area.
	 * @param login
	 * @param parentArea
	 * @param newArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult insertArea(Properties login,
			Area parentArea, Area newArea, boolean inheritance,
			String relationNameSub, String relationNameSuper);
	
	/**
	 * Insert new area.
	 * @param parentArea
	 * @param newArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult insertArea(
			Area parentArea, Area newArea, boolean inheritance,
			String relationNameSub, String relationNameSuper);

	/**
	 * Remove area.
	 * @param area
	 * @return
	 */
	public MiddleResult removeArea(Area area);

	/**
	 * Connects sub area with parent area. Caution: the resulting area graph
	 * must not contain any loops.
	 * @param login
	 * @param parentArea
	 * @param subArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub
	 * @param recursion
	 * @return
	 */
	public MiddleResult connectSimplyAreas(Properties login,
			Area parentArea, Area subArea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub,
			boolean recursion);
	
	/**
	 * Connects sub area with parent area. Caution: the resulting area graph
	 * must not contain any loops.
	 * @param parentArea
	 * @param subArea
	 * @param inheritance
	 * @param relationNameSub
	 * @param relationNameSuper
	 * @param hideSub
	 * @return
	 */
	public MiddleResult connectSimplyAreas(
			Area parentArea, Area subArea, boolean inheritance,
			String relationNameSub, String relationNameSuper, boolean hideSub);

	/**
	 * Update area description.
	 * @param login
	 * @param area
	 * @param description
	 * @return
	 */
	public MiddleResult updateAreaDescription(Properties login, Area area,
			String description);

	/**
	 * Get area description ID.
	 * @param conection
	 * @param areaId
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult loadAreaDescriptionId(long areaId,
			Obj<Long> descriptionId);

	/**
	 * Updates sub area edge.
	 * @param login
	 * @param id
	 * @param nextId
	 * @return
	 */
	public MiddleResult updateIsSubAreaEdge(Properties login, long id,
			long nextId, boolean inheritance);

	/**
	 * Loads MIME types.
	 * @param login
	 * @param mimeTypes
	 * @return
	 */
	public MiddleResult loadMimeTypes(Properties login,
			ArrayList<MimeType> mimeTypes);

	/**
	 * Remove all MIME records from the database. MIME types that have dependencies
	 * are not removed.
	 * @param login
	 * @return
	 */
	public MiddleResult removeAllMimes(Properties login);

	/**
	 * Removes single MIME record.
	 */
	public MiddleResult removeMime(long id);

	/**
	 * Removes MIME type.
	 * @param connection
	 * @param type
	 * @param extension
	 * @return
	 */
	public MiddleResult removeMime(String type,
			String extension);

	/**
	 * Insert new MIME type.
	 * @param login
	 * @param type
	 * @param extension
	 * @param errorOnExists
	 * @return
	 */
	public MiddleResult insertMime(Properties login, String type,
			String extension, boolean preference, boolean errorOnExists);

	/**
	 * Update MIME type.
	 * @return
	 */
	public MiddleResult updateMime(Properties login, String oldType,
			String oldExtension, String type, String extension,
			boolean preference);
	
	/**
	 * Get MIME type.
	 * @param loginProperties
	 * @param fileExtension
	 * @param mimeType
	 * @return
	 */
	public  MiddleResult getMimeType(Properties login,
			String fileExtension, LinkedList<MimeType> mimeTypes);

	/**
	 * Update MIME.
	 * @param login
	 * @param oldMimeType
	 * @param newMimeType
	 * @return
	 */
	public MiddleResult updateMime(Properties login,
			MimeType oldMimeType, MimeType newMimeType);

	/**
	 * Load MIME type.
	 * @param login
	 * @param id
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadMimeType(Properties login,
			long id, MimeType mimeType);

	/**
	 * Load name space path.
	 * @param login
	 * @param id
	 * @param namespacePath
	 * @param divider 
	 * @return
	 */
	public MiddleResult loadNameSpacePath(Properties login, long id,
			Obj<String> namespacePath, String divider);
	
	/**
	 * Load name space path.
	 * @param login
	 * @param id
	 * @param namespacePath
	 * @param divider 
	 * @return
	 */
	public MiddleResult loadNameSpacePath(long id,
			Obj<String> namespacePath, String divider);
	
	/**
	 * Load resources.
	 * @param login
	 * @param namespaceId
	 * @param showHidden 
	 * @param swingWorkerHelper 
	 * @param resources
	 * @return
	 */
	public MiddleResult loadResources(Properties login,
			long namespaceId, boolean showHidden, SwingWorkerHelper<MiddleResult> swingWorkerHelper,
			LinkedList<Resource> resources);
	
	/**
	 * Insert resource to the area.
	 * @param login
	 * @param container
	 * @param file
	 * @param encoding 
	 * @param saveAsText 
	 * @param resource
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertAreaResource(Properties login,
			ResContainer container, File file,
			boolean saveAsText, String encoding, AreaResource resource,
			SwingWorkerHelper<Resource> thisThread);
	
	/**
	 * Insert resource to the container.
	 * @param login
	 * @param container
	 * @param resource
	 * @return
	 */
	public MiddleResult insertResourceToContainerText(Properties login,
			ResContainer container, AreaResource resource, String text);
	/**
	 * Load area resources count.
	 * @param areaId
	 * @param count
	 * @return
	 */
	public MiddleResult loadAreaResourcesCount(long areaId, Obj<Long> count);
	
	/**
	 * Load area resources.
	 * @param login
	 * @param area
	 * @param resources
	 * @return
	 */
	public MiddleResult loadAreaResources(
			Area area, LinkedList<AreaResource> resources,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Load area resources.
	 * @param login
	 * @param area
	 * @param resources
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadAreaResources(Properties login,
			Area area, LinkedList<AreaResource> resources,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load resource image.
	 * @param resource
	 * @return
	 */
	public MiddleResult loadResourceImage(Resource resource);

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @return
	 */
	public MiddleResult updateResourceNoFile(Properties login, Resource resource);

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @return
	 */
	public MiddleResult updateAreaResourceNoFile(Properties login,
			AreaResource resource);

	/**
	 * Update resource text.
	 * @param login
	 * @param resourceId
	 * @param text
	 * @return
	 */
	public MiddleResult updateResourceText(Properties login, long resourceId,
			String text);

	/**
	 * Update resource.
	 * @param login
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param workerThread
	 * @return
	 */
	public MiddleResult updateResource(Properties login,
			Resource resource, File file, Boolean saveAsText, String encoding,
			SwingWorkerHelper<Resource> workerThread);

	/**
	 * Update resource of container.
	 * @param login
	 * @param resource
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateAreaResource(Properties login,
			AreaResource resource,
			File file, Boolean saveAsText, String encoding,
			SwingWorkerHelper<Resource> workerThread);

	/**
	 * Remove resources of given container.
	 * @param login
	 * @param resources
	 * @param container
	 * @return
	 */
	public MiddleResult removeResourcesFromContainer(Properties login,
			LinkedList<Resource> resources, ResContainer container,
			Obj<Boolean> removed);

	/**
	 * Remove resource from container.
	 * @param resource
	 * @param container
	 * @param removed 
	 * @return
	 */
	public MiddleResult removeResourceFromContainer(Resource resource,
			ResContainer container, Obj<Boolean> removed);

	/**
	 * Insert resource.
	 * @param login
	 * @param file
	 * @param saveAsText
	 * @param encoding
	 * @param resource
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertResource(Properties login, File file,
			boolean saveAsText, String encoding, Resource resource,
			SwingWorkerHelper<Resource> thisThread);

	/**
	 * Remove resources.
	 * @param login
	 * @param resources
	 * @param removed
	 * @return
	 */
	public MiddleResult removeResources(Properties login,
			LinkedList<Resource> resources, Obj<Boolean> removed);

	/**
	 * Insert resource record to the container.
	 * @param login
	 * @param resource
	 * @param container
	 * @return
	 */
	public MiddleResult insertResourceRecordToContainer(Properties login,
			AreaResource resource, ResContainer container);
	
	/**
	 * Insert resource to area.
	 * @param login
	 * @param areaId
	 * @param resourceId
	 * @param localDescription
	 * @return
	 */
	public MiddleResult insertResourceLinkToArea(Properties login,
			long areaId, long resourceId, String localDescription);
	
	/**
	 * Insert link record to area.
	 * @param areaId
	 * @param resourceId
	 * @param localDescription
	 * @return
	 */
	public MiddleResult insertResourceLinkToArea(long areaId, 
			long resourceId, String localDescription);
	
	/**
	 * Insert resource file to area.
	 * @param login
	 * @param areaId
	 * @param file
	 * @param localDescription
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertResourceFileToArea(Properties login, long areaId,
			File file, boolean saveAsText, String textEncoding, String localDescription,
			boolean visibility, SwingWorkerHelper<MiddleResult> thisThread);
	
	/**
	 * Insert resource file to area.
	 * @param areaId
	 * @param file
	 * @param localDescription
	 * @param thisThread
	 * @return
	 */
	public MiddleResult insertResourceFileToArea(long areaId,
			File file, boolean saveAsText, String textEncoding, String localDescription,
			boolean visibility, SwingWorkerHelper<MiddleResult> thisThread);
	
	/**
	 * Insert empty resource to the area.
	 * @param login
	 * @param areaId
	 * @param saveAsText
	 * @param description
	 * @param visibility
	 * @return
	 */
	public MiddleResult insertResourceEmptyToArea(Properties login, long areaId,
			boolean saveAsText, String description, boolean visibility);
	
	/**
	 * Insert empty resource to the area.
	 * @param areaId
	 * @param saveAsText
	 * @param description
	 * @param visibility
	 * @return
	 */
	public MiddleResult insertResourceEmptyToArea(long areaId,
			boolean saveAsText, String description, boolean visibility);
	
	/**
	 * Load MIME type ID from file name.
	 * @param fileName
	 * @param mimeTypeId
	 * @return
	 */
	public MiddleResult loadMimeTypeIdFromFile(String fileName,
			Obj<Long> mimeTypeId);
	
	/**
	 * Change resources namespaces.
	 * @param login
	 * @param resourcesIds
	 * @param namespace
	 * @return
	 */
	public MiddleResult changeResourcesNamespace(Properties login,
			LinkedList<Long> resourcesIds, Namespace namespace);

	/**
	 * Set container start resource.
	 * @param login
	 * @param container
	 * @param resource
	 * @param version 
	 * @param startResourceNotLocalized 
	 * @return
	 */
	public MiddleResult updateStartResource(Properties login,
			ResContainer container, Resource resource, VersionObj version,
			boolean startResourceNotLocalized);
	
	/**
	 * Set container start resource.
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @param startResourceNotLocalized
	 * @return
	 */
	public MiddleResult updateStartResource(long areaId, long resourceId, long versionId,
			boolean startResourceNotLocalized);

	/**
	 * Load container start resource ID.
	 * @param login
	 * @param container
	 * @param resourceId
	 * @param versionId 
	 * @param startResourceNotLocalized 
	 * @return
	 */
	public MiddleResult loadContainerStartResource(Properties login,
			ResContainer container, Obj<Long> resourceId, Obj<Long> versionId,
			Obj<Boolean> startResourceNotLocalized);
	
	/**
	 * Load languages.
	 * @param languages
	 * @return
	 */
	public MiddleResult loadLanguages(LinkedList<Language> languages);

	/**
	 * Load languages.
	 * @param login
	 * @param languages
	 * @return
	 */
	public MiddleResult loadLanguages(Properties login,
			LinkedList<Language> languages);

	/**
	 * Insert new language.
	 * @param login
	 * @param description
	 * @param alias
	 * @param image2 
	 * @param languageId 
	 * @return
	 */
	public MiddleResult insertLanguage(Properties login,
			String description, String alias, BufferedImage image,
			Obj<Long> languageId);

	/**
	 * Insert language.
	 * @param description
	 * @param alias
	 * @param image
	 * @param languageId
	 * @return
	 */
	public MiddleResult insertLanguage(String description, String alias,
			BufferedImage image, Obj<Long> languageId);
	
	/**
	 * Remove language.
	 * @param language
	 * @return
	 */
	public MiddleResult removeLanguage(Language language);

	/**
	 * Remove language.
	 * @param login
	 * @param language
	 * @return
	 */
	public MiddleResult removeLanguage(Properties login,
			Language language);

	/**
	 * Update language.
	 * @param login
	 * @param language
	 * @return
	 */
	public MiddleResult updateLanguage(Properties login, Language language);

	/**
	 * Update language.
	 * @param language
	 * @return
	 */
	public MiddleResult updateLanguage(Language language);

	/**
	 * Load area description.
	 * @param login
	 * @param areaId 
	 * @param description
	 * @return
	 */
	public MiddleResult loadAreaDescription(Properties login,
			long areaId, Obj<String> description);

	/**
	 * Set start area.
	 * @param areaId
	 * @return
	 */
	public MiddleResult setHomeArea(long areaId);

	/**
	 * Set start area.
	 * @param login
	 * @param area
	 * @param isStart
	 * @return
	 */
	public MiddleResult setStartArea(Properties login, long areaId);

	/**
	 * Load start area identifier.
	 * @param login
	 * @param startAreaId
	 * @return
	 */
	public MiddleResult loadStartAreaId(Properties login,
			Obj<Long> startAreaId);

	/**
	 * Update area visibility.
	 * @param login
	 * @param areaId
	 * @param visible
	 * @return
	 */
	public MiddleResult updateAreaVisibility(Properties login,
			long areaId, boolean visible);

	/**
	 * Update area visibility.
	 * @param areaId
	 * @param visible
	 * @return
	 */
	public MiddleResult updateAreaVisibility(long areaId, boolean visible);

	/**
	 * Update area read only flag.
	 * @param login
	 * @param areaId
	 * @param readOnly
	 * @return
	 */
	public MiddleResult updateAreaReadOnly(Properties login,
			long areaId, boolean readOnly);

	/**
	 * Update area read only flag.
	 * @param areaId
	 * @param readOnly
	 * @return
	 */
	public MiddleResult updateAreaReadOnly(long areaId, boolean readOnly);

	/**
	 * Load sub areas using priorities.
	 * @param area
	 * @param subAreasIds
	 * @return
	 */
	public MiddleResult loadAreaSubAreas(Area area,
			LinkedList<Long> subAreasIds);

	/**
	 * Load super areas using priorities.
	 * @param area
	 * @param superAreasIds
	 * @return
	 */
	public MiddleResult loadAreaSuperAreas(Area area, LinkedList<Long> superAreasIds);

	/**
	 * Update area subarea priority.
	 * @param login
	 * @param superArea
	 * @param area
	 * @param priority
	 * @return
	 */
	public MiddleResult updateAreaSubAreaPriority(Properties login,
			Area superArea, Area area, int priority);
	
	/**
	 * Update area subarea priority.
	 * @param areaId
	 * @param subAreaId
	 * @param priority
	 * @return
	 */
	public MiddleResult updateAreaSubAreaPriority(
			long areaId, long subAreaId, int priority);

	/**
	 * Initialize area subareas priorities.
	 * @param areaId 
	 * @param subAreasIds
	 * @return
	 */
	public MiddleResult initAreaSubareasPriorities(long areaId,
			LinkedList<Long> subAreasIds);

	/**
	 * Initialize area superareas priorities.
	 * @param areaId
	 * @param superAreasIds
	 * @return
	 */
	public MiddleResult initAreaSuperareasPriorities(long areaId,
			LinkedList<Long> superAreasIds);

	/**
	 * Swap area sub areas priorities.
	 * @param login 
	 * @param area
	 * @param subArea1
	 * @param subArea2
	 * @return
	 */
	public MiddleResult swapAreaSubAreasPriorities(Properties login,
			Area area, Area subArea1, Area subArea2);

	/**
	 * Swap area super areas priorities.
	 * @param login
	 * @param area
	 * @param superArea1
	 * @param superArea2
	 * @return
	 */
	public MiddleResult swapAreaSuperAreasPriorities(Properties login,
			Area area, Area superArea1, Area superArea2);

	/**
	 * Reset sub areas priorities.
	 * @param login
	 * @param superAreaId
	 * @return
	 */
	public MiddleResult resetSubAreasPriorities(Properties login,
			long superAreaId);

	/**
	 * Save area alias.
	 * @param login
	 * @param areaId
	 * @param alias
	 * @return
	 */
	public MiddleResult updateAreaAlias(Properties login, long areaId, String alias);
	
	/**
	 * Save area alias.
	 * @param login
	 * @param areaId
	 * @param alias
	 * @return
	 */
	public MiddleResult updateAreaAlias(Properties login, Area area, String alias);

	/**
	 * Update start language.
	 * @param login
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult updateStartLanguage(Properties login, long startLanguageId);

	/**
	 * Update start language.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult updateStartLanguage(long startLanguageId);

	/**
	 * Load slots of given slot holders.
	 * @param login
	 * @param holders
	 * @param showHiddenSlots 
	 * @param showConstructorSlots 
	 * @param model 
	 * @return
	 */
	public MiddleResult loadAreasSlots(Properties login,
			LinkedList<? extends SlotHolder> holders, boolean showHiddenSlots, boolean showConstructorSlots);

	/**
	 * Load slots.
	 * @param holder
	 * @param model
	 * @return
	 */
	public MiddleResult loadSlots(SlotHolder holder, boolean loadHiddenSlots);
	
	/**
	 * Remove slot.
	 * @param slot
	 * @return
	 */
	public MiddleResult removeSlot(Slot slot);
	
	/**
	 * Load slot revision
	 * @param slot
	 * @param revision
	 * @return
	 */
	public MiddleResult loadSlotHeadRevision(Slot slot, Obj<Long> revision);
	
	/**
	 * Insert new slot.
	 * @param slot
	 * @return
	 */
	public MiddleResult insertSlot(Slot slot);

	/**
	 * Update slot.
	 * @param slot
	 * @param newSlot
	 * @return
	 */
	public MiddleResult updateSlot(Slot slot, Slot newSlot,
			boolean removeCurrentLanguageText);
	
	/**
	 * Update slot revision
	 * @param slotId
	 * @param revision
	 * @return
	 */
	public MiddleResult updateSlotRevision(long slotId, long revision);
	
	/**
	 * Load dictionary.
	 * @param login
	 * @param language
	 * @param selectedAreas 
	 * @param dictionary
	 * @return
	 */
	public MiddleResult loadDictionary(Properties login,
			Language language, LinkedList<Area> selectedAreas,
			ArrayList<DictionaryItem> dictionary);

	/**
	 * Load localized texts.
	 * @param languageId
	 * @param excludedTextIds
	 * @param localizedTexts
	 * @return
	 */
	public MiddleResult loadLocalizedTexts(long languageId,
			LinkedList<Long> excludedTextIds,
			LinkedList<LocalizedText> localizedTexts);

	/**
	 * Update dictionary.
	 * @param login
	 * @param selectedLanguages
	 * @param localizedTexts
	 * @param errorMessages
	 * @return
	 */
	public MiddleResult updateDictionary(Properties login,
			LinkedList<Language> selectedLanguages,
			LinkedList<LocalizedText> localizedTexts,
			LinkedList<String> errorMessages);

	/**
	 * Load resource saving method.
	 * @param login
	 * @param resourceId
	 * @param savedAsText
	 * @return
	 */
	public MiddleResult loadResourceSavingMethod(Properties login,
			long resourceId, Obj<Boolean> savedAsText);

	/**
	 * Update start resource.
	 * @param container
	 * @param resourceId
	 * @return
	 */
	public MiddleResult resetStartResource(
			ResContainer container);

	/**
	 * Reset start resource.
	 * @param login
	 * @param container
	 * @return
	 */
	public MiddleResult resetStartResource(Properties login, ResContainer container);

	/**
	 * Remove slots.
	 * @param holder
	 * @return
	 */
	public MiddleResult removeSlots(SlotHolder holder);

	/**
	 * Load resource data to stream.
	 * @param login
	 * @param resource
	 * @param outputStream
	 * @return
	 */
	public MiddleResult loadResourceToStream(Properties login, Resource resource,
			OutputStream outputStream);

	/**
	 * Load inherited slots.
	 * @param areaId
	 * @param slotsAliases
	 * @return
	 */
	public MiddleResult loadSlotsInheritedAliases(long areaId, LinkedList<String> slotsAliases, boolean isInheritedArea);
	
	/**
	 * Load direct user slots.
	 * @param areaId
	 * @param slotsAliases
	 * @return
	 */
	public MiddleResult loadSlotsAliasesUser(long areaId, LinkedList<String> slotsAliases);
	
	/**
	 * Update is sub area relation name.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSub
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSub(Properties login, long areaId,
			long subAreaId, String relationNameSub);

	/**
	 * Update is sub area relation name.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSuper(Properties login, long areaId,
			long subAreaId, String relationNameSuper);

	/**
	 * Update is sub area relation name.
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSub
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSub(long areaId,
			long subAreaId, String relationNameSub);

	/**
	 * Update is sub area relation name.
	 * @param areaId
	 * @param subAreaId
	 * @param relationNameSuper
	 * @return
	 */
	public MiddleResult updateIsSubareaNameSuper(long areaId,
			long subAreaId, String relationNameSuper);

	/**
	 * Update slot holder.
	 * @param slot
	 * @param holder
	 * @return
	 */
	public MiddleResult updateSlotHolder(Slot slot,
			SlotHolder holder);

	/**
	 * Update slots holder.
	 * @param slots
	 * @param holder
	 * @return
	 */
	public MiddleResult updateSlotsHolder(List<Slot> slots,
			SlotHolder holder);

	/**
	 * Insert slots to holder. (copy)
	 * @param slots
	 * @param holder
	 * @return
	 */
	public MiddleResult insertSlotsHolder(List<Slot> slots,
			SlotHolder holder);
	
	/**
	 * Load home area identifier.
	 * @param homeAreaId
	 * @return
	 */
	public MiddleResult loadHomeAreaId(Obj<Long> homeAreaId);

	/**
	 * Load resource name.
	 * @param login
	 * @param resourceId
	 * @param name
	 * @return
	 */
	public MiddleResult loadResourceName(Properties login, long resourceId,
			Obj<String> name, Obj<String> type);

	/**
	 * Load help text.
	 * @param login
	 * @param area
	 * @param helpText
	 * @return
	 */
	public MiddleResult loadHelp(Properties login, Area area,
			Obj<String> helpText);

	/**
	 * Update area help text.
	 * @param login
	 * @param area
	 * @param helpText
	 * @return
	 */
	public MiddleResult updateHelp(Properties login, Area area,
			String helpText);

	/**
	 * Find area with help. (Search in super areas)
	 * @param login
	 * @param areaId
	 * @param foundAreaIds
	 * @return
	 */
	public MiddleResult findSuperAreaWithHelp(Properties login,
			long areaId, LinkedList<Long> foundAreaIds);

	/**
	 * Update area's localized flag.
	 * @param login
	 * @param areaId
	 * @param localized
	 * @return
	 */
	public MiddleResult updateAreaLocalized(Properties login,
			long areaId, boolean localized);

	/**
	 * Update localized flag.
	 * @param areaId
	 * @param localized
	 * @return
	 */
	public MiddleResult updateAreaLocalized(long areaId, boolean localized);

	/**
	 * Create area subtree.
	 * @param login
	 * @param containerArea
	 * @param treeWidth
	 * @param treeDepth
	 * @param useIndices
	 * @param newArea
	 * @param inherited
	 * @param slots
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult createAreaSubtree(Properties login, Area containerArea,
			int treeWidth, int treeDepth, boolean useIndices, Area newArea,
			boolean inherited, Object[][] slots,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load languages.
	 * @param areaTreeData
	 * @return
	 */
	public MiddleResult loadLanguages(AreaTreeData areaTreeData);

	/**
	 * Load areas and edges tree.
	 * @param areaId
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadAreasEdgesTreeWithVersions(long areaId,
			AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Load constructor alias.
	 * @param constructorHolderId
	 * @param constructorAlias
	 * @return
	 */
	public MiddleResult loadConstructorAlias(long constructorHolderId,
			Obj<String> constructorAlias);
	
	/**
	 * Load slots.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadSlots(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load localized texts data.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadLocalizedTexts(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load resources data.
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadResources(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Load area tree data.
	 * @param areaId
	 * @param parentAreaId 
	 * @param areaTreeData
	 * @param swingWorkerHelper 
	 * @return
	 */
	public MiddleResult loadAreaTreeData(Properties login, long areaId,
			Long parentAreaId, AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Load area tree data.
	 * @param areaId
	 * @param parentAreaId
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult loadAreaSuperEdgeData(long areaId, Long parentAreaId,
			AreaTreeData areaTreeData, SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	/**
	 * Load resource to stream and set reference.
	 * @param resourceRef
	 * @param outputStream
	 * @param filePosition 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult loadResourceToStreamSetRef(ResourceRef resourceRef,
			OutputStream outputStream, Obj<Long> filePosition,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load language flag.
	 * @param languageRef
	 * @param outputStream
	 * @param filePosition
	 * @return
	 */
	public MiddleResult loadLanguageFlagToStream(LanguageRef languageRef,
			OutputStream outputStream, Obj<Long> filePosition);
	
	/**
	 * Add new languages data.
	 * @param datBlocks 
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertLanguagesNewData(LinkedList<DatBlock> datBlocks,
			AreaTreeData areaTreeData);

	/**
	 * Insert areas data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertAreasData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Insert "is sub area" edge data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertIsSubAreaData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Insert root "is sub area" edge.
	 * @param areaTreeData 
	 * @param importAreaId
	 * @param rootAreaId
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertIsSubAreaConnection(AreaTreeData areaTreeData, long importAreaId, Long rootAreaId,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Insert slot data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertSlotsData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Insert MIME data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertMimeData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Insert area resources data.
	 * @param areaTreeData
	 * @param datBlocks 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertAreaResourcesData(AreaTreeData areaTreeData,
			LinkedList<DatBlock> datBlocks, SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Insert areas' start resources.
	 * @param areaTreeData
	 * @param datBlocks 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateStartResourcesData(AreaTreeData areaTreeData,
			LinkedList<DatBlock> datBlocks, SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Insert area sources from area tree data into the database.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertAreaSourcesData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Update relation hide sub flag.
	 * @param login
	 * @param areaId
	 * @param subAreaId
	 * @param hideSub
	 * @return
	 */
	public MiddleResult updateIsSubareaHideSub(Properties login,
			long areaId, long subAreaId, Boolean hideSub);

	/**
	 * Loads resource image.
	 * @param login
	 * @param resource
	 * @return
	 */
	public MiddleResult loadResourceImage(Properties login,
			Resource resource);

	/**
	 * Update area file name.
	 * @param login
	 * @param areaId
	 * @param fileName
	 * @return
	 */
	public MiddleResult updateAreaFileName(Properties login, long areaId,
			String fileName);

	/**
	 * Update area file name.
	 * @param areaId
	 * @param fileName
	 * @return
	 */
	public MiddleResult updateAreaFileName(long areaId,
			String fileName);
	
	/**
	 * Remove adjacent changes.
	 * @param area
	 * @return
	 */
	public MiddleResult removeAreaAdjacentEdges(Area area);

	/**
	 * Remove is subarea edge.
	 * @param parentArea
	 * @param area
	 * @return
	 */
	public MiddleResult removeIsSubareaEdge(Area parentArea, Area area);

	/**
	 * Load resource data length.
	 * @param resourceId
	 * @param fileLength
	 * @return
	 */
	public MiddleResult loadResourceDataLength(Properties login, long resourceId,
			Obj<Long> fileLength);

	/**
	 * Load resource full image.
	 * @param login
	 * @param resourceId
	 * @param image
	 * @return
	 */
	public MiddleResult loadResourceFullImage(Properties login, long resourceId,
			Obj<BufferedImage> image);

	/**
	 * Load versions.
	 * @param login
	 * @param languageId 
	 * @param versions
	 * @return
	 */
	public MiddleResult loadVersions(Properties login,
			long languageId, LinkedList<VersionObj> versions);

	/**
	 * Insert version.
	 * @param login
	 * @param version
	 * @return
	 */
	public MiddleResult insertVersion(Properties login, VersionObj version);
	
	/**
	 * Insert version.
	 * @param version
	 * @return
	 */
	public MiddleResult insertVersion(VersionObj version);

	/**
	 * Update version.
	 * @param login
	 * @param version
	 * @return
	 */
	public MiddleResult updateVersion(Properties login, VersionObj version);
	
	/**
	 * Update version.
	 * @param version
	 * @return
	 */
	public MiddleResult updateVersion(VersionObj version);
	
	/**
	 * Load version description ID.
	 * @param versionId
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult loadVersionDescriptionId(long versionId, Obj<Long> descriptionId);

	/**
	 * Remove version.
	 * @param login
	 * @param versionId
	 * @return
	 */
	public MiddleResult removeVersion(Properties login, long versionId);

	/**
	 * Remove version.
	 * @param versionId
	 * @return
	 */
	public MiddleResult removeVersion(long versionId);

	/**
	 * Insert new versions.
	 * @param areaTreeData
	 * @return
	 */
	public MiddleResult insertVersionsNewData(AreaTreeData areaTreeData);

	/**
	 * Update area folder name.
	 * @param login
	 * @param areaId
	 * @param folderName
	 * @return
	 */
	public MiddleResult updateAreaFolderName(Properties login, long areaId,
			String folderName);
	
	/**
	 * Update area folder name.
	 * @param areaId
	 * @param folderName
	 * @return
	 */
	public MiddleResult updateAreaFolderName(long areaId, String folderName);

	/**
	 * Update slot access.
	 * @param areaId
	 * @param alias
	 * @param access 
	 * @return
	 */
	public MiddleResult updateSlotAccess(long areaId, String alias, String access);

	/**
	 * Update slot hidden.
	 * @param areaId
	 * @param alias 
	 * @param hidden
	 * @return
	 */
	public MiddleResult updateSlotHidden(long areaId, String alias, boolean hidden);

	/**
	 * Search text in areas' text resources.
	 * @param searchedText 
	 * @param areas
	 * @param caseSensitive
	 * @param wholeWords
	 * @param exactMatch
	 * @param foundResources
	 * @return
	 */
	public MiddleResult searchAreasTextResources(String searchedText, LinkedList<Area> areas,
			boolean caseSensitive, boolean wholeWords, boolean exactMatch,
			LinkedList<Resource> foundResources);

	/**
	 * Search in all text resources.
	 * @param searchedText
	 * @param caseSensitive
	 * @param wholeWords
	 * @param exactMatch
	 * @param foundResources
	 * @return
	 */
	public MiddleResult searchAllTextResources(String searchedText,
			boolean caseSensitive, boolean wholeWords, boolean exactMatch,
			LinkedList<Resource> foundResources);
	
	/**
	 * Load all text resources.
	 * @param resources
	 * @return
	 */
	public MiddleResult loadTextResources(LinkedList<Resource> resources);
	
	/**
	 * Load area text resources.
	 * @param area
	 * @param resources
	 * @return
	 */
	public MiddleResult loadAreaTextResources(Area area,
			LinkedList<AreaResource> resources);
	/**
	 * Search in text resources.
	 * @param areasResources
	 * @param searchedText
	 * @param caseSensitive
	 * @param wholeWords
	 * @param exactMatch
	 * @param foundResources
	 * @return
	 */
	public MiddleResult searchResourcesTexts(
			LinkedList<? extends Resource> resources, String searchedText,
			boolean caseSensitive, boolean wholeWords, boolean exactMatch,
			LinkedList<Resource> foundResources);

	/**
	 * Update area constructor ID and "is source" flag.
	 * @param login
	 * @param areaId
	 * @param constructorGroupId
	 * @param constructorsSource
	 * @return
	 */
	MiddleResult updateAreaConstructorGroupReferenceSourceOld(Properties login,
			long areaId, Long constructorGroupId, Boolean constructorsSource);

	/**
	 * Update area constructor ID and "is source" flag.
	 * @param areaId
	 * @param constructorGroupId
	 * @param constructorsSource
	 * @return
	 */
	MiddleResult updateAreaConstructorGroupReferenceSourceOld(
			long areaId, Long constructorGroupId, Boolean constructorsSource);

	/**
	 * Checks if the area is a constructor source area.
	 * @param login
	 * @param areaId 
	 * @param isSource
	 * @return
	 */
	public MiddleResult loadAreaConstructorIsSourceOld(Properties login,
			long areaId, Obj<Boolean> isSource);
	
	/**
	 * Checks if the area is a constructor source area.
	 * @param login
	 * @param areaId 
	 * @param isSource
	 * @return
	 */
	public MiddleResult loadAreaConstructorIsSourceOld(long areaId,
			Obj<Boolean> isSource);
	
	/**
	 * Load area constructor group ID.
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult loadAreaConstructorGroupIdOld(long areaId,
			Obj<Long> constructorGroupId);

	/**
	 * Load area constructor group ID.
	 * @param login
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult loadAreaConstructorGroupIdOld(Properties login,
			long areaId, Obj<Long> constructorGroupId);

	/**
	 * Insert new enumeration.
	 * @param login
	 * @param description
	 * @return
	 */
	public MiddleResult insertEnumeration(Properties login, String description);

	/**
	 * Insert new enumeration.
	 * @param description
	 * @return
	 */
	public MiddleResult insertEnumeration(String description);

	/**
	 * Remove enumeration.
	 * @param login
	 * @param enumerationId
	 * @return
	 */
	public MiddleResult removeEnumeration(Properties login, long enumerationId);
	
	/**
	 * Remove enumeration.
	 * @param login
	 * @param enumerationId
	 * @return
	 */
	public MiddleResult removeEnumeration(long enumerationId);

	/**
	 * Update enumeration.
	 * @param login
	 * @param enumerationId
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumeration(Properties login, long enumerationId,
			String description);
	
	/**
	 * Update enumeration.
	 * @param enumerationId
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumeration(long enumerationId,
			String description);

	/**
	 * Insert enumeration value.
	 * @param login
	 * @param enumerationId
	 * @param enumerationValue
	 * @param description 
	 * @return
	 */
	public MiddleResult insertEnumerationValue(Properties login, long enumerationId,
			String enumerationValue, String description);

	/**
	 * Insert enumeration value.
	 * @param enumerationId
	 * @param enumerationValue
	 * @param description
	 * @return
	 */
	public MiddleResult insertEnumerationValue(long enumerationId,
			String enumerationValue, String description);

	/**
	 * Update enumeration value.
	 * @param login
	 * @param enumerationValueId
	 * @param enumerationValue
	 * @return
	 */
	public MiddleResult updateEnumerationValue(Properties login, long enumerationValueId,
			String enumerationValue);

	/**
	 * Update enumeration value.
	 * @param enumerationValueId
	 * @param enumerationValue
	 * @return
	 */
	public MiddleResult updateEnumerationValue(long enumerationValueId,
			String enumerationValue);

	/**
	 * Remove enumeration value.
	 * @param login
	 * @param enumerationValueId
	 * @return
	 */
	public MiddleResult removeEnumerationValue(Properties login, long enumerationValueId);

	/**
	 * Remove enumeration value.
	 * @param enumerationValueId
	 * @return
	 */
	public MiddleResult removeEnumerationValue(long enumerationValueId);

	/**
	 * Reset slot enumeration value.
	 * @param slotId
	 * @return
	 */
	public MiddleResult updateSlotResetEnumerationValue(long slotId);

	/**
	 * Insert enumerations data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertEnumerationsData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Insert enumeration.
	 * @param description
	 * @param newEnumerationId
	 * @return
	 */
	public MiddleResult insertEnumeration(String description,
			Obj<Long> newEnumerationId);

	/**
	 * Insert enumeration values data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertEnumerationValuesData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	
	/**
	 * Insert enumeration value.
	 * @param enumerationId
	 * @param value
	 * @param newEnumerationValueId
	 * @return
	 */
	public MiddleResult insertEnumerationValue(long enumerationId,
			String value, Obj<Long> newEnumerationValueId);

	/**
	 * Insert constructor trees.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertConstructorTrees(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);
	/**
	 * Update constructor link ID.
	 * @param contructorId
	 * @param constructorLink
	 * @return
	 */
	public MiddleResult updateConstructorLinkId(long contructorId, long constructorLink);
	
	/**
	 * Load area constructor group ID and source flag.
	 * @param areaId
	 * @param constructorGroupId
	 * @param isSource
	 * @return
	 */
	public MiddleResult loadAreaConstructorGroupIdSource(long areaId,
			Obj<Long> constructorGroupId, Obj<Boolean> isSource);
	
	/**
	 * Remove constructor tree orphan.
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult removeConstructorTreeOrphan(long constructorGroupId);

	/**
	 * Insert new text resource.
	 * @param login
	 * @param resource
	 * @param text
	 * @param encoding
	 * @return
	 */
	public MiddleResult insertResourceText(Properties login,
			Resource resource, String text);
	
	/**
	 * Insert new text resource.
	 * @param resource
	 * @param text
	 * @param encoding
	 * @return
	 */
	public MiddleResult insertResourceText(Resource resource, String text);

	/**
	 * Load MIME types.
	 * @param mimeTypes
	 * @return
	 */
	MiddleResult loadMimeTypes(ArrayList<MimeType> mimeTypes);

	/**
	 * Load resource areas' IDs.
	 * @param login
	 * @param resourceId
	 * @param areasIds
	 * @return
	 */
	public MiddleResult loadResourceAreasIds(Properties login, long resourceId, LinkedList<Long> areasIds);

	/**
	 * Load resource areas' IDs.
	 * @param resourceId
	 * @param areasIds 
	 * @return
	 */
	public MiddleResult loadResourceAreasIds(long resourceId, LinkedList<Long> areasIds);

	/**
	 * Update area resource.
	 * @param login
	 * @param areaId
	 * @param resourceId
	 * @param localDescription
	 * @return
	 */
	public MiddleResult updateAreaResourceSimple(Properties login, long areaId,
			long resourceId, String localDescription);
	
	/**
	 * Update area resource.
	 * @param areaResourceId
	 * @param resourceId
	 * @param localDescription
	 * @return
	 */
	public MiddleResult updateAreaResourceSimple(long areaResourceId,
			long resourceId, String localDescription);

	/**
	 * Checks if the resource can be deleted.
	 * @param resourceId
	 * @param isOrphan
	 * @return
	 */
	public MiddleResult selectAreaResourceIsOrphan(long resourceId,
			Obj<Boolean> isOrphan);

	/**
	 * Remove resource.
	 * @param resourceId
	 * @return
	 */
	public MiddleResult removeResource(long resourceId);

	/**
	 * Update resource visibility.
	 * @param resourceId
	 * @param visible
	 * @return
	 */
	public MiddleResult updateResourceVisibiliy(long resourceId, boolean visible);

	/**
	 * Load number of connections.
	 * @param login
	 * @param number
	 * @return
	 */
	public MiddleResult loadNumberConnections(Properties login,
			Obj<Integer> number);
	

	/**
	 * Load number of connections.
	 * @param number
	 * @return
	 */
	public MiddleResult loadNumberConnections(Obj<Integer> number);

	/**
	 * Get slot description.
	 * @param login
	 * @param slotId
	 * @param description
	 * @return
	 */
	public MiddleResult loadSlotDescription(Properties login, long slotId,
			Obj<String> description);

	/**
	 * Get slot description.
	 * @param slotId
	 * @param description
	 * @return
	 */
	public MiddleResult loadSlotDescription(long slotId, Obj<String> description);

	/**
	 * Update slot description.
	 * @param login
	 * @param slotId
	 * @param description
	 * @return
	 */
	public MiddleResult updateSlotDescription(Properties login, long slotId,
			String description);
	
	/**
	 * Update slot description.
	 * @param slotId
	 * @param description
	 * @return
	 */
	public MiddleResult updateSlotDescription(long slotId, String description);

	/**
	 * Load constructor tree.
	 * @param login
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorTree(Properties login, long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Load constructor tree.
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorTree(long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Load constructor tree.
	 * @param constructorGroupId
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorTree2(long constructorGroupId,
			ConstructorGroup constructorGroup);

	/**
	 * Load area constructor group ID.
	 * @param login
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult loadAreaConstructorGroupId(Properties login,
			long areaId, Obj<Long> constructorGroupId);
	
	/**
	 * Load area constructor group ID.
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult loadAreaConstructorGroupId(
			long areaId, Obj<Long> constructorGroupId);

	/**
	 * Update area constructor group reference (for source group).
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult updateAreaConstructorGroupReferenceSource(long areaId,
			Long constructorGroupId, Boolean constructorsSource);

	/**
	 * Remove constructor tree.
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult removeConstructorTree(long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Remove constructor tree orphan.
	 * @param constructorGroup
	 * @return
	 */
	MiddleResult removeConstructorTreeOrphan(ConstructorGroup constructorGroup);

	/**
	 * Insert constructor tree.
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult insertConstructorTree(long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Load constructor group for new area.
	 * @param login
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	MiddleResult loadConstructorGroupForNewArea(Properties login, long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Load constructor group for new area.
	 * @param areaId
	 * @param constructorGroup
	 * @return
	 */
	MiddleResult loadConstructorGroupForNewArea(long areaId,
			ConstructorGroup constructorGroup);

	/**
	 * Load area slots.
	 * @param login
	 * @param area
	 * @return
	 */
	public MiddleResult loadAreaSlots(Properties login, Area area);

	/**
	 * Insert area slots.
	 * @param login
	 * @param area
	 * @param slots
	 * @return
	 */
	public MiddleResult insertAreaSlots(Properties login, Area area,
			LinkedList<Slot> slots);
	
	/**
	 * Insert area slots.
	 * @param area
	 * @param slots
	 * @return
	 */
	public MiddleResult insertAreaSlots(Area area,
			LinkedList<Slot> slots);

	/**
	 * Update area constructor group ID and is source flag.
	 * @param login
	 * @param areaId
	 * @param constructorGroupId
	 * @param constructorsSource
	 * @return
	 */
	public MiddleResult updateAreaConstructorGroupReferenceSource(Properties login,
			long areaId, Long constructorGroupId, Boolean constructorsSource);

	/**
	 * Update constructor and group references.
	 * @param areaTreeData
	 * @param importAreaId
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateAreaConstructorGroupsHoldersIds(
			AreaTreeData areaTreeData,
			long importAreaId, SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Reset constructor holders area links.
	 * @param areaId
	 * @return
	 */
	public MiddleResult updateConstructorHoldersAreaLinksReset(long areaId);

	/**
	 * @param slotId
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult loadSlotDescriptionId(long slotId, Obj<Long> descriptionId);

	/**
	 * Update slot description ID. Can be null.
	 * @param slotId
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult updateSlotDescriptionId(long slotId, Long descriptionId);

	/**
	 * Get description referenced boolean value.
	 * @param descriptionId
	 * @param isReferenced
	 * @return
	 */
	public MiddleResult loadDescriptionIsReferenced(long descriptionId,
			Obj<Boolean> isReferenced);
	
	/**
	 * Check if slot description is an orphan.
	 * @param login
	 * @param slotId
	 * @param isOrphan
	 * @return
	 */
	public MiddleResult loadSlotDescriptionIsOrphan(Properties login, long slotId, Obj<Boolean> isOrphan);
	
	/**
	 * Check if description is an orphan.
	 * @param descriptionId
	 * @param isOrphan
	 * @return
	 */
	public MiddleResult loadDescriptionIsOrphan(long descriptionId, Obj<Boolean> isOrphan);
	
	/**
	 * Delete description.
	 * @param descriptionId
	 * @return
	 */
	public MiddleResult deleteDescription(long descriptionId);

	/**
	 * Insert description.
	 * @param description
	 * @param newDescriptionId
	 * @return
	 */
	public MiddleResult insertDescription(String description,
			Obj<Long> newDescriptionId);

	/**
	 * Update description record.
	 * @param descriptionId
	 * @param description
	 * @return
	 */
	public MiddleResult updateDescription(long descriptionId, String description);

	/**
	 * Insert description data.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult insertDescriptionData(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Load default language data.
	 * @param datBlocks
	 * @param areaTreeData
	 * @return
	 */
	public MiddleResult updateDefaultLanguageData(AreaTreeData areaTreeData);
	
	/**
	 * Update area related area.
	 * @param login
	 * @param areaId
	 * @param relatedAreaId
	 * @return
	 */
	public MiddleResult updateAreaRelatedArea(Properties login, long areaId,
			Long relatedAreaId);
	
	/**
	 * Update area related area.
	 * @param areaId
	 * @param relatedAreaId
	 * @return
	 */
	public MiddleResult updateAreaRelatedArea(long areaId, Long relatedAreaId);

	/**
	 * Update related areas.
	 * @param areaTreeData
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateAreaRelatedAreas(AreaTreeData areaTreeData,
			SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Update language priorities.
	 * @param login
	 * @param languages
	 * @return
	 */
	public MiddleResult updateLanguagePriorities(Properties login,
			LinkedList<Language> languages);

	/**
	 * Update language priorities.
	 * @param languages
	 * @return
	 */
	public MiddleResult updateLanguagePriorities(
			LinkedList<Language> languages);

	/**
	 * Reset language priorities.
	 * @param login
	 * @return
	 */
	public MiddleResult updateLanguagePrioritiesReset(Properties login);
	
	/**
	 * Reset language priorities.
	 * @return
	 */
	public MiddleResult updateLanguagePrioritiesReset();

	/**
	 * Clear related areas' links.
	 * @param areaId
	 * @return
	 */
	public MiddleResult updateRelatedAreaClearLinks(long areaId);

	/**
	 * Update enumeration value description.
	 * @param login
	 * @param enumerationValueId
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumerationValueDescription(Properties login,
			long enumerationValueId, String description);
	
	/**
	 * Update enumeration value description.
	 * @param enumerationValueId
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumerationValueDescription(
			long enumerationValueId, String description);

	/**
	 * Update enumeration value and description.
	 * @param login
	 * @param enumerationValueId
	 * @param value
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumerationValueAndDescription(Properties login,
			long enumerationValueId, String value, String description);
	
	/**
	 * Update enumeration value and description.
	 * @param enumerationValueId
	 * @param value
	 * @param description
	 * @return
	 */
	public MiddleResult updateEnumerationValueAndDescription(
			long enumerationValueId, String value, String description);

	/**
	 * Insert constructor group.
	 * @param login
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult insertConstructorGroup(Properties login,
			ConstructorGroup constructorGroup);
	
	/**
	 * Insert constructor group.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult insertConstructorGroup(
			ConstructorGroup constructorGroup);
	
	/**
	 * Update constructor holder sub group ID.
	 * @param login
	 * @param parentConstructorHolderId
	 * @param groupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderSubGroupId(Properties login,
			long parentConstructorHolderId, Long groupId);
	
	/**
	 * Update constructor holder sub group ID.
	 * @param parentConstructorHolderId
	 * @param groupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderSubGroupId(
			long parentConstructorHolderId, Long groupId);

	/**
	 * Remove constructor object with sub tree.
	 */
	public MiddleResult removeConstructorObjectWithSubTree(Properties login,
			Object constructorObject);
	
	/**
	 * Remove constructor object with sub tree.
	 */
	public MiddleResult removeConstructorObjectWithSubTree(
			Object constructorObject);

	/**
	 * Remove constructor group with dependencies.
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult removeConstructorGroupWithDependencies(long constructorGroupId);
	
	/**
	 * Remove constructor group tree dependencies.
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult removeConstructorGroupTreeDependencies(long constructorGroupId);
	
	/**
	 * Remove constructor holder with dependencies.
	 * @param constructorHolderId
	 * @return
	 */
	public MiddleResult removeConstructorHolderWithDependencies(long constructorHolderId);
	
	/**
	 * Remove constructor holder links.
	 * @param constructorHolderId
	 * @return
	 */
	public MiddleResult removeConstructorHolderLinks(long constructorHolderId);
	
	/**
	 * Remove constructor holder area dependencies.
	 * @param constructorHolderId
	 * @return
	 */
	public MiddleResult removeConstructorHolderAreaDependencies(long constructorHolderId);

	/**
	 * Insert constructor holder.
	 * @param login
	 * @param constructorHolder
	 * @return
	 */
	public MiddleResult insertConstructorHolder(Properties login,
			ConstructorHolder constructorHolder);
	
	/**
	 * Insert constructor holder.
	 * @param constructorHolder
	 * @return
	 */
	public MiddleResult insertConstructorHolder(ConstructorHolder constructorHolder);
	
	/**
	 * Insert orphan constructor group.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult insertConstructorGroupOrphan(ConstructorGroup constructorGroup);

	/**
	 * Insert area constructor group ID.
	 * @param areaId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult updateAreaConstructorGroup(long areaId, Long constructorGroupId);

	/**
	 * Update constructor holder sub group reference.
	 * @param login
	 * @param constructorHolderId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderSubReference(Properties login,
			long constructorHolderId, long constructorGroupId);
	

	/**
	 * Update constructor holder sub group reference.
	 * @param constructorHolderId
	 * @param constructorGroupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderSubReference(
			long constructorHolderId, long constructorGroupId);

	/**
	 * Update constructor holder group ID.
	 * @param login
	 * @param constructorHolderId
	 * @param groupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderGroupId(Properties login,
			long constructorHolderId, long groupId);
	
	/**
	 * Update constructor holder group ID.
	 * @param constructorHolderId
	 * @param groupId
	 * @return
	 */
	public MiddleResult updateConstructorHolderGroupId(
			long constructorHolderId, long groupId);

	/**
	 * Insert constructor holder sub tree. Return root holder ID in root object.
	 * @param login
	 * @param rootConstructorHolder
	 * @return
	 */
	public MiddleResult insertConstructorHolderSubTree(Properties login,
			ConstructorHolder rootConstructorHolder);
	
	/**
	 * Insert constructor holder sub tree. Return root holder ID in root object.
	 * @param rootConstructorHolder
	 * @return
	 */
	public MiddleResult insertConstructorHolderSubTree(
			ConstructorHolder rootConstructorHolder);

	/**
	 * Update constructor holder.
	 * @param login
	 * @param constructorHolder
	 * @return
	 */
	public MiddleResult updateConstructorHolderProperties(Properties login,
			ConstructorHolder constructorHolder);

	/**
	 * Update constructor holder.
	 * @param constructorHolder
	 * @return
	 */
	public MiddleResult updateConstructorHolderProperties(
			ConstructorHolder constructorHolder);

	/**
	 * Update area constructor holder.
	 * @param login
	 * @param areaId
	 * @param constructorHolderId
	 * @return
	 */
	public MiddleResult updateAreaConstructorHolder(Properties login,
			long areaId, long constructorHolderId);
	
	/**
	 * Update area constructor holder.
	 * @param areaId
	 * @param constructorHolderId
	 * @return
	 */
	public MiddleResult updateAreaConstructorHolder(
			long areaId, long constructorHolderId);

	/**
	 * Update area file extension.
	 * @param login
	 * @param areaId
	 * @param fileExtension
	 * @return
	 */
	public MiddleResult updateAreaFileExtension(Properties login, long areaId,
			String fileExtension);
	
	/**
	 * Update area file extension.
	 * @param areaId
	 * @param fileExtension
	 * @return
	 */
	public MiddleResult updateAreaFileExtension(long areaId,
			String fileExtension);

	/**
	 * Update constructor group extension.
	 * @param login
	 * @param constructorGroupId
	 * @param extensionAreaId
	 * @return
	 */
	public MiddleResult updateConstructorGroupExtension(Properties login,
			long constructorGroupId, Long extensionAreaId);
	
	/**
	 * Update constructor group extension.
	 * @param constructorGroupId
	 * @param extensionAreaId
	 * @return
	 */
	public MiddleResult updateConstructorGroupExtension(
			long constructorGroupId, Long extensionAreaId);

	/**
	 * Load constructor groups properties.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorGroupsProperties(
			ConstructorGroup constructorGroup);

	/**
	 * Load constructor group extension.
	 * @param contructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorGroupProperties(ConstructorGroup contructorGroup);
	
	/**
	 * Load constructor group extension.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorGroupExtension(
			ConstructorGroup constructorGroup);
	
	/**
	 * Load constructor group alias.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorGroupAlias(
			ConstructorGroup constructorGroup);
	
	/**
	 * Load constructor group extension constructors.
	 * @param constructorGroup
	 * @return
	 */
	public MiddleResult loadConstructorGroupExtensionConstructors(
			ConstructorGroup constructorGroup);

	/**
	 * Remove extension area links to constructor groups.
	 * @param areaId
	 * @return
	 */
	public MiddleResult updateConstructorGroupsAreaExtensionsReset(long areaId);

	/**
	 * Set new area start resource from constructor area.
	 * @param login
	 * @param newAreaId
	 * @param constructorAreaId
	 * @return
	 */
	public MiddleResult updateAreaStartResourceFromConstructorArea(
			Properties login, long newAreaId, long constructorAreaId);

	/**
	 * Set new area start resource from constructor area.
	 * @param newAreaId
	 * @param constructorAreaId
	 * @return
	 */
	public MiddleResult updateAreaStartResourceFromConstructorArea(
			long newAreaId, long constructorAreaId);

	/**
	 * Update slot is default flag.
	 * @param areaId
	 * @param alias
	 * @param isDefault
	 * @return
	 */
	public MiddleResult updateSlotIsDefault(long areaId, String alias, boolean isDefault);

	/**
	 * Update slot is preferred flag.
	 * @param areaId
	 * @param alias
	 * @param isPreferred
	 * @return
	 */
	public MiddleResult updateSlotIsPreferred(long areaId, String alias, boolean isPreferred);

	/**
	 * Copy area help text from source to the destination.
	 * @param login
	 * @param sourceAreaId
	 * @param destinationAreaId
	 * @return
	 */
	public MiddleResult updateCopyAreaHelpText(Properties login, long sourceAreaId,
			long destinationAreaId);
	
	/**
	 * Copy area help text from source to the destination.
	 * @param sourceAreaId
	 * @param destinationAreaId
	 * @return
	 */
	public MiddleResult updateCopyAreaHelpText(long sourceAreaId,
			long destinationAreaId);

	/**
	 * Create database name.
	 * @param server
	 * @param port
	 * @param useSsl
	 * @param userName
	 * @param password
	 * @param databaseName
	 */
	MiddleResult createDatabase(String server, int port, boolean useSsl,
			String userName, String password, String databaseName);

	/**
	 * Get database names list.
	 * @param server
	 * @param port
	 * @param useSsl
	 * @param userName
	 * @param password
	 * @param databaseNames 
	 * @return
	 */
	public MiddleResult getDatabaseNames(String server, int port,
			boolean useSsl, String userName, String password, LinkedList<String> databaseNames);
	
	/**
	 * Get database names list.
	 * @param loginProperties
	 * @param databaseNames
	 */
	public MiddleResult getDatabaseNames(Properties loginProperties, LinkedList<String> databaseNames);

	/**
	 * Drop database.
	 * @param server
	 * @param port
	 * @param useSsl
	 * @param userName
	 * @param password
	 * @param databaseName
	 * @return
	 */
	public MiddleResult dropDatabase(String server, int port, boolean useSsl,
			String userName, String password, String databaseName);
	
	/**
	 * Update constructor group alias.
	 * @param login
	 * @param constructorGroupId
	 * @param groupAlias
	 * @return
	 */
	public MiddleResult updateConstructorGroupAlias(Properties login,
			long constructorGroupId, String groupAlias);
	
	/**
	 * Update constructor group alias.
	 * @param constructorGroupId
	 * @param groupAlias
	 * @return
	 */
	public MiddleResult updateConstructorGroupAlias(long constructorGroupId,
			String groupAlias);
	
	/**
	 * Load constructor holder.
	 * @param id
	 * @param constructorHolder
	 * @return
	 */
	public MiddleResult loadConstructorHolder(long id, ConstructorHolder constructorHolder);
	
	/**
	 * Load constructor groups with aliases.
	 * @param areaId
	 * @param aliases
	 * @param groupsIdsWithAliases
	 * @return
	 */
	public MiddleResult loadConstructorGroupsWithAliases(long areaId, String aliases,
			HashSet<Long> groupsIdsWithAliases);
	
	/**
	 * Load root constructor group of given group.
	 * @param groupId
	 * @param rootGroupId
	 * @return
	 */
	public MiddleResult loadRootConstructorGroupOfGroup(long groupId,
			Obj<Long> rootGroupId);

	/**
	 * Load extended group ID.
	 * @param areasIds
	 * @param extendedGroupId
	 * @return
	 */
	public MiddleResult loadExtendedGroupId(long areaId, Obj<Long> extendedGroupId);

	/**
	 * Load area super areas IDs.
	 * @param areaId
	 * @param extendedGroupsIds
	 * @return
	 */
	public MiddleResult loadSuperAreasIds(long areaId, HashSet<Long> superAreasIds);

	/**
	 * Load root group area ID.
	 * @param rootGroupId
	 * @param areaId
	 * @return
	 */
	public MiddleResult loadRootConstructorGroupAreaId(long rootGroupId,
			Obj<Long> areaId);

	/**
	 * Load groups with aliases in a tree.
	 * @param ref
	 * @param aliases
	 * @param groupsIdsWithAliases 
	 * @return
	 */
	public MiddleResult loadGroupsWithAliasesInTree(long rootGroupId,
			HashSet<String> aliases, HashSet<Long> groupsIdsWithAliases);

	/**
	 * Load group sub groups IDs.
	 * @param groupId
	 * @param subGroupsIds
	 * @return
	 */
	public MiddleResult loadGroupSubGroupsIds(long groupId,
			LinkedList<Long> subGroupsIds);

	/**
	 * Load root group.
	 * @param areaId
	 * @param rootGroupId
	 * @return
	 */
	public MiddleResult loadRootConstructorGroup(long areaId, Obj<Long> rootGroupId);

	/**
	 * Load parent constructor ID.
	 * @param groupId
	 * @param parentConstructorId
	 * @return
	 */
	public MiddleResult loadParentConstructorId(long groupId,
			Obj<Long> parentConstructorId);

	/**
	 * Load area constructor ID.
	 * @param areaId
	 * @param areaConstructorId
	 * @return
	 */
	public MiddleResult loadAreaConstructor(long areaId, Obj<Long> areaConstructorId);

	/**
	 * Load parent constructor of given group.
	 * @param constructorId
	 * @param parentGroupId
	 * @return
	 */
	public MiddleResult loadParentGroup(long constructorId, Obj<Long> parentGroupId);

	/**
	 * Load area constructor sub groups aliases.
	 * @param areaId
	 * @param groupsAliases
	 * @return
	 */
	public MiddleResult loadAreaConstructorSubGroupAliases(long areaId,
			Obj<String> groupsAliases);

	/**
	 * Set preferred slot flag.
	 * @param login
	 * @param slotId
	 * @param isPreferred
	 * @return
	 */
	public MiddleResult updateSlotIsPreferred(Properties login, long slotId,
			boolean isPreferred);

	/**
	 * Set preferred slot flag.
	 * @param slotId
	 * @param isPreferred
	 * @return
	 */
	public MiddleResult updateSlotIsPreferred(long slotId, boolean isPreferred);

	/**
	 * Update constructor holder alias
	 * @param login
	 * @param constructorId
	 * @param alias
	 * @return
	 */
	public MiddleResult updateConstructorHolderAlias(Properties login, long constructorId,
			String alias);
	
	/**
	 * Update constructor holder alias
	 * @param constructorId
	 * @param alias
	 * @return
	 */
	public MiddleResult updateConstructorHolderAlias(long constructorId,
			String alias);

	/**
	 * Link unlinked area constructors.
	 * @param areaTreeData
	 * @param importAreaId
	 * @param rootAreaId 
	 * @param swingWorkerHelper
	 * @return
	 */
	public MiddleResult updateUnlinkedAreasConstructors(
			AreaTreeData areaTreeData, long importAreaId,
			long rootAreaId, SwingWorkerHelper<MiddleResult> swingWorkerHelper);

	/**
	 * Update area can import flag.
	 * @param login
	 * @param areaId
	 * @param canImport
	 * @return
	 */
	public MiddleResult updateAreaCanImport(Properties login,
			long areaId, boolean canImport);
	
	/**
	 * Update area can import flag.
	 * @param areaId
	 * @param canImport
	 * @return
	 */
	public MiddleResult updateAreaCanImport(long areaId, boolean canImport);

	/**
	 * Update area project root flag.
	 * @param login
	 * @param areaId
	 * @param projectRoot
	 * @return
	 */
	public MiddleResult updateAreaProjectRoot(Properties login,
			long areaId, boolean projectRoot);
	
	/**
	 * Update area project root flag.
	 * @param areaId
	 * @param projectRoot
	 * @return
	 */
	public MiddleResult updateAreaProjectRoot(long areaId, boolean projectRoot);

	/**
	 * Load constructor area ID.
	 * @param login
	 * @param constructorId
	 * @param constructorAreaId
	 * @return
	 */
	public MiddleResult loadConstructorHolderAreaId(Properties login,
			Long constructorId, Obj<Long> constructorAreaId);

	/**
	 * Insert area source.
	 * @param login
	 * @param areaId
	 * @param resourceId
	 * @param versionId 
	 * @param notLocalized
	 * @return
	 */
	public MiddleResult insertAreaSource(Properties login, long areaId, long resourceId,
			long versionId, boolean notLocalized);

	/**
	 * Insert area source.
	 * @param areaId
	 * @param resourceId
	 * @param notLocalized
	 * @return
	 */
	public MiddleResult insertAreaSource(long areaId, long resourceId, long versionId,
			boolean notLocalized);

	/**
	 * Load area sources.
	 * @param login
	 * @param areaId
	 * @param areaSourcesData
	 * @return
	 */
	public MiddleResult loadAreaSources(Properties login, long areaId,
			LinkedList<AreaSourceData> areaSourcesData);

	/**
	 * Load area sources.
	 * @param areaId
	 * @param areaSourcesData
	 * @return
	 */
	public MiddleResult loadAreaSources(long areaId,
			LinkedList<AreaSourceData> areaSourcesData);

	/**
	 * Delete area source.
	 * @param login
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @return
	 */
	public MiddleResult deleteAreaSource(Properties login, long areaId, long resourceId,
			long versionId);
	
	/**
	 * Delete area source.
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @return
	 */
	public MiddleResult deleteAreaSource(long areaId, long resourceId,
			long versionId);

	/**
	 * Update area source "not localized" flag.
	 * @param login
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @param notLocalized
	 * @return
	 */
	public MiddleResult updateAreaSourceNotLocalized(Properties login, long areaId,
			long resourceId, long versionId, boolean notLocalized);
	
	/**
	 * Update area source "not localized" flag.
	 * @param areaId
	 * @param resourceId
	 * @param versionId
	 * @param notLocalized
	 * @return
	 */
	public MiddleResult updateAreaSourceNotLocalized(long areaId,
			long resourceId, long versionId, boolean notLocalized);

	/**
	 * Delete area sources.
	 * @param login
	 * @param areaId
	 * @return
	 */
	public MiddleResult deleteAreaSources(Properties login, long areaId);
	
	/**
	 * Delete area sources.
	 * @param areaId
	 * @return
	 */
	public MiddleResult deleteAreaSources(long areaId);

	/**
	 * Load area source.
	 * @param login
	 * @param areaId
	 * @param versionId
	 * @param resourceId
	 * @return
	 */
	public MiddleResult loadAreaSource(Properties login, long areaId, long versionId, Obj<Long> resourceId);

	/**
	 * Load area source.
	 * @param areaId
	 * @param versionId
	 * @param resourceId
	 * @return
	 */
	public MiddleResult loadAreaSource(long areaId, long versionId, Obj<Long> resourceId);

	/**
	 * Insert area sources.
	 * @param login
	 * @param areaId
	 * @param areaSourcesCollection
	 * @return
	 */
	public MiddleResult insertAreaSources(Properties login, long areaId,
			Collection<AreaSource> areaSourcesCollection);
	
	/**
	 * Insert area sources.
	 * @param areaId
	 * @param areaSourcesCollection
	 * @return
	 */
	public MiddleResult insertAreaSources(long areaId,
			Collection<AreaSource> areaSourcesCollection);

	/**
	 * Reset area slots' area reference values.
	 * @param areaId
	 * @return
	 */
	public MiddleResult resetAreaSlotsAreaReferences(long areaId);

	/**
	 * Disable the area. Do not render it
	 * @param loginProperties
	 * @param areaId
	 * @param isDisabled
	 * @return
	 */
	public MiddleResult setAreaDisabled(Properties loginProperties, long areaId, boolean isDisabled);

	/**
	 * Disable the area. Do not render it
	 * @param areaId
	 * @param isDisabled
	 * @return
	 */
	public MiddleResult setAreaDisabled(long areaId, boolean isDisabled);
	
	/**
	 * Load revisions
	 * @param slotId
	 * @param revisions
	 * @return
	 */
	public MiddleResult loadRevisions(Slot slot, LinkedList<Revision> revisions);
	
	/**
	 * Load revised slot
	 * @param revision
	 * @param editedSlot
	 */
	public MiddleResult loadRevisedSlot(Revision revision, Slot editedSlot);

	/**
	 * Inserts new revision of the slot
	 * @param slot
	 * @param newSlot
	 * @return
	 */
	public MiddleResult insertSlotRevision(Slot slot, Slot newSlot);
	
	/**
	 * Remove slot revision
	 * @param slot
	 * @param revision
	 * @return
	 */
	public MiddleResult removeSlotRevision(Slot slot, Revision revision);

	/**
	 * Load external slots found in an area.
	 * @param area - input area
	 * @param externalSlots - found external slots
	 * @return
	 */
	public MiddleResult loadAreaExternalSlots(Area area, LinkedList<Slot> externalSlots);
	
	/**
	 * Load external slot link.
	 * @param externalSlot
	 * @return
	 */
	public MiddleResult loadSlotExternalLinkAndOutputText(Slot externalSlot);
	
	/**
	 * Update slot link.
	 * @param slotId 
	 * @return
	 */
	public MiddleResult updateSlotUnlock(long slotId);
	
	/**
	 * Load slot text value.
	 * @param slotId
	 * @param textValue
	 * @return
	 */
	public MiddleResult loadSlotTextValue(long slotId, Obj<String> textValue);
	
	/**
	 * Load slot properties.
	 * @param slot
	 * @return
	 */
	public MiddleResult loadSlotProperties(Slot slot);
	
	/**
	 * Save slot properties.
	 * @param slot
	 * @return
	 */
	public MiddleResult updateSlotProperties(Slot slot);
	
	/**
	 * Load path slots' IDs for an area.
	 * @param areaId
	 * @param pathSlotIds
	 * @return
	 */
	public MiddleResult loadPathSlotsIds(long areaId, LinkedList<Long> pathSlotIds);

	/**
	 * Import embedded template into basic area.
	 * @param xmlInputStream
	 * @param datInputStream
	 * @return
	 */
	public MiddleResult importTemplate(InputStream xmlInputStream, InputStream datInputStream);

	/**
	 * Import data fromDAT stream.
	 * @param areaTreeData
	 * @param datStream
	 * @param datBlocks
	 * @return
	 */
	public MiddleResult importDatStream(AreaTreeData areaTreeData, InputStream datStream,
			LinkedList<DatBlock> datBlocks);
	
	/**
	 * Update language icon.
	 * @param languageId
	 * @param datStream
	 * @param blockLength
	 * @return
	 */
	public MiddleResult updateLanguageIcon(long languageId, InputStream datStream, int blockLength);
	
	/**
	 * Update resource blob.
	 * @param resourceId
	 * @param datStream
	 * @param blockLength
	 * @return
	 */
	public MiddleResult updateResourceBlob(long resourceId, InputStream datStream, int blockLength);
	
	/**
	 * Set GUIDs for areas without them.
	 * @return
	 */
	public MiddleResult updateAreaEmptyGuids();
}
