/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.maclan;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Blob;
import java.util.*;
import java.util.function.Function;

import javax.sql.DataSource;

import org.multipage.util.Obj;

/**
 * @author
 *
 */
public interface MiddleLight {

	/**
	 * Dump analysis.
	 */
	public void dumpAnalysis();
	
	/**
	 * Enable / disable cache.
	 */
	public void enableCache(boolean enable);
	
	/**
	 * Get cache enabled.
	 */
	public boolean isCacheEnabled();
	
	/**
	 * Clear cache.
	 */
	public void clearCache();

	/**
	 * Add listener.
	 * @param middleListener
	 */
	public void addListener(MiddleListener middleListener);

	/**
	 * Load current root area.
	 * @param areaId
	 * @return
	 */
	public void setCurrentRootArea(Area area);

	/**
	 * Log in to the database.
	 */
	public MiddleResult login(Properties properties);

	/**
	 * Fast login.
	 * @param dataSource
	 * @return
	 */
	public MiddleResult loginFast(DataSource dataSource, String username,
			String password);

	/**
	 * Logout.
	 */
	public MiddleResult logout(MiddleResult currentResult);
	
	/**
	 * End all transactions.
	 * @param currentResult
	 * @return
	 */
	public MiddleResult endOverallTransaction(MiddleResult currentResult);

	/**
	 * Start transaction.
	 * @return
	 */
	public MiddleResult startSubTransaction();
	
	/**
	 * End partial transaction.
	 * @param currentResult
	 * @return
	 */
	public MiddleResult endSubTransaction(MiddleResult currentResult);

	/**
	 * End sub transaction.
	 * @param ok
	 */
	public MiddleResult endSubTransaction(boolean ok);

	/**
	 * Get connection state.
	 * @return
	 */
	public MiddleResult checkConnection();

	/**
	 * Set language.
	 * @param languageAlias
	 */
	public MiddleResult setLanguage(String languageAlias);
	
	/**
	 * Load start language ID.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult loadStartLanguageId(Properties login, Obj<Long> startLanguageId);
	/**
	 * Load start language ID.
	 * @param startLanguageId
	 * @return
	 */
	public MiddleResult loadStartLanguageId(Obj<Long> startLanguageId);
	
	/**
	 * Set start language as current language.
	 */
	public MiddleResult setStartLanguageCurrent();

	/**
	 * Get language text.
	 * @param language
	 * @param textId
	 * @return
	 */
	public String getLanguageText(long languageId, long textId);

	/**
	 * Load resource text to string.
	 * @param resourceId
	 * @param text
	 */
	public MiddleResult loadResourceTextToString(long resourceId,
			Obj<String> text);

	/**
	 * Load resource text to string.
	 * @param login
	 * @param resourceId
	 * @param text
	 * @return
	 */
	public MiddleResult loadResourceTextToString(Properties login, long resourceId,
			Obj<String> text);

	/**
	 * Load area resource.
	 * @param areaId
	 * @param resourceName
	 * @param resourceId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadAreaResourceId(long areaId, String resourceName, Obj<Long> resourceId);

	/**
	 * Get resource saving method.
	 * @param resourceId 
	 * @param savedAsText
	 * @return
	 */
	public MiddleResult loadResourceSavingMethod(
			Long resourceId, Obj<Boolean> savedAsText);

	/**
	 * Load resource data to the stream.
	 * @param resourceId
	 * @param outputStream
	 * @return
	 */
	public MiddleResult loadResourceBlobToStream(
			Long resourceId, OutputStream outputStream);
	/**
	 * Load resource from text.
	 * @param resourceId
	 * @param outputStream
	 */
	public MiddleResult loadResourceTextToStream(long resourceId,
			OutputStream outputStream);

	/**
	 * Load area start resource ID and type.
	 * @param area
	 * @param versionId
	 * @param startResource
	 * @return
	 */
	public MiddleResult loadAreaStartResourceOldStyle(
			Area area, long versionId, Obj<StartResource> startResource);

	/**
	 * Load inherited start resource area.
	 * @param area
	 * @param versionId 
	 * @param startResource
	 * @return
	 */
	public MiddleResult loadAreaInheritedStartResource(
			Area area, long versionId, Obj<StartResource> startResource);

	/**
	 * Load area.
	 * @param areaId
	 * @param area
	 * @return
	 */
	public MiddleResult loadArea(long areaId, Obj<Area> area);
	
	/**
	 * Load basic area.
	 * @param area
	 * @return
	 */
	public MiddleResult loadBasicArea(Obj<Area> area);

	/**
	 * Load subareas UI data.
	 * @param area
	 * @return
	 */
	public MiddleResult loadSubAreasData(Area area);
	
	/**
	 * Load super areas UI data.
	 * @param area
	 * @return
	 */
	public MiddleResult loadSuperAreasData(Area area);

	/**
	 * Load languages.
	 * @param languages
	 * @return
	 */
	public MiddleResult loadLanguagesNoFlags(
			LinkedList<Language> languages);
	/**
	 * Load language flag.
	 * @param languageId
	 * @return
	 */
	public MiddleResult loadLanguageFlag(
			long languageId, Obj<byte []> bytesArray);

	/**
	 * Set language.
	 * @param languageId
	 * @return
	 */
	public MiddleResult setLanguage(long languageId);

	/**
	 * Load language without flag using alias.
	 * @param alias
	 * @param language
	 * @return
	 */
	public MiddleResult loadLanguageNoFlag(
			String alias, Language language);
	/**
	 * Load home area.
	 * @param homeArea
	 * @return
	 */
	public MiddleResult loadHomeAreaData(Obj<Area> homeArea);
	
	/**
	 * Load area ID for given alias.
	 * @param alias
	 * @param outputAreaId
	 * @return
	 */
	public MiddleResult loadAreaWithAlias(String alias, Obj<Area> outputArea);
	
	/**
	 * Load first projects' area with given alias.
	 * @param projectRootAreas
	 * @param alias
	 * @param outputArea
	 * @return
	 */
	public MiddleResult loadProjectAreaWithAlias(
			LinkedList<Area> projectRootAreas, String alias,
			Obj<Area> outputArea);
	
	/**
	 * Load area ID for given project and alias.
	 * @param projectAlias
	 * @param alias
	 * @param outputArea
	 * @return
	 */
	public MiddleResult loadProjectAreaWithAlias(String projectAlias, String alias, Obj<Area> outputArea);
	
	/**
	 * Load project root areas.
	 * @param currentArea
	 * @param projectRootAreas
	 * @return
	 */
	public MiddleResult loadProjectRootAreas(Area currentArea,
			LinkedList<Area> projectRootAreas);
	
	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param skipDefault
	 * @param slot 
	 * @param lastFoundDefaultSlot
	 * @param isInheritance
	 * @param skipCurrentArea
	 * @param loadValue
	 * @return
	 */
	public MiddleResult loadSlotPrivate(Area area, String alias,
			boolean inherit, boolean skipDefault, Obj<Slot> slot, Obj<Slot> lastFoundDefaultSlot, int hint,
			boolean isInheritance, Long inheritanceLevel, boolean skipCurrentArea, boolean loadValue);
	
	/**
	 * Load slot value.
	 * @param slot
	 * @return
	 */
	public MiddleResult loadSlotValue(Slot slot);

	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param parent
	 * @param skipDefault 
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	public MiddleResult loadSlot(Area area, String alias,
			boolean inherit, boolean parent, boolean skipDefault, Obj<Slot> slot, boolean loadValue);
	
	/**
	 * Load area slot.
	 * @param area
	 * @param alias
	 * @param inherit
	 * @param parent
	 * @param skipDefault 
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	public MiddleResult loadSlot(Area area, String alias, int hint,
			boolean inherit, boolean parent, boolean skipDefault, Obj<Slot> slot, boolean loadValue);

	/**
	 * Load area slot with inheritance level.
	 * @param area
	 * @param alias
	 * @param skipDefault 
	 * @param parent
	 * @param inheritanceLevel
	 * @param slot 
	 * @param loadValue 
	 * @return
	 */
	public MiddleResult loadSlotInheritanceLevel(Area area, String alias, int hint, boolean skipDefault,
			boolean parent, Long inheritanceLevel, Obj<Slot> slot, boolean loadValue);
	
	/**
	 * Load area slots.
	 * @param area
	 * @param slotAlias
	 * @return
	 */
	public MiddleResult loadAreaSlotsRefData(Area area);

	/**
	 * Load area slots (extended).
	 * @param area
	 * @param isDefaultValue
	 * @return
	 */
	public MiddleResult loadAreaSlotsRefDataEx(Area area, boolean isDefaultValue);
	
	/**
	 * Update slot text value
	 * @param slotId
	 * @param textValue
	 * @return
	 */
	public MiddleResult updateSlotTextValue(long slotId, String textValue, Obj<Boolean> updatedTextValue);
	
	/**
	 * Load area resource.
	 * @param areaId
	 * @param resourceName 
	 * @param resource
	 * @return
	 */
	public MiddleResult loadAreaResource(long areaId, String resourceName,
			Obj<AreaResource> resource);
	
	/**
	 * Load blob length.
	 * @param blobId
	 * @param fileLength
	 * @return
	 */
	public MiddleResult loadBlobLength(long blobId, Obj<Long> fileLength);
	
	/**
	 * Load BLOB length.
	 * @param blob
	 * @param fileLength
	 * @return
	 */
	public MiddleResult loadBlobLength(Blob blob, Obj<Long> fileLength);

	/**
	 * Load resource data length.
	 * @param login
	 * @param resourceId
	 * @param fileLength
	 * @return
	 */
	public MiddleResult loadResourceDataLength(long resourceId, Obj<Long> fileLength);
	
	/**
	 * Load MIME type.
	 * @param mimeTypeId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadMimeType(long mimeTypeId, Obj<MimeType> mimeType);
	
	/**
	 * Load resource MIME type object
	 * @param resourceId
	 * @param mimeType
	 * @return
	 */
	public MiddleResult loadResourceMimeType(Long resourceId, Obj<String> mimeType);
	
	/**
	 * Load resouce MIME extension.
	 * @param resourceId
	 * @param mimeExtension
	 * @return
	 */
	public MiddleResult loadResourceMimeExt(long resourceId,
			Obj<String> mimeExtension);

	/**
	 * Load area resources IDs.
	 * @param areaId
	 * @param resourcesIds
	 */
	public MiddleResult loadAreaResourcesIds(long areaId, LinkedList<Long> resourcesIds);

	/**
	 * @return the currentLanguageId
	 */
	public long getCurrentLanguageId();

	/**
	 * @param currentLanguageId the currentLanguageId to set
	 */
	public void setCurrentLanguageId(long currentLanguageId);

	/**
	 * @return the currentRootArea
	 */
	public Area getCurrentRootArea();

	/**
	 * Load resource BLOB to string.
	 * @param resourceId
	 * @param coding
	 * @param contentText
	 * @return
	 */
	public MiddleResult loadResourceBlobToString(long resourceId, String coding,
			Obj<String> contentText);
	
	/**
	 * Load versions.
	 * @param versions
	 * @return
	 */
	public MiddleResult loadVersions(long languageId, LinkedList<VersionObj> versions);

	/**
	 * Insert new MIME type.
	 * @param type
	 * @param extension
	 * @param preference 
	 * @param errorOnExists - If this parameter is true, then if the record already exists,
	 * 						  an error is returned.
	 * @return
	 */
	public MiddleResult insertMime(String type,
			String extension, boolean preference, boolean errorOnExists);
	
	/**
	 * Load version.
	 * @param versionId
	 * @param version
	 */
	public MiddleResult loadVersionData(long versionId, VersionData version);

	/**
	 * Load version from cache or database table.
	 * @param versionId
	 * @param version
	 * @return
	 */
	public MiddleResult loadVersion(long versionId, Obj<VersionObj> version);

	/**
	 * Load version from cache or database table.
	 * @param versionAlias
	 * @param version
	 * @return
	 */
	public MiddleResult loadVersion(String versionAlias, Obj<VersionObj> version);


	/**
	 * Load area resource.
	 * @param resourceId
	 * @param areaId
	 * @param resource
	 * @return
	 */
	public MiddleResult loadAreaResource(long resourceId, long areaId,
			Obj<AreaResource> areaResource);

	/**
	 * Load resource.
	 * @param resourceId
	 * @param resource
	 * @return
	 */
	public MiddleResult loadResource(long resourceId, Obj<Resource> resource);
	
	/**
	 * Load enumerations.
	 * @param login
	 * @param enumerations
	 * @return
	 */
	public MiddleResult loadEnumerations(Properties login, LinkedList<EnumerationObj> enumerations);
	
	/**
	 * Load enumerations.
	 * @param enumerations
	 * @return
	 */
	public MiddleResult loadEnumerations(LinkedList<EnumerationObj> enumerations);
	
	/**
	 * Load enumerations without values.
	 * @param enumerations
	 * @return
	 */
	public MiddleResult loadEnumerationsWitoutValues(
			LinkedList<EnumerationObj> enumerations);
	
	/**
	 * Load enumeration values.
	 * @param enumerationId
	 * @param enumerationValues
	 * @return
	 */
	public MiddleResult loadEnumerationValues(long enumerationId,
			LinkedList<EnumerationValue> enumerationValues);
	
	/**
	 * Load enumeration values.
	 * @param enumeration
	 * @return
	 */
	public MiddleResult loadEnumerationValues(EnumerationObj enumeration);
	
	/**
	 * Load enumeration ID.
	 * @param description
	 * @param enumerationId
	 * @return
	 */
	public MiddleResult loadEnumerationId(String description,
			Obj<Long> enumerationId);
	
	/**
	 * Load enumeration value ID.
	 * @param value
	 * @param enumerationId
	 * @param newEnumerationValueId
	 * @return
	 */
	public MiddleResult loadEnumerationValueId(String value,
			long enumerationId, Obj<Long> newEnumerationValueId);
	
	/**
	 * Load enumeration value. Information is cached.
	 * @param enumerationValueId
	 * @param enumerationValue
	 * @return
	 */
	public MiddleResult loadEnumerationValue(long enumerationValueId,
			Obj<EnumerationValue> enumerationValue);

	/**
	 * Load enumeration value.
	 * @param enumerationDescription
	 * @param enumerationValueText
	 * @param enumerationValue
	 * @return
	 */
	public MiddleResult loadEnumerationValue(String enumerationDescription, String enumerationValueText,
			Obj<EnumerationValue> enumerationValue);

	/**
	 * Load resource full image.
	 * @param login
	 * @param resourceId
	 * @param image
	 * @return
	 */
	public MiddleResult loadResourceFullImage(long resourceId,
			Obj<BufferedImage> image);

	/**
	 * Load image size.
	 * @param resource
	 * @return
	 */
	public MiddleResult loadImageSize(Resource resource);

	/**
	 * Load related area data.
	 * @param area
	 * @return
	 */
	public MiddleResult loadRelatedAreaData(Area area);

	/**
	 * Get constructor holder area ID.
	 * @param constructorHolderId
	 * @param constructorAreaId
	 * @return
	 */
	public MiddleResult loadConstructorHolderAreaId(Long constructorHolderId,
			Obj<Long> constructorAreaId);

	/**
	 * Create database if not exist.
	 * @param loginProperties 
	 * @param isNewDatabase
	 */
	public MiddleResult attachOrCreateNewBasicArea(Properties loginProperties,
			Function<LinkedList<String>, String> selectDatabaseCallback, Obj<Boolean> isNewDatabase);
	
	/**
	 * Load area source.
	 * @param areaId
	 * @param versionId
	 * @param startResource
	 * @return
	 */
	public MiddleResult loadAreaStartResource(Area area, long versionId,
			Obj<StartResource> startResource);

	/**
	 * Unzip resource to a file in directory with given path using given method.
	 * @param resourceId
	 * @param path
	 * @param method
	 * @return
	 */
	public MiddleResult loadResourceAndUnzip(long resourceId, String path, String method);
	
	/**
	 * Load slot text value directly using slot ID. The method loads only necessary
	 * information, alias, revision and text_value into slot object
	 * @param slotId
	 * @param slot
	 * @return
	 */
	public MiddleResult loadSlotTextValueDirectly(long slotId, Slot slot);

	/**
	 * Update slot's read lock.
	 * @param slotId
	 * @param locked
	 * @return
	 */
	public MiddleResult updateInputLock(long slotId, boolean locked);
	
	/**
	 * Update slot's write lock.
	 * @param slotId
	 * @param locked
	 * @return
	 */
	public MiddleResult updateOutputLock(long slotId, boolean locked);
	
	/**
	 * Update slot output text.
	 * @param slotId
	 * @param outputText
	 * @return
	 */
	public MiddleResult updateSlotOutputText(long slotId, String outputText);
	
	/**
	 * Select slot's read lock.
	 * @param slotId
	 * @param locked
	 * @return
	 */
	public MiddleResult loadSlotInputLock(long slotId, Obj<Boolean> locked);
	
	/**
	 * Select slot's write lock.
	 * @param slotId
	 * @param locked
	 * @return
	 */
	public MiddleResult loadSlotOutputLock(long slotId, Obj<Boolean> locked);

	/**
	 * Update external provider change.
	 * @param slotId
	 * @param change
	 * @return
	 */
	public MiddleResult updateSlotExternalChange(long slotId, boolean change);
	
	/**
	 * Try to login user.
	 * @param user
	 * @param password
	 * @return
	 */
	public MiddleResult checkLogin(String user, String password);
}
